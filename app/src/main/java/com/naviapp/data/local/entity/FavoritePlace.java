package com.naviapp.data.local.entity;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Избранное место пользователя. Поле "type" отличает обычное избранное
 * от специальных ярлыков "Дом" и "Работа" (см. PlaceType) —
 * это позволяет хранить их в одной таблице без дублирования схемы.
 */
@Entity(tableName = "favorite_places")
public class FavoritePlace {

    public static final String TYPE_REGULAR = "REGULAR";
    public static final String TYPE_HOME = "HOME";
    public static final String TYPE_WORK = "WORK";

    @PrimaryKey(autoGenerate = true)
    public long id;

    @NonNull
    public String displayName;

    public double latitude;
    public double longitude;

    @NonNull
    public String type; // REGULAR / HOME / WORK

    public long createdAt;

    public FavoritePlace(@NonNull String displayName, double latitude, double longitude,
                          @NonNull String type, long createdAt) {
        this.displayName = displayName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.type = type;
        this.createdAt = createdAt;
    }
}
