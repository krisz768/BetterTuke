package hu.krisz768.bettertuke;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.OnBackPressedDispatcher;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.os.BuildCompat;
import androidx.fragment.app.FragmentContainerView;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.animation.TypeEvaluator;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.Property;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import android.window.OnBackInvokedCallback;
import android.window.OnBackInvokedDispatcher;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusNum;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.IncomingBusFragment.BottomSheetIncomingBusFragment;
import hu.krisz768.bettertuke.NearStops.BottomSheetNearStops;
import hu.krisz768.bettertuke.SearchFragment.SearchViewFragment;
import hu.krisz768.bettertuke.TrackBusFragment.BottomSheetTrackBusFragment;
import hu.krisz768.bettertuke.UserDatabase.Favorite;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.models.BackStack;
import hu.krisz768.bettertuke.models.IncomBusBackStack;
import hu.krisz768.bettertuke.models.LatLngInterpolator;
import hu.krisz768.bettertuke.models.MarkerDescriptor;
import hu.krisz768.bettertuke.models.ScheduleBackStack;
import hu.krisz768.bettertuke.models.SearchResult;

public class MainActivity extends AppCompatActivity {
    private FusedLocationProviderClient fusedLocationClient;
    private BottomSheetBehavior<View> bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback BottomSheetCallback;
    private SearchView searchView;
    private SearchViewFragment Svf;
    private Integer CurrentPlace = -1;
    private Integer CurrentStop = -1;
    private Integer CurrentBusTrack = -1;
    private BusLine busLine;
    private LatLng SelectedPlace;
    private IncomBusBackStack IncomBusMode = new IncomBusBackStack("", "", false);
    private GoogleMap googleMap;
    private HashMap<Integer, BusPlaces> busPlaces;
    private HashMap<Integer, BusStops> busStops;
    private final List<BackStack> backStack = new ArrayList<>();
    private boolean smallMarkerMode = false;
    private boolean IsBackButtonHalfExpanded = true;
    private Marker BusMarker;
    private boolean UserTouchedMap = false;
    private ObjectAnimator MarkerAnimator;
    private Integer ShortcutType;
    private String ShortcutData;
    private boolean OnStartFragmentError = false;
    private boolean IsMapInitialized = false;
    private OnBackInvokedCallback onBackPressedCallback;
    private OnBackPressedCallback onBackPressedCallbackOld;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            ShortcutType = b.getInt("ShortcutType");
            ShortcutData = b.getString("ShortcutId");
        }

        setTheme();

        HelperProvider.RenderAllBitmap(this);

        busStops = BusStops.GetAllStops(this);
        busPlaces = BusPlaces.getAllBusPlaces(this);

        setContentView(R.layout.activity_main);

        View bottomSheet = findViewById(R.id.standard_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(bottomSheet);

        SetupBackButton();

        SetupBottomSheet();

        SetupSearchView();

        findViewById(R.id.ShowScheduleButton).setOnClickListener(view -> ShowSchedule(-1, null, null, null, false));

        findViewById(R.id.PosButton).setOnClickListener(view -> SelectPosUserPos());

        findViewById(R.id.PosButton).setVisibility(View.GONE);

        UserDatabase userDatabase = new UserDatabase(this);

        String AdEnabled = userDatabase.GetPreference("AdEnabled");
        AdView mAdView = findViewById(R.id.adView);
        if (AdEnabled != null && AdEnabled.equals("true")){
            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        } else {
            mAdView.setVisibility(View.GONE);
        }

        ((Flow)findViewById(R.id.MainFlow)).setMaxElementsWrap(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1);

        SetupGoogleMap();
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        SetupBottomSheet();

        final FragmentContainerView fragmentView = findViewById(R.id.fragmentContainerView2);

        ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int displayHeight = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    newConfig.screenHeightDp,
                    displayMetrics
            ));
            params.height = Math.round(displayHeight * bottomSheetBehavior.getHalfExpandedRatio());
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            params.height = bottomSheetBehavior.getMaxHeight();

        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int displayHeight = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    newConfig.screenHeightDp,
                    displayMetrics
            ));
            params.height = Math.round(displayHeight * bottomSheetBehavior.getHalfExpandedRatio());
        }

        fragmentView.setLayoutParams(params);

        bottomSheetBehavior.setHideable(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        ((Flow)findViewById(R.id.MainFlow)).setMaxElementsWrap(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE ? 2 : 1);
    }

    public void ChangeStop(int Id) {
        AddBackStack();

        CurrentStop = Id;
        ZoomToMarker();
        MarkerRenderer();
    }

    private void SetupGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        final Context ctx = this;

        assert mapFragment != null;
        mapFragment.getMapAsync(googleMap_ -> {
            googleMap = googleMap_;
            IsMapInitialized = true;

            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

            MapStyleOptions style = new MapStyleOptions(HelperProvider.GetMapTheme(ctx));
            googleMap.setMapStyle(style);

            googleMap.setOnMarkerClickListener(marker -> {
                MarkerClickListener(marker);
                return true;
            });

            googleMap.setOnCameraMoveStartedListener(i -> {
                if (i == GoogleMap.OnCameraMoveStartedListener.REASON_GESTURE) {
                    UserTouchedMap = true;
                }
            });

            googleMap.setOnCameraMoveListener(() -> {
                final float ZoomLevel = googleMap.getCameraPosition().zoom;
                if (ZoomLevel > 13.7) {
                    if (!smallMarkerMode) {
                        smallMarkerMode = true;
                        MarkerRenderer();
                    }
                } else {
                    if (smallMarkerMode) {
                        smallMarkerMode = false;
                        MarkerRenderer();
                    }
                }
            });

            googleMap.setOnMapLongClickListener(this::OnMapLongClickListener);

            boolean GetClosestStop = true;

            if (ShortcutType != null && ShortcutData != null) {
                if (ShortcutType == 0) {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    Date date = new Date();

                    ShowSchedule(-1, ShortcutData, "O", formatter.format(date),true);
                } else {
                    SelectStop(Integer.parseInt(ShortcutData), false);
                    GetClosestStop = false;
                }
            }

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                MarkerRenderer();
            } else {
                MarkerRenderer();

                googleMap.setMyLocationEnabled(true);
                findViewById(R.id.PosButton).setVisibility(View.VISIBLE);
                if (GetClosestStop) {
                    GetClosestStop();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            if (permissions[i].equals("android.permission.ACCESS_FINE_LOCATION")) {
                if (grantResults[i] != -1) {
                    if (IsMapInitialized) {
                        googleMap.setMyLocationEnabled(true);
                    }

                    findViewById(R.id.PosButton).setVisibility(View.VISIBLE);
                    GetClosestStop();
                } else {
                    GPSErr();
                }
            }
        }
    }

    private void GPSErr() {
        final LatLng Pecs = new LatLng(46.0707, 18.2331);

        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Pecs).zoom(12).build();

        if(IsMapInitialized) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        bottomSheetBehavior.setHideable(true);
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        Toast.makeText(this, R.string.GPSHint, Toast.LENGTH_LONG).show();
    }

    private void MarkerClickListener(Marker marker) {
        MarkerDescriptor Md = (MarkerDescriptor) marker.getTag();

        assert Md != null;
        if (Md.getType() == MarkerDescriptor.Types.Stop) {
            SelectStop(Md.getId(), true);
        } else if (Md.getType() == MarkerDescriptor.Types.Place) {
            SelectPlace(Md.getId());
        } else if (Md.getType() == MarkerDescriptor.Types.Bus){
            ZoomTo(BusMarker.getPosition());
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            UserTouchedMap = false;
        } else {
            ZoomTo(SelectedPlace);
        }
    }

    public void SelectStop(int StopId, boolean SaveBack) {
        if (SaveBack) {
            AddBackStack();
        }

        if (CurrentBusTrack != -1) {
            CurrentBusTrack = -1;
            busLine = null;
        }

        if (SelectedPlace != null) {
            SelectedPlace = null;
        }

        CurrentStop = StopId;

        BusStops Stop = busStops.get(StopId);
        if (Stop != null) {
            CurrentPlace = Stop.getPlace();
        }

        ZoomToMarker();

        IncomBusMode = new IncomBusBackStack("", "", false);
        ShowBottomSheetIncomingBuses();
        MarkerRenderer();
    }

    public void SelectPlace(int PlaceId) {
        AddBackStack();

        if (CurrentBusTrack != -1) {
            CurrentBusTrack = -1;
            busLine = null;
        }

        if (SelectedPlace != null) {
            SelectedPlace = null;
        }

        CurrentPlace = PlaceId;

        List<Integer> StopIds = new ArrayList<>();

        for (BusStops value : busStops.values()) {
            if (value.getPlace() == CurrentPlace) {
                StopIds.add(value.getId());
            }
        }

        UserDatabase userDatabase = new UserDatabase(this);

        int FavId = Integer.MAX_VALUE;

        for (int i = 0; i < StopIds.size(); i++) {
            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, StopIds.get(i).toString())) {
                int tFavId = userDatabase.GetId(StopIds.get(i).toString(), UserDatabase.FavoriteType.Stop);
                if (FavId > tFavId) {
                    CurrentStop = StopIds.get(i);
                    FavId = tFavId;
                }
            }
        }

        if (FavId == Integer.MAX_VALUE) {
            CurrentStop = StopIds.get(0);
        }

        ZoomToMarker();

        IncomBusMode = new IncomBusBackStack("", "", false);
        ShowBottomSheetIncomingBuses();
        MarkerRenderer();
    }

    private void ZoomToMarker() {
        if (CurrentStop == -1 && CurrentPlace == -1)
            return;

        if (CurrentStop == -1)  {
            BusPlaces CurrentPlaceObject = busPlaces.get(CurrentPlace);

            if (CurrentPlaceObject != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(CurrentPlaceObject.getGpsLatitude(), CurrentPlaceObject.getGpsLongitude())).zoom(17.5F).build();

                if (IsMapInitialized) {
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        } else {
            BusStops CurrentStopObject = busStops.get(CurrentStop);

            if (CurrentStopObject != null) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(CurrentStopObject.getGpsLatitude(), CurrentStopObject.getGpsLongitude())).zoom(17.5F).build();

                if (IsMapInitialized) {
                    googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                }
            }
        }
    }

    private void MarkerRenderer() {
        if (!IsMapInitialized)
            return;

        googleMap.clear();

        if (busLine == null) {
            BusMarker = null;
        }
        if (BusMarker != null) {
            BitmapDescriptor BusBitmap = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapBus));
            MarkerOptions BusMarkerOption = new MarkerOptions().position(new LatLng(BusMarker.getPosition().latitude, BusMarker.getPosition().longitude)).icon(BusBitmap);
            CreateBusMarker(BusMarkerOption);
        }

        BitmapDescriptor StopSelected = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));
        BitmapDescriptor StopNotSelected = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopNotSelected));
        BitmapDescriptor Place;
        if (smallMarkerMode) {
            Place = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapSmallPlace));
        } else {
            Place = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapPlace));
        }

        if (CurrentBusTrack == -1) {
            if (SelectedPlace != null) {
                BitmapDescriptor PlaceSelected = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.LocationPin));

                Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(SelectedPlace.latitude, SelectedPlace.longitude)).icon(PlaceSelected));
                assert marker != null;
                marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.PinPoint, -1));
                marker.setZIndex(Float.MAX_VALUE);
            }

            for (BusPlaces busPlace : busPlaces.values()) {
                if (busPlace.getId() == CurrentPlace) {
                    for (BusStops busStop : busStops.values()) {
                        if (busStop.getPlace() == CurrentPlace) {

                            BitmapDescriptor icon;
                            if ((busStop.getId() == CurrentStop || CurrentStop == -1) && SelectedPlace == null) {
                                icon = StopSelected;
                            } else {
                                icon = StopNotSelected;
                            }

                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude())).icon(icon));
                            assert marker != null;
                            marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Stop, busStop.getId()));
                        }
                    }
                } else {
                    Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busPlace.getGpsLatitude(), busPlace.getGpsLongitude())).icon(Place));
                    assert marker != null;
                    marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Place, busPlace.getId()));
                }
            }
        } else {
            for (int i = 0; i < busLine.getStops().length; i++) {
                BusStops busStop = busStops.get(busLine.getStops()[i].getStopId());
                if (busStop == null) {
                    continue;
                }
                BitmapDescriptor icon;
                if (CurrentStop == busStop.getId()) {
                    icon = StopSelected;
                } else {
                    icon = StopNotSelected;
                }

                Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude())).icon(icon));
                assert marker != null;
                marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Stop, busStop.getId()));
            }

            PolylineOptions lineOptions = new PolylineOptions();
            ArrayList<LatLng> points = new ArrayList<>();

            for (int i = 0; i < busLine.getRoute().length; i++) {
                LatLng position = new LatLng(busLine.getRoute()[i].getGpsLatitude(), busLine.getRoute()[i].getGpsLongitude());
                points.add(position);
            }

            lineOptions.addAll(points);
            lineOptions.width(12);

            TypedValue typedValue = new TypedValue();
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            ContextCompat.getColor(this, typedValue.resourceId);

            lineOptions.color(ContextCompat.getColor(this, typedValue.resourceId));
            lineOptions.geodesic(true);

            googleMap.addPolyline(lineOptions);
        }
    }

    private void GetClosestStop() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            final LatLng Pecs = new LatLng(46.0707, 18.2331);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(Pecs).zoom(12).build();

            if (IsMapInitialized) {
                googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return this;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            }).addOnSuccessListener(location -> {
                if (location != null) {
                    GetStartupStop(location);
                } else {
                    GPSErr();
                }
            });
        } catch (Exception e) {
            Log.e("Main", e.toString());
        }
    }

    private void GetStartupStop(Location location)
    {
        UserDatabase userDatabase = new UserDatabase(this);
        Favorite[] favoriteStops = userDatabase.GetFavorites(UserDatabase.FavoriteType.Stop);

        float Closest = Float.MAX_VALUE;
        int ClosestId = -1;
        BusStops ClosestStop = null;

        for (BusStops busStop : busStops.values()) {
            Location stopLocation = new Location("");
            stopLocation.setLongitude(busStop.getGpsLongitude());
            stopLocation.setLatitude(busStop.getGpsLatitude());

            float Distance = location.distanceTo(stopLocation);
            if (Distance < 17.5F && Closest > Distance) {
                Closest = Distance;
                ClosestId = busStop.getId();
                ClosestStop = busStop;
            }
        }

        if (ClosestId != -1) {
            SelectStop(ClosestId, false);
            ZoomClose(new LatLng(ClosestStop.getGpsLatitude(), ClosestStop.getGpsLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
            return;
        }

        for (Favorite favoriteStop : favoriteStops) {
            BusStops busStop = busStops.get(Integer.parseInt(favoriteStop.getData()));

            if (busStop == null) {
                continue;
            }

            Location stopLocation = new Location("");
            stopLocation.setLongitude(busStop.getGpsLongitude());
            stopLocation.setLatitude(busStop.getGpsLatitude());

            float Distance = location.distanceTo(stopLocation);
            if (Distance < 500 && Distance < Closest) {
                Closest = Distance;
                ClosestId = busStop.getId();
                ClosestStop = busStop;
            }
        }

        if (ClosestId != -1) {
            SelectStop(ClosestId, false);
            ZoomClose(new LatLng(ClosestStop.getGpsLatitude(), ClosestStop.getGpsLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            for (BusStops busStop : busStops.values()) {
                Location stopLocation = new Location("");
                stopLocation.setLongitude(busStop.getGpsLongitude());
                stopLocation.setLatitude(busStop.getGpsLatitude());

                float Distance = location.distanceTo(stopLocation);
                if (Distance < Closest) {
                    Closest = Distance;
                    ClosestId = busStop.getId();
                    ClosestStop = busStop;
                }
            }

            SelectStop(ClosestId,false);
            if (ClosestStop != null) {
                ZoomClose(new LatLng(ClosestStop.getGpsLatitude(), ClosestStop.getGpsLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
            }
        }
    }

    private void ZoomClose(LatLng location, LatLng location2) {
        try {
            LatLng First = new LatLng(Math.min(location.latitude, location2.latitude), Math.min(location.longitude, location2.longitude));
            LatLng Second = new LatLng(Math.max(location.latitude, location2.latitude), Math.max(location.longitude, location2.longitude));

            LatLngBounds Bounds = new LatLngBounds(First, Second);

            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

            int dp20 = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    20,
                    displayMetrics
            ));

            if (IsMapInitialized) {
                googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(Bounds, dp20 * 4));
            }
        } catch (Exception e) {
            if (BusMarker != null) {
                ZoomTo(location2);
            } else {
                ZoomToMarker();
            }
        }
    }

    private void SetupBottomSheet() {
        final FragmentContainerView fragmentView = findViewById(R.id.fragmentContainerView2);
        final FloatingActionButton ScheduleButton = findViewById(R.id.ShowScheduleButton);

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);

        int dp20 = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20,
                displayMetrics
        ));

        if (BottomSheetCallback == null) {
            BottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
                    ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

                    if (newState == BottomSheetBehavior.STATE_HALF_EXPANDED) {
                        IsBackButtonHalfExpanded = true;

                        params.height = Math.round(bottomSheet.getMeasuredHeight() * bottomSheetBehavior.getHalfExpandedRatio());

                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            if(IsMapInitialized) {
                                googleMap.setPadding(0, dp20 * 4, 0, 0);
                            }
                            params2.bottomMargin = dp20;
                        } else {
                            if(IsMapInitialized) {
                                googleMap.setPadding(0, dp20 * 4, 0, params.height);
                            }
                            params2.bottomMargin = params.height + dp20;
                        }
                        DisableBack();
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        params.height = bottomSheet.getHeight();
                        EnableBack();
                    } else if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            if(IsMapInitialized) {
                                googleMap.setPadding(0, dp20 * 4, 0, 0);
                            }
                            params2.bottomMargin = dp20;
                        } else {
                            if(IsMapInitialized) {
                                googleMap.setPadding(0, dp20 * 4, 0, bottomSheetBehavior.getPeekHeight());
                            }
                            params2.bottomMargin = bottomSheetBehavior.getPeekHeight() + dp20;
                        }
                        EnableBack();
                    } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        EnableBack();
                    }

                    fragmentView.setLayoutParams(params);
                    ScheduleButton.setLayoutParams(params2);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
                    ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

                    if (((bottomSheet.getMeasuredHeight()-bottomSheetBehavior.getPeekHeight())*slideOffset)+bottomSheetBehavior.getPeekHeight() < bottomSheet.getMeasuredHeight() * bottomSheetBehavior.getHalfExpandedRatio()) {
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            if(IsMapInitialized) {
                                googleMap.setPadding(0, dp20 * 4, 0, 0);
                            }
                            params2.bottomMargin = dp20;
                        } else {
                            int CalculatedHeight = Math.round(((bottomSheet.getMeasuredHeight()-bottomSheetBehavior.getPeekHeight())*slideOffset)+bottomSheetBehavior.getPeekHeight());
                            if(IsMapInitialized) {
                                googleMap.setPadding(0, dp20 * 4, 0, CalculatedHeight);
                            }
                            params2.bottomMargin = CalculatedHeight + dp20;
                        }
                    } else {
                        params.height = Math.round(bottomSheetBehavior.getPeekHeight() + ((bottomSheet.getMeasuredHeight() - bottomSheetBehavior.getPeekHeight()) * slideOffset));
                    }

                    fragmentView.setLayoutParams(params);
                    ScheduleButton.setLayoutParams(params2);
                }
            };
            bottomSheetBehavior.addBottomSheetCallback(BottomSheetCallback);
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);

            int height = displayMetrics.heightPixels / 10;
            bottomSheetBehavior.setPeekHeight(height);

            bottomSheetBehavior.setMaxHeight(height);
            bottomSheetBehavior.setHideable(false);

            if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();
                params2.bottomMargin = dp20;
                ScheduleButton.setLayoutParams(params2);
            } else {
                ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();
                params2.bottomMargin = height + dp20;
                ScheduleButton.setLayoutParams(params2);
            }

            GPSLoadFragment InBusFragment = GPSLoadFragment.newInstance();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, InBusFragment)
                    .commit();
        } else {
            int height = displayMetrics.heightPixels / 3;

            float Ratio = 0.33F;

            int MinHeight = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    180,
                    displayMetrics
            ));


            if (MinHeight > height) {
                Ratio = ((float)MinHeight)/((float)displayMetrics.heightPixels);
                height = MinHeight;
            }

            bottomSheetBehavior.setFitToContents(false);
            bottomSheetBehavior.setHalfExpandedRatio(Ratio);

            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

            if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if(IsMapInitialized) {
                        googleMap.setPadding(0, dp20 * 4, 0, 0);
                    }
                    params2.bottomMargin = dp20;
                } else {
                    if(IsMapInitialized) {
                        googleMap.setPadding(0, dp20 * 4, 0, height);
                    }
                    params2.bottomMargin = height + dp20;
                }

                ScheduleButton.setLayoutParams(params2);
            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED){
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if(IsMapInitialized) {
                        googleMap.setPadding(0, dp20 * 4, 0, 0);
                    }
                    params2.bottomMargin = dp20;
                } else {
                    if(IsMapInitialized) {
                        googleMap.setPadding(0, dp20 * 4, 0, height);
                    }
                    params2.bottomMargin = height + dp20;
                }

                ScheduleButton.setLayoutParams(params2);
            } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED){
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    if(IsMapInitialized) {
                        googleMap.setPadding(0, dp20 * 4, 0, 0);
                    }
                    params2.bottomMargin = dp20;
                } else {
                    if(IsMapInitialized) {
                        googleMap.setPadding(0, dp20 * 4, 0, bottomSheetBehavior.getPeekHeight());
                    }
                    params2.bottomMargin = bottomSheetBehavior.getPeekHeight() + dp20;
                }

                ScheduleButton.setLayoutParams(params2);
            }
        }
    }

    private void ShowBottomSheetIncomingBuses() {
        BottomSheetSetNormalParams(65);

        try{
            BottomSheetIncomingBusFragment InBusFragment = BottomSheetIncomingBusFragment.newInstance(CurrentPlace, CurrentStop, IncomBusMode, busPlaces, busStops);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, InBusFragment)
                    .commit();

        }catch (Exception ignored) {
            OnStartFragmentError = true;
        }
    }

    public void TrackBus(int Id, String Date) {
        if (!(busLine != null && busLine.getCTrip() != null && busLine.getCTrip().getLineId() == Id)) {
            AddBackStack();
        }

        busLine = BusLine.BusLinesByLineId(Id, true, this);
        if (busLine == null) {
            Toast.makeText(this, R.string.DatabaseError, Toast.LENGTH_LONG).show();
            return;
        }

        if (busLine.getCTrip() != null) {
            Toast.makeText(this, "folytatódik: " + busLine.getCTrip().getRouteInfo().getLineNum(), Toast.LENGTH_LONG).show();
        }

        if (SelectedPlace != null) {
            SelectedPlace = null;
        }

        CurrentBusTrack = Id;
        if (Date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.util.Date date = new Date();
            if (!Date.equals(formatter.format(date))) {
                busLine.setDate(Date);
            }
        }

        if (BusMarker != null) {
            if(MarkerAnimator != null) {
                MarkerAnimator.cancel();
            }
            BusMarker.remove();
            BusMarker = null;
        }

        boolean ZoomToFirst = true;

        for (int i = 0; i < busLine.getStops().length; i++) {
            if (busLine.getStops()[i].getStopId() == CurrentStop) {
                ZoomToFirst = false;
            }
        }

        if (ZoomToFirst) {
            BusStops busStop = busStops.get(busLine.getStops()[0].getStopId());
            if (busStop != null) {
                ZoomTo(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude()));
            }
        }

        MarkerRenderer();
        ShowBottomSheetTrackBus();
    }

    private void BottomSheetSetNormalParams(int HeaderInDp) {
        IsBackButtonHalfExpanded = bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int dp20 = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20,
                displayMetrics
        ));

        int HeaderInPx = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                HeaderInDp,
                displayMetrics
        ));

        int MinHeight = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                180,
                displayMetrics
        ));

        int height = displayMetrics.heightPixels / 3;

        float Ratio = 0.33F;

        if (MinHeight > height) {
            height = MinHeight;
            Ratio = ((float)MinHeight)/((float)displayMetrics.heightPixels);
        }

        bottomSheetBehavior.setFitToContents(false);
        bottomSheetBehavior.setHalfExpandedRatio(Ratio);

        bottomSheetBehavior.setMaxHeight(-1);
        bottomSheetBehavior.setHideable(getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);

        bottomSheetBehavior.setPeekHeight(HeaderInPx);

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
        }

        final View Fc = findViewById(R.id.fragmentContainerView2);
        final FloatingActionButton ScheduleButton = findViewById(R.id.ShowScheduleButton);

        ViewGroup.LayoutParams params = Fc.getLayoutParams();
        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED) {
            params.height = height;
        } else {
            params.height = getWindow().getDecorView().getHeight();
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            if(IsMapInitialized) {
                googleMap.setPadding(0, dp20 * 4, 0, 0);
            }
            params2.bottomMargin = dp20;
        } else {
            if(IsMapInitialized) {
                googleMap.setPadding(0, dp20 * 4, 0, height);
            }
            params2.bottomMargin = height + dp20;
        }
        Fc.setLayoutParams(params);
        ScheduleButton.setLayoutParams(params2);
    }

    private void ShowBottomSheetTrackBus() {
        BottomSheetSetNormalParams(90);

        BottomSheetTrackBusFragment TrackBusFragment = BottomSheetTrackBusFragment.newInstance( CurrentStop, busPlaces, busStops, busLine);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, TrackBusFragment)
                .commit();
    }

    public void BusPositionMarker(LatLng BusPosition) {
        if (busLine != null) {
            if (BusPosition != null && IsMapInitialized) {
                BitmapDescriptor BusBitmap = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapBus));
                if (BusMarker == null) {
                    UserTouchedMap = false;

                    MarkerOptions BusMarkerOption = new MarkerOptions().position(new LatLng(BusPosition.latitude, BusPosition.longitude)).icon(BusBitmap);
                    CreateBusMarker(BusMarkerOption);

                    boolean ZoomToLast = true;

                    for (int i = 0; i < busLine.getStops().length; i++) {
                        if (busLine.getStops()[i].getStopId() == CurrentStop) {
                            ZoomToLast = false;
                        }
                    }

                    BusStops busStop;

                    if (ZoomToLast) {
                        busStop = busStops.get(busLine.getStops()[busLine.getStops().length - 1].getStopId());

                    } else {
                        busStop  = busStops.get(CurrentStop);
                    }

                    if (busStop != null) {
                        ZoomClose(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude()), new LatLng(BusPosition.latitude, BusPosition.longitude));
                    }
                } else {
                    animateMarker(BusMarker, BusPosition, new LatLngInterpolator.Linear());
                    if (!UserTouchedMap && IsMapInitialized) {
                        LatLngBounds currentScreen = googleMap.getProjection().getVisibleRegion().latLngBounds;

                        if(!currentScreen.contains(BusPosition)) {
                            ZoomTo(BusPosition);
                        }
                    }
                }
            } else {
                if (BusMarker != null) {
                    if(MarkerAnimator != null) {
                        MarkerAnimator.cancel();
                    }
                    BusMarker.remove();
                    BusMarker = null;
                }
            }
        } else {
            if (BusMarker != null) {
                if(MarkerAnimator != null) {
                    MarkerAnimator.cancel();
                }
                BusMarker.remove();
                BusMarker = null;
            }
        }
    }

    private void animateMarker(Marker marker, LatLng finalPosition, final LatLngInterpolator latLngInterpolator) {
        if(MarkerAnimator != null) {
            MarkerAnimator.cancel();
        }

        TypeEvaluator<LatLng> typeEvaluator = latLngInterpolator::interpolate;
        Property<Marker, LatLng> property = Property.of(Marker.class, LatLng.class, "position");
        MarkerAnimator = ObjectAnimator.ofObject(marker, property, typeEvaluator, finalPosition);
        MarkerAnimator.setDuration(500);
        MarkerAnimator.start();
    }

    public void ZoomTo(LatLng Position) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Position).zoom(15).build();

        if(IsMapInitialized) {
            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }
    }

    private void CreateBusMarker(MarkerOptions option) {
        if (IsMapInitialized) {
            BusMarker = googleMap.addMarker(option);
        }

        assert BusMarker != null;
        BusMarker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Bus, -1));
        BusMarker.setZIndex(Float.MAX_VALUE);
    }

    public boolean IsBottomSheetHalfExpanded() {
        return bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HALF_EXPANDED || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    private void SetupBackButton() {
            onBackPressedCallback = this::DoBack;

            onBackPressedCallbackOld = new OnBackPressedCallback(true) {
                @Override
                public void handleOnBackPressed() {
                    DoBack();
                }
            };
    }

    private void DoBack() {
        if (searchView.isShowing()) {
            searchView.hide();
            return;
        }

        if ((bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN || bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) && CurrentStop != -1) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            return;
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && IsBackButtonHalfExpanded) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
            return;
        }

        if (backStack.size() == 1) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressedCallback);
            }
        }

        if (backStack.size() == 0) {
            finish();
        }

        RestorePrevState();
    }

    private void EnableBack() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            getOnBackInvokedDispatcher().registerOnBackInvokedCallback(
                    OnBackInvokedDispatcher.PRIORITY_DEFAULT,
                    onBackPressedCallback
            );
        } else {
            getOnBackPressedDispatcher().addCallback(onBackPressedCallbackOld);
        }
    }

    private void DisableBack() {
        if (backStack.size() == 0) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                getOnBackInvokedDispatcher().unregisterOnBackInvokedCallback(onBackPressedCallback);
            }
        }
    }

    private void RestorePrevState() {
        BackStack PrevState = backStack.get(backStack.size() - 1);

        ScheduleBackStack scheduleBackStack = PrevState.getScheduleBackStack();
        if (scheduleBackStack != null) {
            ShowSchedule(scheduleBackStack.getStopId(), scheduleBackStack.getLineNum(), scheduleBackStack.getDirection(), scheduleBackStack.getDate(), scheduleBackStack.isPreSelected());

            backStack.remove(backStack.size() - 1);

            RestorePrevState();
            return;
        }

        CurrentPlace = PrevState.getCurrentPlace();
        CurrentStop = PrevState.getCurrentStop();
        CurrentBusTrack = PrevState.getCurrentBusTrack();
        busLine = PrevState.getBusLine();
        SelectedPlace = PrevState.getSelectedPlace();
        IncomBusMode = PrevState.getIncomBusMode();

        backStack.remove(backStack.size() - 1);

        if (BusMarker != null) {
            if(MarkerAnimator != null) {
                MarkerAnimator.cancel();
            }
            BusMarker.remove();
            BusMarker = null;
        }

        switch (DetermineMode()) {
            case IncBus:
                ShowBottomSheetIncomingBuses();
                ZoomToMarker();
                break;
            case TrackBus:
                ShowBottomSheetTrackBus();

                break;
            case NearStops:
                ShowBottomSheetNearStops();
                ZoomTo(SelectedPlace);
                break;
        }

        IsBackButtonHalfExpanded = PrevState.isBackButtonCollapse();

        MarkerRenderer();
    }

    private Mode DetermineMode() {
        if (CurrentStop == -1 && CurrentPlace == -1) {
            return Mode.None;
        } else if (SelectedPlace != null) {
            return Mode.NearStops;
        } else if (CurrentBusTrack != -1) {
            return Mode.TrackBus;
        } else {
            return Mode.IncBus;
        }
    }

    private enum Mode {
        None,
        IncBus,
        TrackBus,
        NearStops
    }

    private void AddBackStack() {
        backStack.add(new BackStack(CurrentPlace, CurrentStop, CurrentBusTrack, busLine, null, IsBackButtonHalfExpanded, SelectedPlace, IncomBusMode));
        EnableBack();
    }

    public void ShowSchedule(int StopId, String LineNum, String Direction, String Date, boolean PreSelected) {
        Intent scheduleIntent = new Intent(this, ScheduleActivity.class);
        scheduleIntent.putExtra("StopId", StopId);
        scheduleIntent.putExtra("LineNum", LineNum);
        scheduleIntent.putExtra("Direction", Direction);
        scheduleIntent.putExtra("Date", Date);
        scheduleIntent.putExtra("PreSelected", PreSelected);
        scheduleResultLaunch.launch(scheduleIntent);
    }

    ActivityResultLauncher<Intent> scheduleResultLaunch = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == Activity.RESULT_OK) {
                    if (result.getData() != null) {
                        TrackBus(result.getData().getExtras().getInt("ScheduleId"), result.getData().getExtras().getString("ScheduleDate"));
                        backStack.add(new BackStack(null, null, null, null, new ScheduleBackStack(result.getData().getExtras().getString("LineNum"), result.getData().getExtras().getString("Direction"), result.getData().getExtras().getString("ScheduleDate"), result.getData().getExtras().getInt("StopId"), result.getData().getExtras().getBoolean("PreSelected")), false, null, null));
                    }
                }
            });

    @SuppressLint("NonConstantResourceId")
    private void SetupSearchView() {
        searchView = findViewById(R.id.search_view);
        SearchBar searchBar = findViewById(R.id.search_bar);

        Activity activity = this;
        searchBar.setOnMenuItemClickListener(item -> {
            switch (item.getItemId()) {
                case R.id.DisableAdButton:
                    Intent updateIntent = new Intent(activity, UpdateAndOnboarding.class);
                    updateIntent.putExtra("AdSettingScreen", true);
                    startActivity(updateIntent);
                    return true;
                case R.id.AboutUsButton:
                    ShowAbout();
                    return true;
                default:
                    return false;
            }
        });

        searchView.setupWithSearchBar(searchBar);

        searchView.addTransitionListener(
                (searchView, previousState, newState) -> {
                    if (newState == SearchView.TransitionState.SHOWING) {
                        List<SearchResult> AllItemList = new ArrayList<>();

                        for (BusPlaces busPlace : busPlaces.values()) {
                            AllItemList.add(new SearchResult(SearchResult.SearchType.Stop, busPlace.getName(), busPlace));
                        }

                        DatabaseManager Dm = new DatabaseManager(this);

                        BusNum[] busNums = Dm.GetActiveBusLines();

                        for (BusNum busNum : busNums) {
                            AllItemList.add(new SearchResult(SearchResult.SearchType.Line, busNum.getLineName(), busNum));
                        }

                        SearchResult[] AllItem = new SearchResult[AllItemList.size()];

                        AllItemList.toArray(AllItem);

                        Svf = SearchViewFragment.newInstance(AllItem);

                        getSupportFragmentManager().beginTransaction()
                                .replace(R.id.SearchViewFragmentContainer, Svf)
                                .commit();
                    }
                });

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (Svf != null) {
                    Svf.OnSearchTextChanged(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    public void OnSearchResultClick(SearchResult searchResult) {
        searchView.hide();

        if (searchResult.getType() == SearchResult.SearchType.Line) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            Date date = new Date();
            ShowSchedule(-1, ((BusNum) searchResult.getData()).getLineName(), "O", formatter.format(date), true);
        } else if (searchResult.getType() == SearchResult.SearchType.FavStop) {
            SelectStop((int) searchResult.getData(), true);
        } else if (searchResult.getType() == SearchResult.SearchType.Stop) {
            SelectPlace(((BusPlaces) searchResult.getData()).getId());
        }
    }

    private void OnMapLongClickListener(LatLng latLng) {
        AddBackStack();

        if (CurrentBusTrack != -1) {
            CurrentBusTrack = -1;
            busLine = null;
        }

        SelectedPlace = latLng;

        MarkerRenderer();

        ZoomTo(latLng);

        ShowBottomSheetNearStops();
    }

    private void ShowBottomSheetNearStops() {
        try {
            BottomSheetSetNormalParams(65);

            BottomSheetNearStops NearStopFragment = BottomSheetNearStops.newInstance(SelectedPlace.latitude, SelectedPlace.longitude, busStops, busPlaces);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainerView2, NearStopFragment)
                    .commit();
        } catch (Exception ignored) {

        }
    }

    public String getAddressFromLatLng(LatLng latLng) {
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses != null && addresses.size() > 0) {
                return addresses.get(0).getAddressLine(0).split(",")[1];
            } else {
                return latLng.latitude + ", " + latLng.longitude;
            }
        } catch (Exception e) {
            return latLng.latitude + ", " + latLng.longitude;
        }
    }

    private void SelectPosUserPos() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }

        fusedLocationClient.getLastLocation()
                .addOnSuccessListener(this, location -> {
                    if (location != null) {
                        OnMapLongClickListener(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                });
    }

    public void SetUserTouchedMap(boolean value){
        UserTouchedMap = value;
    }

    public boolean isUserTouchedMap() {
        return UserTouchedMap;
    }

    public void IncBusSelectedDate(IncomBusBackStack Data) {
        AddBackStack();

        IncomBusMode = Data;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (OnStartFragmentError) {
            ShowBottomSheetIncomingBuses();
            OnStartFragmentError = false;
        }
    }

    public void CollapseBottomSheet() {
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HALF_EXPANDED);
    }

    private void ShowAbout() {
        MaterialAlertDialogBuilder welcomeAlert  = new MaterialAlertDialogBuilder(this);
        welcomeAlert.setMessage(R.string.AboutText);
        welcomeAlert.setTitle(R.string.AboutUsMenu);
        welcomeAlert.setPositiveButton(R.string.Ok,null);
        welcomeAlert.setCancelable(false);
        welcomeAlert.create().show();
    }
}