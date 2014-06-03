package com.zibilal.arthesis2.app.data;

import android.location.Location;
import android.util.Log;

import com.zibilal.arthesis2.app.operation.Matrix;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by bmuhamm on 5/22/14.
 */
public class ARGlobal {
    private static final String TAG="ARGlobal";

    private static Matrix rotationMatrix = new Matrix();
    private static com.zibilal.arthesis2.app.outside.Matrix rotationMatrix2 = new com.zibilal.arthesis2.app.outside.Matrix();
    private static final Object rotationMatrixLock = new Object();


    /* defaulting to our place */
    public static Location hardFix = new Location("ATL");
    static {
        hardFix.setLatitude(-6.2657);
        hardFix.setLongitude(106.7843);
        hardFix.setAltitude(1);
    }
    private static Location currentLocation=hardFix;
    private static final Object currentLocationLock = new Object();

    private static final AtomicBoolean dirty = new AtomicBoolean(false);
    private static final Map<String, Marker> markerList = new ConcurrentHashMap<String, Marker>();
    private static final List<Marker> markerCache = new CopyOnWriteArrayList<Marker>();

    public static void setCurrentLocation(Location currLocation) {
        synchronized (ARGlobal.currentLocationLock) {
            ARGlobal.currentLocation = currLocation;
        }
    }

    public static Location getCurrentLocation(){
        synchronized (ARGlobal.currentLocationLock) {
            return ARGlobal.currentLocation;
        }
    }

    public static Matrix getRotationMatrix() {

         synchronized (ARGlobal.rotationMatrixLock) {
             Log.d(TAG, String.format("Get rotation matrix --> %s", ARGlobal.rotationMatrix));
             return ARGlobal.rotationMatrix;
         }
    }

    public static void setRotationMatrix(Matrix rotationMatrix) {
        synchronized (ARGlobal.rotationMatrixLock) {
            Log.d(TAG, String.format("Set rotation matrix --> %s", ARGlobal.rotationMatrix));
            ARGlobal.rotationMatrix = rotationMatrix;
        }
    }

    public static void setRotationMatrix(com.zibilal.arthesis2.app.outside.Matrix rotationMatrix) {
        synchronized (ARGlobal.rotationMatrixLock) {
            Log.d(TAG, String.format("Set rotation matrix --> %s", ARGlobal.rotationMatrix));
            ARGlobal.rotationMatrix2 = rotationMatrix;
        }
    }

    public static com.zibilal.arthesis2.app.outside.Matrix getRotationMatrix2() {

        synchronized (ARGlobal.rotationMatrixLock) {
            Log.d(TAG, String.format("Get rotation matrix --> %s", ARGlobal.rotationMatrix2));
            return ARGlobal.rotationMatrix2;
        }
    }

    public static void addMarkers(Collection<Marker> markers) {
        for(Marker marker : markers) {
            //if(!markerList.containsKey(marker.getName())) {
                marker.calculateRelativePosition(getCurrentLocation());
                markerList.put(marker.getName(), marker);
            //}
        }

        if(dirty.compareAndSet(false, true)) {
            markerCache.clear();
        }
    }

    public static List<Marker> getMarkers() {
        if(dirty.compareAndSet(true, false)) {
            List<Marker> copy = new ArrayList<Marker>(markerList.size());
            copy.addAll(markerList.values());
            Collections.sort(copy, comparator);
            markerCache.clear();
            markerCache.addAll(copy);
        }
        return Collections.unmodifiableList(markerCache);
    }

    private static final Comparator<Marker> comparator = new Comparator<Marker>() {
        @Override
        public int compare(Marker marker1, Marker marker2) {
            return Double.compare(marker1.getDistance(), marker2.getDistance());
        }
    };
}
