package hu.krisz768.bettertuke;

import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;
import hu.krisz768.bettertuke.api_interface.models.IncommingBusRespModel;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link BottomSheetIncomingBusFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class BottomSheetIncomingBusFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String PLACE = "Place";
    private static final String STOP = "Stop";
    private static final String PLACELIST = "PlaceList";

    // TODO: Rename and change types of parameters
    private int mPlace;
    private int mStop;
    private BusPlaces[] mPlaceList;

    public BottomSheetIncomingBusFragment() {
        // Required empty public constructor
    }
    public static BottomSheetIncomingBusFragment newInstance(int Place, int Stop, BusPlaces[] PlaceList) {
        BottomSheetIncomingBusFragment fragment = new BottomSheetIncomingBusFragment();
        Bundle args = new Bundle();
        args.putInt(PLACE, Place);
        args.putInt(STOP, Stop);
        args.putSerializable(PLACELIST, PlaceList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getInt(PLACE);
            mStop = getArguments().getInt(STOP);
            mPlaceList = (BusPlaces[]) getArguments().getSerializable(PLACELIST);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_incoming_bus_view, container, false);

        TextView Teszt = view.findViewById(R.id.BusStopName);

        for (int i = 0; i < mPlaceList.length; i++) {
            if (mPlaceList[i].getId() == mPlace) {
                Teszt.setText(mPlaceList[i].getName());
            }
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                GetIncommingBuses(view);
            }
        }).start();

        return view;
    }

    private void GetIncommingBuses(View view) {
        try {
            TukeServerApi serverApi = new TukeServerApi(this.getActivity());
            IncommingBusRespModel[] BusList = serverApi.getNextIncommingBuses(mStop);

            for (int i = 0; i < BusList.length; i++) {
                BusList[i].setElindult(serverApi.getIsBusHasStarted(BusList[i].getJaratid()));
            }

            FragmentContainerView Fcv = view.findViewById(R.id.BusListFragment);

            IncomingBusListFragment InBusFragment = IncomingBusListFragment.newInstance(BusList);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.BusListFragment, InBusFragment)
                    .commit();
        } catch (Exception r) {

        }


    }
}