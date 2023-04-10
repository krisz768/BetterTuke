package hu.krisz768.bettertuke.IncomingBusFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
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

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.timepicker.MaterialTimePicker;
import com.google.android.material.timepicker.TimeFormat;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.BuildConfig;
import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.InfoFragment;
import hu.krisz768.bettertuke.LoadingFragment;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;
import hu.krisz768.bettertuke.models.IncomBusBackStack;

public class BottomSheetIncomingBusFragment extends Fragment {
    private static final String PLACE = "Place";
    private static final String STOP = "Stop";
    private static final String STARTMODE = "StartMode";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";

    private int mPlace;
    private volatile int mStop;
    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<Integer, BusStops> mStopList;
    private IncomBusBackStack mStartMode;

    private IncomingBusStopSelectorAdapter Ibssa;
    private IncomingBusListFragment InBusFragment;

    private ScheduledExecutorService UpdateLoop;

    private String SelectedDate;
    private String SelectedTime;
    private boolean DateTimeSelected = false;

    public BottomSheetIncomingBusFragment() {

    }
    public static BottomSheetIncomingBusFragment newInstance(int Place, int Stop, IncomBusBackStack StartMode, HashMap<Integer, BusPlaces> PlaceList, HashMap<Integer, BusStops> StopList) {
        BottomSheetIncomingBusFragment fragment = new BottomSheetIncomingBusFragment();
        Bundle args = new Bundle();
        args.putInt(PLACE, Place);
        args.putInt(STOP, Stop);
        args.putSerializable(PLACELIST, PlaceList);
        args.putSerializable(STOPLIST, StopList);
        args.putSerializable(STARTMODE, StartMode);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getInt(PLACE);
            mStop = getArguments().getInt(STOP);
            mPlaceList = (HashMap<Integer, BusPlaces>) getArguments().getSerializable(PLACELIST);
            mStopList = (HashMap<Integer, BusStops>) getArguments().getSerializable(STOPLIST);
            mStartMode = (IncomBusBackStack) getArguments().getSerializable(STARTMODE);
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

        BusStops[] SelectedPlaceStopsArray = new BusStops[SelectedPlaceStops.size()];
        SelectedPlaceStops.toArray(SelectedPlaceStopsArray);

        ImageView FavButton = view.findViewById(R.id.StopFavoriteButton);
        if (getContext() != null){
            UserDatabase userDatabase = new UserDatabase(getContext());

            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop))) {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
            } else {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
            }

            FavButton.setOnClickListener(view12 -> {
                if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop))) {
                    userDatabase.DeleteFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop));
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
                } else {
                    String StopNum = "1";
                    for (BusStops busStops : SelectedPlaceStopsArray) {
                        if (busStops.getId() == mStop) {
                            StopNum = busStops.getStopNum();
                            break;
                        }
                    }

                    userDatabase.AddFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop),  getString(R.string.BusStopNameWithNum, BusStopName.getText().toString().trim(), StopNum.trim()));
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                }
            });
        }

        Ibssa = new IncomingBusStopSelectorAdapter(SelectedPlaceStopsArray,mStop, this,getContext());

        RecyclerView StopSelectorRec = view.findViewById(R.id.BusStopListRecView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        StopSelectorRec.setLayoutManager(mLayoutManager);
        StopSelectorRec.setAdapter(Ibssa);
        StopSelectorRec.setItemAnimator(null);

        UpdateDateTimeOnSelector(view);

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

        int scrollPosition = 0;
        for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
            if (SelectedPlaceStopsArray[i].getId() == mStop) {
                scrollPosition = i;
                break;
            }
        }

        smoothScroller.setTargetPosition(scrollPosition);
        if (StopSelectorRec.getLayoutManager() != null) {
            StopSelectorRec.getLayoutManager().startSmoothScroll(smoothScroller);
        }

        StopSelectorRec.setNestedScrollingEnabled(false);

        StartNewUpdateThread();

        return view;
    }

    private void StartNewUpdateThread() {
        if (UpdateLoop != null) {
            if (!UpdateLoop.isShutdown()){
                return;
            }
        }

        TukeServerApi serverApi = new TukeServerApi(this.getActivity());

        UpdateLoop = Executors.newScheduledThreadPool(1);
        UpdateLoop.scheduleAtFixedRate(() -> GetIncomingBuses(serverApi), 0, 10, TimeUnit.SECONDS);
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
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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

            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
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

                TukeServerApi serverApi = new TukeServerApi(getActivity());
                GetIncomingBuses(serverApi);
            }).start();
        });

        TimePicker.show(getChildFragmentManager(), "TimePicker");
    }

    @SuppressLint("NotifyDataSetChanged")
    public void OnStopClick(int Id) {
        if (getActivity() != null){
            ((MainActivity)getActivity()).ChangeStop(Id);
        }

        mStop = Id;
        Ibssa.setSelectedStop(mStop);
        Ibssa.notifyDataSetChanged();

        ResetList();

        if (getView() != null && getContext() != null){
            ImageView FavButton = getView().findViewById(R.id.StopFavoriteButton);

            UserDatabase userDatabase = new UserDatabase(getContext());
            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop))) {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
            } else {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
            }
        }

        new Thread(() -> {
            TukeServerApi serverApi = new TukeServerApi(getActivity());
            GetIncomingBuses(serverApi);
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
                    DateText.setText(getString(R.string.IncBusAnotherDay, SelectedDate.replace("-", ". ") + ". " + SelectedTime));
                } else {
                    Calendar Now = Calendar.getInstance();

                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd. HH:mm", Locale.US);

                    DateText.setText(formatter.format(Now.getTime()));
                }
            }
        }
    }

    private void GetIncomingBuses(TukeServerApi serverApi) {
        try {
            if (BuildConfig.DEBUG) {
                Log.i("Update", "Updating List");
            }

            final int SendStopId = mStop;
            final String SendDate = SelectedDate;
            final String SendTime = SelectedTime;
            final boolean SendCustom = DateTimeSelected;

            IncomingBusRespModel[] BusList = null;

            MainActivity mainActivity = (MainActivity)getActivity();

            if (DateTimeSelected) {
                if (mainActivity != null) {
                    DatabaseManager Dm = new DatabaseManager(mainActivity);
                    BusList = Dm.GetOfflineDepartureTimes(SendStopId, SelectedDate, SelectedTime);
                }
            } else {
                BusList = serverApi.getNextIncomingBuses(mStop);
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
                    DatabaseManager Dm = new DatabaseManager(mainActivity);

                    SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat Sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
                    String CurrentDate = sdf1.format(new Date());
                    String CurrentTime = Sdf2.format(new Date());

                    BusList = Dm.GetOfflineDepartureTimes(SendStopId, CurrentDate, CurrentTime);
                }
            }


            SimpleDateFormat Sdf = new SimpleDateFormat("H", Locale.US);
            SimpleDateFormat Sdf2 = new SimpleDateFormat("m", Locale.US);
            SimpleDateFormat sdf3 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);

            if (!DateTimeSelected || sdf3.format(new Date()).equals(SelectedDate)) {
                if (BusList != null) {

                    Calendar Now = Calendar.getInstance();
                    for (IncomingBusRespModel incomingBusRespModel : BusList) {
                        incomingBusRespModel.setMiss(false);

                        BusLine Bj = BusLine.BusLinesByLineId(incomingBusRespModel.getLineId(), mainActivity);

                        if (Bj.getDepartureHour() < Integer.parseInt(Sdf.format(currentTime)) || (Bj.getDepartureHour() == Integer.parseInt(Sdf.format(currentTime)) && Bj.getDepartureMinute() <= Integer.parseInt(Sdf2.format(currentTime)))) {
                            Boolean IsBusStarted = serverApi.getIsBusHasStarted(incomingBusRespModel.getLineId());
                            if (IsBusStarted == null) {
                                IsBusStarted = false;
                            }
                            incomingBusRespModel.setStarted(IsBusStarted);
                            if (!IsBusStarted && ((Bj.getDepartureHour() == Integer.parseInt(Sdf.format(currentTime)) && Bj.getDepartureMinute() < Integer.parseInt(Sdf2.format(currentTime))) || Bj.getDepartureHour() < Integer.parseInt(Sdf.format(currentTime)))) {
                                Calendar ArrTime = Calendar.getInstance();
                                ArrTime.setTime(incomingBusRespModel.getArriveTime());

                                if (ArrTime.after(Now)) {
                                    incomingBusRespModel.setMiss(true);
                                }
                            }
                        } else {
                            incomingBusRespModel.setStarted(false);
                        }
                    }
                }
            }

            if(SendStopId != mStop || !SendDate.equals(SelectedDate) || !SendTime.equals(SelectedTime) || SendCustom != DateTimeSelected) {
                return;
            }

            if (BusList != null && BusList.length > 0) {
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

                InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.EmptyList), -1);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.BusListFragment, Fragment)
                        .commit();
            }
        } catch (Exception e) {
            Log.e("Update bus list error", e.toString());
            e.printStackTrace();
        }
    }
}