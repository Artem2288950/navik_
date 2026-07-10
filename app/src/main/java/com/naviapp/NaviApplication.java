package com.naviapp;

import android.app.Application;
import org.maplibre.android.MapLibre;

/**
 * Точка инициализации приложения.
 * Инициализирует MapLibre один раз при старте процесса —
 * это дешевле, чем инициализировать карту в каждой Activity.
 */
public class NaviApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        // Инициализация движка карты (векторные тайлы OpenStreetMap через MapLibre)
        MapLibre.getInstance(this);
    }
}
