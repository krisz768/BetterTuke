package hu.krisz768.bettertuke.SwitchFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListAdapter;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListFragment;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.SwitchActivity;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link SwitchBusListFregment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class SwitchBusListFregment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "List";
    private static final String ARG_PARAM2 = "Date";

    private IncomingBusRespModel[] mList;
    private SwitchBusListAdapter Sbla;
    private String mDate;

    public SwitchBusListFregment() {
        // Required empty public constructor
    }

    public static SwitchBusListFregment newInstance(IncomingBusRespModel[] List, String Date) {
        SwitchBusListFregment fragment = new SwitchBusListFregment();
        Bundle args = new Bundle();
        args.putSerializable(ARG_PARAM1, List);
        args.putString(ARG_PARAM2, Date);

        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mList = (IncomingBusRespModel[])getArguments().getSerializable(ARG_PARAM1);
            mDate = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_switch_bus_list_fregment, container, false);

        RecyclerView Recv = view.findViewById(R.id.SwitchListRecView);

        Sbla = new SwitchBusListAdapter(mList,mDate, getContext(), this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Sbla);

        return view;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void UpdateList(IncomingBusRespModel[] List) {
        if (Sbla != null) {
            Sbla.UpdateList(List);
            Sbla.notifyDataSetChanged();
        }
    }

    public void OnBusClick(String Id, String Date) {
        if (getActivity() != null) {
            ((SwitchActivity)getActivity()).TrackBus(Id, Date);
        }
    }
}