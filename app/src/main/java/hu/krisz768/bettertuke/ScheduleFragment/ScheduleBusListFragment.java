package hu.krisz768.bettertuke.ScheduleFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.ScheduleActivity;

public class ScheduleBusListFragment extends Fragment {

    private static final String STOPID= "StopId";

    private int mStopId;

    public ScheduleBusListFragment() {
        // Required empty public constructor
    }

    public static ScheduleBusListFragment newInstance(int StopId) {
        ScheduleBusListFragment fragment = new ScheduleBusListFragment();
        Bundle args = new Bundle();
        args.putInt(STOPID, StopId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStopId = getArguments().getInt(STOPID);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_list, container, false);

        DatabaseManager Dm = new DatabaseManager(getContext());
        BusLine[] BusLines;
        if (mStopId == -1) {
            BusLines = Dm.GetActiveBusLines();
        } else{
            BusLines = Dm.GetActiveBusLinesFromStop(mStopId);
            String StopName = Dm.GetStopName(mStopId);
            String StopNum = Dm.GetStopNum(mStopId);

            ((TextView)view.findViewById(R.id.ScheduleTargetText)).setText(StopName.trim() + " (" + StopNum + ")");
        }

        ScheduleBusListAdapter Sbla = new ScheduleBusListAdapter(BusLines, getContext(), this);

        RecyclerView BusLineRecv = view.findViewById(R.id.ScheduleBusLineRec);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        BusLineRecv.setLayoutManager(mLayoutManager);
        BusLineRecv.setAdapter(Sbla);



        return view;
    }

    public void OnLineClick(String Line) {
        if (getActivity() != null) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ((ScheduleActivity)getActivity()).selectLine(Line);
                }
            });
        }
    }
}