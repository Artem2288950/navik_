package com.naviapp.data.repository;

import android.content.Context;
import androidx.lifecycle.LiveData;
import com.naviapp.data.local.AppDatabase;
import com.naviapp.data.local.dao.FavoritePlaceDao;
import com.naviapp.data.local.dao.SearchHistoryDao;
import com.naviapp.data.local.entity.FavoritePlace;
import com.naviapp.data.local.entity.SearchHistoryItem;
import com.naviapp.util.AppExecutors;
import java.util.List;

/**
 * Репозиторий для работы с локальным хранилищем: избранное (в т.ч. Дом/Работа)
 * и история поиска/поездок. Все записи выполняются в фоновом потоке через AppExecutors,
 * чтение — через LiveData, которая сама уведомляет UI при изменениях в БД.
 */
public class FavoritesRepository {

    private final FavoritePlaceDao favoritePlaceDao;
    private final SearchHistoryDao searchHistoryDao;
    private final AppExecutors executors = AppExecutors.getInstance();

    public FavoritesRepository(Context context) {
        AppDatabase db = AppDatabase.getInstance(context);
        favoritePlaceDao = db.favoritePlaceDao();
        searchHistoryDao = db.searchHistoryDao();
    }

    public LiveData<List<FavoritePlace>> getRegularFavorites() {
        return favoritePlaceDao.getRegularFavorites();
    }

    public LiveData<FavoritePlace> getHome() {
        return favoritePlaceDao.getByType(FavoritePlace.TYPE_HOME);
    }

    public LiveData<FavoritePlace> getWork() {
        return favoritePlaceDao.getByType(FavoritePlace.TYPE_WORK);
    }

    public LiveData<List<SearchHistoryItem>> getRecentHistory() {
        return searchHistoryDao.getRecent();
    }

    public void addFavorite(String name, double lat, double lon) {
        executors.runInBackground(() -> favoritePlaceDao.insert(
                new FavoritePlace(name, lat, lon, FavoritePlace.TYPE_REGULAR, System.currentTimeMillis())));
    }

    public void setHome(String name, double lat, double lon) {
        executors.runInBackground(() -> {
            favoritePlaceDao.deleteByType(FavoritePlace.TYPE_HOME);
            favoritePlaceDao.insert(new FavoritePlace(name, lat, lon, FavoritePlace.TYPE_HOME, System.currentTimeMillis()));
        });
    }

    public void setWork(String name, double lat, double lon) {
        executors.runInBackground(() -> {
            favoritePlaceDao.deleteByType(FavoritePlace.TYPE_WORK);
            favoritePlaceDao.insert(new FavoritePlace(name, lat, lon, FavoritePlace.TYPE_WORK, System.currentTimeMillis()));
        });
    }

    public void addToHistory(String name, double lat, double lon) {
        executors.runInBackground(() -> searchHistoryDao.insert(
                new SearchHistoryItem(name, lat, lon, System.currentTimeMillis(), 0, 0)));
    }

    public void addTripToHistory(String name, double lat, double lon, double distanceMeters, double durationSeconds) {
        executors.runInBackground(() -> searchHistoryDao.insert(
                new SearchHistoryItem(name, lat, lon, System.currentTimeMillis(), distanceMeters, durationSeconds)));
    }
}
