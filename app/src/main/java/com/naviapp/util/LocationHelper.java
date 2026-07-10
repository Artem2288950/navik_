package com.naviapp.util;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import androidx.core.content.ContextCompat;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.naviapp.data.model.GeoPoint;

/**
 * Обёртка над FusedLocationProviderClient для получения текущей позиции пользователя.
 */
public class LocationHelper {

    public interface LocationCallback {
        void onLocationReceived(GeoPoint point);
        void onLocationUnavailable();
    }

    private final FusedLocationProviderClient client;
    private final Context context;

    public LocationHelper(Context context) {
        this.context = context.getApplicationContext();
        this.client = LocationServices.getFusedLocationProviderClient(this.context);
    }

    public boolean hasLocationPermission() {
        return ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED;
    }

    @SuppressLint("MissingPermission")
    public void getLastLocation(LocationCallback callback) {
        if (!hasLocationPermission()) {
            callback.onLocationUnavailable();
            return;
        }
        client.getLastLocation()
                .addOnSuccessListener(location -> {
                    if (location != null) {
                        callback.onLocationReceived(new GeoPoint(location.getLatitude(), location.getLongitude()));
                    } else {
                        callback.onLocationUnavailable();
                    }
                })
                .addOnFailureListener(e -> callback.onLocationUnavailable());
    }
}
