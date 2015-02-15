package com.zibilal.arthesis2.app.data;

/**
 * Created by bmuhamm on 6/2/14.
 */
public interface DataSource {

    public void createRequest(double lat, double lon, float radius);
    public void fetchData();
}
