package hu.krisz768.bettertuke;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.splashscreen.SplashScreen;
import androidx.fragment.app.FragmentContainerView;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
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
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

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
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.search.SearchBar;
import com.google.android.material.search.SearchView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
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
import hu.krisz768.bettertuke.models.MarkerDescriptor;
import hu.krisz768.bettertuke.models.ScheduleBackStack;
import hu.krisz768.bettertuke.models.SearchResult;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;

    View BottomSheet;
    BottomSheetBehavior<View> bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback BottomSheetCallback;

    private SearchView searchView;
    SearchViewFragment Svf;

    private Integer CurrentPlace = -1;
    private Integer CurrentStop = -1;
    private Integer CurrentBusTrack = -1;
    private BusLine busLine;
    private LatLng SelectedPlace;

    private GoogleMap googleMap;

    private BusStops[] busStops;
    private BusPlaces[] busPlaces;

    private final List<BackStack> backStack = new ArrayList<>();

    private boolean smallMarkerMode = false;

    private boolean IsBackButtonCollapse = true;

    private Marker BusMarker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Bundle b = getIntent().getExtras();

        boolean Error = false;

        if (b != null) {
            Error = b.getBoolean("ERROR");
        }

        if (Error) {
            AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
            dlgAlert.setMessage("Az alkalmazás első indításához internetkapcsolat szükséges.");
            dlgAlert.setTitle("Hiba");
            dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialogInterface, int i) {
                    finish();
                }
            });
            dlgAlert.setCancelable(false);
            dlgAlert.create().show();

            return;
        }

        setTheme();

        HelperProvider.RenderAllBitmap(this);

        setContentView(R.layout.activity_main);


        BottomSheet = findViewById(R.id.standard_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(BottomSheet);

        SetupBottomSheet();

        SetupSearchView();

        findViewById(R.id.ShowScheduleButton).setOnClickListener(view -> ShowSchedule(-1, null, null, null, false));

        findViewById(R.id.PosButton).setOnClickListener(view -> SelectPosUserPos());

        findViewById(R.id.PosButton).setVisibility(View.GONE);

        busStops = BusStops.GetAllStops(this);
        busPlaces = BusPlaces.getAllBusPlaces(this);

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
            params.height = bottomSheetBehavior.getPeekHeight();
        } else if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
            params.height = bottomSheetBehavior.getMaxHeight();
        }

        fragmentView.setLayoutParams(params);
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

            googleMap.getUiSettings().setZoomControlsEnabled(false);
            googleMap.getUiSettings().setMyLocationButtonEnabled(false);

            MapStyleOptions style = new MapStyleOptions(HelperProvider.GetMapTheme(ctx));
            googleMap.setMapStyle(style);


            googleMap.setOnMarkerClickListener(marker -> {
                MarkerClickListener(marker);
                return true;
            });

            googleMap.setOnCameraMoveListener(() -> {
                //Log.e("ZOOM", googleMap.getCameraPosition().zoom + "");
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

            if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(MainActivity.this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                MarkerRenderer();
            } else {
                MarkerRenderer();

                googleMap.setMyLocationEnabled(true);
                GetClosestStop();
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < permissions.length; i++) {
            //Log.e("TESZT", permissions[i] + " " + grantResults[i]);
            if (permissions[i].equals("android.permission.ACCESS_FINE_LOCATION")) {
                if (grantResults[i] != -1) {
                    googleMap.setMyLocationEnabled(true);
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

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
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
        for (BusStops busStop : busStops) {
            if (busStop.getId() == CurrentStop) {
                int FoldhelyId = busStop.getPlace();
                for (BusPlaces busPlace : busPlaces) {
                    if (busPlace.getId() == FoldhelyId) {
                        CurrentPlace = FoldhelyId;
                        break;
                    }
                }
                break;
            }
        }

        ZoomToMarker();
        ShowBottomSheetIncommingBuses();
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

        for (BusStops busStop : busStops) {
            if (busStop.getPlace() == CurrentPlace) {
                StopIds.add(busStop.getId());
            }
        }

        UserDatabase userDatabase = new UserDatabase(this);

        int Favid = Integer.MAX_VALUE;

        for (int i = 0; i < StopIds.size(); i++) {
            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, StopIds.get(i).toString())) {
                int tFavId = userDatabase.GetId(StopIds.get(i).toString(), UserDatabase.FavoriteType.Stop);
                if (Favid > tFavId) {
                    CurrentStop = StopIds.get(i);
                    Favid = tFavId;
                }
            }
        }

        if (Favid == Integer.MAX_VALUE) {
            CurrentStop = StopIds.get(0);
        }

        ZoomToMarker();
        ShowBottomSheetIncommingBuses();
        MarkerRenderer();
    }

    private void ZoomToMarker() {
        if (CurrentStop == -1)
            return;

        for (BusStops busStop : busStops) {
            if (busStop.getId() == CurrentStop) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude())).zoom(17.5F).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void MarkerRenderer() {
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

            for (BusPlaces busPlace : busPlaces) {
                if (busPlace.getId() == CurrentPlace) {
                    for (BusStops busStop : busStops) {
                        if (busStop.getPlace() == CurrentPlace) {

                            BitmapDescriptor icon;
                            if (busStop.getId() == CurrentStop && SelectedPlace == null) {
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
                for (BusStops busStop : busStops) {
                    if (busLine.getStops()[i].getStopId() == busStop.getId()) {
                        BitmapDescriptor icon;
                        if (CurrentStop == busStop.getId()) {
                            icon = StopSelected;
                        } else {
                            icon = StopNotSelected;
                        }

                        Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude())).icon(icon));
                        assert marker != null;
                        marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Stop, busStop.getId()));
                        break;
                    }
                }
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

            //CurrentLocationRequest asd = new CurrentLocationRequest.Builder().


            final LatLng Pecs = new LatLng(46.0707, 18.2331);

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(Pecs).zoom(12).build();

            //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

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
                    findViewById(R.id.PosButton).setVisibility(View.VISIBLE);
                    GetStartupStop(location);
                    //GetClosestStopFromList(location);
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

        for (BusStops busStop : busStops) {
            Location stopLocation = new Location("");
            stopLocation.setLongitude(busStop.getGpsLongitude());
            stopLocation.setLatitude(busStop.getGpsLatitude());

            float Distance = location.distanceTo(stopLocation);
            if (Distance < 17.5F) {
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
            for (BusStops busStop : busStops) {
                if (Integer.parseInt(favoriteStop.getData()) == busStop.getId()) {
                    Location stopLocation = new Location("");
                    stopLocation.setLongitude(busStop.getGpsLongitude());
                    stopLocation.setLatitude(busStop.getGpsLatitude());

                    float Distance = location.distanceTo(stopLocation);
                    if (Distance < 500 && Distance < Closest) {
                        Closest = Distance;
                        ClosestId = busStop.getId();
                        ClosestStop = busStop;
                    }

                    break;
                }
            }
        }

        if (ClosestId != -1) {
            SelectStop(ClosestId, false);
            ZoomClose(new LatLng(ClosestStop.getGpsLatitude(), ClosestStop.getGpsLongitude()), new LatLng(location.getLatitude(), location.getLongitude()));
        } else {
            for (BusStops busStop : busStops) {
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

            googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(Bounds, dp20 * 4));
        } catch (Exception e) {
            if (BusMarker != null) {
                ZoomTo(location2);
            } else {
                ZoomToMarker();
            }

        }
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(googleMap.getCameraPosition().zoom - 2.0f));
    }

    private void SetupBottomSheet() {

        final FragmentContainerView fragmentView = findViewById(R.id.fragmentContainerView2);
        final FloatingActionButton ScheduleButton = findViewById(R.id.ShowScheduleButton);

        ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
        params.height = bottomSheetBehavior.getPeekHeight();
        fragmentView.setLayoutParams(params);

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

                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        IsBackButtonCollapse = true;

                        params.height = bottomSheetBehavior.getPeekHeight();

                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            googleMap.setPadding(0, dp20 * 4, 0, 0);
                            params2.bottomMargin = dp20;
                        } else {
                            googleMap.setPadding(0, dp20 * 4, 0, params.height);
                            params2.bottomMargin = params.height + dp20;
                        }
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        params.height = bottomSheet.getHeight();
                    } else if (newState == BottomSheetBehavior.STATE_HIDDEN) {
                        googleMap.setPadding(0, dp20 * 4, 0, 0);
                        params2.bottomMargin = dp20;
                    }

                    fragmentView.setLayoutParams(params);
                    ScheduleButton.setLayoutParams(params2);
                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {
                    ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
                    ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

                    if (slideOffset > 0) {
                        params.height = Math.round(bottomSheetBehavior.getPeekHeight() + ((bottomSheet.getHeight() - bottomSheetBehavior.getPeekHeight()) * slideOffset));
                    } else if (slideOffset < 0) {
                        params.height = bottomSheetBehavior.getPeekHeight();
                        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                            googleMap.setPadding(0, dp20 * 4, 0, 0);
                            params2.bottomMargin = dp20;
                        } else {
                            googleMap.setPadding(0, dp20 * 4, 0, Math.round(bottomSheetBehavior.getPeekHeight() + ((bottomSheetBehavior.getPeekHeight()) * slideOffset)));
                            params2.bottomMargin = Math.round(bottomSheetBehavior.getPeekHeight() + ((bottomSheetBehavior.getPeekHeight()) * slideOffset)) + dp20;
                        }
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

            int MinHeight = (int) Math.ceil(180 * displayMetrics.density);
            if (MinHeight > height) {
                height = MinHeight;
            }

            bottomSheetBehavior.setPeekHeight(height);

            ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

            if (bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_HIDDEN) {
                if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
                    googleMap.setPadding(0, dp20 * 4, 0, 0);
                    params2.bottomMargin = dp20;
                } else {
                    googleMap.setPadding(0, dp20 * 4, 0, height);
                    params2.bottomMargin = height + dp20;
                }


                ScheduleButton.setLayoutParams(params2);
            }
        }
    }

    private void ShowBottomSheetIncommingBuses() {
        BottomSheetSetNormalParams();

        BottomSheetIncomingBusFragment InBusFragment = BottomSheetIncomingBusFragment.newInstance(CurrentPlace, CurrentStop, busPlaces, busStops);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, InBusFragment)
                .commit();
    }

    public void TrackBus(int Id, String Date) {
        AddBackStack();

        if (SelectedPlace != null) {
            SelectedPlace = null;
        }

        CurrentBusTrack = Id;
        busLine = BusLine.BusLinesByLineId(Id, this);
        if (Date != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            java.util.Date date = new Date();
            if (!Date.equals(formatter.format(date))) {
                busLine.setDate(Date);
            }
        }

        if (BusMarker != null) {
            BusMarker.remove();
            BusMarker = null;
        }

        MarkerRenderer();
        ShowBottomSheetTrackBus();
    }

    private void BottomSheetSetNormalParams() {
        IsBackButtonCollapse = bottomSheetBehavior.getState() != BottomSheetBehavior.STATE_EXPANDED;

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int dp20 = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                20,
                displayMetrics
        ));
        int height = displayMetrics.heightPixels / 3;

        int MinHeight = (int) Math.ceil(180 * displayMetrics.density);
        if (MinHeight > height) {
            height = MinHeight;
        }

        bottomSheetBehavior.setPeekHeight(height);

        bottomSheetBehavior.setMaxHeight(-1);
        bottomSheetBehavior.setHideable(true);

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        final View Fc = findViewById(R.id.fragmentContainerView2);
        final FloatingActionButton ScheduleButton = findViewById(R.id.ShowScheduleButton);

        ViewGroup.LayoutParams params = Fc.getLayoutParams();
        ConstraintLayout.LayoutParams params2 = (ConstraintLayout.LayoutParams) ScheduleButton.getLayoutParams();

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED) {
            params.height = height;
        } else {
            params.height = getWindow().getDecorView().getHeight();
        }

        if (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
            googleMap.setPadding(0, dp20 * 4, 0, 0);
            params2.bottomMargin = dp20;
        } else {
            googleMap.setPadding(0, dp20 * 4, 0, height);
            params2.bottomMargin = height + dp20;
        }
        Fc.setLayoutParams(params);
        ScheduleButton.setLayoutParams(params2);
    }

    private void ShowBottomSheetTrackBus() {
        BottomSheetSetNormalParams();

        BottomSheetTrackBusFragment TrackBusFragment = BottomSheetTrackBusFragment.newInstance(CurrentPlace, CurrentStop, busPlaces, busStops, busLine);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, TrackBusFragment)
                .commit();
    }

    public void BuspositionMarker(LatLng BusPosition) {
        if (busLine != null) {
            if (BusPosition != null) {
                BitmapDescriptor BusBitmap = BitmapDescriptorFactory.fromBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapBus));
                if (BusMarker == null) {
                    MarkerOptions BusMarkerOption = new MarkerOptions().position(new LatLng(BusPosition.latitude, BusPosition.longitude)).icon(BusBitmap);
                    CreateBusMarker(BusMarkerOption);

                    for (BusStops busStop : busStops) {
                        if (busStop.getId() == CurrentStop) {
                            ZoomClose(new LatLng(busStop.getGpsLatitude(), busStop.getGpsLongitude()), new LatLng(BusPosition.latitude, BusPosition.longitude));
                        }
                    }
                } else {
                    BusMarker.setPosition(BusPosition);
                }
            } else {
                if (BusMarker != null) {
                    BusMarker.remove();
                    BusMarker = null;
                }
            }
        } else {
            if (BusMarker != null) {
                BusMarker.remove();
                BusMarker = null;
            }
        }

    }

    public void ZoomTo(LatLng Position) {
        CameraPosition cameraPosition = new CameraPosition.Builder()
                .target(Position).zoom(15).build();

        googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
    }

    private void CreateBusMarker(MarkerOptions option) {
        BusMarker = googleMap.addMarker(option);
        assert BusMarker != null;
        BusMarker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Bus, -1));
        BusMarker.setZIndex(Float.MAX_VALUE);
    }

    public boolean IsBottomSheetCollapsed() {
        return bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_COLLAPSED;
    }

    @Override
    public void onBackPressed() {
        if (searchView.isShowing()) {
            searchView.hide();
            return;
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_EXPANDED && IsBackButtonCollapse) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
            return;
        }

        if (backStack.size() == 0) {
            finish();
        } else {
            RestorePrevState();
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

        backStack.remove(backStack.size() - 1);

        if (BusMarker != null) {
            BusMarker.remove();
            BusMarker = null;
        }

        switch (DetermineMode()) {
            case IncBus:
                ShowBottomSheetIncommingBuses();
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

        IsBackButtonCollapse = PrevState.isBackButtonCollapse();

        MarkerRenderer();
    }

    private Mode DetermineMode() {
        if (CurrentStop == -1) {
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
        backStack.add(new BackStack(CurrentPlace, CurrentStop, CurrentBusTrack, busLine, null, IsBackButtonCollapse, SelectedPlace));
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
                    }

                    backStack.add(new BackStack(null, null, null, null, new ScheduleBackStack(result.getData().getExtras().getString("LineNum"), result.getData().getExtras().getString("Direction"), result.getData().getExtras().getString("ScheduleDate"), result.getData().getExtras().getInt("StopId"), result.getData().getExtras().getBoolean("PreSelected")), false, null));
                }
            });

    private void SetupSearchView() {
        searchView = findViewById(R.id.search_view);
        SearchBar searchBar = findViewById(R.id.search_bar);

        searchView.setupWithSearchBar(searchBar);


        searchView.addTransitionListener(
                (searchView, previousState, newState) -> {
                    if (newState == SearchView.TransitionState.SHOWING) {
                        List<SearchResult> AllItemList = new ArrayList<>();

                        for (BusPlaces busPlace : busPlaces) {
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
                                .replace(R.id.SerchViewFragmentContainer, Svf)
                                .commit();
                    }
                });

        searchView.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                Svf.OnSearchTextChanged(charSequence.toString());
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
        BottomSheetSetNormalParams();

        BottomSheetNearStops NearStopFragment = BottomSheetNearStops.newInstance(SelectedPlace.latitude, SelectedPlace.longitude, busStops, busPlaces);
        getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, NearStopFragment)
                .commit();
    }

    public String getAddressFromLatLng(LatLng latLng) {
        Geocoder gcd = new Geocoder(getBaseContext(), Locale.getDefault());

        List<Address> addresses;
        try {
            addresses = gcd.getFromLocation(latLng.latitude, latLng.longitude, 1);

            if (addresses.size() > 0) {
                //return addresses.get(0).getThoroughfare() + " " + addresses.get(0).getSubThoroughfare();
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
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        OnMapLongClickListener(new LatLng(location.getLatitude(), location.getLongitude()));
                    }
                });
    }
}