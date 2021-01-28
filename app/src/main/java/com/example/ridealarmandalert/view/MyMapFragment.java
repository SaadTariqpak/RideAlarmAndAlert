package com.example.ridealarmandalert.view;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.example.ridealarmandalert.R;
import com.example.ridealarmandalert.reciever.AlarmReceiver;
import com.example.ridealarmandalert.utils.Constants;
import com.example.ridealarmandalert.utils.FragUtil;
import com.example.ridealarmandalert.utils.SharedPreferencesManager;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mapbox.android.core.location.LocationEngine;
import com.mapbox.android.core.location.LocationEngineCallback;
import com.mapbox.android.core.location.LocationEngineProvider;
import com.mapbox.android.core.location.LocationEngineRequest;
import com.mapbox.android.core.location.LocationEngineResult;
import com.mapbox.api.directions.v5.DirectionsCriteria;
import com.mapbox.api.directions.v5.MapboxDirections;
import com.mapbox.api.directions.v5.models.DirectionsResponse;
import com.mapbox.api.directions.v5.models.DirectionsRoute;
import com.mapbox.api.geocoding.v5.models.CarmenFeature;
import com.mapbox.geojson.Feature;
import com.mapbox.geojson.LineString;
import com.mapbox.geojson.Point;
import com.mapbox.mapboxsdk.Mapbox;
import com.mapbox.mapboxsdk.camera.CameraPosition;
import com.mapbox.mapboxsdk.camera.CameraUpdateFactory;
import com.mapbox.mapboxsdk.geometry.LatLng;
import com.mapbox.mapboxsdk.location.LocationComponent;
import com.mapbox.mapboxsdk.location.LocationComponentActivationOptions;
import com.mapbox.mapboxsdk.location.modes.CameraMode;
import com.mapbox.mapboxsdk.location.modes.RenderMode;
import com.mapbox.mapboxsdk.maps.MapFragment;
import com.mapbox.mapboxsdk.maps.MapView;
import com.mapbox.mapboxsdk.maps.MapboxMap;
import com.mapbox.mapboxsdk.maps.OnMapReadyCallback;
import com.mapbox.mapboxsdk.maps.Style;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.OnSymbolLongClickListener;
import com.mapbox.mapboxsdk.plugins.annotation.Symbol;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolManager;
import com.mapbox.mapboxsdk.plugins.annotation.SymbolOptions;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.PlaceAutocomplete;
import com.mapbox.mapboxsdk.plugins.places.autocomplete.model.PlaceOptions;
import com.mapbox.mapboxsdk.style.layers.LineLayer;
import com.mapbox.mapboxsdk.style.layers.Property;
import com.mapbox.mapboxsdk.style.sources.GeoJsonSource;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionButton;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionHelper;
import com.wangjie.rapidfloatingactionbutton.RapidFloatingActionLayout;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RFACLabelItem;
import com.wangjie.rapidfloatingactionbutton.contentimpl.labellist.RapidFloatingActionContentLabelList;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import timber.log.Timber;

import static android.content.Context.ALARM_SERVICE;
import static android.os.Looper.getMainLooper;
import static com.example.ridealarmandalert.utils.Constants.flagAlarm;
import static com.example.ridealarmandalert.utils.Constants.locationReqCount;
import static com.mapbox.core.constants.Constants.PRECISION_6;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineCap;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineColor;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineJoin;
import static com.mapbox.mapboxsdk.style.layers.PropertyFactory.lineWidth;

public class MyMapFragment extends Fragment implements OnMapReadyCallback, MapboxMap.OnMapClickListener, RapidFloatingActionContentLabelList.OnRapidFloatingActionContentLabelListListener {
    View v, mViewBg;
    private static final int PERMISSION_REQUEST_CODE = 999;
    private static final int GPS_DILAOG_REQUEST = 765;
    private static final int REQUEST_CODE_AUTOCOMPLETE = 777;
    private MapboxMap mapboxMap;
    MapFragment mapFragment;

    private DirectionsRoute currentRoute;
    private MapboxDirections client;
    private Point origin;
    private Point destination;

