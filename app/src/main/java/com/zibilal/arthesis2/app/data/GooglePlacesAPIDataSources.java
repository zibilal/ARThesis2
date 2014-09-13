package com.zibilal.arthesis2.app.data;

import android.graphics.Color;
import android.util.Log;

import com.zibilal.consumeapi.lib.network.HttpClient;
import com.zibilal.consumeapi.lib.network.Response;
import com.zibilal.consumeapi.lib.worker.HttpAsyncTask;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bmuhamm on 6/1/14.
 */
public class GooglePlacesAPIDataSources implements DataSource{

    private static final String TAG="GooglePlacesAPIDataSources";

    private static final String BASE_URL="https://maps.googleapis.com/maps/api/place/nearbysearch";
    private static final String OUTPUT_JSON="/json";
    private static final String API_KEY="AIzaSyAZ872BI38ls6ey6pKL7jNbIHjiPFpOX1Q";

    private static final String TYPES = "airport|amusement_park|aquarium|art_gallery|bus_station|campground|car_rental|city_hall|embassy|establishment|hindu_temple|local_government_office|mosque|museum|night_club|park|place_of_worship|police|post_office|stadium|spa|subway_station|synagogue|taxi_stand|train_station|travel_agency|University|zoo";


    private String key;
    private List<Marker> markers;
    private String mUrl;

    public GooglePlacesAPIDataSources() {
        key = API_KEY;
        markers = new ArrayList<Marker>();
    }

    public void createRequest(double lat, double lon, float radius) {
        StringBuilder urlBuilder = new StringBuilder(BASE_URL);
        urlBuilder.append(OUTPUT_JSON);
        urlBuilder.append("?location=" + lat + "," + lon);
        urlBuilder.append("&radius=" + (radius*1000.0f));
        urlBuilder.append("&types=" + TYPES);
        urlBuilder.append("&sensor=true");
        urlBuilder.append("&key=" + key);
        mUrl = urlBuilder.toString();
    }


    @Override
    public void fetchData() {
        if(mUrl==null)
            throw new IllegalStateException(" Request is empty.");

        HttpAsyncTask worker = new HttpAsyncTask(new HttpAsyncTask.OnPostExecute() {
            @Override
            public void onProgress(Integer i) {

            }

            @Override
            public void onUpdate(Response response, String url) {
                if(response != null) {
                    GooglePlacesJSONResponse jresponse = (GooglePlacesJSONResponse) response;
                    List<Geonames>  data = (List<Geonames>)jresponse.responseData();
                    for(Geonames g : data) {
                        Geonames.Geometry geo = g.getGeometry();
                        if(geo != null) {
                            Geonames.Loc loc = geo.getLocation();
                            if(loc != null) {
                                Marker m = new Marker(g.getName(), loc.getLat(), loc.getLng(), 0d, 0d, Color.BLUE);
                                Log.d(TAG, String.format("Name %s , Lat %f, Lng %f", m.getName(), m.getLatitude(), m.getLongitude()));
                                markers.add(m);
                            }
                        }
                    }

                    if(markers.size() > 0) {
                        ARGlobal.addMarkers(markers);
                    }
                }
            }
        }, GooglePlacesJSONResponse.class, false, null);
        worker.execute(mUrl, HttpClient.JSON_TYPE);
    }
}
