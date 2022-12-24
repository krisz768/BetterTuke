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
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
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
        MarkBusStops();
        ShowBottomSheet();
    }

    private void SetupGoogleMap() {
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap_) {
                googleMap = googleMap_;

                googleMap.getUiSettings().setZoomControlsEnabled(false);
                googleMap.getUiSettings().setMyLocationButtonEnabled(false);

                MapStyleOptions style = new MapStyleOptions(GetMapTheme());

                googleMap.setMapStyle(style);

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {
                        MarkerClickListener(marker);
                        return true;
                    }
                });

                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    MarkBusStops();
                } else {
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
                    LatLng Pecs = new LatLng(46.0707, 18.2331);
                    googleMap.moveCamera(CameraUpdateFactory.newLatLng(Pecs));
                    googleMap.moveCamera(CameraUpdateFactory.zoomTo(13));
                }
            }
        }

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

        ShowBottomSheet();
        MarkBusStops();
    }

    private void MarkBusStops() {
        googleMap.clear();

        BitmapDescriptor StopSelected = BitmapFromVector(R.drawable.bus_stop_svgrepo_com__1___1_, true);
        BitmapDescriptor StopNotSelected = BitmapFromVector(R.drawable.bus_stop_svgrepo_com__1___1_, false);
        BitmapDescriptor Place = BitmapFromVector(R.drawable.bus_stop_pointer_svgrepo_com, false);

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

    private BitmapDescriptor BitmapFromVector(int vectorResId, boolean primary) {
        Drawable vectorDrawable = ContextCompat.getDrawable(this, vectorResId);

        vectorDrawable.setBounds(0, 0, vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight());

        TypedValue typedValue = new TypedValue();
        if (primary) {
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        } else {
            getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        }

        int color = ContextCompat.getColor(this, typedValue.resourceId);
        vectorDrawable.setTint(color);

        Bitmap bitmap = Bitmap.createBitmap(vectorDrawable.getIntrinsicWidth(), vectorDrawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        Canvas canvas = new Canvas(bitmap);

        vectorDrawable.draw(canvas);

        return BitmapDescriptorFactory.fromBitmap(bitmap);
    }

    private void GetClosestStop() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                return;
            }

            //CurrentLocationRequest asd = new CurrentLocationRequest.Builder().

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
                    GetClosestStopFromList(location);
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

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(Bounds, 0));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(googleMap.getCameraPosition().zoom - 2.0f));
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

    private String GetMapTheme() {
        TypedValue typedValue = new TypedValue();
        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        String PrimaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        String OnPrimaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
        String PrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimaryContainer, typedValue, true);
        //String OnPrimaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));





        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        String SecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        String OnSecondaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true);
        String SecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true);
        //String OnSecondaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));




        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiary, typedValue, true);
        //String TertiaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnTertiary, typedValue, true);
        //String OnTertiaryColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorTertiaryContainer, typedValue, true);
        String TertiaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnTertiaryContainer, typedValue, true);
        //String OnTertiaryContainerColor = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.backgroundColor, typedValue, true);
        //String Background = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnBackground, typedValue, true);
        //String OnBackground = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurface, typedValue, true);
        String Surface = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        //getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSurface, typedValue, true);
        //String OnSurface = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        getTheme().resolveAttribute(com.google.android.material.R.attr.colorSurfaceInverse, typedValue, true);
        String OnSurfaceVariant = String.format("#%06X", (0xFFFFFF & ContextCompat.getColor(this, typedValue.resourceId)));

        int TextStroke = 0;

        int nightModeFlags =
                getResources().getConfiguration().uiMode &
                        Configuration.UI_MODE_NIGHT_MASK;
        switch (nightModeFlags) {
            case Configuration.UI_MODE_NIGHT_YES:
                TextStroke = -100;
                break;

            case Configuration.UI_MODE_NIGHT_NO:
                TextStroke = 0;
                break;

            case Configuration.UI_MODE_NIGHT_UNDEFINED:
                TextStroke = 0;
                break;
        }


        String JSON = "[\n" +
                "  {\n" +
                "    \"featureType\": \"all\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + Surface  + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"all\",\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"lightness\": " + TextStroke + "\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"administrative\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"administrative.locality\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + OnSurfaceVariant + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi.park\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + TertiaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"poi.park\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#6b9a76\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.arterial\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.arterial\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + OnSecondaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + OnPrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.highway\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.local\",\n" +
                "    \"elementType\": \"geometry.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + SecondaryContainerColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"road.local\",\n" +
                "    \"elementType\": \"geometry.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"" + PrimaryColor + "\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"transit\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#2f3948\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"transit.station\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#d59563\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"geometry\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#17263c\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"labels.text.fill\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"color\": \"#515c6d\"\n" +
                "      }\n" +
                "    ]\n" +
                "  },\n" +
                "  {\n" +
                "    \"featureType\": \"water\",\n" +
                "    \"elementType\": \"labels.text.stroke\",\n" +
                "    \"stylers\": [\n" +
                "      {\n" +
                "        \"lightness\": -20\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "]";


        return JSON;
    }
}