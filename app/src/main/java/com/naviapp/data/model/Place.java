package com.naviapp.data.model;

/**
 * Единая модель "места" — результат поиска (адрес, организация, POI).
 * Заполняется как из Nominatim, так и из Photon — оба сервиса приводятся
 * к этому общему виду в SearchRepository, чтобы UI не знал о разнице API.
 */
public class Place {

    private String displayName;   // Полное отображаемое название/адрес
    private String category;      // Категория: кафе, аптека, заправка и т.д.
    private double latitude;
    private double longitude;

    public Place(String displayName, String category, double latitude, double longitude) {
        this.displayName = displayName;
        this.category = category;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getCategory() {
        return category;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public GeoPoint toGeoPoint() {
        return new GeoPoint(latitude, longitude);
    }
}
