package com.naviapp.ui.main;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.naviapp.R;
import com.naviapp.data.model.GeoPoint;
import com.naviapp.data.model.Place;
import com.naviapp.data.model.RouteResult;
import com.naviapp.data.repository.RouteRepository;
import com.naviapp.databinding.ActivityMainBinding;
import com.naviapp.ui.navigation.NavigationActivity;
import com.naviapp.ui.search.SearchResultAdapter;
import com.naviapp.ui.search.SearchViewModel;
import com.naviapp.util.Constants;
import com.naviapp.util.LocationHelper;
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
import java.util.ArrayList;

/**
 * Главный экран: карта на весь экран, поиск, выбор точки Б, построение и
 * отображение маршрута А→Б, запуск экрана пошаговой навигации.
 *
 * Бесплатный векторный стиль карты (демо-стиль MapLibre на основе OSM данных) —
 * при желании его можно заменить на собственный self-hosted стиль (см. README).
 */
public class MainActivity extends AppCompatActivity {

    private static final String MAP_STYLE_URL = "https://demotiles.maplibre.org/style.json";
    private static final String ROUTE_SOURCE_ID = "route-source";
    private static final String ROUTE_LAYER_ID = "route-layer";

    private ActivityMainBinding binding;
    private MapView mapView;
    private MapLibreMap mapLibreMap;

    private MapViewModel mapViewModel;
    private SearchViewModel searchViewModel;

    private LocationHelper locationHelper;
    private SearchResultAdapter searchResultAdapter;

    private GeoPoint originPoint;
    private RouteResult selectedRoute;
    private String selectedProfile = RouteRepository.PROFILE_CAR;

