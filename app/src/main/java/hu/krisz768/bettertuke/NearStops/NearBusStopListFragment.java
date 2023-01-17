package hu.krisz768.bettertuke.NearStops;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NearBusStopListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NearBusStopListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String BUSPLACES = "BusPlaces";
    private static final String FAVCOUNT = "FavCount";

    private BusPlaces[] mBusPlaces;
    private int mFavCount;

    public NearBusStopListFragment() {
        // Required empty public constructor
    }

    public static NearBusStopListFragment newInstance(BusPlaces[] BusPlaces, int FavCount) {
        NearBusStopListFragment fragment = new NearBusStopListFragment();
        Bundle args = new Bundle();
        args.putSerializable(BUSPLACES, BusPlaces);
        args.putInt(FAVCOUNT, FavCount);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mBusPlaces = (BusPlaces[]) getArguments().getSerializable(BUSPLACES);
            mFavCount = getArguments().getInt(FAVCOUNT);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_near_bus_stop_list, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.NearBusStopRecView);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        recyclerView.setLayoutManager(mLayoutManager);



        NearBusStopListAdapter NearBusStopList = new NearBusStopListAdapter(mBusPlaces, this, mFavCount);
        recyclerView.setAdapter(NearBusStopList);


        return view;
    }

    public void OnStopClick(int PlaceId) {
        if (getActivity() != null) {
            ((MainActivity)getActivity()).SelectPlace(PlaceId);
        }
    }
}