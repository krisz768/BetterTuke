package hu.krisz768.bettertuke.SwitchFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.IncomingBusFragment.BottomSheetIncomingBusFragment;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListFragment;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusStopSelectorAdapter;
import hu.krisz768.bettertuke.InfoFragment;
import hu.krisz768.bettertuke.LoadingFragment;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.NewApiInterface.GTFSRProvider;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.TrackBusFragment.TrackBusListFragment;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class Switchfragment extends Fragment {
    private static final String ARG_PARAM1 = "TripID";
    private static final String ARG_PARAM2 = "StopID";
    private static final String ARG_PARAM3 = "Date";
    private static final String ARG_PARAM4 = "CurrentStopID";

    private String TripID;
    private String StopID;
    private String CurrentStopID;
    private String Date;

    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<String, BusStops> mStopList;

    private BusStops[] SelectedPlaceStopsArray;
    private SwitchBusStopSelectorAdapter Sbssa;
    private ScheduledExecutorService UpdateLoop;
    private BusLine mBusLine;
    private String ArrTime = "";
    private SwitchBusListFregment InBusFragment;

    public Switchfragment() {

    }

    public static Switchfragment newInstance(String TripID, String StopID,String CurrentStopID, String Date) {
        Switchfragment fragment = new Switchfragment();
        Bundle args = new Bundle();

        args.putString(ARG_PARAM1, TripID);
        args.putString(ARG_PARAM2, StopID);
        args.putString(ARG_PARAM3, Date);
        args.putString(ARG_PARAM4, CurrentStopID);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            TripID = getArguments().getString(ARG_PARAM1);
            StopID = getArguments().getString(ARG_PARAM2);
            CurrentStopID = getArguments().getString(ARG_PARAM4);
            Date = getArguments().getString(ARG_PARAM3);
        }

        mPlaceList = MainActivity.busPlaces;
        mStopList = MainActivity.busStops;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_switchfragment, container, false);

        TextView BusStopName = view.findViewById(R.id.SwichBusStopName);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date parsedDate = null;

        try {
            parsedDate = formatter.parse(Date);
        } catch (Exception ignored) {

        }

        mBusLine = BusLine.BusLinesByLineId(TripID, true, parsedDate, getContext());

        for (int i = 0; i < mBusLine.getStops().length; i++) {
            if (mBusLine.getStops()[i].getStopId().equals(StopID)) {
                Calendar ArriveTime = Calendar.getInstance();
                String[] TimeParts = mBusLine.getStops()[i].getArriveTime().split(":");

                ArriveTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                ArriveTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));

                ArrTime = String.format(Locale.US, "%02d", ArriveTime.get(Calendar.HOUR_OF_DAY)) + ":" + String.format(Locale.US, "%02d", ArriveTime.get(Calendar.MINUTE));
            }
        }

        List<BusStops> SelectedPlaceStops = new ArrayList<>();

        Integer PlaceId = null;
        for (BusStops busStops : mStopList.values()) {
            if (Objects.equals(busStops.getId(), StopID)) {
                PlaceId = busStops.getPlace();
            }
        }

        BusPlaces busPlace = mPlaceList.get(PlaceId);

        if (busPlace != null) {
            BusStopName.setText(busPlace.getName());

            for (BusStops busStops : mStopList.values()) {
                if (busStops.getPlace() == busPlace.getId()) {
                    SelectedPlaceStops.add(busStops);
                }
            }
        }

        SelectedPlaceStopsArray = new BusStops[SelectedPlaceStops.size()];
        SelectedPlaceStops.toArray(SelectedPlaceStopsArray);

        new Thread(this::SetupStopList).start();

        StartUpdateThread();

        return view;
    }

    private void SetupStopList () {
        Context ctx = getContext();
        View view = getView();
        Activity activity = getActivity();

        Switchfragment switchfragment = this;

        if (ctx != null && view != null && activity != null) {
            String[] StopNames = new String[SelectedPlaceStopsArray.length];

            for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
                StopNames[i] = HelperProvider.GetStopDirectionString(ctx,SelectedPlaceStopsArray[i].getId());
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Sbssa = new SwitchBusStopSelectorAdapter(SelectedPlaceStopsArray,StopID, switchfragment, StopNames, ctx);
                    Sbssa.setSelectedStop(CurrentStopID);

                    RecyclerView StopSelectorRec = view.findViewById(R.id.SwichBusStopListRecView);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(ctx);
                    mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

                    StopSelectorRec.setLayoutManager(mLayoutManager);
                    StopSelectorRec.setAdapter(Sbssa);
                    StopSelectorRec.setItemAnimator(null);

                    RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(ctx) {
                        @Override
                        protected int getVerticalSnapPreference() {
                            return LinearSmoothScroller.SNAP_TO_START;
                        }

                        @Override
                        protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                            return super.calculateSpeedPerPixel(displayMetrics) *4;
                        }
                    };

                    int scrollPosition = 0;

                    if (StopID.equals("-1")) {
                        scrollPosition = SelectedPlaceStopsArray.length;
                    } else {
                        for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
                            if (SelectedPlaceStopsArray[i].getId().equals(StopID)) {
                                scrollPosition = i;
                                break;
                            }
                        }
                    }

                    smoothScroller.setTargetPosition(scrollPosition);
                    if (StopSelectorRec.getLayoutManager() != null) {
                        StopSelectorRec.getLayoutManager().startSmoothScroll(smoothScroller);
                    }

                    //StopSelectorRec.setNestedScrollingEnabled(false);
                }
            });
        } else {
            try {
                Thread.sleep(1000);
                SetupStopList();
            } catch (Exception ignored) {

            }
        }
    }

    public void OnStopClick(String StopID) {
        CurrentStopID = StopID;
        Sbssa.setSelectedStop(StopID);
        Sbssa.notifyDataSetChanged();

        ResetList();

        new Thread(() -> {
            GTFSRProvider GTFSRProvider_ = new GTFSRProvider(getActivity());
            GetBusPosition(GTFSRProvider_);
        }).start();
    }

    private void ResetList() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.BusSwitchListFragment, new LoadingFragment())
                .commit();
        InBusFragment = null;
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

        GTFSRProvider GTFSRProvider_ = new GTFSRProvider(this.getActivity());

        UpdateLoop = Executors.newScheduledThreadPool(1);
        UpdateLoop.scheduleAtFixedRate(() -> GetBusPosition(GTFSRProvider_), 0, 5, TimeUnit.SECONDS);
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private void GetBusPosition(GTFSRProvider GTFSRProvider_) {
        try {
            if (mBusLine.getDate() != null) {
                UpdateLoop.shutdown();
                return;
            }

            TrackBusRespModel BusPosition = null;

            if (Date == null) {
                BusPosition = GTFSRProvider_.getBusLocation(mBusLine.getLineId());
            }

            View view = getView();
            if (view == null) {
                return;
            }

            Context ctx = getContext();
            if (ctx == null) {
                return;
            }

            TextView BusArrInfo = view.findViewById(R.id.SwitchBusStopInfo);

            Date CurrentTime = new Date();

            if (BusPosition != null) {

                if (BusPosition.getDelayMin()==1){
                    BusArrInfo.setText(ctx.getString(R.string.SwitchTimeStringOneMinute, ArrTime, BusPosition.getDelayMin() >= 0 ? "+" + BusPosition.getDelayMin() : BusPosition.getDelayMin()));
                } else {
                    BusArrInfo.setText(ctx.getString(R.string.SwitchTimeString, ArrTime, BusPosition.getDelayMin() >= 0 ? "+" + BusPosition.getDelayMin() : BusPosition.getDelayMin()));
                }

                Calendar Parsecalendar = Calendar.getInstance();
                SimpleDateFormat Sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
                Parsecalendar.setTime(Sdf2.parse(ArrTime));
                Parsecalendar.add(Calendar.MINUTE, BusPosition.getDelayMin());

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Parsecalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, Parsecalendar.get(Calendar.MINUTE));

                if (calendar.getTime().after(CurrentTime)) {
                    GetIncomingBuses(Sdf2.format(calendar.getTime()));
                } else {
                    DisplayInfoFragmentBusLeft();
                }
            } else {
                if (Date == null) {
                    BusArrInfo.setText(ArrTime);
                } else {
                    StringBuilder str = new StringBuilder(Date);

                    str.insert(6, ". ");
                    str.insert(4, ". ");

                    BusArrInfo.setText(str + ". " + ArrTime);
                }


                Calendar Parsecalendar = Calendar.getInstance();
                SimpleDateFormat Sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
                Parsecalendar.setTime(Sdf2.parse(ArrTime));

                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, Parsecalendar.get(Calendar.HOUR_OF_DAY));
                calendar.set(Calendar.MINUTE, Parsecalendar.get(Calendar.MINUTE));

                Log.e("TAG", "GetBusPosition: " + calendar.toString());
                
                if (calendar.getTime().after(CurrentTime) || Date != null) {
                    GetIncomingBuses(Sdf2.format(calendar.getTime()));
                } else {
                    DisplayInfoFragmentBusLeft();
                }
            }
        } catch (Exception e) {
            Log.e("Update bus pos error", e.toString());
            e.printStackTrace();
        }
    }

    private void GetIncomingBuses(String Time) {
        NewGTFSDatabase NDm = new NewGTFSDatabase(getContext());

        FragmentActivity mainActivity = (FragmentActivity)getActivity();
        Date currentTime = Calendar.getInstance().getTime();
        GTFSRProvider GTFSRProvider_ = new GTFSRProvider(this.getActivity());

        IncomingBusRespModel[] BusList = null;

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
        String CurrentDate = sdf1.format(new Date());

        if (Date != null) {
            if (CurrentStopID.equals("-1")) {
                ArrayList<IncomingBusRespModel> list = new ArrayList<>();

                for (BusStops element : SelectedPlaceStopsArray) {
                    list.addAll(Arrays.asList(NDm.GetOfflineDepartureTimes(element.getId(), Date, Time)));
                }

                Collections.sort(list, (o1, o2) -> o1.getArriveTime().compareTo(o2.getArriveTime()));

                BusList = new IncomingBusRespModel[list.size()];
                list.toArray(BusList);
            } else {
                BusList = NDm.GetOfflineDepartureTimes(CurrentStopID, Date, Time);
            }
        } else {
            if (CurrentStopID.equals("-1")) {
                ArrayList<IncomingBusRespModel> list = new ArrayList<>();

                for (BusStops element : SelectedPlaceStopsArray) {
                    IncomingBusRespModel[] Data = GTFSRProvider_.getNextIncomingBuses(element.getId(), CurrentDate, Time);
                    if (Data != null) {
                        list.addAll(Arrays.asList(Data));
                    } else {
                        list = null;
                        break;
                    }
                }

                if (list != null) {
                    Collections.sort(list, (o1, o2) -> o1.getArriveTime().compareTo(o2.getArriveTime()));

                    BusList = new IncomingBusRespModel[list.size()];
                    list.toArray(BusList);
                }
            } else {
                BusList = GTFSRProvider_.getNextIncomingBuses(CurrentStopID, CurrentDate, Time);
            }
        }

        if (BusList == null) {
            if (CurrentStopID.equals("-1")) {
                ArrayList<IncomingBusRespModel> list = new ArrayList<>();

                for (BusStops element : SelectedPlaceStopsArray) {
                    list.addAll(Arrays.asList(NDm.GetOfflineDepartureTimes(element.getId(), Date == null ? CurrentDate : Date, Time)));
                }

                Collections.sort(list, (o1, o2) -> o1.getArriveTime().compareTo(o2.getArriveTime()));

                BusList = new IncomingBusRespModel[list.size()];
                list.toArray(BusList);
            } else {
                BusList = NDm.GetOfflineDepartureTimes(CurrentStopID, Date == null ? CurrentDate : Date, Time);
            }
            if(mainActivity != null && HelperProvider.displayOfflineText()) {
                mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                HelperProvider.setOfflineTextDisplayed();
            }
        }

        if (BusList != null && Date == null) {
            ArrayList<IncomingBusRespModel> list2 = new ArrayList<>();
            for (IncomingBusRespModel incomingBusRespModel : BusList) {
                if (!incomingBusRespModel.getLineId().equals(TripID)) {
                    list2.add(incomingBusRespModel);
                }
            }

            BusList = new IncomingBusRespModel[list2.size()];
            list2.toArray(BusList);


            Calendar Now = Calendar.getInstance();
            for (IncomingBusRespModel incomingBusRespModel : BusList) {
                incomingBusRespModel.setMiss(false);

                BusLine Bj = BusLine.BusLinesByLineId(incomingBusRespModel.getLineId(), false, null, mainActivity);

                if (Bj != null) {
                    SimpleDateFormat Sdf = new SimpleDateFormat("H", Locale.US);
                    SimpleDateFormat Sdf2 = new SimpleDateFormat("m", Locale.US);

                    if (Bj.getDepartureHour() < Integer.parseInt(Sdf.format(currentTime)) || (Bj.getDepartureHour() == Integer.parseInt(Sdf.format(currentTime)) && Bj.getDepartureMinute() <= Integer.parseInt(Sdf2.format(currentTime)))) {
                        Boolean IsBusStarted = GTFSRProvider_.getIsBusHasStarted(incomingBusRespModel.getLineId() + "");
                        if (IsBusStarted == null) {
                            IsBusStarted = false;
                        } else {
                            if (!IsBusStarted && ((Bj.getDepartureHour() == Integer.parseInt(Sdf.format(currentTime)) && Bj.getDepartureMinute() < Integer.parseInt(Sdf2.format(currentTime))) || Bj.getDepartureHour() < Integer.parseInt(Sdf.format(currentTime)))) {
                                Calendar ArrTime = Calendar.getInstance();
                                ArrTime.setTime(incomingBusRespModel.getArriveTime());

                                ArrTime.add(Calendar.MINUTE, 1);
                                if (ArrTime.after(Now)) {
                                    incomingBusRespModel.setMiss(true);
                                }
                            }
                        }
                        incomingBusRespModel.setStarted(IsBusStarted);
                    } else {
                        incomingBusRespModel.setStarted(false);
                    }
                } else {
                    incomingBusRespModel.setMiss(true);
                }
            }
        }

        if (InBusFragment == null) {
            try {
                InBusFragment = SwitchBusListFregment.newInstance(BusList, Date, StopID, CurrentStopID);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.BusSwitchListFragment, InBusFragment)
                        .commit();
            } catch (Exception e) {
                InBusFragment = null;
            }
        } else {

            if (mainActivity != null) {
                IncomingBusRespModel[] finalBusList = BusList;
                mainActivity.runOnUiThread(() -> {
                    if (InBusFragment != null) {
                        InBusFragment.UpdateList(finalBusList);
                    }
                });
            }
        }
    }

    private void DisplayInfoFragmentBusLeft() {
        InBusFragment = null;

        InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.BusLeftStop), -1);
        getChildFragmentManager().beginTransaction()
                .replace(R.id.BusSwitchListFragment, Fragment)
                .commit();
    }
}