package com.naviapp.data.repository;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.naviapp.data.model.Place;
import com.naviapp.data.remote.NominatimApi;
import com.naviapp.data.remote.PhotonApi;
import com.naviapp.data.remote.RetrofitClient;
import com.naviapp.data.remote.dto.NominatimResult;
import com.naviapp.data.remote.dto.PhotonResponse;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * Репозиторий поиска: адреса — через Nominatim, организации/POI — через Photon.
 * Оба результата объединяются в единый список Place и возвращаются вызывающей стороне.
 * Так реализуются пункты ТЗ "Поиск адресов/организаций/магазинов/кафе/..." без
 * необходимости в платных API (Google Places и т.п.).
 */
public class SearchRepository {

    private final NominatimApi nominatimApi = RetrofitClient.getNominatimApi();
    private final PhotonApi photonApi = RetrofitClient.getPhotonApi();

    public void search(@NonNull String query, @Nullable Double biasLat, @Nullable Double biasLon,
                        @NonNull String languageCode, @NonNull RepositoryCallback<List<Place>> callback) {

        // Запускаем оба поиска параллельно и объединяем результаты по мере готовности.
        List<Place> combined = new ArrayList<>();
        final boolean[] nominatimDone = {false};
        final boolean[] photonDone = {false};

        nominatimApi.search(query, "json", 8, languageCode).enqueue(new Callback<List<NominatimResult>>() {
            @Override
            public void onResponse(@NonNull Call<List<NominatimResult>> call,
                                    @NonNull Response<List<NominatimResult>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    for (NominatimResult r : response.body()) {
                        try {
                            double lat = Double.parseDouble(r.lat);
                            double lon = Double.parseDouble(r.lon);
                            combined.add(new Place(r.displayName, r.category, lat, lon));
                        } catch (NumberFormatException ignored) {
                        }
                    }
                }
                nominatimDone[0] = true;
                finishIfReady();
            }

            @Override
            public void onFailure(@NonNull Call<List<NominatimResult>> call, @NonNull Throwable t) {
                nominatimDone[0] = true;
                finishIfReady();
            }

            private void finishIfReady() {
                if (nominatimDone[0] && photonDone[0]) {
                    if (combined.isEmpty()) {
                        callback.onError("Ничего не найдено");
                    } else {
                        callback.onSuccess(combined);
                    }
                }
            }
        });

        photonApi.search(query, 8, languageCode, biasLat, biasLon).enqueue(new Callback<PhotonResponse>() {
            @Override
            public void onResponse(@NonNull Call<PhotonResponse> call,
                                    @NonNull Response<PhotonResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().features != null) {
                    for (PhotonResponse.Feature f : response.body().features) {
                        if (f.geometry == null || f.geometry.coordinates == null
                                || f.geometry.coordinates.size() != 2 || f.properties == null) {
                            continue;
                        }
                        String name = buildDisplayName(f.properties);
                        double lon = f.geometry.coordinates.get(0);
                        double lat = f.geometry.coordinates.get(1);
                        combined.add(new Place(name, f.properties.category, lat, lon));
                    }
                }
                photonDone[0] = true;
                finishIfReady();
            }

            @Override
            public void onFailure(@NonNull Call<PhotonResponse> call, @NonNull Throwable t) {
                photonDone[0] = true;
                finishIfReady();
            }

            private void finishIfReady() {
                if (nominatimDone[0] && photonDone[0]) {
                    if (combined.isEmpty()) {
                        callback.onError("Ничего не найдено");
                    } else {
                        callback.onSuccess(combined);
                    }
                }
            }
        });
    }

    private String buildDisplayName(PhotonResponse.Properties p) {
        StringBuilder sb = new StringBuilder();
        if (p.name != null) sb.append(p.name);
        if (p.street != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.street);
            if (p.houseNumber != null) sb.append(" ").append(p.houseNumber);
        }
        if (p.city != null) {
            if (sb.length() > 0) sb.append(", ");
            sb.append(p.city);
        }
        return sb.length() > 0 ? sb.toString() : "Без названия";
    }
}
