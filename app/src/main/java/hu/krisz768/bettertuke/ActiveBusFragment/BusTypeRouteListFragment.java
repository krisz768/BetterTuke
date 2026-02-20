package hu.krisz768.bettertuke.ActiveBusFragment;

import android.app.Activity;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.krisz768.bettertuke.ActiveBusActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;

public class BusTypeRouteListFragment extends Fragment {

    private static final String ARG_DATA = "DATA";

    private ActiveBusTypeRespModel mData;

    public BusTypeRouteListFragment() {

    }


    public static BusTypeRouteListFragment newInstance(ActiveBusTypeRespModel Data) {
        BusTypeRouteListFragment fragment = new BusTypeRouteListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATA, Data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = (ActiveBusTypeRespModel)getArguments().getSerializable(ARG_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bus_type_list, container, false);

        TextView Title = view.findViewById(R.id.BusTypeListTitle);

        Title.setText(mData.getBusTypeName());

        RecyclerView Recv = view.findViewById(R.id.BusTypeListRec);

        BusTypeRouteListAdapter Btrla = new BusTypeRouteListAdapter(mData, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Btrla);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefreshBusType);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    Activity activity = getActivity();
                    if (activity != null) {
                        ((ActiveBusActivity)activity).OnReloadInitOnType();
                    }
                }
        );

        return view;
    }

    public void OnBusClick(String TripID) {
        Activity activity = getActivity();
        if (activity != null) {
            ((ActiveBusActivity)activity).OnBusClick(TripID);
        }
    }
}