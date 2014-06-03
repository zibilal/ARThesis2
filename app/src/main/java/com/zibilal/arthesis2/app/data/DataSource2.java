package com.zibilal.arthesis2.app.data;

import android.graphics.Color;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmuhamm on 5/25/14.
 */
public class DataSource2 {

    private List<Marker> cachedMarkers;

    public DataSource2(){
        cachedMarkers = new ArrayList<Marker>();
    }

    public void fetchData(){}

    public List<Marker> getMarkers() {
        Marker pondokIndah = new Marker("Pondok Indah Mall", -6.265708333333333d, 106.78429722222222d, 0d, 0d, Color.RED);
        cachedMarkers.add(pondokIndah);
        return cachedMarkers;
    }
}