    private static final String IMAGE_RED_ID = "image-red";
    private static final String IMAGE_BLUE_ID = "image-blue";
    private static final String IMAGE_YELLOW_ID = "image-yellow";
    private static final String SOURCE_ORIGIN_ID = "source-origin";
    private static final String SOURCE_DESTINATION_ID = "source-destination";
    private static final String LAYER_BASE_ID = "layer-base";
    private static final String LAYER_TEMP_ID = "layer-base";
    private static final String ROUTE_SOURCE_ID = "route-path";
    private static final String ROUTE_LAYER_ID = "route-layer";

    private TextView txtSheetLatLng, txtTimer;

    private RapidFloatingActionHelper rfabHelper;
    private RapidFloatingActionContentLabelList rfaContent;

    private BottomSheetBehavior sheetBehaviorShowLatLng;
    private LinearLayout bottomSheetShowLatLng, llBtnFindRoute;

    private LocationEngine locationEngine;
    LocationComponent locationComponent;

    private long DEFAULT_INTERVAL_IN_MILLISECONDS = 1000L;
    private long DEFAULT_MAX_WAIT_TIME = DEFAULT_INTERVAL_IN_MILLISECONDS * 20;
    // Variables needed to listen to location updates
    private MainActivityLocationCallback callback = new MainActivityLocationCallback(this);
    SymbolManager symbolManager;
    Symbol lastSymbol;
    String mEmail, uKey;

    DatabaseReference databaseReference;
    SharedPreferences sharedPreferencesManager;

