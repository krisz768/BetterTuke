package hu.krisz768.bettertuke.IncomingBusFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.IncommingBusRespModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link IncomingBusListFragment#newInstance} factory method to
 * create an instance of this fragment.
 *
 */
public class IncomingBusListFragment extends Fragment {

    private static final String ARG_PARAM1 = "List";

    private IncommingBusRespModel[] mList;

    private IncomingBusListAdapter Ibla;


    public static IncomingBusListFragment newInstance(IncommingBusRespModel[] List) {
        IncomingBusListFragment fragment = new IncomingBusListFragment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, List);
        fragment.setArguments(args);
        return fragment;
    }

    public IncomingBusListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = (IncommingBusRespModel[])getArguments().getSerializable(ARG_PARAM1);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_incoming_bus_list, container, false);

        RecyclerView Recv = view.findViewById(R.id.InBusListRecView);

        Ibla = new IncomingBusListAdapter(mList, getContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Ibla);

        return view;
    }

    public void UpdateList(IncommingBusRespModel[] List) {
        if (Ibla != null) {
            Ibla.UpdateList(List);
            Ibla.notifyDataSetChanged();
        }
    }

    public void OnBusClick(int Id) {
        ((MainActivity)getActivity()).TrackBus(Id);
    }
}