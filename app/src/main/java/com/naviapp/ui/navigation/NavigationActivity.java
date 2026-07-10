package com.naviapp.ui.navigation;

import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import com.naviapp.R;
import com.naviapp.data.model.GeoPoint;
import com.naviapp.data.model.RouteResult;
import com.naviapp.data.model.RouteStep;
import com.naviapp.databinding.ActivityNavigationBinding;
import com.naviapp.ui.route.RouteViewModel;
import com.naviapp.util.TtsHelper;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.maplibre.android.camera.CameraUpdateFactory;
import org.maplibre.android.geometry.LatLng;
import org.maplibre.android.maps.MapLibreMap;
import org.maplibre.android.maps.MapView;
import org.maplibre.android.maps.Style;
import org.maplibre.android.style.layers.LineLayer;
import org.maplibre.android.style.layers.PropertyFactory;
import org.maplibre.android.style.sources.GeoJsonSource;
import org.maplibre.geojson.Feature;
import org.maplibre.geojson.FeatureCollection;
import org.maplibre.geojson.LineString;
import org.maplibre.geojson.Point;

/**
 * Экран пошаговой навигации: карта с прогрессом маршрута, текущая инструкция
 * крупным текстом сверху, озвучивание каждого нового шага через TTS.
 *
 * Phase 1: озвучивание срабатывает при переходе к следующему шагу вручную
 * (кнопкой) — полноценное автоматическое определение прогресса по GPS
 * с "прилипанием к дороге" (map matching) запланировано в Phase 2.
 */
public class NavigationActivity extends AppCompatActivity {

    public static final String EXTRA_PROFILE = "extra_profile";
    public static final String EXTRA_ORIGIN_LAT = "extra_origin_lat";
    public static final String EXTRA_ORIGIN_LON = "extra_origin_lon";
    public static final String EXTRA_DEST_LAT = "extra_dest_lat";
    public static final String EXTRA_DEST_LON = "extra_dest_lon";

    private static final String MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json";
    private static final String ROUTE_SOURCE_ID = "nav-route-source";
    private static final String ROUTE_LAYER_ID = "nav-route-layer";

    private ActivityNavigationBinding binding;
    private MapView mapView;
    private MapLibreMap mapLibreMap;
    private RouteViewModel routeViewModel;
    private TtsHelper ttsHelper;

    private RouteResult currentRoute;
    private int currentStepIndex = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityNavigationBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.navMapView;
        mapView.onCreate(savedInstanceState);

        ttsHelper = new TtsHelper(this, Locale.getDefault().toLanguageTag());
        routeViewModel = new ViewModelProvider(this).get(RouteViewModel.class);

        setupMap();

        binding.btnStopNavigation.setOnClickListener(v -> finish());

        String profile = getIntent().getStringExtra(EXTRA_PROFILE);
        double originLat = getIntent().getDoubleExtra(EXTRA_ORIGIN_LAT, 0);
        double originLon = getIntent().getDoubleExtra(EXTRA_ORIGIN_LON, 0);
        double destLat = getIntent().getDoubleExtra(EXTRA_DEST_LAT, 0);
        double destLon = getIntent().getDoubleExtra(EXTRA_DEST_LON, 0);

        routeViewModel.getRoutes().observe(this, routes -> {
            if (routes == null || routes.isEmpty()) return;
            currentRoute = routes.get(0);
            currentStepIndex = 0;
            drawRouteOnMap(currentRoute);
            announceCurrentStep();
        });

        if (profile != null) {
            routeViewModel.buildRoute(new GeoPoint(originLat, originLon), new GeoPoint(destLat, destLon), profile);
        }

        // Временная кнопка "следующий шаг" — тап по карте продвигает навигацию.
        // В Phase 2 будет заменено на автоматическое отслеживание позиции GPS.
        binding.getRoot().setOnClickListener(v -> advanceStep());
    }

    private void setupMap() {
        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            map.setStyle(new Style.Builder().fromUri(MAP_STYLE_URL), style -> {
                GeoJsonSource routeSource = new GeoJsonSource(ROUTE_SOURCE_ID);
                style.addSource(routeSource);
                LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
                        .withProperties(
                                PropertyFactory.lineColor("#FF7043"),
                                PropertyFactory.lineWidth(6f),
                                PropertyFactory.lineCap("round"),
                                PropertyFactory.lineJoin("round")
                        );
                style.addLayer(routeLayer);
                if (currentRoute != null) {
                    drawRouteOnMap(currentRoute);
                }
            });
        });
    }

    private void drawRouteOnMap(RouteResult route) {
        if (mapLibreMap == null || mapLibreMap.getStyle() == null) return;
        List<Point> points = new ArrayList<>();
        for (GeoPoint gp : route.getGeometry()) {
            points.add(Point.fromLngLat(gp.getLongitude(), gp.getLatitude()));
        }
        LineString lineString = LineString.fromLngLats(points);
        FeatureCollection collection = FeatureCollection.fromFeature(Feature.fromGeometry(lineString));
        GeoJsonSource source = mapLibreMap.getStyle().getSourceAs(ROUTE_SOURCE_ID);
        if (source != null) source.setGeoJson(collection);

        if (!points.isEmpty()) {
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(points.get(0).latitude(), points.get(0).longitude()), 16));
        }

        binding.textRemainingDistance.setText(route.getFormattedDistance());
        binding.textEta.setText(route.getFormattedDuration());
    }

    private void announceCurrentStep() {
        if (currentRoute == null || currentRoute.getSteps().isEmpty()) return;
        RouteStep step = currentRoute.getSteps().get(currentStepIndex);
        binding.textInstruction.setText(step.getInstruction());
        binding.textStepDistance.setText(String.format(Locale.getDefault(), "через %d м", (int) step.getDistanceMeters()));
        ttsHelper.speak(step.getInstruction());
    }

    private void advanceStep() {
        if (currentRoute == null || currentRoute.getSteps().isEmpty()) return;
        if (currentStepIndex < currentRoute.getSteps().size() - 1) {
            currentStepIndex++;
            announceCurrentStep();
        } else {
            ttsHelper.speak(getString(R.string.you_have_arrived));
            binding.textInstruction.setText(R.string.you_have_arrived);
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        mapView.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mapView.onResume();
    }

    @Override
    protected void onPause() {
        mapView.onPause();
        super.onPause();
    }

    @Override
    protected void onStop() {
        mapView.onStop();
        super.onStop();
    }

    @Override
    protected void onLowMemory() {
        super.onLowMemory();
        mapView.onLowMemory();
    }

    @Override
    protected void onDestroy() {
        ttsHelper.shutdown();
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
