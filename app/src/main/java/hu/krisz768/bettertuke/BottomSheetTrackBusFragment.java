package hu.krisz768.bettertuke;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import hu.krisz768.bettertuke.Database.BusJaratok;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomSheetTrackBusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomSheetTrackBusFragment extends Fragment {

    private static final String PLACE = "Place";
    private static final String STOP = "Stop";
    private static final String JARAT = "Jarat";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";
    private static final String JARATOBJ = "JaratObj";

    private int mPlace;
    private int mStop;
    private int mJarat;

    private BusPlaces[] mPlaceList;
    private BusStops[] mStopList;
    private BusJaratok mBusJarat;

    Thread UpdateThread;
    TrackBusListFragment TrackBusFragment;

    public BottomSheetTrackBusFragment() {
        // Required empty public constructor
    }

    public static BottomSheetTrackBusFragment newInstance(int Place, int Stop, int Jarat, BusPlaces[] PlaceList, BusStops[] StopList, BusJaratok JaratObj) {
        BottomSheetTrackBusFragment fragment = new BottomSheetTrackBusFragment();
        Bundle args = new Bundle();
        args.putInt(PLACE, Place);
        args.putInt(STOP, Stop);
        args.putInt(JARAT, Jarat);
        args.putSerializable(PLACELIST, PlaceList);
        args.putSerializable(STOPLIST, StopList);
        args.putSerializable(JARATOBJ, JaratObj);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getInt(PLACE);
            mStop = getArguments().getInt(STOP);
            mJarat = getArguments().getInt(JARAT);
            mPlaceList = (BusPlaces[]) getArguments().getSerializable(PLACELIST);
            mStopList = (BusStops[]) getArguments().getSerializable(STOPLIST);
            mBusJarat = (BusJaratok) getArguments().getSerializable(JARATOBJ);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_track_bus, container, false);
        // Inflate the layout for this fragment
        TextView BusNum = view.findViewById(R.id.TrackBusNumber);
        TextView BusText = view.findViewById(R.id.TrackBusNev);

        BusNum.setText(mBusJarat.getNyomvonalInfo().getJaratSzam());
        int Whitecolor = Color.rgb(255,255,255);
        BusNum.setTextColor(Whitecolor);

        BusText.setText(mBusJarat.getNyomvonalInfo().getJaratNev());

        UpdateThread = new Thread(new Runnable() {
            @Override
            public void run() {
                TukeServerApi serverApi = new TukeServerApi(view.getContext());

                try {
                    while (true) {
                        if (getContext() == null)
                            break;
                        GetBusPosition(serverApi);
                        Thread.sleep(2000);
                    }
                } catch (Exception e) {

                }
            }
        });
        UpdateThread.start();

        return view;
    }

    private void GetBusPosition(TukeServerApi serverApi) {
        try {
            TrackBusRespModel BusPosition = serverApi.getBusLocation(mBusJarat.getJaratid());
            TextView BusNum = getView().findViewById(R.id.TrackBusNumber);

            if (BusPosition != null) {
                BusNum.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bus_number_background_active));
            } else {
                BusNum.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bus_number_background_inactive));
            }


            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    MainActivity mainActivity = (MainActivity)getActivity();
                    if (TrackBusFragment != null && mainActivity != null) {
                        mainActivity.BuspositionMarker(BusPosition != null ? new LatLng(BusPosition.getGPSY(), BusPosition.getGPSx()) : null);
                    }
                }
            });

            if (TrackBusFragment == null) {
                TrackBusFragment = TrackBusListFragment.newInstance(mBusJarat, mPlace, mStop, mPlaceList, mStopList, BusPosition);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.BusTrackFragmentView, TrackBusFragment)
                        .commit();
            } else {
                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (TrackBusFragment != null) {
                            TrackBusFragment.UpdateData(BusPosition);
                            MainActivity mainActivity = (MainActivity)getActivity();
                            if (mainActivity != null) {
                                if (((MainActivity)getActivity()).IsBottomSheetCollapsed()) {
                                    TrackBusFragment.scrollSmoothTo();
                                }
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Update bus pos error", e.toString());
        }
    }
}