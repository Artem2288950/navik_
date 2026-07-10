package com.naviapp.data.remote;

import java.util.concurrent.TimeUnit;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import java.io.IOException;

/**
 * Единая точка создания Retrofit-клиентов для всех внешних бесплатных API.
 * Синглтоны, чтобы не создавать новый OkHttpClient (и его пул соединений/потоков)
 * на каждый запрос — важно для экономии памяти и CPU на слабых устройствах.
 */
public final class RetrofitClient {

    // Публичные демо-сервера. Для продакшн-нагрузки рекомендуется self-hosting.
    private static final String OSRM_BASE_URL = "https://router.project-osrm.org/";
    private static final String NOMINATIM_BASE_URL = "https://nominatim.openstreetmap.org/";
    private static final String PHOTON_BASE_URL = "https://photon.komoot.io/";

    private static volatile OsrmApi osrmApi;
    private static volatile NominatimApi nominatimApi;
    private static volatile PhotonApi photonApi;

    private RetrofitClient() {
    }

    private static OkHttpClient buildHttpClient(String userAgent) {
        Interceptor userAgentInterceptor = new Interceptor() {
            @Override
            public Response intercept(Chain chain) throws IOException {
                Request original = chain.request();
                Request withUa = original.newBuilder()
                        .header("User-Agent", userAgent)
                        .build();
                return chain.proceed(withUa);
            }
        };

        return new OkHttpClient.Builder()
                .connectTimeout(10, TimeUnit.SECONDS)
                .readTimeout(15, TimeUnit.SECONDS)
                .addInterceptor(userAgentInterceptor)
                .build();
    }

    public static OsrmApi getOsrmApi() {
        if (osrmApi == null) {
            synchronized (RetrofitClient.class) {
                if (osrmApi == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(OSRM_BASE_URL)
                            .client(buildHttpClient("NaviApp/0.1 (Android; contact: n/a)"))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    osrmApi = retrofit.create(OsrmApi.class);
                }
            }
        }
        return osrmApi;
    }

    public static NominatimApi getNominatimApi() {
        if (nominatimApi == null) {
            synchronized (RetrofitClient.class) {
                if (nominatimApi == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(NOMINATIM_BASE_URL)
                            .client(buildHttpClient("NaviApp/0.1 (Android; contact: n/a)"))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    nominatimApi = retrofit.create(NominatimApi.class);
                }
            }
        }
        return nominatimApi;
    }

    public static PhotonApi getPhotonApi() {
        if (photonApi == null) {
            synchronized (RetrofitClient.class) {
                if (photonApi == null) {
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(PHOTON_BASE_URL)
                            .client(buildHttpClient("NaviApp/0.1 (Android; contact: n/a)"))
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    photonApi = retrofit.create(PhotonApi.class);
                }
            }
        }
        return photonApi;
    }
}
