package com.naviapp.data.remote;

import com.naviapp.data.remote.dto.OsrmRouteResponse;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;
import retrofit2.http.Query;

/**
 * Публичный демо-сервер OSRM (router.project-osrm.org) используется по умолчанию
 * для Phase 1 — он бесплатен, но не предназначен для промышленной нагрузки.
 * Для продакшна замените BASE_URL в RetrofitClient на собственный self-hosted
 * инстанс OSRM/Valhalla (см. README в дальнейших фазах разработки).
 *
 * Формат координат в пути: "lon1,lat1;lon2,lat2".
 * profile: driving | walking | cycling
 */
public interface OsrmApi {

    @GET("route/v1/{profile}/{coordinates}")
    Call<OsrmRouteResponse> getRoute(
            @Path("profile") String profile,
            @Path("coordinates") String coordinates,
            @Query("overview") String overview,      // "full" — вся геометрия
            @Query("geometries") String geometries,   // "geojson"
            @Query("steps") boolean steps,             // true — пошаговые инструкции
            @Query("alternatives") boolean alternatives // true — альтернативные маршруты
    );
}
