package hu.krisz768.bettertuke.SwitchFragment;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.IncomingBusFragment.BottomSheetIncomingBusFragment;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusStopSelectorAdapter;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;

public class Switchfragment extends Fragment {
    private static final String ARG_PARAM1 = "TripID";
    private static final String ARG_PARAM2 = "StopID";
    private static final String ARG_PARAM3 = "Date";

    private String TripID;
    private String StopID;
    private String Date;

    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<String, BusStops> mStopList;

    private BusStops[] SelectedPlaceStopsArray;
    private SwitchBusStopSelectorAdapter Sbssa;

    public Switchfragment() {

    }

    public static Switchfragment newInstance(String TripID, String StopID, String Date) {
        Switchfragment fragment = new Switchfragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, TripID);
        args.putString(ARG_PARAM2, StopID);
        args.putString(ARG_PARAM3, Date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TripID = getArguments().getString(ARG_PARAM1);
            StopID = getArguments().getString(ARG_PARAM2);
            Date = getArguments().getString(ARG_PARAM3);
        }

        mPlaceList = MainActivity.busPlaces;
        mStopList = MainActivity.busStops;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_switchfragment, container, false);

        TextView BusStopName = view.findViewById(R.id.SwichBusStopName);

        List<BusStops> SelectedPlaceStops = new ArrayList<>();

        Integer PlaceId = null;
        for (BusStops busStops : mStopList.values()) {
            if (Objects.equals(busStops.getId(), StopID)) {
                PlaceId = busStops.getPlace();
            }
        }

        BusPlaces busPlace = mPlaceList.get(PlaceId);

        if (busPlace != null) {
            BusStopName.setText(busPlace.getName());

            for (BusStops busStops : mStopList.values()) {
                if (busStops.getPlace() == busPlace.getId()) {
                    SelectedPlaceStops.add(busStops);
                }
            }
        }

        SelectedPlaceStopsArray = new BusStops[SelectedPlaceStops.size()];
        SelectedPlaceStops.toArray(SelectedPlaceStopsArray);

        new Thread(this::SetupStopList).start();

        return view;
    }

    private void SetupStopList () {
        Context ctx = getContext();
        View view = getView();
        Activity activity = getActivity();

        Switchfragment switchfragment = this;

        if (ctx != null && view != null && activity != null) {
            String[] StopNames = new String[SelectedPlaceStopsArray.length];

            for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
                StopNames[i] = HelperProvider.GetStopDirectionString(ctx,SelectedPlaceStopsArray[i].getId());
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Sbssa = new SwitchBusStopSelectorAdapter(SelectedPlaceStopsArray,StopID, switchfragment, StopNames, ctx);

                    RecyclerView StopSelectorRec = view.findViewById(R.id.BusStopListRecView);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(ctx);
                    mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

                    StopSelectorRec.setLayoutManager(mLayoutManager);
                    StopSelectorRec.setAdapter(Sbssa);
                    StopSelectorRec.setItemAnimator(null);

                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(ctx) {
                        @Override
                        protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }

                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return super.calculateSpeedPerPixel(displayMetrics) *4;
                        }
                    };

                    int scrollPosition = 0;

                    if (StopID.equals("-1")) {
                        scrollPosition = SelectedPlaceStopsArray.length;
                    } else {
                        for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
                            if (SelectedPlaceStopsArray[i].getId().equals(StopID)) {
                                scrollPosition = i;
                                break;
                            }
                        }
                    }

                    smoothScroller.setTargetPosition(scrollPosition);
                    if (StopSelectorRec.getLayoutManager() != null) {
                        StopSelectorRec.getLayoutManager().startSmoothScroll(smoothScroller);
                    }

                    StopSelectorRec.setNestedScrollingEnabled(false);
                }
            });
        } else {
            try {
                Thread.sleep(1000);
                SetupStopList();
            } catch (Exception ignored) {

            }
        }
    }

    public void OnStopClick(String StopID) {

    }
}