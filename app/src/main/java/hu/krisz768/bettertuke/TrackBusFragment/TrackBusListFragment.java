package hu.krisz768.bettertuke.TrackBusFragment;

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

import hu.krisz768.bettertuke.Database.BusJaratok;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link TrackBusListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class TrackBusListFragment extends Fragment {

    private static final String PLACE = "Place";
    private static final String STOP = "Stop";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";
    private static final String JARATOK = "JaratInfo";
    private static final String POSITION = "BusPosition";

    private int mPlace;
    private int mStop;


    private BusPlaces[] mPlaceList;
    private BusStops[] mStopList;
    private BusJaratok mJarat;
    private TrackBusRespModel mBusPosition;

    private TrackBusListAdapter Tbla;

    private RecyclerView Recv;



    public TrackBusListFragment() {
        // Required empty public constructor
    }

    public static TrackBusListFragment newInstance(BusJaratok JaratInfo, int Place, int Stop, BusPlaces[] PlaceList, BusStops[] StopList, TrackBusRespModel BusPosition) {
        TrackBusListFragment fragment = new TrackBusListFragment();
        Bundle args = new Bundle();
        args.putSerializable(JARATOK, JaratInfo);
        args.putInt(PLACE, Place);
        args.putInt(STOP, Stop);
        args.putSerializable(PLACELIST, PlaceList);
        args.putSerializable(STOPLIST, StopList);
        args.putSerializable(POSITION, BusPosition);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mJarat = (BusJaratok) getArguments().getSerializable(JARATOK);
            mPlace = getArguments().getInt(PLACE);
            mStop = getArguments().getInt(STOP);
            mPlaceList = (BusPlaces[]) getArguments().getSerializable(PLACELIST);
            mStopList = (BusStops[]) getArguments().getSerializable(STOPLIST);
            mBusPosition = (TrackBusRespModel) getArguments().getSerializable(POSITION);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_track_bus_list, container, false);

        Calendar calendar = Calendar.getInstance();
        calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), mJarat.getIndulasOra(), mJarat.getIndulasPerc());

        Tbla = new TrackBusListAdapter(mJarat.getMegallok(), mPlaceList, mStopList, calendar, mBusPosition, mStop, this, getContext());

        Recv = view.findViewById(R.id.BusTrackListRecv);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Tbla);

        int scrollposition = 0;

        if (mBusPosition != null) {
            for (int i = 0; i < mJarat.getMegallok().length; i++) {
                if (mJarat.getMegallok()[i].getSorrend() == mBusPosition.getMegalloSorszam()) {
                    scrollposition = i;
                }
            }
        }

        Recv.scrollToPosition(scrollposition);

        return view;
    }

    public void scrollSmoothTo() {
        if (mBusPosition != null) {
            for (int i = 0; i < mJarat.getMegallok().length; i++) {
                if (mJarat.getMegallok()[i].getSorrend() == mBusPosition.getMegalloSorszam()) {
                    int scrollposition = i;
                    if (mJarat.getMegallok().length > i+1) {
                        i++;
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
                    smoothScroller.setTargetPosition(scrollposition);
                    Recv.getLayoutManager().startSmoothScroll(smoothScroller);
                }
            }
        }


    }

    public void UpdateData(TrackBusRespModel TrackData) {
        if (Tbla != null) {
            Tbla.UpdateList(TrackData);
            Tbla.notifyDataSetChanged();
            mBusPosition = TrackData;
        }
    }

    public void OnStopClick(int Id) {
        for (int i = 0; i < mStopList.length; i++) {
            if (mStopList[i].getId() == Id) {
                ((MainActivity) getActivity()).ZoomTo(new LatLng(mStopList[i].getGpsY(), mStopList[i].getGpsX()));
                break;
            }
        }
    }
}