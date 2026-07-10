package com.naviapp.data.model;

/**
 * Простая координата точки на карте. Используется вместо LatLng конкретного
 * SDK, чтобы слой данных (data) не зависел от слоя карты (ui) —
 * это часть Clean Architecture: внутренние модели независимы от библиотек.
 */
public class GeoPoint {

    private final double latitude;
    private final double longitude;

    public GeoPoint(double latitude, double longitude) {
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    @Override
    public String toString() {
        return latitude + "," + longitude;
    }
}
