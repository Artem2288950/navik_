package com.naviapp.ui.route;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.naviapp.data.model.GeoPoint;
import com.naviapp.data.model.RouteResult;
import com.naviapp.data.repository.RepositoryCallback;
import com.naviapp.data.repository.RouteRepository;
import java.util.List;

public class RouteViewModel extends ViewModel {

    private final RouteRepository repository = new RouteRepository();

    private final MutableLiveData<List<RouteResult>> routes = new MutableLiveData<>();
    private final MutableLiveData<String> error = new MutableLiveData<>();
    private final MutableLiveData<Boolean> loading = new MutableLiveData<>(false);

    public LiveData<List<RouteResult>> getRoutes() {
        return routes;
    }

    public LiveData<String> getError() {
        return error;
    }

    public LiveData<Boolean> getLoading() {
        return loading;
    }

    public void buildRoute(@NonNull GeoPoint from, @NonNull GeoPoint to, @NonNull String profile) {
        loading.setValue(true);
        repository.buildRoute(from, to, profile, true, new RepositoryCallback<List<RouteResult>>() {
            @Override
            public void onSuccess(List<RouteResult> result) {
                loading.postValue(false);
                routes.postValue(result);
            }

            @Override
            public void onError(String errorMessage) {
                loading.postValue(false);
                error.postValue(errorMessage);
            }
        });
    }
}
