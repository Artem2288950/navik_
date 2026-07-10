package com.naviapp.data.remote;

import com.naviapp.data.remote.dto.NominatimResult;
import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Query;

/**
 * Nominatim (nominatim.openstreetmap.org) — бесплатный геокодер OSM.
 * ВАЖНО: политика использования требует указывать User-Agent приложения
 * и не превышать 1 запрос/сек — это соблюдается в RetrofitClient через interceptor.
 */
public interface NominatimApi {

    @GET("search")
    Call<List<NominatimResult>> search(
            @Query("q") String query,
            @Query("format") String format,   // "json"
            @Query("limit") int limit,
            @Header("Accept-Language") String language
    );
}
