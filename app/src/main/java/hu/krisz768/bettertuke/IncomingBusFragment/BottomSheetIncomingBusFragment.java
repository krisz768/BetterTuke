package hu.krisz768.bettertuke.IncomingBusFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
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

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.InfoFragment;
import hu.krisz768.bettertuke.LoadingFragment;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.NewApiInterface.GTFSRProvider;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.models.BusPositionRespModel;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;
import hu.krisz768.bettertuke.models.IncomBusBackStack;

public class BottomSheetIncomingBusFragment extends Fragment {
    private static final String PLACE = "Place";
    private static final String STOP = "Stop";
    private static final String STARTMODE = "StartMode";
    private int mPlace;
    private volatile String mStop;
    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<String, BusStops> mStopList;
    private IncomBusBackStack mStartMode;
    private IncomingBusStopSelectorAdapter Ibssa;
    private IncomingBusListFragment InBusFragment;
    private ScheduledExecutorService UpdateLoop;
    private String SelectedDate;
    private String SelectedTime;
    private boolean DateTimeSelected = false;
    private BusStops[] SelectedPlaceStopsArray;

    public BottomSheetIncomingBusFragment() {

    }
    public static BottomSheetIncomingBusFragment newInstance(int Place, String Stop, IncomBusBackStack StartMode) {
        BottomSheetIncomingBusFragment fragment = new BottomSheetIncomingBusFragment();
        Bundle args = new Bundle();
        args.putInt(PLACE, Place);
        args.putString(STOP, Stop);
        args.putSerializable(STARTMODE, StartMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getInt(PLACE);
            mStop = getArguments().getString(STOP);
            mStartMode = (IncomBusBackStack) getArguments().getSerializable(STARTMODE);
        }

        mPlaceList = MainActivity.busPlaces;
        mStopList = MainActivity.busStops;

        if (mPlaceList == null) {
            mPlaceList = BusPlaces.getAllBusPlaces(this.getContext());
        }
        if (mStopList == null) {
            mStopList = BusStops.GetAllStops(this.getContext());
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_incoming_bus_view, container, false);

        SelectedDate = mStartMode.getDate();
        SelectedTime = mStartMode.getTime();
        DateTimeSelected = mStartMode.isCustomTime();

        TextView BusStopName = view.findViewById(R.id.BusStopName);

        ImageView ScheduleButton = view.findViewById(R.id.StopScheduleButton);
        ScheduleButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));
        if (getActivity() != null){
            ScheduleButton.setOnClickListener(view1 -> ((MainActivity)getActivity()).ShowSchedule(mStop, null, null, null, false));
        }

        ImageView DateSelectButton = view.findViewById(R.id.StopDateTimeButton);
        if (DateTimeSelected) {
            DateSelectButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DateSelectActive));
        } else {
            DateSelectButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DateSelectInactive));
        }
        DateSelectButton.setOnClickListener(view13 -> OnSelectDateClick());

        List<BusStops> SelectedPlaceStops = new ArrayList<>();

        BusPlaces busPlace = mPlaceList.get(mPlace);

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

        ImageView FavButton = view.findViewById(R.id.StopFavoriteButton);
        if (getContext() != null){
            UserDatabase userDatabase = new UserDatabase(getContext());

            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, mStop)) {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
            } else {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
            }

            String AdEnabled = userDatabase.GetPreference("AdEnabled");
            AdView mAdView = view.findViewById(R.id.adView1);
            if (AdEnabled != null && AdEnabled.equals("true") && HelperProvider.IsAdConsentOk()){

                AdRequest adRequest = new AdRequest.Builder().build();
                mAdView.loadAd(adRequest);
            }else {
                mAdView.setVisibility(View.GONE);
            }

            if (mStop.equals("-1")) {
                FavButton.setVisibility(View.GONE);
                ScheduleButton.setVisibility(View.GONE);
            }

            FavButton.setOnClickListener(view12 -> {
                if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, mStop)) {
                    userDatabase.DeleteFavorite(UserDatabase.FavoriteType.Stop, mStop);
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
                } else {
                    String StopNum = "1";
                    for (BusStops busStops : SelectedPlaceStopsArray) {
                        if (busStops.getId().equals(mStop)) {
                            StopNum = busStops.getStopNum();
                            break;
                        }
                    }

                    String StopName;

                    String DirectionText = HelperProvider.GetStopDirectionString(getContext(),mStop);

                    if (DirectionText.equals("-") || DirectionText.isEmpty() || DirectionText.equals(" ")) {
                        StopName = StopNum.trim();
                    } else {
                        StopName = DirectionText;
                    }

                    userDatabase.AddFavorite(UserDatabase.FavoriteType.Stop, mStop,  getString(R.string.BusStopNameWithNum, BusStopName.getText().toString().trim(), StopName));
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                }
            });
        }

        new Thread(this::SetupStopList).start();

        UpdateDateTimeOnSelector(view);

        StartNewUpdateThread();

        return view;
    }

    private void StartNewUpdateThread() {
        if (UpdateLoop != null) {
            if (!UpdateLoop.isShutdown()){
                return;
            }
        }

        GTFSRProvider GTFSRProvider_ = new GTFSRProvider(this.getActivity());

        UpdateLoop = Executors.newScheduledThreadPool(1);
        UpdateLoop.scheduleAtFixedRate(() -> GetIncomingBuses(GTFSRProvider_), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onStop() {
        super.onStop();

        UpdateLoop.shutdown();
    }

    @Override
    public void onStart() {
        super.onStart();

        ResetList();
        StartNewUpdateThread();
    }

    public void OnSelectDateClick() {
        MaterialDatePicker<Long> DatePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.SelectDate)).setSelection((new Date()).getTime()).build();
        DatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
            sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
            SelectedDate = sdf1.format(new Date(selection));

            SelectTime();
        });
        DatePicker.show(getChildFragmentManager(), "DatePicker");
    }

    @SuppressLint("DefaultLocale")
    public void SelectTime() {
        Calendar Now = Calendar.getInstance();

        int CurrentHour = Now.get(Calendar.HOUR_OF_DAY);
        int CurrentMinute = Now.get(Calendar.MINUTE);

        MaterialTimePicker TimePicker = new  MaterialTimePicker.Builder().setTimeFormat(TimeFormat.CLOCK_24H).setTitleText(getString(R.string.SelectTime)).setInputMode(MaterialTimePicker.INPUT_MODE_CLOCK).setHour(CurrentHour).setMinute(CurrentMinute).build();

        TimePicker.addOnPositiveButtonClickListener(view -> {
            Activity mainActivity = getActivity();

            SelectedTime = String.format("%02d", TimePicker.getHour()) + ":" + String.format("%02d", TimePicker.getMinute());

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
            String CurrentDate = sdf1.format(new Date());

            DateTimeSelected = !SelectedDate.equals(CurrentDate) || CurrentHour != TimePicker.getHour() || (CurrentMinute != TimePicker.getMinute() && CurrentMinute != TimePicker.getMinute() - 1);

            if (mainActivity != null) {
                ((MainActivity)mainActivity).IncBusSelectedDate(new IncomBusBackStack(SelectedDate, SelectedTime, DateTimeSelected));
            }

            UpdateDateTimeOnSelector(getView());

            if (getView() != null) {
                ImageView DateSelectButton = getView().findViewById(R.id.StopDateTimeButton);
                if (DateTimeSelected) {
                    DateSelectButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DateSelectActive));
                } else {
                    DateSelectButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DateSelectInactive));
                }
            }


            new Thread(() -> {
                ResetList();

                GTFSRProvider GTFSRProvider_ = new GTFSRProvider(getActivity());
                GetIncomingBuses(GTFSRProvider_);
            }).start();
        });

        TimePicker.show(getChildFragmentManager(), "TimePicker");
    }

    @SuppressLint("NotifyDataSetChanged")
    public void OnStopClick(String Id) {
        if (getActivity() != null){
            ((MainActivity)getActivity()).ChangeStop(Id);
        }

        mStop = Id;
        Ibssa.setSelectedStop(mStop);
        Ibssa.notifyDataSetChanged();

        ResetList();

        if (getView() != null && getContext() != null){
            ImageView FavButton = getView().findViewById(R.id.StopFavoriteButton);
            ImageView ScheduleButton = getView().findViewById(R.id.StopScheduleButton);

            if (mStop.equals("-1")) {
                FavButton.setVisibility(View.GONE);
                ScheduleButton.setVisibility(View.GONE);
            } else {
                FavButton.setVisibility(View.VISIBLE);
                ScheduleButton.setVisibility(View.VISIBLE);

                UserDatabase userDatabase = new UserDatabase(getContext());
                if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, mStop)) {
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                } else {
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
                }
            }
        }

        new Thread(() -> {
            GTFSRProvider GTFSRProvider_ = new GTFSRProvider(getActivity());
            GetIncomingBuses(GTFSRProvider_);
        }).start();
    }

    private void ResetList() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.BusListFragment, new LoadingFragment())
                .commit();
        InBusFragment = null;
    }

    private void UpdateDateTimeOnSelector(View view) {
        if (view != null) {
            TextView DateText = view.findViewById(R.id.BusStopDate);
            if (DateText != null) {
                if (DateTimeSelected) {
                    StringBuilder str = new StringBuilder(SelectedDate);

                    str.insert(6, ". ");
                    str.insert(4, ". ");

                    DateText.setText(getString(R.string.IncBusAnotherDay, str + ". " + SelectedTime));
                } else {
                    Calendar Now = Calendar.getInstance();

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.US);

                    DateText.setText(formatter.format(Now.getTime()));
                }
            }
        }
    }

    private void SetupStopList () {
        Context ctx = getContext();
        View view = getView();
        Activity activity = getActivity();
        BottomSheetIncomingBusFragment bottomSheetIncomingBusFragment = this;

        if (ctx != null && view != null && activity != null) {
            String[] StopNames = new String[SelectedPlaceStopsArray.length];

            for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
                StopNames[i] = HelperProvider.GetStopDirectionString(ctx,SelectedPlaceStopsArray[i].getId());
            }

            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    Ibssa = new IncomingBusStopSelectorAdapter(SelectedPlaceStopsArray,mStop, bottomSheetIncomingBusFragment, StopNames, ctx);

                    RecyclerView StopSelectorRec = view.findViewById(R.id.BusStopListRecView);
                    LinearLayoutManager mLayoutManager = new LinearLayoutManager(ctx);
                    mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

                    StopSelectorRec.setLayoutManager(mLayoutManager);
                    StopSelectorRec.setAdapter(Ibssa);
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

                    if (mStop.equals("-1")) {
                        scrollPosition = SelectedPlaceStopsArray.length;
                    } else {
                        for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
                            if (SelectedPlaceStopsArray[i].getId().equals(mStop)) {
                                scrollPosition = i;
                                break;
                            }
                        }
                    }

                    smoothScroller.setTargetPosition(scrollPosition);
                    if (StopSelectorRec.getLayoutManager() != null) {
                        StopSelectorRec.getLayoutManager().startSmoothScroll(smoothScroller);
                    }

                    StopSelectorRec.setNestedScrollingEnabled(false);
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

    private void GetIncomingBuses(GTFSRProvider GTFSRProvider_) {
        try {
            final String SendStopId = mStop;
            final String SendDate = SelectedDate;
            final String SendTime = SelectedTime;
            final boolean SendCustom = DateTimeSelected;

            IncomingBusRespModel[] BusList = null;

            MainActivity mainActivity = (MainActivity)getActivity();
            SimpleDateFormat sdf3 = new SimpleDateFormat("yyyyMMdd", Locale.US);

            if (DateTimeSelected) {
                if (mainActivity != null) {
                    if (mStop.equals("-1")) {
                        ArrayList<IncomingBusRespModel> list = null;
                        if (sdf3.format(new Date()).equals(SelectedDate)) {
                            list = new ArrayList<>();

                            for (BusStops element : SelectedPlaceStopsArray) {
                                IncomingBusRespModel[] Data = GTFSRProvider_.getNextIncomingBuses(element.getId(), SelectedDate, SelectedTime);
                                if (Data != null) {
                                    list.addAll(Arrays.asList(Data));
                                } else {
                                    list = null;
                                    break;
                                }
                            }
                        }

                        if (list != null) {
                            Collections.sort(list, (o1, o2) -> o1.getArriveTime().compareTo(o2.getArriveTime()));

                            BusList = new IncomingBusRespModel[list.size()];
                            list.toArray(BusList);
                        } else {
                            if(HelperProvider.displayOfflineText() && sdf3.format(new Date()).equals(SelectedDate)) {
                                mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                                HelperProvider.setOfflineTextDisplayed();
                            }

                            list = new ArrayList<>();
                            NewGTFSDatabase NDm = new NewGTFSDatabase(mainActivity);

                            for (BusStops element : SelectedPlaceStopsArray) {
                                list.addAll(Arrays.asList(NDm.GetOfflineDepartureTimes(element.getId(), SelectedDate, SelectedTime)));
                            }

                            Collections.sort(list, (o1, o2) -> o1.getArriveTime().compareTo(o2.getArriveTime()));

                            BusList = new IncomingBusRespModel[list.size()];
                            list.toArray(BusList);
                        }
                    } else {
                        if (sdf3.format(new Date()).equals(SelectedDate)) {
                            BusList = GTFSRProvider_.getNextIncomingBuses(SendStopId, SelectedDate, SelectedTime);
                        }

                        if (BusList == null) {
                            if(HelperProvider.displayOfflineText() && sdf3.format(new Date()).equals(SelectedDate)) {
                                mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                                HelperProvider.setOfflineTextDisplayed();
                            }

                            NewGTFSDatabase NDm = new NewGTFSDatabase(mainActivity);
                            BusList = NDm.GetOfflineDepartureTimes(SendStopId, SelectedDate, SelectedTime);
                        }
                    }
                }
            } else {
                if (mStop.equals("-1")) {
                    ArrayList<IncomingBusRespModel> list = new ArrayList<>();

                    for (BusStops element : SelectedPlaceStopsArray) {
                        IncomingBusRespModel[] Data = GTFSRProvider_.getNextIncomingBuses(element.getId());
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
                    BusList = GTFSRProvider_.getNextIncomingBuses(mStop);
                }
            }

            if (mainActivity != null) {
                mainActivity.runOnUiThread(() -> UpdateDateTimeOnSelector(getView()));
            }

            Date currentTime = Calendar.getInstance().getTime();

            if (BusList == null) {
                if(mainActivity != null && HelperProvider.displayOfflineText()) {
                    mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                    HelperProvider.setOfflineTextDisplayed();
                }
                if (mainActivity != null) {
                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
                    SimpleDateFormat Sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
                    String CurrentDate = sdf1.format(new Date());
                    String CurrentTime = Sdf2.format(new Date());

                    if (mStop.equals("-1")) {
                        ArrayList<IncomingBusRespModel> list = new ArrayList<>();
                        NewGTFSDatabase NDm = new NewGTFSDatabase(mainActivity);

                        for (BusStops element : SelectedPlaceStopsArray) {
                            list.addAll(Arrays.asList(NDm.GetOfflineDepartureTimes(element.getId(), CurrentDate, CurrentTime)));
                        }

                        Collections.sort(list, (o1, o2) -> o1.getArriveTime().compareTo(o2.getArriveTime()));

                        BusList = new IncomingBusRespModel[list.size()];
                        list.toArray(BusList);
                    } else {
                        NewGTFSDatabase NDm = new NewGTFSDatabase(mainActivity);

                        BusList = NDm.GetOfflineDepartureTimes(SendStopId, CurrentDate, CurrentTime);
                    }
                }
            }

            SimpleDateFormat Sdf = new SimpleDateFormat("H", Locale.US);
            SimpleDateFormat Sdf2 = new SimpleDateFormat("m", Locale.US);

            if (!DateTimeSelected || sdf3.format(new Date()).equals(SelectedDate)) {
                if (BusList != null) {

                    Calendar Now = Calendar.getInstance();
                    for (IncomingBusRespModel incomingBusRespModel : BusList) {
                        incomingBusRespModel.setMiss(false);

                        BusLine Bj = BusLine.BusLinesByLineId(incomingBusRespModel.getLineId(), false, null, mainActivity);

                        if (Bj != null) {
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
            }

            if(!SendStopId.equals(mStop) || !SendDate.equals(SelectedDate) || !SendTime.equals(SelectedTime) || SendCustom != DateTimeSelected) {
                return;
            }

            if (BusList != null && BusList.length > 0) {
                NewGTFSDatabase NDm = new NewGTFSDatabase(mainActivity);

                for (IncomingBusRespModel incomingBusRespModel : BusList) {
                    String newName = NDm.GetBusNameByStop(incomingBusRespModel.getLineId(),incomingBusRespModel.getArriveStop());
                    if (newName != null) {
                        incomingBusRespModel.setLineName(newName);
                    }
                }

                if (InBusFragment == null) {
                    try {
                        InBusFragment = IncomingBusListFragment.newInstance(BusList, SelectedDate, DateTimeSelected);
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.BusListFragment, InBusFragment)
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
            } else {
                InBusFragment = null;
                if (mainActivity != null) {
                    NewGTFSDatabase NDm = new NewGTFSDatabase(mainActivity);
                    if (DateTimeSelected && !NDm.GetBusDatabaseValidDate(SelectedDate)) {
                        InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.DatabaseNotContain), -1);
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.BusListFragment, Fragment)
                                .commit();
                    } else {
                        InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.EmptyList), -1);
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.BusListFragment, Fragment)
                                .commit();
                    }
                } else {
                    InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.EmptyList), -1);
                    getChildFragmentManager().beginTransaction()
                            .replace(R.id.BusListFragment, Fragment)
                            .commit();
                }
            }

            BusPositionRespModel[] BusPositions = GTFSRProvider_.getALLBusLocation();
            if (BusPositions != null) {
                if (mainActivity != null) {
                    mainActivity.runOnUiThread(() -> mainActivity.BusPositionMarkers(BusPositions));
                }
            } else {
                if (mainActivity != null) {
                    mainActivity.runOnUiThread(() -> mainActivity.BusPositionMarkers(new BusPositionRespModel[0]));
                }
            }


        } catch (Exception e) {
            Log.e("Update bus list error", e.toString());
            e.printStackTrace();
        }
    }
}