package com.naviapp.data.repository;

import androidx.annotation.NonNull;
import com.naviapp.data.model.GeoPoint;
import com.naviapp.data.model.RouteResult;
import com.naviapp.data.model.RouteStep;
import com.naviapp.data.remote.OsrmApi;
import com.naviapp.data.remote.RetrofitClient;
import com.naviapp.data.remote.dto.OsrmRouteResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий построения маршрутов. Инкапсулирует обращение к OSRM и
 * преобразование сырых DTO в чистые domain-модели (RouteResult/RouteStep).
 *
 * Профили движения соответствуют требованиям ТЗ: авто, пешком, велосипед.
 * Общественный транспорт OSRM не поддерживает — вынесен в отдельный TODO
 * для интеграции с OpenTripPlanner/Motis в следующей фазе (см. финальное summary).
 */
public class RouteRepository {

    public static final String PROFILE_CAR = "driving";
    public static final String PROFILE_FOOT = "walking";
    public static final String PROFILE_BIKE = "cycling";

    private final OsrmApi osrmApi = RetrofitClient.getOsrmApi();

    public void buildRoute(@NonNull GeoPoint from, @NonNull GeoPoint to, @NonNull String profile,
                            boolean withAlternatives, @NonNull RepositoryCallback<List<RouteResult>> callback) {

        String coordinates = String.format(Locale.US, "%f,%f;%f,%f",
                from.getLongitude(), from.getLatitude(),
                to.getLongitude(), to.getLatitude());

        Call<OsrmRouteResponse> call = osrmApi.getRoute(
                profile, coordinates, "full", "geojson", true, withAlternatives);

        call.enqueue(new Callback<OsrmRouteResponse>() {
            @Override
            public void onResponse(@NonNull Call<OsrmRouteResponse> call,
                                    @NonNull Response<OsrmRouteResponse> response) {
                if (!response.isSuccessful() || response.body() == null
                        || !"Ok".equals(response.body().code) || response.body().routes == null) {
                    callback.onError("Не удалось построить маршрут");
                    return;
                }
                List<RouteResult> results = new ArrayList<>();
                for (OsrmRouteResponse.OsrmRoute route : response.body().routes) {
                    results.add(mapToRouteResult(route, profile));
                }
                if (results.isEmpty()) {
                    callback.onError("Маршрут не найден");
                } else {
                    callback.onSuccess(results);
                }
            }

            @Override
            public void onFailure(@NonNull Call<OsrmRouteResponse> call, @NonNull Throwable t) {
                callback.onError("Ошибка сети: " + t.getMessage());
            }
        });
    }

    private RouteResult mapToRouteResult(OsrmRouteResponse.OsrmRoute route, String profile) {
        List<GeoPoint> geometry = new ArrayList<>();
        if (route.geometry != null && route.geometry.coordinates != null) {
            for (List<Double> coord : route.geometry.coordinates) {
                // OSRM отдаёт [lon, lat]
                geometry.add(new GeoPoint(coord.get(1), coord.get(0)));
            }
        }

        List<RouteStep> steps = new ArrayList<>();
        if (route.legs != null) {
            for (OsrmRouteResponse.OsrmLeg leg : route.legs) {
                if (leg.steps == null) continue;
                for (OsrmRouteResponse.OsrmStep step : leg.steps) {
                    String instruction = buildInstructionText(step);
                    GeoPoint loc = null;
                    if (step.maneuver != null && step.maneuver.location != null
                            && step.maneuver.location.size() == 2) {
                        loc = new GeoPoint(step.maneuver.location.get(1), step.maneuver.location.get(0));
                    }
                    steps.add(new RouteStep(
                            instruction,
                            step.maneuver != null ? step.maneuver.type : "",
                            step.maneuver != null ? step.maneuver.modifier : "",
                            step.distance,
                            loc));
                }
            }
        }

        return new RouteResult(geometry, steps, route.distance, route.duration, profile);
    }

    /**
     * Строит человекочитаемую инструкцию на русском из "сырого" maneuver.
     * В следующей фазе стоит вынести в отдельный InstructionFormatter
     * с полной локализацией под RU/UK/EN (сейчас — базовая русская формулировка).
     */
    private String buildInstructionText(OsrmRouteResponse.OsrmStep step) {
        String street = (step.name != null && !step.name.isEmpty()) ? " на " + step.name : "";
        String type = step.maneuver != null ? step.maneuver.type : "";
        String modifier = step.maneuver != null ? step.maneuver.modifier : "";

        if ("depart".equals(type)) {
            return "Начните движение" + street;
        }
        if ("arrive".equals(type)) {
            return "Вы прибыли в пункт назначения";
        }
        if ("roundabout".equals(type)) {
            return "Двигайтесь по круговому движению" + street;
        }
        if (modifier != null) {
            switch (modifier) {
                case "left":
                    return "Поверните налево" + street;
                case "right":
                    return "Поверните направо" + street;
                case "slight left":
                    return "Держитесь левее" + street;
                case "slight right":
                    return "Держитесь правее" + street;
                case "sharp left":
                    return "Резко поверните налево" + street;
                case "sharp right":
                    return "Резко поверните направо" + street;
                case "straight":
                    return "Продолжайте движение прямо" + street;
                case "uturn":
                    return "Развернитесь" + street;
            }
        }
        return "Продолжайте движение" + street;
    }
}
