package hu.krisz768.bettertuke.NearStops;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.BusPositionRespModel;

public class NearBusStopListFragment extends Fragment {
    private static final String BUSPLACES = "BusPlaces";
    private static final String FAVCOUNT = "FavCount";
    private static final String BUSLIST = "BusList";
    private static final String BUSFAVCOUNT = "BusFavCount";
    private BusPlaces[] mBusPlaces;
    private int mFavCount;
    private BusPositionRespModel[] mNearBuses;
    private int mFavBusCount;

    public NearBusStopListFragment() {

    }

    public static NearBusStopListFragment newInstance(BusPlaces[] BusPlaces, int FavCount, BusPositionRespModel[] mNearBuses, int mFavBusCount) {
        NearBusStopListFragment fragment = new NearBusStopListFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUSPLACES, BusPlaces);
        args.putInt(FAVCOUNT, FavCount);
        args.putSerializable(BUSLIST, mNearBuses);
        args.putInt(BUSFAVCOUNT, mFavBusCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {

            mBusPlaces = (BusPlaces[]) getArguments().getSerializable(BUSPLACES);
            mFavCount = getArguments().getInt(FAVCOUNT);
            mNearBuses = (BusPositionRespModel[]) getArguments().getSerializable(BUSLIST);
            mFavBusCount = getArguments().getInt(BUSFAVCOUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_near_bus_stop_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.NearBusStopRecView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);

        NearBusStopListAdapter NearBusStopList = new NearBusStopListAdapter(mBusPlaces, this, mFavCount, mNearBuses, mFavBusCount);
        recyclerView.setAdapter(NearBusStopList);

        return view;
    }

    public void OnStopClick(int PlaceId) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).SelectPlace(PlaceId);
        }
    }

    public void OnBusClick(String TripID) {
        if (getActivity() != null) {
            SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
            ((MainActivity)getActivity()).TrackBus(TripID, formatter.format(new Date()));
        }
    }
}