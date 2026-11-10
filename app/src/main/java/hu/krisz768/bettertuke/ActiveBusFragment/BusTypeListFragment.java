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
    // Bus type shown in the detail pane (two-pane mode), highlighted in the list
    private String mSelectedType;
    private boolean mScrollToSelectedType = false;

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
        ScrollToSelectedTypeIfRequested(Recv);

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

    public void UpdateData(ActiveBusTypeRespModel[] Data) {
        mData = Data;
        if (getArguments() != null) {
            getArguments().putSerializable(ARG_DATA, Data);
        }

        View view = getView();
        if (view != null) {
            RecyclerView Recv = view.findViewById(R.id.BusTypeListRec);
            Recv.setAdapter(new BusTypeListAdapter(mData, this));
        }

        StopRefreshing();
    }

    public void SetSelectedType(String Type) {
        if (Type == null ? mSelectedType == null : Type.equals(mSelectedType)) {
            return;
        }

        mSelectedType = Type;
        View view = getView();
        if (view != null) {
            RecyclerView Recv = view.findViewById(R.id.BusTypeListRec);
            if (Recv.getAdapter() != null) {
                Recv.getAdapter().notifyItemRangeChanged(0, Recv.getAdapter().getItemCount());
            }
        }
    }

    public String GetSelectedType() {
        return mSelectedType;
    }

    // Scrolls the list to the selected bus type (e.g. the screen was opened on a type)
    public void ScrollToSelectedType() {
        mScrollToSelectedType = true;
        View view = getView();
        if (view != null) {
            ScrollToSelectedTypeIfRequested(view.findViewById(R.id.BusTypeListRec));
        }
    }

    private void ScrollToSelectedTypeIfRequested(RecyclerView Recv) {
        if (!mScrollToSelectedType || mSelectedType == null || mData == null) {
            return;
        }
        mScrollToSelectedType = false;

        for (int i = 0; i < mData.length; i++) {
            if (mData[i].getBusTypeName().equals(mSelectedType)) {
                Recv.scrollToPosition(i);
                return;
            }
        }
    }

    public void StopRefreshing() {
        View view = getView();
        if (view != null) {
            ((SwipeRefreshLayout) view.findViewById(R.id.swiperefreshBusType)).setRefreshing(false);
        }
    }

    public void OnBusClick(String Type) {
        Activity activity = getActivity();
        if (activity != null) {
            ((ActiveBusActivity)activity).ShowRoutes(Type);
        }
    }
}