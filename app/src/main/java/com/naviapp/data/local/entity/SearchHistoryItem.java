package com.naviapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Запись истории поиска/поездок. Хранит и точку назначения, и метрики
 * поездки (если она состоялась) — используется и как "История поиска",
 * и как "История поездок" в требованиях ТЗ, чтобы не заводить две похожие таблицы.
 */
@Entity(tableName = "search_history")
public class SearchHistoryItem {

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String displayName;

    public double latitude;
    public double longitude;

    public long timestamp;

    public double tripDistanceMeters; // 0, если это просто поисковый запрос без поездки
    public double tripDurationSeconds;

    public SearchHistoryItem(@NonNull String displayName, double latitude, double longitude,
                              long timestamp, double tripDistanceMeters, double tripDurationSeconds) {
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.timestamp = timestamp;
        this.tripDistanceMeters = tripDistanceMeters;
        this.tripDurationSeconds = tripDurationSeconds;
    }
}
