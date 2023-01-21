package hu.krisz768.bettertuke.TrackBusFragment;

import android.graphics.Color;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.maps.model.LatLng;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.BuildConfig;
import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;
import hu.krisz768.bettertuke.models.BusAttributes;

public class BottomSheetTrackBusFragment extends Fragment {

    private static final String STOP = "Stop";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";
    private static final String LINEOBJ = "LineObj";

    private int mStop;

    private BusPlaces[] mPlaceList;
    private BusStops[] mStopList;
    private BusLine mBusLine;

    private TrackBusListFragment TrackBusFragment;
    private TextView PlateNumber;
    private TextView BusType;
    private TextView Articulated;
    private TextView Doors;
    private ImageView Electric;
    private ImageView LowFloor;
    private ImageView AirConditioner;
    private ImageView Wifi;
    private ImageView Usb;

    private boolean BusAttributesVisible = false;

    ScheduledExecutorService UpdateLoop;

    public BottomSheetTrackBusFragment() {

    }

    public static BottomSheetTrackBusFragment newInstance(int Stop, BusPlaces[] PlaceList, BusStops[] StopList, BusLine LineObj) {
        BottomSheetTrackBusFragment fragment = new BottomSheetTrackBusFragment();
        Bundle args = new Bundle();
        args.putInt(STOP, Stop);
        args.putSerializable(PLACELIST, PlaceList);
        args.putSerializable(STOPLIST, StopList);
        args.putSerializable(LINEOBJ, LineObj);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStop = getArguments().getInt(STOP);
            mPlaceList = (BusPlaces[]) getArguments().getSerializable(PLACELIST);
            mStopList = (BusStops[]) getArguments().getSerializable(STOPLIST);
            mBusLine = (BusLine) getArguments().getSerializable(LINEOBJ);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_track_bus, container, false);
        TextView BusNum = view.findViewById(R.id.TrackBusNumber);
        TextView BusText = view.findViewById(R.id.TrackBusName);

        BusNum.setText(mBusLine.getRouteInfo().getLineNum());
        int WhiteColor = Color.rgb(255,255,255);
        BusNum.setTextColor(WhiteColor);

        BusText.setText(mBusLine.getRouteInfo().getLineName());

        findBusAttributes(view);

