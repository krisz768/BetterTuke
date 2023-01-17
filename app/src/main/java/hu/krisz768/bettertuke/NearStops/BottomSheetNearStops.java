package hu.krisz768.bettertuke.NearStops;

import android.location.Location;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.SearchFragment.SearchAdapter;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomSheetNearStops#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomSheetNearStops extends Fragment {

    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String STOPS = "Stops";
    private static final String PLACES = "Places";

    private double mLatitude;
    private double mLongitude;
    private BusStops[] mStops;
    private BusPlaces[] mPlaces;

    public BottomSheetNearStops() {
        // Required empty public constructor
    }

    public static BottomSheetNearStops newInstance(double Latitude, double Longitude, BusStops[] Stops, BusPlaces[] Places) {
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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStops = (BusStops[]) getArguments().getSerializable(STOPS);
            mPlaces = (BusPlaces[]) getArguments().getSerializable(PLACES);
            mLatitude = getArguments().getDouble(LATITUDE);
            mLongitude = getArguments().getDouble(LONGITUDE);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view =  inflater.inflate(R.layout.fragment_bottom_sheet_near_stops, container, false);

        TextView NearStopLocationText = view.findViewById(R.id.NearStopLocationText);

        NearStopLocationText.setText(mLatitude + ", " + mLongitude);

        ImageView NearStopDirButton = view.findViewById(R.id.NearStopDirButton);
        NearStopDirButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.Navigation));

        GetStreetName(view);

        GetNearestPlaces(view);

        return view;
    }

    private void GetStreetName(View view) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (getActivity() != null) {
                    String Name = ((MainActivity)getActivity()).getAddressFromLatLng(new LatLng(mLatitude, mLongitude));

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView NearStopLocationText = view.findViewById(R.id.NearStopLocationText);
                            NearStopLocationText.setText(Name);
                        }
                    });
                }
            }
        }).start();
    }

    private void GetNearestPlaces(View view) {
        BottomSheetNearStops Callback = this;

        new Thread(new Runnable() {
            @Override
            public void run() {
                Location location = new Location("");
                location.setLongitude(mLongitude);
                location.setLatitude(mLatitude);

                List<BusPlaces> NearBusPlacesList = new ArrayList<>();

                for (int i = 0; i < mPlaces.length; i++) {
                    Location StopLocation = new Location("");
                    StopLocation.setLatitude(mPlaces[i].getGpsY());
                    StopLocation.setLongitude(mPlaces[i].getGpsX());

                    if (location.distanceTo(StopLocation) < 500) {
                        NearBusPlacesList.add(mPlaces[i]);
                    }
                }

                UserDatabase userDatabase = new UserDatabase(getContext());

                List<BusPlaces> FavNearBusPlacesList = new ArrayList<>();

                for (int i = 0; i < NearBusPlacesList.size(); i++) {
                    Log.e("b", NearBusPlacesList.get(i).getName());
                    for (int j = 0; j < mStops.length; j++){
                        if (NearBusPlacesList.get(i).getId() == mStops[j].getFoldhely()) {
                            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStops[j].getId()))) {
                                FavNearBusPlacesList.add(NearBusPlacesList.get(i));
                                NearBusPlacesList.remove(i);
                                i--;
                                break;
                            }
                        }
                    }
                }

                for (int i = 0; i < FavNearBusPlacesList.size(); i++) {
                    //
                }

                Collections.sort(NearBusPlacesList, new Comparator<BusPlaces>() {
                    @Override
                    public int compare(BusPlaces busPlaces, BusPlaces t1) {
                        Location StopLocation1 = new Location("");
                        StopLocation1.setLatitude(busPlaces.getGpsY());
                        StopLocation1.setLongitude(busPlaces.getGpsX());

                        Location StopLocation2 = new Location("");
                        StopLocation2.setLatitude(t1.getGpsY());
                        StopLocation2.setLongitude(t1.getGpsX());

                        return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
                    }
                });

                Collections.sort(FavNearBusPlacesList, new Comparator<BusPlaces>() {
                    @Override
                    public int compare(BusPlaces busPlaces, BusPlaces t1) {
                        Location StopLocation1 = new Location("");
                        StopLocation1.setLatitude(busPlaces.getGpsY());
                        StopLocation1.setLongitude(busPlaces.getGpsX());

                        Location StopLocation2 = new Location("");
                        StopLocation2.setLatitude(t1.getGpsY());
                        StopLocation2.setLongitude(t1.getGpsX());

                        return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
                    }
                });

                NearBusPlacesList.addAll(0, FavNearBusPlacesList);

                BusPlaces[] busPlaces = new BusPlaces[NearBusPlacesList.size()];
                NearBusPlacesList.toArray(busPlaces);

                int FavNum = FavNearBusPlacesList.size();

                if (getActivity() != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            NearBusStopListFragment NearStopFragment = NearBusStopListFragment.newInstance(busPlaces, FavNum);
                            getChildFragmentManager().beginTransaction()
                                    .replace(R.id.NearStopListFragment, NearStopFragment)
                                    .commit();
                        }
                    });
                }

            }
        }).start();
    }
}