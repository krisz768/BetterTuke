package hu.krisz768.bettertuke;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BlurMaskFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.Priority;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.CancellationToken;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.OnTokenCanceledListener;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.bottomsheet.BottomSheetDialogFragment;

import java.security.Permission;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.models.MarkerDescriptor;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;

    View BottomSheet;
    BottomSheetBehavior bottomSheetBehavior;
    private BottomSheetBehavior.BottomSheetCallback BottomSheetCallback;

    private Integer CurrentPlace = -1;
    private Integer CurrentStop = -1;
    GoogleMap googleMap;

    BusStops[] busStops;
    BusPlaces[] busPlaces;

    private boolean smallMarkerMode = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomSheet = findViewById(R.id.standard_bottom_sheet);
        bottomSheetBehavior = BottomSheetBehavior.from(BottomSheet);

        SetupBottomSheet();
        bottomSheetBehavior.setState(BottomSheetBehavior.STATE_HIDDEN);

        busStops = BusStops.GetAllStops(this);
        busPlaces = BusPlaces.getAllBusPlaces(this);

        SetupGoogleMap();
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
        CurrentStop = Id;
        ZoomToMarker();
        MarkBusStops();
    }

    private void SetupGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        final Context ctx = this;

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap_) {
                googleMap = googleMap_;

                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                MapStyleOptions style = new MapStyleOptions(HelperProvider.GetMapTheme(ctx));

                googleMap.setMapStyle(style);;

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        MarkerClickListener(marker);
                        return true;
                    }
                });

                googleMap.setOnCameraMoveListener(new GoogleMap.OnCameraMoveListener() {
                    @Override
                    public void onCameraMove() {
                        Log.e("ZOOM", googleMap.getCameraPosition().zoom + "");
                        final float ZoomLevel = googleMap.getCameraPosition().zoom;
                        if (ZoomLevel > 13.7) {
                            if (smallMarkerMode){
                                smallMarkerMode = false;
                                MarkBusStops();
                            }
                        } else {
                            if (!smallMarkerMode){
                                smallMarkerMode = true;
                                MarkBusStops();
                            }
                        }
                    }
                });

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    MarkBusStops();
                } else {
                    MarkBusStops();

                    googleMap.setMyLocationEnabled(true);
                    GetClosestStop();
                }
            }
        });
    }

    @SuppressLint("MissingPermission")
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i =0; i < permissions.length; i++) {
            Log.e("TESZT", permissions[i] + " " + grantResults[i]);
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
        LatLng Pecs = new LatLng(46.0707, 18.2331);
        googleMap.moveCamera(CameraUpdateFactory.newLatLng(Pecs));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(12));

        Toast.makeText(this, R.string.GPSHint, Toast.LENGTH_LONG).show();
    }

    private void MarkerClickListener(Marker marker) {
        MarkerDescriptor Md = (MarkerDescriptor)marker.getTag();
        if (Md.getType() == MarkerDescriptor.Types.Stop) {
            CurrentStop = Md.getId();
        } else {
            CurrentPlace = Md.getId();
            for (int i = 0; i < busStops.length; i++) {
                if (busStops[i].getFoldhely() == Md.getId()) {
                    CurrentStop = busStops[i].getId();
                    break;
                }
            }
        }

        ZoomToMarker();
        ShowBottomSheet();
        MarkBusStops();
    }

    private void ZoomToMarker() {
        if (CurrentStop == -1)
            return;

        for (int i = 0; i < busStops.length; i++) {
            if (busStops[i].getId() == CurrentStop) {
                CameraPosition cameraPosition = new CameraPosition.Builder()
                        .target(new LatLng(busStops[i].getGpsY(), busStops[i].getGpsX())).zoom(17.5F).build();

                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            }
        }
    }

    private void MarkBusStops() {
        googleMap.clear();

        BitmapDescriptor StopSelected = HelperProvider.BitmapFromVector(R.drawable.bus_stop_svgrepo_com__1___1_, true, this);
        BitmapDescriptor StopNotSelected = HelperProvider.BitmapFromVector(R.drawable.bus_stop_svgrepo_com__1___1_, false, this);
        BitmapDescriptor Place;
        if (smallMarkerMode) {
            Place = HelperProvider.BitmapFromVector(R.drawable.bus_marker_small, false, this);
        } else {
            Place = HelperProvider.BitmapFromVector(R.drawable.bus_stop_pointer_svgrepo_com, false, this);
        }


        for (int i = 0; i < busPlaces.length; i++) {
            if (busPlaces[i].getId() == CurrentPlace) {
                for (int j = 0; j < busStops.length; j++) {
                    if (busStops[j].getFoldhely() == CurrentPlace) {

                        if (busStops[j].getId() == CurrentStop) {
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busStops[j].getGpsY(), busStops[j].getGpsX())).icon(StopSelected));
                            marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Stop, busStops[j].getId()));
                        } else {
                            Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busStops[j].getGpsY(), busStops[j].getGpsX())).icon(StopNotSelected));
                            marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Stop, busStops[j].getId()));
                        }

                    }
                }
            } else  {
                Marker marker = googleMap.addMarker(new MarkerOptions().position(new LatLng(busPlaces[i].getGpsY(), busPlaces[i].getGpsX())).icon(Place));
                marker.setTag(new MarkerDescriptor(MarkerDescriptor.Types.Place, busPlaces[i].getId()));
            }
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

            googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

            fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, new CancellationToken() {
                @NonNull
                @Override
                public CancellationToken onCanceledRequested(@NonNull OnTokenCanceledListener onTokenCanceledListener) {
                    return null;
                }

                @Override
                public boolean isCancellationRequested() {
                    return false;
                }
            }).addOnSuccessListener(new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    if (location != null) {
                        GetClosestStopFromList(location);
                    } else {
                        GPSErr();
                    }

                }
            });
            /*fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        GetClosestStopFromList(location);
                    } else {
                        //TODO
                    }
                }
            });*/
        } catch (Exception e) {
            Log.e("Main", e.toString());
        }
    }

    private void GetClosestStopFromList(Location location) {
        int Closest = -1;

        double ClosestDistance = Double.MAX_VALUE;

        if(CurrentPlace != -1) {
            return;
        }

        for(int i = 0; i < busStops.length; i++) {
            double x = location.getLongitude() - busStops[i].getGpsX();
            double y = location.getLatitude() - busStops[i].getGpsY();

            double Distance = Math.sqrt((x*x)+(y*y));

            if (Distance < ClosestDistance){
                ClosestDistance = Distance;
                Closest = i;
            }
        }

        if (Closest != -1) {
            CurrentPlace = busStops[Closest].getFoldhely();
            CurrentStop = busStops[Closest].getId();

            ZoomClose(Closest, location);
        }

        MarkBusStops();
        ShowBottomSheet();
    }

    private void ZoomClose(int Closest, Location location) {
        LatLngBounds Bounds = new LatLngBounds(
                new LatLng(location.getLatitude() > busStops[Closest].getGpsY() ? busStops[Closest].getGpsY() : location.getLatitude(), location.getLongitude() > busStops[Closest].getGpsX() ? busStops[Closest].getGpsX() : location.getLongitude()),
                new LatLng(location.getLatitude() > busStops[Closest].getGpsY() ? location.getLatitude() : busStops[Closest].getGpsY(), location.getLongitude() > busStops[Closest].getGpsX() ? location.getLongitude() : busStops[Closest].getGpsX())
        );

        googleMap.animateCamera(CameraUpdateFactory.newLatLngBounds(Bounds, 500));
        //googleMap.animateCamera(CameraUpdateFactory.zoomTo(googleMap.getCameraPosition().zoom - 2.0f));
    }

    private void SetupBottomSheet() {

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int height = displayMetrics.heightPixels/3;

        final FragmentContainerView fragmentView = findViewById(R.id.fragmentContainerView2);

        bottomSheetBehavior.setPeekHeight(height);

        if (BottomSheetCallback == null) {
            BottomSheetCallback = new BottomSheetBehavior.BottomSheetCallback() {

                @Override
                public void onStateChanged(@NonNull View bottomSheet, int newState) {
                    ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
                    if (newState == BottomSheetBehavior.STATE_COLLAPSED) {
                        params.height = bottomSheetBehavior.getPeekHeight();
                    } else if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                        params.height = bottomSheet.getHeight();
                    }

                    fragmentView.setLayoutParams(params);

                }

                @Override
                public void onSlide(@NonNull View bottomSheet, float slideOffset) {

                    ViewGroup.LayoutParams params = fragmentView.getLayoutParams();
                    if (slideOffset > 0) {
                        params.height = Math.round(bottomSheetBehavior.getPeekHeight() + ((bottomSheet.getHeight() - bottomSheetBehavior.getPeekHeight())*slideOffset));
                    } else if (slideOffset < 0) {
                        params.height = bottomSheetBehavior.getPeekHeight();
                    }

                    fragmentView.setLayoutParams(params);
                }
            };
            bottomSheetBehavior.addBottomSheetCallback(BottomSheetCallback);
        }
    }

    private void ShowBottomSheet() {
        if (bottomSheetBehavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            bottomSheetBehavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        }

        FragmentContainerView fragmentView = findViewById(R.id.fragmentContainerView2);

        BottomSheetIncomingBusFragment InBusFragment = BottomSheetIncomingBusFragment.newInstance(CurrentPlace, CurrentStop, busPlaces, busStops);
                getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragmentContainerView2, InBusFragment)
                .commit();
    }
}