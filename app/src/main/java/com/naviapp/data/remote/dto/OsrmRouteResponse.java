package com.naviapp.data.remote.dto;

import com.google.gson.annotations.SerializedName;
import java.util.List;

/**
 * Сырая структура JSON-ответа OSRM (http://project-osrm.org/docs/v5.24.0/api/#route-service).
 * Поле "code" == "Ok" при успехе. Держим DTO отдельно от domain-моделей (RouteResult),
 * чтобы смена бэкенда маршрутизации (например, на Valhalla или GraphHopper)
 * не затронула остальное приложение — достаточно переписать RouteRepository.
 */
public class OsrmRouteResponse {

    @SerializedName("code")
    public String code;

    @SerializedName("routes")
    public List<OsrmRoute> routes;

    public static class OsrmRoute {
        @SerializedName("distance")
        public double distance; // метры

        @SerializedName("duration")
        public double duration; // секунды

        @SerializedName("geometry")
        public OsrmGeometry geometry;

        @SerializedName("legs")
        public List<OsrmLeg> legs;
    }

    public static class OsrmGeometry {
        @SerializedName("coordinates")
        public List<List<Double>> coordinates; // [lon, lat] пары
    }

    public static class OsrmLeg {
        @SerializedName("steps")
        public List<OsrmStep> steps;
    }

    public static class OsrmStep {
        @SerializedName("distance")
        public double distance;

        @SerializedName("duration")
        public double duration;

        @SerializedName("name")
        public String name; // название улицы

        @SerializedName("maneuver")
        public OsrmManeuver maneuver;
    }

    public static class OsrmManeuver {
        @SerializedName("type")
        public String type; // turn, depart, arrive, roundabout...

        @SerializedName("modifier")
        public String modifier; // left, right, straight...

        @SerializedName("location")
        public List<Double> location; // [lon, lat]
    }
}
