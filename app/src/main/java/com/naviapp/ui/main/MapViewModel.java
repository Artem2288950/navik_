package com.naviapp.ui.main;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.naviapp.data.model.GeoPoint;
import com.naviapp.data.model.RouteResult;

/**
 * Хранит текущее состояние экрана карты (точка назначения, активный маршрут),
 * переживает пересоздание Activity при повороте экрана.
 */
public class MapViewModel extends ViewModel {

    public final MutableLiveData<GeoPoint> destination = new MutableLiveData<>();
    public final MutableLiveData<RouteResult> activeRoute = new MutableLiveData<>();
    public final MutableLiveData<GeoPoint> currentLocation = new MutableLiveData<>();
}
