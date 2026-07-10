package com.naviapp.data.model;

import java.util.List;

/**
 * Готовый к отображению маршрут: геометрия для отрисовки на карте,
 * список пошаговых инструкций и агрегированные метрики (время, расстояние).
 * Это "чистая" модель — не зависит от формата ответа OSRM.
 */
public class RouteResult {

    private final List<GeoPoint> geometry;   // Полилиния маршрута для отрисовки
    private final List<RouteStep> steps;     // Пошаговые инструкции навигации
    private final double distanceMeters;
    private final double durationSeconds;
    private final String profile;            // car / foot / bike

    public RouteResult(List<GeoPoint> geometry, List<RouteStep> steps,
                        double distanceMeters, double durationSeconds, String profile) {
        this.geometry = geometry;
        this.steps = steps;
        this.distanceMeters = distanceMeters;
        this.durationSeconds = durationSeconds;
        this.profile = profile;
    }

    public List<GeoPoint> getGeometry() {
        return geometry;
    }

    public List<RouteStep> getSteps() {
        return steps;
    }

    public double getDistanceMeters() {
        return distanceMeters;
    }

    public double getDurationSeconds() {
        return durationSeconds;
    }

    public String getProfile() {
        return profile;
    }

    /** Форматированное расстояние: "1.2 км" или "350 м" */
    public String getFormattedDistance() {
        if (distanceMeters >= 1000) {
            return String.format("%.1f км", distanceMeters / 1000.0);
        }
        return String.format("%d м", (int) distanceMeters);
    }

    /** Форматированное время в пути: "1 ч 25 мин" или "8 мин" */
    public String getFormattedDuration() {
        int totalMinutes = (int) Math.ceil(durationSeconds / 60.0);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        if (hours > 0) {
            return hours + " ч " + minutes + " мин";
        }
        return minutes + " мин";
    }
}
