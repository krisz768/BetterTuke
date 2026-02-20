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
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.NewApiInterface.GTFSRProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.models.BusPositionRespModel;

public class BottomSheetNearStops extends Fragment {
    private static final String LATITUDE = "Latitude";
    private static final String LONGITUDE = "Longitude";
    private static final String STOPS = "Stops";
    private static final String PLACES = "Places";
    private double mLatitude;
    private double mLongitude;
    private HashMap<String, BusStops> mStops;
    private HashMap<Integer, BusPlaces> mPlaces;
    private ScheduledExecutorService UpdateLoop;
    private BusPositionRespModel[] BusList = null;
    private int BusFavCount;
    private  BusPlaces[] busPlacesList = null;
    private int FavPlaceCount;
    private boolean IsDataDisplayed = false;


    public BottomSheetNearStops() {

    }

    public static BottomSheetNearStops newInstance(double Latitude, double Longitude, HashMap<String, BusStops> Stops, HashMap<Integer, BusPlaces> Places) {
        BottomSheetNearStops fragment = new BottomSheetNearStops();
        Bundle args = new Bundle();

        args.putDouble(LATITUDE, Latitude);
        args.putDouble(LONGITUDE, Longitude);
        args.putSerializable(STOPS, Stops);
        args.putSerializable(PLACES, Places);
        fragment.setArguments(args);
        return fragment;
    }

    private void StartNewUpdateThread() {
        if (UpdateLoop != null) {
            if (!UpdateLoop.isShutdown()){
                return;
            }
        }

        GTFSRProvider GTFSRProvider_ = new GTFSRProvider(this.getActivity());

        UpdateLoop = Executors.newScheduledThreadPool(1);
        UpdateLoop.scheduleWithFixedDelay(() -> GetRealTimeData(GTFSRProvider_), 0, 10, TimeUnit.SECONDS);
    }

    private void GetRealTimeData(GTFSRProvider GTFSRProvider_) {
        BusPositionRespModel[] BusPositions = GTFSRProvider_.getALLBusLocation();
        MainActivity mainActivity = (MainActivity)getActivity();

        if (BusPositions != null) {
            if (mainActivity != null) {
                mainActivity.runOnUiThread(() -> mainActivity.BusPositionMarkers(BusPositions));
            }

            Location location = new Location("");
            location.setLongitude(mLongitude);
            location.setLatitude(mLatitude);

            List<BusPositionRespModel> NearBusList = new ArrayList<>();

            for (BusPositionRespModel mBus : BusPositions) {
                Location BusLocation = new Location("");
                BusLocation.setLatitude(mBus.getGpsLatitude());
                BusLocation.setLongitude(mBus.getGpsLongitude());

                if (location.distanceTo(BusLocation) < 500) {
                    NearBusList.add(mBus);
                }
            }

            List<BusPositionRespModel> FavBusList = new ArrayList<>();

            if (getContext() != null){
                UserDatabase userDatabase = new UserDatabase(getContext());

                for (int i = 0; i < NearBusList.size(); i++) {
                    if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Line, NearBusList.get(i).getLineNum())) {
                        FavBusList.add(NearBusList.get(i));
                        NearBusList.remove(i);
                        i--;
                    }
                }
            }

            NearBusList.sort((busPlaces, t1) -> {
                Location StopLocation1 = new Location("");
                StopLocation1.setLatitude(busPlaces.getGpsLatitude());
                StopLocation1.setLongitude(busPlaces.getGpsLongitude());

                Location StopLocation2 = new Location("");
                StopLocation2.setLatitude(t1.getGpsLatitude());
                StopLocation2.setLongitude(t1.getGpsLongitude());

                return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
            });

            FavBusList.sort((busPlaces, t1) -> {
                Location StopLocation1 = new Location("");
                StopLocation1.setLatitude(busPlaces.getGpsLatitude());
                StopLocation1.setLongitude(busPlaces.getGpsLongitude());

                Location StopLocation2 = new Location("");
                StopLocation2.setLatitude(t1.getGpsLatitude());
                StopLocation2.setLongitude(t1.getGpsLongitude());

                return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
            });

            NearBusList.addAll(0, FavBusList);

            BusList = new BusPositionRespModel[NearBusList.size()];
            NearBusList.toArray(BusList);

            BusFavCount = FavBusList.size();

        } else {
            if (mainActivity != null) {
                mainActivity.runOnUiThread(() -> mainActivity.BusPositionMarkers(new BusPositionRespModel[0]));
            }

            BusList = new BusPositionRespModel[0];
            BusFavCount = 0;
        }

        DisplayResult();
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStops = (HashMap<String, BusStops>) getArguments().getSerializable(STOPS);
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

        StartNewUpdateThread();

        return view;
    }

    @Override
    public void onStop() {
        super.onStop();

        UpdateLoop.shutdown();
    }

    @Override
    public void onStart() {
        super.onStart();

        StartNewUpdateThread();
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
                            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, mStop.getId())) {
                                FavNearBusPlacesList.add(NearBusPlacesList.get(i));
                                NearBusPlacesList.remove(i);
                                i--;
                                break;
                            }
                        }
                    }
                }
            }

            NearBusPlacesList.sort((busPlaces, t1) -> {
                Location StopLocation1 = new Location("");
                StopLocation1.setLatitude(busPlaces.getGpsLatitude());
                StopLocation1.setLongitude(busPlaces.getGpsLongitude());

                Location StopLocation2 = new Location("");
                StopLocation2.setLatitude(t1.getGpsLatitude());
                StopLocation2.setLongitude(t1.getGpsLongitude());

                return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
            });

            FavNearBusPlacesList.sort((busPlaces, t1) -> {
                Location StopLocation1 = new Location("");
                StopLocation1.setLatitude(busPlaces.getGpsLatitude());
                StopLocation1.setLongitude(busPlaces.getGpsLongitude());

                Location StopLocation2 = new Location("");
                StopLocation2.setLatitude(t1.getGpsLatitude());
                StopLocation2.setLongitude(t1.getGpsLongitude());

                return Math.round(location.distanceTo(StopLocation1) - location.distanceTo(StopLocation2));
            });

            NearBusPlacesList.addAll(0, FavNearBusPlacesList);

            busPlacesList = new BusPlaces[NearBusPlacesList.size()];
            NearBusPlacesList.toArray(busPlacesList);

            FavPlaceCount = FavNearBusPlacesList.size();

            DisplayResult();

        }).start();
    }

    private void DisplayResult() {
        if (BusList == null || busPlacesList == null && !IsDataDisplayed) {
            return;
        }

        IsDataDisplayed = true;

        if (getActivity() != null) {
            getActivity().runOnUiThread(() -> {
                try {
                    NearBusStopListFragment NearStopFragment = NearBusStopListFragment.newInstance(busPlacesList, FavPlaceCount, BusList, BusFavCount);
                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.NearStopListFragment, NearStopFragment)
                            .commit();
                } catch (Exception ignored) {

                }
            });
        }
    }
}