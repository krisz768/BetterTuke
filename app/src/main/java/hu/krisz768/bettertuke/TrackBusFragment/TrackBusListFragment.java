package hu.krisz768.bettertuke.TrackBusFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.HashMap;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class TrackBusListFragment extends Fragment {
    private static final String STOP = "Stop";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";
    private static final String LINES = "LineInfo";
    private static final String POSITION = "BusPosition";

    private int mStop;

    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<Integer, BusStops> mStopList;
    private BusLine mLine;
    private TrackBusRespModel mBusPosition;

    private TrackBusListAdapter Tbla;

    private RecyclerView Recv;

    public TrackBusListFragment() {

    }

    public static TrackBusListFragment newInstance(BusLine LineInfo, int Stop, HashMap<Integer, BusPlaces> PlaceList, HashMap<Integer, BusStops> StopList, TrackBusRespModel BusPosition) {
        TrackBusListFragment fragment = new TrackBusListFragment();
        Bundle args = new Bundle();
        args.putSerializable(LINES, LineInfo);
        args.putInt(STOP, Stop);
        args.putSerializable(PLACELIST, PlaceList);
        args.putSerializable(STOPLIST, StopList);
        args.putSerializable(POSITION, BusPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLine = (BusLine) getArguments().getSerializable(LINES);
            mStop = getArguments().getInt(STOP);
            mPlaceList = (HashMap<Integer, BusPlaces>) getArguments().getSerializable(PLACELIST);
            mStopList = (HashMap<Integer, BusStops>) getArguments().getSerializable(STOPLIST);
            mBusPosition = (TrackBusRespModel) getArguments().getSerializable(POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_bus_list, container, false);

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), mLine.getDepartureHour(), mLine.getDepartureMinute());

        Tbla = new TrackBusListAdapter(mLine.getStops(), mPlaceList, mStopList, calendar, mBusPosition, mStop, this, getContext(), mLine.getDate());

        Recv = view.findViewById(R.id.BusTrackListRecv);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Tbla);

        int scrollPosition = 0;

        if (mBusPosition != null) {
            for (int i = 0; i < mLine.getStops().length; i++) {
                if (mLine.getStops()[i].getOrder() == mBusPosition.getStopNumber()) {
                    scrollPosition = i;
                }
            }
        }

        Recv.scrollToPosition(scrollPosition);

        return view;
    }

    public void scrollSmoothTo() {
        if (mBusPosition != null) {
            for (int i = 0; i < mLine.getStops().length; i++) {
                if (mLine.getStops()[i].getOrder() == mBusPosition.getStopNumber()) {
                    int scrollPosition = i;
                    if (mLine.getStops().length > i+1) {
                        i++;
                    }

                    if (getContext() == null) {
                        return;
                    }

                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
                        @Override
                        protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }

                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return super.calculateSpeedPerPixel(displayMetrics) *4;
                        }
                    };
                    smoothScroller.setTargetPosition(scrollPosition);
                    if ( Recv.getLayoutManager() != null) {
                        Recv.getLayoutManager().startSmoothScroll(smoothScroller);
                    }
                }
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void UpdateData(TrackBusRespModel TrackData) {
        if (Tbla != null) {
            Tbla.UpdateList(TrackData);
            Tbla.notifyDataSetChanged();
            mBusPosition = TrackData;
        }
    }

    public void OnStopClick(int Id) {
        BusStops busStops = mStopList.get(Id);

        if(getActivity() != null && busStops != null){
            ((MainActivity) getActivity()).SetUserTouchedMap(true);
            ((MainActivity) getActivity()).ZoomTo(new LatLng(busStops.getGpsLatitude(), busStops.getGpsLongitude()));
        }

        if (mBusPosition != null) {
            for (int i = 0; i < mLine.getStops().length; i++) {
                if (mLine.getStops()[i].getOrder() == mBusPosition.getStopNumber()) {
                    if (mLine.getStops()[i].getStopId() == Id){
                        if(getActivity() != null){
                            ((MainActivity) getActivity()).SetUserTouchedMap(false);
                        }
                    }
                    break;
                }
            }
        } else {
            if(getActivity() != null){
                ((MainActivity) getActivity()).SetUserTouchedMap(false);
            }
        }
    }
}