    ArrayList<Symbol> symbolArrayList = new ArrayList<>();
    List<RFACLabelItem> fabItems = new ArrayList<>();
    private ArrayList<Timer> timerList = new ArrayList<>();
    private SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
    private NumberFormat f = new DecimalFormat("00");

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Mapbox.getInstance(getActivity(), getString(R.string.mapbox_access_token));

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.layout_map_fragment, container, false);
        try {

            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Home");

            txtTimer = v.findViewById(R.id.txt_timer);
            rfaContent = new RapidFloatingActionContentLabelList(getContext());
            rfaContent.setOnRapidFloatingActionContentLabelListListener(this);
            mViewBg = v.findViewById(R.id.bg);
            sharedPreferencesManager = new SharedPreferencesManager(getActivity()).getPreferencesManager();
            mEmail = sharedPreferencesManager.getString(SharedPreferencesManager.USERNAME, "");
            uKey = sharedPreferencesManager.getString(SharedPreferencesManager.USERUNIQUEID, "");

            databaseReference = FirebaseDatabase.getInstance().getReference();


            mapFragment = MapFragment.newInstance();
            ((AppCompatActivity) getActivity()).getFragmentManager()
                    .beginTransaction()
                    .add(R.id.map_fragment, mapFragment)
                    .commit();
            mapFragment.onCreate(savedInstanceState);
            mapFragment.getMapAsync(this::onMapReady);

        } catch (Exception ignore) {

        }
        return v;
    }


    private void requestPermission() {

        if (shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION)) {
            Toast.makeText(getContext(), "GPS permission allows us to access location data. Please allow in App Settings for additional functionality.", Toast.LENGTH_LONG).show();
            //ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        } else
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);

    }

    private boolean checkPermission() {
        boolean result = false;
        if (ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            result = true;
        }
        return result;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        switch (requestCode) {
            case PERMISSION_REQUEST_CODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getContext(), "Permission Granted, Now you can access location data.", Toast.LENGTH_SHORT).show();
                    // Get an instance of the component
                    LocationComponent locationComponent = mapboxMap.getLocationComponent();
                    locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions.builder(getActivity(), mapboxMap.getStyle()).build());
                    if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                    locationComponent.setRenderMode(RenderMode.COMPASS);
                    initLocationEngine();

                } else {
                    Log.e("value", "Permission Denied, You cannot use local drive .");
                    Toast.makeText(getContext(), "Permission Denied, You cannot use local drive .", Toast.LENGTH_SHORT).show();

                }
                break;
        }

    }


    @Override
    public void onMapReady(@NonNull MapboxMap mapboxMa) {
        mapboxMa.setStyle(Style.MAPBOX_STREETS, new Style.OnStyleLoaded() {
            @Override
            public void onStyleLoaded(@NonNull Style style) {

                setCustomFab();
                setBottomSheetLatLng();
                mapboxMap = mapboxMa;


                initSymbolManager();
                initSource(mapboxMap.getStyle());

                initLayers(mapboxMap.getStyle());
                mapboxMap.addOnMapClickListener(MyMapFragment.this::onMapClick);
                getUserFromFb();

                if (checkPermission()) {

                    locationComponent = mapboxMap.getLocationComponent();
                    locationComponent.activateLocationComponent(
                            LocationComponentActivationOptions.builder(getActivity(), mapboxMap.getStyle()).build());
                    locationComponent.setLocationComponentEnabled(true);
                    locationComponent.setCameraMode(CameraMode.TRACKING);
                    locationComponent.setRenderMode(RenderMode.COMPASS);

                    initLocationEngine();
                } else {
                    requestPermission();
                }


            }
        });
    }

    private void initLocationEngine() {
        locationEngine = LocationEngineProvider.getBestLocationEngine(getActivity());

        LocationEngineRequest request = new LocationEngineRequest.Builder(DEFAULT_INTERVAL_IN_MILLISECONDS)
                .setPriority(LocationEngineRequest.PRIORITY_HIGH_ACCURACY)
                .setMaxWaitTime(DEFAULT_MAX_WAIT_TIME).build();

        if (checkPermission()) {
            locationEngine.requestLocationUpdates(request, callback, getMainLooper());
        } else {
            requestPermission();
        }
        // locationEngine.getLastLocation(callback);
    }


    private boolean focusOnLocation(final Location location) {
        try {
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(location.getLatitude(), location.getLongitude()))
                    .zoom(14)
                    .build();

            mapboxMap.animateCamera(CameraUpdateFactory
                    .newCameraPosition(cameraPosition), 5000);

            List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
            symbolLayerIconFeatureList.add(Feature.fromGeometry(
                    Point.fromLngLat(location.getLongitude(), location.getLatitude())));

            //  mapboxMap.getStyle().addSource(new GeoJsonSource("mapbox.poi", FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));


            mapboxMap.getStyle().addImage(IMAGE_RED_ID, BitmapFactory.decodeResource(
                    getActivity().getResources(), R.drawable.mapbox_marker_icon_default));

            mapboxMap.getStyle().addImage(IMAGE_BLUE_ID, BitmapFactory.decodeResource(
                    getActivity().getResources(), R.drawable.map_default_map_marker));

            mapboxMap.getStyle().addImage(IMAGE_YELLOW_ID, BitmapFactory.decodeResource(
                    getActivity().getResources(), R.drawable.placeholder));


//            if (mapboxMap.getStyle().getSource(SOURCE_ORIGIN_ID) == null) {
//
//                mapboxMap.getStyle().addSource(new GeoJsonSource(SOURCE_ORIGIN_ID, FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));
//            }
////
//            if (mapboxMap.getStyle().getLayer(LAYER_BASE_ID) == null) {
//                mapboxMap.getStyle().addLayer(new SymbolLayer(LAYER_BASE_ID, SOURCE_ORIGIN_ID)
//                        .withProperties(
//                                iconImage(IMAGE_RED_ID),
//                                iconAllowOverlap(true),
//                                iconIgnorePlacement(true),
//                                iconOffset(new Float[]{0f, -9f}),
//                                iconSize(2.3f)
//
//                        ));
//            }
            return true;

        } catch (NullPointerException e) {
            return false;
        }
    }

    private void callFuseLocation() {
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                if (checkPermission()) {

                    // initFusedLocationManager(MapFrag.this);

                } else {
                    requestPermission();
                }
            }
        }, 1000);


    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == GPS_DILAOG_REQUEST) {
            switch (resultCode) {
                case Activity.RESULT_OK:
                    callFuseLocation();
                    break;

                case Activity.RESULT_CANCELED:

                    try {
                        getActivity().onBackPressed();
                    } catch (NullPointerException e) {
                    }
                    break;

            }

        } else if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_AUTOCOMPLETE) {

            // Retrieve selected location's CarmenFeature
            CarmenFeature selectedCarmenFeature = PlaceAutocomplete.getPlace(data);

            // Create a new FeatureCollection and add a new Feature to it using selectedCarmenFeature above.
            // Then retrieve and update the source designated for showing a selected location's symbol layer icon

            if (mapboxMap != null) {
                Style style = mapboxMap.getStyle();
                if (style != null) {

                    // Move map camera to the selected location
                    mapboxMap.animateCamera(CameraUpdateFactory.newCameraPosition(
                            new CameraPosition.Builder()
                                    .target(new LatLng(((Point) selectedCarmenFeature.geometry()).latitude(),
                                            ((Point) selectedCarmenFeature.geometry()).longitude()))
                                    .zoom(14)
                                    .build()), 4000);
                }
            }
        }


    }

    private void initSymbolManager() {
        // Set up a SymbolManager instance
        symbolManager = new SymbolManager((MapView) mapFragment.getView(), mapboxMap, mapboxMap.getStyle());


        // Add click listener and change the symbol to a cafe icon on click
        symbolManager.addClickListener(new OnSymbolClickListener() {
            @Override
            public boolean onAnnotationClick(Symbol symbol1) {
//                if (symbol1.getData() != null && symbol1.getData().getAsJsonObject() != null) {
//                    JsonObject rootObject = symbol1.getData().getAsJsonObject();
//                    String message = rootObject.get("tag").getAsString(); // get property "tag"
//                    Toast.makeText(getActivity(), "" + message, Toast.LENGTH_SHORT).show();
//                }

                return false;
            }
        });


        symbolManager.addLongClickListener(new OnSymbolLongClickListener() {
            @Override
            public boolean onAnnotationLongClick(Symbol symbol) {
                symbolManager.delete(symbol);
                return false;
            }
        });
    }

    @SuppressLint({"DefaultLocale", "SetTextI18n"})
    @Override
    public boolean onMapClick(@NonNull LatLng point) {


        if (sheetBehaviorShowLatLng.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            sheetBehaviorShowLatLng.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return false;
        }

        if (lastSymbol != null)
            symbolManager.delete(lastSymbol);

        lastSymbol = symbolManager.create(new SymbolOptions()
                .withLatLng(point)
                .withIconImage(IMAGE_BLUE_ID).
                        withIconOffset(new Float[]{0f, -9f})
                .withIconSize(1.0f));

        txtSheetLatLng.setText(String.format("%.10f", point.getLatitude()) + "/" + String.format("%.10f", point.getLongitude()));
        sheetBehaviorShowLatLng.setState(BottomSheetBehavior.STATE_EXPANDED);
//        Location location = mapboxMap.getLocationComponent().getLastKnownLocation();
//
//        origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());
//
//        destination = Point.fromLngLat(point.getLongitude(), point.getLatitude());


// Get the directions route from the Mapbox Directions API
        //     getRoute(mapboxMap, origin, destination);

// Add symbol at specified lat/lon


//        List<Feature> symbolLayerIconFeatureList = new ArrayList<>();
//        symbolLayerIconFeatureList.add(Feature.fromGeometry(
//                Point.fromLngLat(point.getLongitude(), point.getLatitude())));
//        if (mapboxMap.getStyle().getLayer(LAYER_TEMP_ID) != null)
//            mapboxMap.getStyle().removeLayer(LAYER_TEMP_ID);
//        if (mapboxMap.getStyle().getSource(SOURCE_DESTINATION_ID) != null)
//            mapboxMap.getStyle().removeSource(SOURCE_DESTINATION_ID);


//        mapboxMap.getStyle().addSource(new GeoJsonSource(SOURCE_DESTINATION_ID, FeatureCollection.fromFeatures(symbolLayerIconFeatureList)));

//        if (mapboxMap.getStyle().getLayer(LAYER_BASE_ID) == null) {
//        mapboxMap.getStyle().addLayer(new SymbolLayer(LAYER_TEMP_ID, SOURCE_DESTINATION_ID)
//                .withProperties(
//
//                        iconImage(IMAGE_BLUE_ID),
//                        iconAllowOverlap(false),
//                        iconIgnorePlacement(false),
//                        iconOffset(new Float[]{0f, -9f}),
//                        iconSize(1.5f)
//
//                ));
//        }
        return false;
    }

    private int dpToPx(int dp) {
        Resources r = getResources();
        return Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, r.getDisplayMetrics()));
    }

    private void setBottomSheetLatLng() {

        bottomSheetShowLatLng = v.findViewById(R.id.bottom_sheet_lat_lng);
        sheetBehaviorShowLatLng = BottomSheetBehavior.from(bottomSheetShowLatLng);

        txtSheetLatLng = v.findViewById(R.id.txt_lat_lng);
        llBtnFindRoute = v.findViewById(R.id.ll_btn_find_route);
        llBtnFindRoute.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Location location = mapboxMap.getLocationComponent().getLastKnownLocation();

                origin = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                destination = Point.fromLngLat(lastSymbol.getLatLng().getLongitude(), lastSymbol.getLatLng().getLatitude());


                //Get the directions route from the Mapbox Directions API
                getRoute(mapboxMap, origin, destination);

                sheetBehaviorShowLatLng.setState(BottomSheetBehavior.STATE_COLLAPSED);

            }
        });


        sheetBehaviorShowLatLng.addBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View view, int newState) {
                switch (newState) {
                    case BottomSheetBehavior.STATE_HIDDEN:
                        break;
                    case BottomSheetBehavior.STATE_EXPANDED: {
//                        spinnerEnDenFb.setSelection(0);

//                        edtLocationNameRCFb.setEnabled(false);

                    }
                    break;
                    case BottomSheetBehavior.STATE_COLLAPSED: {
                        mViewBg.setVisibility(View.GONE);
                    }
                    break;
                    case BottomSheetBehavior.STATE_DRAGGING:
                        break;
                    case BottomSheetBehavior.STATE_SETTLING:
                        break;
                }
            }

            @Override
            public void onSlide(@NonNull View view, float v) {
                mViewBg.setVisibility(View.VISIBLE);
                mViewBg.setAlpha(v);
            }
        });


    }

    private void setCustomFab() {

        fabItems.add(new RFACLabelItem<Integer>()
                .setLabel("Find Place")
                .setResId(R.drawable.ic_baseline_search_24)
                .setIconNormalColor(R.color.themeColor1)
                .setIconPressedColor(R.color.themeColor2)
                .setWrapper(0)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getResources().getDrawable(R.drawable.label_bg))
        );
        fabItems.add(new RFACLabelItem<Integer>()
                .setLabel("Nearby Users")
                .setResId(R.drawable.ic_baseline_supervised_user_circle_24)
                .setIconNormalColor(R.color.themeColor1)
                .setIconPressedColor(R.color.themeColor2)
                .setWrapper(0)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getResources().getDrawable(R.drawable.label_bg))
        );
        fabItems.add(new RFACLabelItem<Integer>()
                .setLabel("Add Alarm")
                .setResId(R.drawable.ic_baseline_add_alarm_24)
                .setIconNormalColor(R.color.themeColor1)
                .setIconPressedColor(R.color.themeColor2)
                .setWrapper(0)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getResources().getDrawable(R.drawable.label_bg))
        );


        fabItems.add(new RFACLabelItem<Integer>()
                .setIconNormalColor(R.color.themeColor1)
                .setIconPressedColor(R.color.themeColor2)
                .setWrapper(0)
                .setLabelColor(Color.WHITE)
                .setLabelBackgroundDrawable(getResources().getDrawable(R.drawable.label_bg))
        );

        if (!flagAlarm)
            fabItems.get(3).setLabel("Start Journey")
                    .setResId(R.drawable.ic_baseline_directions_bike_24);
        else
            fabItems.get(3).setLabel("Take Rest")
                    .setResId(R.drawable.ic_baseline_airline_seat_recline_extra_24);


        rfaContent
                .setItems(fabItems)
                .setIconShadowRadius(dpToPx(5))
                .setIconShadowColor(0xff888888)
                .setIconShadowDy(dpToPx(5))

        ;
        rfabHelper = new RapidFloatingActionHelper(
                getContext(),
                (RapidFloatingActionLayout) v.findViewById(R.id.activity_main_rfal),
                (RapidFloatingActionButton) v.findViewById(R.id.activity_main_rfab),
                rfaContent
        ).build();


    }

    @Override
    public void onRFACItemLabelClick(int position, RFACLabelItem rfacLabelItem) {
        rfabHelper.toggleContent();
        int positionIndex = 6 - position;

    }

    @Override
    public void onRFACItemIconClick(int position, RFACLabelItem rfacLabelItem) {
        rfabHelper.toggleContent();

        switch (position) {
            case 0:
                Location location = mapboxMap.getLocationComponent().getLastKnownLocation();
                Point mpoint = Point.fromLngLat(location.getLongitude(), location.getLatitude());

                Intent intent = new PlaceAutocomplete.IntentBuilder()
                        .accessToken(Mapbox.getAccessToken() != null ? Mapbox.getAccessToken() : getString(R.string.mapbox_access_token))
                        .placeOptions(PlaceOptions.builder()
                                        .backgroundColor(Color.parseColor("#EEEEEE"))
                                        .proximity(mpoint)

//                                .addInjectedFeature(home)
//                                .addInjectedFeature(work)
                                        .build(PlaceOptions.MODE_CARDS)
                        )
                        .build(getActivity());
                startActivityForResult(intent, REQUEST_CODE_AUTOCOMPLETE);
                break;
            case 1:
                locationReqCount = 0;
                if (locationComponent.isLocationComponentActivated()) {
                    Location location1 = mapboxMap.getLocationComponent().getLastKnownLocation();
                    if (location1 != null) {
                        Fragment fragment = new NearbyUsersListFragment();
                        Bundle bundle = new Bundle();
                        bundle.putParcelable("location", location1);
                        fragment.setArguments(bundle);
                        new FragUtil(getActivity()).
                                changeFragmentWithBackstack(fragment,
                                        R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
                                        R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                    } else {
                        Toast.makeText(getActivity(), "Currently location is not available!", Toast.LENGTH_SHORT).show();
                    }
                    break;
                } else {
                    Toast.makeText(getActivity(), "Location component not working properly,restart app", Toast.LENGTH_SHORT).show();

                }

            case 2:
                locationReqCount = 0;
                new FragUtil(getActivity()).
                        changeFragmentWithBackstack(new AlarmListFragment(),
                                R.id.main_container, R.anim.anim_slide_in_right, R.anim.anim_slide_out_left,
                                R.anim.anim_slide_in_left, R.anim.anim_slide_out_right);
                break;
            case 3:

                LinearLayout linearLayout1 = (LinearLayout) rfabHelper.obtainRFAContent().getChildAt(0);
                LinearLayout linearLayout2 = (LinearLayout) linearLayout1.getChildAt(3);
                TextView textViewM = (TextView) linearLayout2.getChildAt(0);
                ImageView imageViewM = (ImageView) linearLayout2.getChildAt(1);
                if (Constants.flagAlarm) {
                    textViewM.setText("Start Journey");
                    imageViewM.setImageResource(R.drawable.ic_baseline_directions_bike_24);
                    stopAlarm();
                } else {
                    textViewM.setText("Take Rest");
                    imageViewM.setImageResource(R.drawable.ic_baseline_airline_seat_recline_extra_24);

                    setAlarm();

                }
                break;
        }
    }

    private void stopAlarm() {

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(Context.ALARM_SERVICE);
        Intent intent = new Intent(getContext(), AlarmReceiver.class);


        alarmManager.cancel(PendingIntent.getBroadcast(getContext(), 787868,
                intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA));
        sharedPreferencesManager.edit().putBoolean(SharedPreferencesManager.FLAGTIMER, false).putLong(SharedPreferencesManager.TIMERTIMER, 0).apply();
        clearTimer();
        txtTimer.setText("00:00:00");

        Toast.makeText(getActivity(), "Time to take some rest", Toast.LENGTH_SHORT).show();

        Constants.flagAlarm = false;
        Constants.timerStartTime = 0;
    }

    private void setAlarm() {
        Calendar c = Calendar.getInstance();
        c.add(Calendar.HOUR, 3);

        long mTime = c.getTimeInMillis();
        Intent intent = new Intent(getActivity(), AlarmReceiver.class);
        intent.putExtra("title", "Hello, its time to take some rest");

        PendingIntent pendingIntent = PendingIntent.getBroadcast(getContext(), 787868, intent, PendingIntent.FLAG_UPDATE_CURRENT | Intent.FILL_IN_DATA);

        AlarmManager alarmManager = (AlarmManager) getContext().getSystemService(ALARM_SERVICE);

        assert alarmManager != null;
        alarmManager.set(AlarmManager.RTC_WAKEUP, mTime, pendingIntent);

        sharedPreferencesManager.edit().putBoolean(SharedPreferencesManager.FLAGTIMER, true).putLong(SharedPreferencesManager.TIMERTIMER, mTime).apply();

        Toast.makeText(getActivity(), "Journey Started", Toast.LENGTH_SHORT).show();

        Constants.flagAlarm = true;
        Constants.timerStartTime = c.getTimeInMillis();
        setTimer();
    }

    public void setTimer() {


        try {

            if (Constants.flagAlarm) {
                timerList.add(new Timer());

                if (timerList.size() > 0 && timerList.get(timerList.size() - 1) != null)

                    timerList.get(timerList.size() - 1).scheduleAtFixedRate(new TimerTask() {
                        @Override
                        public void run() {
                            if (getActivity() != null)
                                requireActivity().runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        try {

                                            Date currentDate = sdf.parse(sdf.format(Calendar.getInstance().getTime()));

                                            Long diff = Constants.timerStartTime - currentDate.getTime();

                                            Long n = diff / 1000;

                                            n %= (24 * 3600);
                                            Long hour = n / 3600;

                                            n %= 3600;
                                            Long minutes = n / 60;

                                            n %= 60;
                                            Long seconds = n;

                                            txtTimer.setText(f.format(hour) + ":" + f.format(minutes) + ":" + f.format(seconds));

                                        } catch (Exception e) {

                                        }
                                    }
                                });

                        }
                    }, 0, 1000);

            }
        } catch (Exception e) {
            //Toast.makeText(vpnService, "" + e.getMessage(), Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }

    }


    private class MainActivityLocationCallback
            implements LocationEngineCallback<LocationEngineResult> {

        //        private final WeakReference<MapFrag> activityWeakReference;
//
        MyMapFragment mapFrag;
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference();
        ;

        MainActivityLocationCallback(MyMapFragment mapFrag) {
            this.mapFrag = mapFrag;
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location has changed.
         *
         * @param result the LocationEngineResult object which has the last known location within it.
         */
        @Override
        public void onSuccess(LocationEngineResult result) {
            // MapFrag activity = activityWeakReference.get();

            if (mapFrag != null) {
                Location location = result.getLastLocation();

                if (location == null) {
                    return;
                }

                databaseReference.child("users").child(uKey).child("location").setValue(location.getLatitude() + "/" + location.getLongitude());


// Create a Toast which displays the new location's coordinates
//                if (mapFrag.getActivity() != null)
//                    Toast.makeText(mapFrag.getActivity(),
//                            String.valueOf(result.getLastLocation().getLatitude()) + String.valueOf(result.getLastLocation().getLongitude()),
//                            Toast.LENGTH_SHORT).show();

                if (locationReqCount == 0) {
                    if (mapFrag.focusOnLocation(location))
                        locationReqCount++;
                }


// Pass the new location to the Maps SDK's LocationComponent
                if (mapFrag.mapboxMap != null && result.getLastLocation() != null) {
                    mapFrag.mapboxMap.getLocationComponent().forceLocationUpdate(result.getLastLocation());

                }
            }
        }

        /**
         * The LocationEngineCallback interface's method which fires when the device's location can not be captured
         *
         * @param exception the exception message
         */
        @Override
        public void onFailure(@NonNull Exception exception) {
            Log.d("LocationChangeActivity", exception.getLocalizedMessage());

            if (mapFrag != null) {
                Toast.makeText(mapFrag.getActivity(), exception.getLocalizedMessage().toString(),
                        Toast.LENGTH_SHORT).show();
            }
        }
    }


    /**
     * Add the route and marker sources to the map
     */
    private void initSource(@NonNull Style loadedMapStyle) {

        loadedMapStyle.addSource(new GeoJsonSource(ROUTE_SOURCE_ID));

//        GeoJsonSource iconGeoJsonSource = new GeoJsonSource(ICON_SOURCE_ID, FeatureCollection.fromFeatures(new Feature[]{
//                Feature.fromGeometry(Point.fromLngLat(origin.longitude(), origin.latitude())),
//                Feature.fromGeometry(Point.fromLngLat(destination.longitude(), destination.latitude()))}));
//        loadedMapStyle.addSource(iconGeoJsonSource);
    }

    /**
     * Add the route and marker icon layers to the map
     */
    private void initLayers(@NonNull Style loadedMapStyle) {


        LineLayer routeLayer = new LineLayer(ROUTE_LAYER_ID, ROUTE_SOURCE_ID);

// Add the LineLayer to the map. This layer will display the directions route.
        routeLayer.setProperties(
                lineCap(Property.LINE_CAP_ROUND),
                lineJoin(Property.LINE_JOIN_ROUND),
                lineWidth(5f),
                lineColor(Color.parseColor("#009688"))
        );
        loadedMapStyle.addLayer(routeLayer);

// Add the red marker icon image to the map
//        loadedMapStyle.addImage(RED_PIN_ICON_ID, BitmapUtils.getBitmapFromDrawable(
//                getResources().getDrawable(R.drawable.red_marker)));

// Add the red marker icon SymbolLayer to the map
//        loadedMapStyle.addLayer(new SymbolLayer(ICON_LAYER_ID, ICON_SOURCE_ID).withProperties(
//                iconImage(RED_PIN_ICON_ID),
//                iconIgnorePlacement(true),
//                iconAllowOverlap(true),
//                iconOffset(new Float[]{0f, -9f})));
    }

    /**
     * Make a request to the Mapbox Directions API. Once successful, pass the route to the
     * route layer.
     *
     * @param mapboxMap   the Mapbox map object that the route will be drawn on
     * @param origin      the starting point of the route
     * @param destination the desired finish point of the route
     */
    private void getRoute(MapboxMap mapboxMap, Point origin, Point destination) {
        client = MapboxDirections.builder()
                .origin(origin)
                .destination(destination)
                .overview(DirectionsCriteria.OVERVIEW_FULL)
                .profile(DirectionsCriteria.PROFILE_DRIVING)
                .accessToken(getString(R.string.mapbox_access_token))
                .build();

        client.enqueueCall(new Callback<DirectionsResponse>() {
            @Override
            public void onResponse(Call<DirectionsResponse> call, Response<DirectionsResponse> response) {
// You can get the generic HTTP info about the response
                Timber.d("Response code: " + response.code());
                if (response.body() == null) {
                    Timber.e("No routes found, make sure you set the right user and access token.");
                    return;
                } else if (response.body().routes().size() < 1) {
                    Timber.e("No routes found");
                    return;
                }

// Get the directions route
                currentRoute = response.body().routes().get(0);


                if (mapboxMap != null) {
                    mapboxMap.getStyle(new Style.OnStyleLoaded() {
                        @Override
                        public void onStyleLoaded(@NonNull Style style) {

// Retrieve and update the source designated for showing the directions route
                            GeoJsonSource source = style.getSourceAs(ROUTE_SOURCE_ID);

// Create a LineString with the directions route's geometry and
// reset the GeoJSON source for the route LineLayer source
                            if (source != null) {
                                source.setGeoJson(LineString.fromPolyline(currentRoute.geometry(), PRECISION_6));
                            }
                        }
                    });
                }
            }

            @Override
            public void onFailure(Call<DirectionsResponse> call, Throwable throwable) {
                Timber.e("Error: " + throwable.getMessage());
                Toast.makeText(getActivity(), "Error: " + throwable.getMessage(),
                        Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onStop() {
        if (locationEngine != null)
            locationEngine.removeLocationUpdates(callback);
        if (locationComponent != null)
            locationComponent = null;

        clearTimer();


        super.onStop();
    }

    @Override
    public void onResume() {
        super.onResume();
        setTimer();
    }

    private void clearTimer() {
        for (Timer timer : timerList) {
            timer.purge();
            timer.cancel();
        }

        timerList.clear();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
// Cancel the Directions API request
        if (client != null) {
            client.cancelCall();
        }

    }


    private void getUserFromFb() {
        databaseReference.child("users").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                clearMarkers();

                for (DataSnapshot data : dataSnapshot.getChildren()) {

                    if (data.hasChildren()) {
                        if (data.hasChild("location")) {
                            Symbol symbol;
                            //  JsonElement jsonElement = JsonParser.parseString("{\"tag\":\"" + data.getKey() + "\"}");

                            String[] arr = data.child("location").getValue().toString().split("/");
                            LatLng point = new LatLng(Double.parseDouble(arr[0]), Double.parseDouble(arr[1]));
                            symbol = symbolManager.create(new SymbolOptions()
                                            .withLatLng(point)
                                            .withIconImage(IMAGE_YELLOW_ID).
                                                    withIconOffset(new Float[]{0f, -9f})
                                    //  .withIconSize(1.5f).withData(jsonElement)

                            );


                            symbolArrayList.add(symbol);
                        }

                    }


                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }


        });

    }

    private void clearMarkers() {
        for (Symbol symbol : symbolArrayList)
            symbolManager.delete(symbol);

        symbolArrayList.clear();
    }
}
