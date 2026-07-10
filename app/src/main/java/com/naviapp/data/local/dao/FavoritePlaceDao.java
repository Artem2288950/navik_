package com.naviapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import com.naviapp.data.local.entity.FavoritePlace;
import java.util.List;

@Dao
public interface FavoritePlaceDao {

    @Insert
    long insert(FavoritePlace place);

    @Delete
    void delete(FavoritePlace place);

    @Query("SELECT * FROM favorite_places WHERE type = 'REGULAR' ORDER BY createdAt DESC")
    LiveData<List<FavoritePlace>> getRegularFavorites();

    @Query("SELECT * FROM favorite_places WHERE type = :type LIMIT 1")
    LiveData<FavoritePlace> getByType(String type);

    @Query("DELETE FROM favorite_places WHERE type = :type")
    void deleteByType(String type);
}
