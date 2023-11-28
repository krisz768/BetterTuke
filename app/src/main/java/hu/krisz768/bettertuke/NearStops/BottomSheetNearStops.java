package hu.krisz768.bettertuke.NearStops;

import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class BottomSheetNearStops extends Fragment {
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String STOPS = "Stops";
    private static final String PLACES = "Places";
    private double mLatitude;
    private double mLongitude;
    private HashMap<Integer, BusStops> mStops;
    private HashMap<Integer, BusPlaces> mPlaces;

    public BottomSheetNearStops() {

    }

    public static BottomSheetNearStops newInstance(double Latitude, double Longitude, HashMap<Integer, BusStops> Stops, HashMap<Integer, BusPlaces> Places) {
        BottomSheetNearStops fragment = new BottomSheetNearStops();
        Bundle args = new Bundle();

        args.putDouble(LATITUDE, Latitude);
        args.putDouble(LONGITUDE, Longitude);
        args.putSerializable(STOPS, Stops);
        args.putSerializable(PLACES, Places);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStops = (HashMap<Integer, BusStops>) getArguments().getSerializable(STOPS);
            mPlaces = (HashMap<Integer, BusPlaces>) getArguments().getSerializable(PLACES);
            mLatitude = getArguments().getDouble(LATITUDE);
            mLongitude = getArguments().getDouble(LONGITUDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_bottom_sheet_near_stops, container, false);

        TextView NearStopLocationText = view.findViewById(R.id.NearStopLocationText);

        NearStopLocationText.setText(getString(R.string.NearStopLocationText, mLatitude, mLongitude));

        GetStreetName(view);

        GetNearestPlaces();

        return view;
    }

    private void GetStreetName(View view) {
        new Thread(() -> {
            MainActivity mainActivity = (MainActivity)getActivity();

            if (mainActivity != null) {
                String Name = mainActivity.getAddressFromLatLng(new LatLng(mLatitude, mLongitude));

                mainActivity.runOnUiThread(() -> {
                    TextView NearStopLocationText = view.findViewById(R.id.NearStopLocationText);
                    NearStopLocationText.setText(Name);
                });
            }
        }).start();
    }

    private void GetNearestPlaces() {
        new Thread(() -> {
            Location location = new Location("");
            location.setLongitude(mLongitude);
            location.setLatitude(mLatitude);

            List<BusPlaces> NearBusPlacesList = new ArrayList<>();

            for (BusPlaces mPlace : mPlaces.values()) {
                Location StopLocation = new Location("");
                StopLocation.setLatitude(mPlace.getGpsLatitude());
                StopLocation.setLongitude(mPlace.getGpsLongitude());

                if (location.distanceTo(StopLocation) < 500) {
                    NearBusPlacesList.add(mPlace);
                }
            }

            List<BusPlaces> FavNearBusPlacesList = new ArrayList<>();

            if (getContext() != null){
                UserDatabase userDatabase = new UserDatabase(getContext());

                for (int i = 0; i < NearBusPlacesList.size(); i++) {
                    for (BusStops mStop : mStops.values()) {
                        if (NearBusPlacesList.get(i).getId() == mStop.getPlace()) {
                            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop.getId()))) {
                                FavNearBusPlacesList.add(NearBusPlacesList.get(i));
                                NearBusPlacesList.remove(i);
                                i--;
                                break;
                            }
                        }
                    }
                }
            }

            Collections.sort(NearBusPlacesList, (busPlaces, t1) -> {
                Location StopLocation1 = new Location("");
                StopLocation1.setLatitude(busPlaces.getGpsLatitude());
                StopLocation1.setLongitude(busPlaces.getGpsLongitude());

                Location StopLocation2 = new Location("");
                StopLocation2.setLatitude(t1.getGpsLatitude());
                StopLocation2.setLongitude(t1.getGpsLongitude());

                return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
            });

            Collections.sort(FavNearBusPlacesList, (busPlaces, t1) -> {
                Location StopLocation1 = new Location("");
                StopLocation1.setLatitude(busPlaces.getGpsLatitude());
                StopLocation1.setLongitude(busPlaces.getGpsLongitude());

                Location StopLocation2 = new Location("");
                StopLocation2.setLatitude(t1.getGpsLatitude());
                StopLocation2.setLongitude(t1.getGpsLongitude());

                return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
            });

            NearBusPlacesList.addAll(0, FavNearBusPlacesList);

            BusPlaces[] busPlaces = new BusPlaces[NearBusPlacesList.size()];
            NearBusPlacesList.toArray(busPlaces);

            int FavNum = FavNearBusPlacesList.size();

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    try {
                        NearBusStopListFragment NearStopFragment = NearBusStopListFragment.newInstance(busPlaces, FavNum);
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.NearStopListFragment, NearStopFragment)
                                .commit();
                    } catch (Exception ignored) {

                    }
                });
            }

        }).start();
    }
}