        if (mBusLine.getDate() == null) {
            StartUpdateThread();
        } else {
            TrackBusFragment = TrackBusListFragment.newInstance(mBusLine, mStop, mPlaceList, mStopList, null);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.BusTrackFragmentView, TrackBusFragment)
                    .commit();
        }

        return view;
    }

    @Override
    public void onPause() {
        super.onPause();
        UpdateLoop.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();
        StartUpdateThread();
    }

    @Override
    public void onStop() {
        super.onStop();
        UpdateLoop.shutdown();
    }

    private void StartUpdateThread() {
        if (UpdateLoop != null) {
            if (!UpdateLoop.isShutdown()){
                return;
            }
        }

        TukeServerApi serverApi = new TukeServerApi(this.getActivity());

        UpdateLoop = Executors.newScheduledThreadPool(1);
        UpdateLoop.scheduleAtFixedRate(() -> GetBusPosition(serverApi), 0, 2, TimeUnit.SECONDS);
    }

    private void GetBusPosition(TukeServerApi serverApi) {
        try {
            if (BuildConfig.DEBUG) {
                Log.i("BusPositionUpdate", "Bus Pos Update");
            }

            TrackBusRespModel BusPosition = serverApi.getBusLocation(mBusLine.getLineId());
            if (getView() == null) {
                return;
            }
            TextView BusNum = getView().findViewById(R.id.TrackBusNumber);

            if (BusPosition != null) {
                if (getContext() != null) {
                    BusNum.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bus_number_background_active));
                }
                if (!BusAttributesVisible) {
                    if (getActivity() != null){
                        getActivity().runOnUiThread(() -> showBusAttributes(HelperProvider.getBusAttributes(getContext(),BusPosition.getLicensePlateNumber())));

                        BusAttributesVisible = true;
                    }
                }
            } else {
                if (getContext() != null && getActivity() != null) {
                    BusAttributesVisible = false;
                    BusNum.setBackground(ContextCompat.getDrawable(getContext(), R.drawable.bus_number_background_inactive));

                    getActivity().runOnUiThread(this::hideBusAttributes);
                }
            }

            if (getActivity() != null) {
                getActivity().runOnUiThread(() -> {
                    MainActivity mainActivity = (MainActivity)getActivity();
                    if (TrackBusFragment != null && mainActivity != null) {
                        mainActivity.BusPositionMarker(BusPosition != null ? new LatLng(BusPosition.getGpsLongitude(), BusPosition.getGpsLatitude()) : null);
                    }
                });
            }

            if (TrackBusFragment == null) {
                TrackBusFragment = TrackBusListFragment.newInstance(mBusLine, mStop, mPlaceList, mStopList, BusPosition);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.BusTrackFragmentView, TrackBusFragment)
                        .commit();
            } else {
                getActivity().runOnUiThread(() -> {
                    if (TrackBusFragment != null) {
                        TrackBusFragment.UpdateData(BusPosition);
                        MainActivity mainActivity = (MainActivity)getActivity();
                        if (mainActivity != null) {
                            if (((MainActivity)getActivity()).IsBottomSheetCollapsed()) {
                                TrackBusFragment.scrollSmoothTo();
                            }
                        }
                    }
                });
            }
        } catch (Exception e) {
            Log.e("Update bus pos error", e.toString());
            e.printStackTrace();
        }
    }

    private void findBusAttributes(View view)
    {
        PlateNumber = view.findViewById(R.id.PlateNumber);
        BusType = view.findViewById(R.id.BusType);
        Articulated = view.findViewById(R.id.Articulated);
        Doors = view.findViewById(R.id.Doors);
        Electric = view.findViewById(R.id.Electric);
        LowFloor = view.findViewById(R.id.LowFloor);
        AirConditioner = view.findViewById(R.id.AirConditioner);
        Wifi = view.findViewById(R.id.Wifi);
        Usb = view.findViewById(R.id.Usb);

        Electric.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.Electric));
        LowFloor.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.LowFloor));
        AirConditioner.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.AirConditioner));
        Wifi.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.Wifi));
        Usb.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.Usb));
    }

    private void showBusAttributes(BusAttributes busAttributes)
    {
        PlateNumber.setText(busAttributes.getPlateNumber());

        if(busAttributes.getDoors()==-1)
            return;

        BusType.setText(busAttributes.getType());

        if(busAttributes.getArticulated()==0)
            Articulated.setText(getString(R.string.SoloBus));
        else if(busAttributes.getArticulated()==1)
            Articulated.setText(getString(R.string.ArticulatedBus));
        else if(busAttributes.getArticulated()==2)
            Articulated.setText(getString(R.string.MidiBus));
        Doors.setText(getString(R.string.BusDoorNumberText, busAttributes.getDoors()));

        if(busAttributes.getPropulsion()==1)
            Electric.setVisibility(View.VISIBLE);
        if(busAttributes.isLowFloor())
            LowFloor.setVisibility(View.VISIBLE);
        if(busAttributes.isAirConditioner())
            AirConditioner.setVisibility(View.VISIBLE);
        if(busAttributes.isWifi())
            Wifi.setVisibility(View.VISIBLE);
        if(busAttributes.isUsb())
            Usb.setVisibility(View.VISIBLE);
    }

    private void hideBusAttributes()
    {
        PlateNumber.setText("");
        BusType.setText("");
        Articulated.setText("");
        Doors.setText("");
        Electric.setVisibility(View.GONE);
        LowFloor.setVisibility(View.GONE);
        AirConditioner.setVisibility(View.GONE);
        Wifi.setVisibility(View.GONE);
        Usb.setVisibility(View.GONE);
    }
}