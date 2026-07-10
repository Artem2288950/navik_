package com.naviapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Ответ Photon (https://photon.komoot.io) — GeoJSON FeatureCollection.
 * Photon хорошо ищет организации/POI по названию (кафе, аптеки и т.д.),
 * в отличие от Nominatim, который сильнее в точных адресах.
 */
public class PhotonResponse {

    @SerializedName("features")
    public List<Feature> features;

    public static class Feature {
        @SerializedName("geometry")
        public Geometry geometry;

        @SerializedName("properties")
        public Properties properties;
    }

    public static class Geometry {
        @SerializedName("coordinates")
        public List<Double> coordinates; // [lon, lat]
    }

    public static class Properties {
        @SerializedName("name")
        public String name;

        @SerializedName("city")
        public String city;

        @SerializedName("street")
        public String street;

        @SerializedName("housenumber")
        public String houseNumber;

        @SerializedName("osm_value")
        public String category; // cafe, pharmacy, fuel, parking...
    }
}
