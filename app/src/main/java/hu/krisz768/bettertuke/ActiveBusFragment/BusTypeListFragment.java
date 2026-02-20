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

import hu.krisz768.bettertuke.ActiveBusActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;

public class BusTypeListFragment extends Fragment {
    private static final String ARG_DATA = "DATA";

    private ActiveBusTypeRespModel[] mData;

    public BusTypeListFragment() {

    }

    public static BusTypeListFragment newInstance(ActiveBusTypeRespModel[] Data) {
        BusTypeListFragment fragment = new BusTypeListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_DATA, Data);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mData = (ActiveBusTypeRespModel[])getArguments().getSerializable(ARG_DATA);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_bus_type_list, container, false);

        RecyclerView Recv = view.findViewById(R.id.BusTypeListRec);

        BusTypeListAdapter Btla = new BusTypeListAdapter(mData, this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Btla);

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefreshBusType);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    Activity activity = getActivity();
                    if (activity != null) {
                        ((ActiveBusActivity)activity).OnReloadInit();
                    }
                }
        );

        return view;
    }

    public void OnBusClick(String Type) {
        Activity activity = getActivity();
        if (activity != null) {
            ((ActiveBusActivity)activity).ShowRoutes(Type);
        }
    }
}