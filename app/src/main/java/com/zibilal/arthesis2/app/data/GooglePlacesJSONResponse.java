package com.zibilal.arthesis2.app.data;

import com.google.gson.annotations.SerializedName;
import com.zibilal.consumeapi.lib.network.Response;

import java.util.List;

/**
 * Created by bmuhamm on 6/2/14.
 */
public class GooglePlacesJSONResponse implements Response {

    @SerializedName("next_page_token")
    private String nextPageToken;
    private List<Geonames> results;

    public String getNextPageToken() {
        return nextPageToken;
    }

    public void setNextPageToken(String nextPageToken) {
        this.nextPageToken = nextPageToken;
    }

    public List<Geonames> getResults() {
        return results;
    }

    public void setResults(List<Geonames> results) {
        this.results = results;
    }

    @Override
    public Object responseData() {
        return results;
    }
}
