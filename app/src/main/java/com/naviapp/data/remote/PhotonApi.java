package com.naviapp.data.remote;

import com.naviapp.data.remote.dto.PhotonResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Photon (photon.komoot.io) — быстрый poi-поиск на основе Nominatim/OSM данных.
 * Используем как второй источник поиска — сильнее в организациях/категориях
 * (кафе, аптеки, заправки), чем чистый Nominatim.
 */
public interface PhotonApi {

    @GET("api")
    Call<PhotonResponse> search(
            @Query("q") String query,
            @Query("limit") int limit,
            @Query("lang") String language,
            @Query("lat") Double lat,  // bias — приоритет ближайших результатов
            @Query("lon") Double lon
    );
}
