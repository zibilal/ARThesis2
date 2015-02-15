package com.zibilal.arthesis2.app.data;

import com.zibilal.consumeapi.lib.network.Response;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by bilalmuhammad on 1/3/15.
 */
public class GeonamesJSONResponse implements Response {

    private List<Geoname> geonames;

    public GeonamesJSONResponse(){
        geonames=new ArrayList<Geoname>();
    }

    @Override
    public Object responseData() {
        return geonames;
    }

    public static class Geoname{
        private String summary;
        private long elevation;
        private long geoNameId;
        private String feature;
        private double lng;
        private double lat;
        private String distance;
        private String countryCode;
        private long rank;
        private String lang;
        private String title;
        private String wikipediaUrl;

        public Geoname(){}

        public String getSummary() {
            return summary;
        }

        public void setSummary(String summary) {
            this.summary = summary;
        }

        public long getElevation() {
            return elevation;
        }

        public void setElevation(long elevation) {
            this.elevation = elevation;
        }

        public long getGeoNameId() {
            return geoNameId;
        }

        public void setGeoNameId(long geoNameId) {
            this.geoNameId = geoNameId;
        }

        public String getFeature() {
            return feature;
        }

        public void setFeature(String feature) {
            this.feature = feature;
        }

        public double getLng() {
            return lng;
        }

        public void setLng(double lng) {
            this.lng = lng;
        }

        public double getLat() {
            return lat;
        }

        public void setLat(double lat) {
            this.lat = lat;
        }

        public String getDistance() {
            return distance;
        }

        public void setDistance(String distance) {
            this.distance = distance;
        }

        public String getCountryCode() {
            return countryCode;
        }

        public void setCountryCode(String countryCode) {
            this.countryCode = countryCode;
        }

        public long getRank() {
            return rank;
        }

        public void setRank(long rank) {
            this.rank = rank;
        }

        public String getLang() {
            return lang;
        }

        public void setLang(String lang) {
            this.lang = lang;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getWikipediaUrl() {
            return wikipediaUrl;
        }

        public void setWikipediaUrl(String wikipediaUrl) {
            this.wikipediaUrl = wikipediaUrl;
        }
    }
}
