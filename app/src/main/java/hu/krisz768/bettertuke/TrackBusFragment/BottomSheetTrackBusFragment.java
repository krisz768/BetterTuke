package hu.krisz768.bettertuke.TrackBusFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.maps.model.LatLng;

import java.util.Calendar;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;
import hu.krisz768.bettertuke.models.BusAttributes;

public class BottomSheetTrackBusFragment extends Fragment {
    private static final String STOP = "Stop";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";
    private static final String LINEOBJ = "LineObj";
    private int mStop;
    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<Integer, BusStops> mStopList;
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
    private ScheduledExecutorService UpdateLoop;
    private TrackBusRespModel RecentBusPosition;
    private boolean IsOldDataWarnDisplayed = false;

    public BottomSheetTrackBusFragment() {

    }

    public static BottomSheetTrackBusFragment newInstance(int Stop, HashMap<Integer, BusPlaces> PlaceList, HashMap<Integer, BusStops> StopList, BusLine LineObj) {
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
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mStop = getArguments().getInt(STOP);
            mPlaceList = (HashMap<Integer, BusPlaces>) getArguments().getSerializable(PLACELIST);
            mStopList = (HashMap<Integer, BusStops>) getArguments().getSerializable(STOPLIST);
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
            new Thread(() -> {
                try {
                    Thread.sleep(600);
                } catch (InterruptedException ignored) {

                }
                Activity activity = getActivity();
                if  (activity != null) {
                    activity.runOnUiThread(() -> ((MainActivity)activity).BusPositionMarker(null));

                }
            }).start();


            TrackBusFragment = TrackBusListFragment.newInstance(mBusLine, mStop, mPlaceList, mStopList, null);
            getChildFragmentManager().beginTransaction()
                    .replace(R.id.BusTrackFragmentView, TrackBusFragment)
                    .commit();
        }

        Context ctx = getContext();

        if (ctx != null) {
            UserDatabase userDatabase = new UserDatabase(ctx);
            String AdEnabled = userDatabase.GetPreference("AdEnabled");
            AdView mAdView = view.findViewById(R.id.adView1_5);
            if (AdEnabled != null && AdEnabled.equals("true") && HelperProvider.IsAdConsentOk()){

                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }else {
                mAdView.setVisibility(View.GONE);
            }
        }



        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
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

    @SuppressLint("UseCompatLoadingForDrawables")
    private void GetBusPosition(TukeServerApi serverApi) {
        try {
            if (mBusLine.getDate() != null) {
                UpdateLoop.shutdown();
                return;
            }

            TrackBusRespModel BusPosition = serverApi.getBusLocation(mBusLine.getLineId());
            if (getView() == null) {
                return;
            }
            TextView BusNum = getView().findViewById(R.id.TrackBusNumber);

            Activity activity = getActivity();

            if (BusPosition != null) {
                if (activity != null) {
                    Calendar calendar = Calendar.getInstance();
                    calendar.add(Calendar.MINUTE, -1);
                    if (BusPosition.getLastUpdate().after(calendar.getTime())) {
                        activity.runOnUiThread(() -> BusNum.setBackground(activity.getDrawable(R.drawable.bus_number_background_active)));
                        IsOldDataWarnDisplayed = false;
                    } else {
                        activity.runOnUiThread(() -> {
                            BusNum.setBackground(activity.getDrawable(R.drawable.bus_number_background_warn));
                            if (!IsOldDataWarnDisplayed) {
                                Toast.makeText(activity ,R.string.TrackOldDataWarm, Toast.LENGTH_LONG).show();
                                IsOldDataWarnDisplayed = true;
                            }
                        });
                    }

                }
                if (!BusAttributesVisible) {
                    if (activity != null){
                        activity.runOnUiThread(() -> showBusAttributes(HelperProvider.getBusAttributes(activity,BusPosition.getLicensePlateNumber())));

                        BusAttributesVisible = true;
                    }
                }
            } else {
                if (activity != null) {
                    BusAttributesVisible = false;
                    activity.runOnUiThread(() -> BusNum.setBackground(activity.getDrawable(R.drawable.bus_number_background_inactive)));

                    activity.runOnUiThread(this::hideBusAttributes);

                    if (mBusLine.getCTrip() != null) {
                        Calendar Now = Calendar.getInstance();
                        int CurrentHour = Now.get(Calendar.HOUR_OF_DAY);
                        int CurrentMinute = Now.get(Calendar.MINUTE);

                        if ((mBusLine.getDepartureHour() == CurrentHour && CurrentMinute > mBusLine.getDepartureMinute()) || mBusLine.getDepartureHour() < CurrentHour) {
                            Boolean IsCBusStarted = serverApi.getIsBusHasStarted(mBusLine.getCTrip().getLineId());

                            if (IsCBusStarted != null && IsCBusStarted) {
                                activity.runOnUiThread(() -> ((MainActivity)activity).TrackBus(mBusLine.getCTrip().getLineId(), mBusLine.getDate()));
                            }
                        }
                    }
                }
            }

            if (activity != null) {
                activity.runOnUiThread(() -> {
                    if (TrackBusFragment != null) {
                        ((MainActivity)activity).BusPositionMarker(BusPosition != null ? new LatLng(BusPosition.getGpsLatitude(), BusPosition.getGpsLongitude()) : null);
                    }
                });
            }

            if (TrackBusFragment == null) {
                TrackBusFragment = TrackBusListFragment.newInstance(mBusLine, mStop, mPlaceList, mStopList, BusPosition);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.BusTrackFragmentView, TrackBusFragment)
                        .commit();
            } else if (activity != null){
                activity.runOnUiThread(() -> {
                    if (TrackBusFragment != null) {
                        TrackBusFragment.UpdateData(BusPosition);
                        MainActivity mainActivity = (MainActivity)getActivity();
                        if (mainActivity != null) {
                            if (((MainActivity)getActivity()).IsBottomSheetHalfExpanded()) {
                                TrackBusFragment.scrollSmoothTo();
                            }

                            if (RecentBusPosition != null && BusPosition != null) {
                                if (RecentBusPosition.getStopNumber() != BusPosition.getStopNumber()) {
                                    if (!((MainActivity)getActivity()).isUserTouchedMap()) {
                                        ((MainActivity)getActivity()).ZoomTo(new LatLng(BusPosition.getGpsLatitude(), BusPosition.getGpsLongitude()));
                                    }
                                }
                            }
                            RecentBusPosition = BusPosition;
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
        try {
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
        } catch (Exception ignored) {

        }
    }

    private void showBusAttributes(BusAttributes busAttributes)
    {
        try {
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
        } catch (Exception ignored) {

        }
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