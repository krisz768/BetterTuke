package hu.krisz768.bettertuke.ScheduleFragment;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleBusTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleBusTimeFragment extends Fragment {

    private static final String LINENUM= "lineNum";

    private String mLineNum;

    public ScheduleBusTimeFragment() {
        // Required empty public constructor
    }

    public static ScheduleBusTimeFragment newInstance(String lineNum) {
        ScheduleBusTimeFragment fragment = new ScheduleBusTimeFragment();
        Bundle args = new Bundle();
        args.putString(LINENUM, lineNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLineNum = getArguments().getString(LINENUM);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_time, container, false);

        TextView NumText = view.findViewById(R.id.ScheduleBusLineNum);
        NumText.setText(mLineNum);

        return view;
    }
}