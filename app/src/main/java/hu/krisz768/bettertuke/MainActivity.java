package hu.krisz768.bettertuke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.CurrentLocationRequest;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MapStyleOptions;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnSuccessListener;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;

public class MainActivity extends AppCompatActivity {

    FusedLocationProviderClient fusedLocationClient;

    private Integer CurrentPlace = -1;
    private Integer CurrentStop = -1;
    GoogleMap googleMap;

    BusStops[] busStops;
    BusPlaces[] busPlaces;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        busStops = BusStops.GetAllStops(this);
        busPlaces = BusPlaces.getAllBusPlaces(this);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);

        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull GoogleMap googleMap_) {
                googleMap = googleMap_;

                googleMap.getUiSettings().setZoomControlsEnabled(false);
                //googleMap.getUiSettings().setMyLocationButtonEnabled(false);
                MarkBusStops();
                GetClosestStop();
                if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    ActivityCompat.requestPermissions(MainActivity.this,
                            new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);

                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                }
                googleMap.setMyLocationEnabled(true);

            }
        });


    }

    private void MarkBusStops() {
        googleMap.getUiSettings().setZoomControlsEnabled(true);
        for (int i = 0; i < busPlaces.length; i++) {
            googleMap.addMarker(new MarkerOptions().position(new LatLng(busPlaces[i].getGpsY(), busPlaces[i].getGpsX())));
        }

        MapStyleOptions style = MapStyleOptions.loadRawResourceStyle(this, R.raw.mapstyle_night);
        googleMap.setMapStyle(style);
    }

    private void GetClosestStop() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        try {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return;
            }

            fusedLocationClient.getLastLocation().addOnSuccessListener(this, new OnSuccessListener<Location>() {
                @Override
                public void onSuccess(Location location) {
                    // Got last known location. In some rare situations this can be null.
                    if (location != null) {
                        GetClosestStopFromList(location);
                    } else {
                        //TODO
                    }
                }
            });
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

        googleMap.clear();

        googleMap.addMarker(new MarkerOptions().position(new LatLng(busStops[Closest].getGpsY(), busStops[Closest].getGpsX())));

        LatLngBounds Bounds = new LatLngBounds(
                new LatLng(location.getLatitude() > busStops[Closest].getGpsY() ? busStops[Closest].getGpsY() : location.getLatitude(), location.getLongitude() > busStops[Closest].getGpsX() ? busStops[Closest].getGpsX() : location.getLongitude()),
                new LatLng(location.getLatitude() > busStops[Closest].getGpsY() ? location.getLatitude() : busStops[Closest].getGpsY(), location.getLongitude() > busStops[Closest].getGpsX() ? location.getLongitude() : busStops[Closest].getGpsX())
        );

        googleMap.moveCamera(CameraUpdateFactory.newLatLngBounds(Bounds, 0));
        googleMap.moveCamera(CameraUpdateFactory.zoomTo(googleMap.getCameraPosition().zoom - 2.0f));
    }
}