    private final ActivityResultLauncher<String> locationPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), granted -> {
                if (granted) {
                    centerOnMyLocation();
                }
            });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mapView = binding.mapView;
        mapView.onCreate(savedInstanceState);

        mapViewModel = new ViewModelProvider(this).get(MapViewModel.class);
        searchViewModel = new ViewModelProvider(this).get(SearchViewModel.class);
        locationHelper = new LocationHelper(this);

        setupSearchUi();
        setupRouteUi();
        setupMap();
        observeViewModels();
    }

    private void setupMap() {
        mapView.getMapAsync(map -> {
            mapLibreMap = map;
            map.setStyle(new Style.Builder().fromUri(MAP_STYLE_URL), style -> {
                map.getUiSettings().setCompassEnabled(true);
                map.getUiSettings().setRotateGesturesEnabled(true);
                map.getUiSettings().setTiltGesturesEnabled(true);

                LatLng start = new LatLng(Constants.DEFAULT_LAT, Constants.DEFAULT_LON);
                map.moveCamera(CameraUpdateFactory.newLatLngZoom(start, Constants.DEFAULT_ZOOM));

                // Источник и слой для отрисовки маршрута — создаются заранее, заполняются позже
                GeoJsonSource routeSource = new GeoJsonSource(ROUTE_SOURCE_ID);
                style.addSource(routeSource);
                LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID)
                        .withProperties(
                                PropertyFactory.lineColor("#00695C"),
                                PropertyFactory.lineWidth(5f),
                                PropertyFactory.lineCap("round"),
                                PropertyFactory.lineJoin("round")
                        );
                style.addLayer(routeLayer);
            });

            map.addOnMapClickListener(point -> {
                // Долгое/короткое нажатие на карту выбирает точку назначения (Б)
                GeoPoint dest = new GeoPoint(point.getLatitude(), point.getLongitude());
                onDestinationSelected(dest, String.format(Locale.getDefault(),
                        "%.5f, %.5f", point.getLatitude(), point.getLongitude()));
                return true;
            });
        });

        binding.fabMyLocation.setOnClickListener(v -> requestLocationAndCenter());
        requestLocationAndCenter();
    }

    private void setupSearchUi() {
        searchResultAdapter = new SearchResultAdapter(place -> {
            binding.resultsCard.setVisibility(android.view.View.GONE);
            binding.editSearch.setText(place.getDisplayName());
            onDestinationSelected(place.toGeoPoint(), place.getDisplayName());
        });
        binding.recyclerResults.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerResults.setAdapter(searchResultAdapter);

        binding.btnSearch.setOnClickListener(v -> performSearch());
        binding.editSearch.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH
                    || (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER)) {
                performSearch();
                return true;
            }
            return false;
        });
    }

    private void performSearch() {
        String query = binding.editSearch.getText().toString().trim();
        if (query.isEmpty()) return;
        Double biasLat = originPoint != null ? originPoint.getLatitude() : Constants.DEFAULT_LAT;
        Double biasLon = originPoint != null ? originPoint.getLongitude() : Constants.DEFAULT_LON;
        searchViewModel.search(query, biasLat, biasLon, Locale.getDefault().getLanguage());
    }

    private void setupRouteUi() {
        binding.btnProfileCar.setOnClickListener(v -> {
            selectedProfile = RouteRepository.PROFILE_CAR;
            rebuildRouteIfPossible();
        });
        binding.btnProfileFoot.setOnClickListener(v -> {
            selectedProfile = RouteRepository.PROFILE_FOOT;
            rebuildRouteIfPossible();
        });
        binding.btnProfileBike.setOnClickListener(v -> {
            selectedProfile = RouteRepository.PROFILE_BIKE;
            rebuildRouteIfPossible();
        });
        binding.btnStartNavigation.setOnClickListener(v -> startNavigation());
    }

    private void observeViewModels() {
        searchViewModel.getResults().observe(this, places -> {
            searchResultAdapter.submitList(places);
            binding.resultsCard.setVisibility(
                    places != null && !places.isEmpty() ? android.view.View.VISIBLE : android.view.View.GONE);
        });
        searchViewModel.getError().observe(this, msg -> {
            if (msg != null) android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
        });

        routeViewModel = new ViewModelProvider(this).get(com.naviapp.ui.route.RouteViewModel.class);
        routeViewModel.getRoutes().observe(this, this::onRoutesReady);
        routeViewModel.getError().observe(this, msg -> {
            if (msg != null) android.widget.Toast.makeText(this, msg, android.widget.Toast.LENGTH_SHORT).show();
        });
    }

    private com.naviapp.ui.route.RouteViewModel routeViewModel;

    private void onDestinationSelected(GeoPoint destination, String label) {
        mapViewModel.destination.setValue(destination);
        requestOriginThenBuildRoute(destination);
    }

    private void requestOriginThenBuildRoute(GeoPoint destination) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            android.widget.Toast.makeText(this, R.string.location_permission_needed, android.widget.Toast.LENGTH_LONG).show();
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
            return;
        }
        locationHelper.getLastLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(GeoPoint point) {
                originPoint = point;
                buildRoute(point, destination);
            }

            @Override
            public void onLocationUnavailable() {
                // Фолбэк на дефолтный центр карты, чтобы маршрут всё равно строился
                originPoint = new GeoPoint(Constants.DEFAULT_LAT, Constants.DEFAULT_LON);
                buildRoute(originPoint, destination);
            }
        });
    }

    private void buildRoute(GeoPoint from, GeoPoint to) {
        routeViewModel.buildRoute(from, to, selectedProfile);
    }

    private void rebuildRouteIfPossible() {
        GeoPoint dest = mapViewModel.destination.getValue();
        if (dest != null && originPoint != null) {
            buildRoute(originPoint, dest);
        }
    }

    private void onRoutesReady(List<RouteResult> routes) {
        if (routes == null || routes.isEmpty()) return;
        selectedRoute = routes.get(0); // основной маршрут — первый (кратчайший по времени)
        mapViewModel.activeRoute.setValue(selectedRoute);

        binding.textRouteDuration.setText(selectedRoute.getFormattedDuration());
        binding.textRouteDistance.setText(selectedRoute.getFormattedDistance());
        binding.routeCard.setVisibility(android.view.View.VISIBLE);

        drawRouteOnMap(selectedRoute);
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
        if (source != null) {
            source.setGeoJson(collection);
        }

        if (!points.isEmpty()) {
            mapLibreMap.moveCamera(CameraUpdateFactory.newLatLngZoom(
                    new LatLng(points.get(0).latitude(), points.get(0).longitude()), 13));
        }
    }

    private void startNavigation() {
        if (selectedRoute == null || originPoint == null) return;
        Intent intent = new Intent(this, NavigationActivity.class);
        intent.putExtra(NavigationActivity.EXTRA_PROFILE, selectedProfile);
        intent.putExtra(NavigationActivity.EXTRA_ORIGIN_LAT, originPoint.getLatitude());
        intent.putExtra(NavigationActivity.EXTRA_ORIGIN_LON, originPoint.getLongitude());
        GeoPoint dest = mapViewModel.destination.getValue();
        if (dest != null) {
            intent.putExtra(NavigationActivity.EXTRA_DEST_LAT, dest.getLatitude());
            intent.putExtra(NavigationActivity.EXTRA_DEST_LON, dest.getLongitude());
        }
        startActivity(intent);
    }

    private void requestLocationAndCenter() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            centerOnMyLocation();
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION);
        }
    }

    private void centerOnMyLocation() {
        locationHelper.getLastLocation(new LocationHelper.LocationCallback() {
            @Override
            public void onLocationReceived(GeoPoint point) {
                originPoint = point;
                mapViewModel.currentLocation.setValue(point);
                if (mapLibreMap != null) {
                    mapLibreMap.animateCamera(CameraUpdateFactory.newLatLngZoom(
                            new LatLng(point.getLatitude(), point.getLongitude()), 15));
                }
            }

            @Override
            public void onLocationUnavailable() {
                // Тихо игнорируем — карта останется на дефолтном положении
            }
        });
    }

    // --- Жизненный цикл MapView должен полностью повторять жизненный цикл Activity ---
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
        mapView.onDestroy();
        super.onDestroy();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        mapView.onSaveInstanceState(outState);
    }
}
