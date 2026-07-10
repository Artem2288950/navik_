package com.naviapp.data.local;

import android.content.Context;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import com.naviapp.data.local.dao.FavoritePlaceDao;
import com.naviapp.data.local.dao.SearchHistoryDao;
import com.naviapp.data.local.entity.FavoritePlace;
import com.naviapp.data.local.entity.SearchHistoryItem;

/**
 * Локальная база данных Room. Синглтон — Room сам по себе не потокобезопасен
 * для нескольких инстансов на одну и ту же БД, а держать несколько инстансов
 * бессмысленно и тратит память.
 */
@Database(
        entities = {FavoritePlace.class, SearchHistoryItem.class},
        version = 1,
        exportSchema = false
)
public abstract class AppDatabase extends RoomDatabase {

    private static volatile AppDatabase instance;

    public abstract FavoritePlaceDao favoritePlaceDao();

    public abstract SearchHistoryDao searchHistoryDao();

    public static AppDatabase getInstance(Context context) {
        if (instance == null) {
            synchronized (AppDatabase.class) {
                if (instance == null) {
                    instance = Room.databaseBuilder(
                                    context.getApplicationContext(),
                                    AppDatabase.class,
                                    "naviapp.db")
                            .fallbackToDestructiveMigration()
                            .build();
                }
            }
        }
        return instance;
    }
}
