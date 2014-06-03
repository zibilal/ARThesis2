package com.zibilal.arthesis2.app.data;

import android.graphics.Canvas;
import android.location.Location;
import android.util.Log;

import com.zibilal.arthesis2.app.camera.PointOfView;
import com.zibilal.arthesis2.app.operation.Vector;
import com.zibilal.arthesis2.app.views.PaintScreen;

/**
 * Created by bmuhamm on 5/23/14.
 */
public class Marker {

    private static final String TAG="Marker";

    private Vector mScreenPosition;
    private Vector mTemp;
    private Vector mLocationVector;
    private Vector mLocationRelativePointOfView;

    private Vector mLocationRelativeToLocation;

    private String mName;
    private double mLatitude;
    private double mLongitude;
    private double mAltitude;
    private double mDistance;
    private int mColor;

    private PointOfView mPointOfView;
    private PaintScreen mPaintScreen;

    public Marker(String name, double latitude, double longitude, double distance, double altitude, int color ) {
        mName=name;
        mLatitude=latitude;
        mLongitude=longitude;
        mAltitude=altitude;
        mDistance=distance;
        mColor=color;

        mScreenPosition = new Vector();
        mTemp = new Vector();
        mLocationVector = new Vector();
        mLocationRelativePointOfView = new Vector();
        mLocationRelativeToLocation = new Vector();
        mScreenPosition = new Vector();

        mPaintScreen = new PaintScreen(color);
    }

    public void setName(String name) {
        mName = name;
    }

    public String getName() {
        return mName;
    }

    public void setLatitude(double latitude) {
        mLatitude = latitude;
    }

    public double getLatitude(){
        return mLatitude;
    }

    public void setLongitude(double longitude) {
        mLongitude = longitude;
    }

    public double getLongitude() {
        return mLongitude;
    }

    public void setAltitude(double altitude) {
        mAltitude = altitude;
    }

    public double getAltitude(){
        return mAltitude;
    }

    public void setDistance(double distance) {
        mDistance = distance;
    }

    public double getDistance() {
        return mDistance;
    }

    public void calculateRelativePosition(Location location) {

        updateDistance(location);

        float[] z = {0f};
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), mLatitude, location.getLongitude(), z);
        float[] x = {0f};
        Location.distanceBetween(location.getLatitude(), location.getLongitude(), location.getLatitude(), mLongitude, x);

        if(mAltitude == 0) {
            mAltitude = location.getAltitude();
        }
        double y = mAltitude - location.getAltitude();
        if(location.getLatitude() < mLatitude) z[0] *= -1;
        if(location.getLongitude() > mLongitude) x[0] *= -1;

        mLocationRelativeToLocation.set(x[0], (float) y, z[0]);

        Log.d("data.Marker", "Location relative to location : " + mLocationRelativeToLocation);
        Log.d("data.Marker", "Updated distance : " + mDistance);
    }

    private void updateDistance(Location location) {
        float[] d = {0f};
        Location.distanceBetween(mLatitude, mLongitude, location.getLatitude(), location.getLongitude(), d);
        mDistance = d[0];
    }

    public void updateAndDraw(Canvas canvas) {

        if(mPointOfView == null) {
            mPointOfView = new PointOfView(canvas.getWidth(), canvas.getHeight());
        }

//        if(mCam == null) {
//            mCam = new CameraModel(canvas.getWidth(), canvas.getHeight());
//        }
//        mCam.setViewAngle(CameraModel.DEFAULT_VIEW_ANGLE);

        populateMatrices();
        //updateView();

        // just draw something here...
        Log.d(TAG, String.format("Update and draw --> location relative pov, %s", mLocationRelativePointOfView));
        if(mLocationRelativePointOfView.getZ() >= -1f) {
            Log.d(TAG, "The Z = " + mLocationRelativePointOfView.getZ());
            return;
        } else {

            float x = mLocationRelativePointOfView.getX();
            float y = mLocationRelativePointOfView.getY();

            double angle = 2.0 * Math.atan2(10, mDistance);
            float radius = (float) ( angle  * canvas.getHeight() );
            mPaintScreen.circle(canvas, x, y, radius);
            mPaintScreen.boxedText(canvas, mName, mName.length(), x, y, 0);
        }
    }

    public void populateMatrices() {
        mTemp.set(0f, 0f, 0f);
        Log.d(TAG, "Start");
        Log.d(TAG, String.format("1. MTemp : %s", mTemp));
        mTemp.add(mLocationRelativeToLocation);
        Log.d(TAG, String.format("2. MTemp : %s", mTemp));
        mTemp.prod(ARGlobal.getRotationMatrix());
        Log.d(TAG, String.format("3. MTemp : %s", mTemp));

        Vector result = new Vector();

        mPointOfView.projectPoint(mTemp, result);
        Log.d(TAG, String.format("4. Result : %s",result));
        Log.d(TAG, "Finish");
        mLocationRelativePointOfView.set(result);
    }

    private void updateView(){

        float[] arry = {0f, 0f, 0f};
        mLocationRelativePointOfView.get(arry);

        float x = arry[0];
        float y = arry[1];
        float z = arry[2];

        // If it's not in the  same side as our viewing angle (behind us)
        if(z >= -1f) return;

        // From how it works codes. They assumed it's a "square" axis aligned square.
        //float max = Math.max(getWi)
    }
}
