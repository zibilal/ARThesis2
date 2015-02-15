package com.zibilal.arthesis2.app.data;

import android.graphics.Color;

import com.zibilal.consumeapi.lib.network.HttpClient;
import com.zibilal.consumeapi.lib.network.Response;
import com.zibilal.consumeapi.lib.worker.HttpAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bilalmuhammad on 1/3/15.
 */
public class GeonamesDataSource implements DataSource {

    private static final String TAG = "GeonamesDataSource";
    private static final String BASE_URL = "http://api.geonames.org/findNearbyWikipediaJSON?lat=%f&lng=%f&radius=%f&maxRows=%d&lang=en&username=zibilal";
    private static final int MAX_ROWS=20;
    private List<Marker> markers;
    private String mUrl;
    public GeonamesDataSource(){
        markers = new ArrayList<Marker>();
    }

    @Override
    public void createRequest(double lat, double lon, float radius) {
        mUrl = String.format(BASE_URL,
                lat, lon, radius, MAX_ROWS);
    }

    @Override
    public void fetchData() {
        if(mUrl == null) {
            throw new IllegalStateException("Request is empty");
        }

        HttpAsyncTask worker = new HttpAsyncTask(new HttpAsyncTask.OnPostExecute() {
            @Override
            public void onProgress(Integer i) {

            }

            @Override
            public void onUpdate(Response response, String url) {
                if(response != null) {
                    GeonamesJSONResponse jresponse = (GeonamesJSONResponse) response;
                    List<GeonamesJSONResponse.Geoname> data =
                            (List<GeonamesJSONResponse.Geoname>) jresponse.responseData();
                    for(GeonamesJSONResponse.Geoname g : data) {
                        double distance=0d;
                        try{
                            distance=Double.parseDouble(g.getDistance());
                        } catch(Exception e) {}
                        Marker m = new Marker(g.getTitle(), g.getLat(), g.getLng(), distance,  g.getElevation(), Color.GREEN);
                        markers.add(m);
                    }
                }

                if(markers.size() > 0) {
                    ARGlobal.addMarkers(markers);
                }
            }
        }, GeonamesJSONResponse.class, false, null);

        worker.execute(mUrl, HttpClient.JSON_TYPE);
    }
}
