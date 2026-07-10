package com.naviapp.data.local.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import com.naviapp.data.local.entity.SearchHistoryItem;
import java.util.List;

@Dao
public interface SearchHistoryDao {

    @Insert
    long insert(SearchHistoryItem item);

    @Query("SELECT * FROM search_history ORDER BY timestamp DESC LIMIT 50")
    LiveData<List<SearchHistoryItem>> getRecent();

    @Query("DELETE FROM search_history")
    void clearAll();
}
