package hu.krisz768.bettertuke.ScheduleFragment;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentContainerView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.datepicker.MaterialDatePicker;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.BusVariation;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.InfoFragment;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.ScheduleActivity;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

public class ScheduleBusTimeFragment extends Fragment {
    private static final String LINENUM= "lineNum";
    private static final String STOPID= "StopId";
    private static final String DIRECTION= "Direction";
    private static final String DATE= "Date";

    private String mLineNum;
    private int mStopId;
    private String mDirection;
    private String mDate;

    private String SelectedDate;
    private String SelectedWay;
    private boolean TwoWay = false;
    private String WayDescStringForward = "";
    private String WayDescStringBackwards = "";
    private BusScheduleTime[] CurrentLines;
    private int[] CurrentHours;
    private int[][] CurrentMinutes;

    private int colorOnPrimary;
    private int colorPrimaryContainer;
    private int colorOnPrimaryContainer;
    private int colorSec;
    private int colorOnSecContainer;
    private int colorSecContainer;
    private int colorOnError;
    private Drawable MinuteBackground;
    private Drawable MinuteBackgroundFull;
    private Drawable MinuteBackgroundStarted;
    private Drawable MinuteBackgroundFullStarted;
    private Drawable MinuteBackgroundErr;
    private Drawable MinuteBackgroundFullErr;

    private ScheduleBusTimeHourAdapter Sbta;

    private BusVariation[] Variations;

    public ScheduleBusTimeFragment() {

    }

    public static ScheduleBusTimeFragment newInstance(String lineNum, int StopId, String Direction, String Date) {
        ScheduleBusTimeFragment fragment = new ScheduleBusTimeFragment();
        Bundle args = new Bundle();
        args.putString(LINENUM, lineNum);
        args.putInt(STOPID, StopId);
        args.putString(DIRECTION, Direction);
        args.putString(DATE, Date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLineNum = getArguments().getString(LINENUM);
            mStopId = getArguments().getInt(STOPID);
            mDirection = getArguments().getString(DIRECTION);
            mDate = getArguments().getString(DATE);
        }

        SelectedDate = mDate;
        SelectedWay = mDirection;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_time, container, false);

        LoadColorsAndBackgrounds();

        setColors(view);

        TextView NumText = view.findViewById(R.id.ScheduleBusLineNum);
        NumText.setText(mLineNum);

        if (getContext() == null) {
            return view;
        }

        DatabaseManager Dm = new DatabaseManager(getContext());

        TextView StartPosText = view.findViewById(R.id.StartPosText);
        if (mStopId == -1) {
            StartPosText.setText(getString(R.string.StartingDataFromStartingPosition));
        } else {
            String StopName = Dm.GetStopName(mStopId);
            String StopNum = HelperProvider.GetStopDirectionString(getContext(), mStopId);
            StartPosText.setText(getString(R.string.StartingDataFromStop, StopName.trim(), StopNum));
        }

        TextView SelectedDateText = view.findViewById(R.id.ScheduleBusLineDate);
        TextView DescText = view.findViewById(R.id.ScheduleBusLineDesc);

        ImageView BusLineTimeDirectionIcon = view.findViewById(R.id.BusLineTimeDirectionIcon);

        if (mStopId == -1) {
            Variations = Dm.GetBusVariations(mLineNum);
        } else {
            Variations = Dm.GetBusVariationsFromStop(mLineNum, mStopId);
        }

        boolean IsForwardWayDescTextSet = false;
        boolean IsBackWayDescTextSet = false;
        for (BusVariation variation : Variations) {
            if (!IsForwardWayDescTextSet && variation.getDirection().equals("O")) {
                IsForwardWayDescTextSet = true;
                WayDescStringForward = variation.getName();
                if (SelectedWay.equals("O")) {
                    DescText.setText(variation.getName());
                }
            }
            if (!IsBackWayDescTextSet && !variation.getDirection().equals("O")) {
                IsBackWayDescTextSet = true;
                WayDescStringBackwards = variation.getName();
                if (!SelectedWay.equals("O")) {
                    DescText.setText(variation.getName());
                }
            }
        }

        if (IsForwardWayDescTextSet && IsBackWayDescTextSet) {
            TwoWay = true;
        }

        if (!IsForwardWayDescTextSet && IsBackWayDescTextSet) {
            DescText.setText(WayDescStringBackwards);
            SelectedWay = "V";
        }

        if (mStopId != -1 && !TwoWay) {
            BusLineTimeDirectionIcon.setVisibility(View.GONE);
        }

        ImageView BusLineTimeFavIcon = view.findViewById(R.id.BusLineTimeFavIcon);

        UserDatabase userDatabase = new UserDatabase(getContext());

        if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Line, mLineNum)) {
            BusLineTimeFavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
        } else {
            BusLineTimeFavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
        }

        BusLineTimeFavIcon.setOnClickListener(view1 -> {
            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Line, mLineNum)) {
                userDatabase.DeleteFavorite(UserDatabase.FavoriteType.Line, mLineNum);
                BusLineTimeFavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
            } else {
                userDatabase.AddFavorite(UserDatabase.FavoriteType.Line, mLineNum, mLineNum);
                BusLineTimeFavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
            }
        });

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd.", Locale.US);
        SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = new Date();
        try {
            date = formatter2.parse(SelectedDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        assert date != null;
        SelectedDateText.setText(formatter.format(date));

        SelectedDateText.setOnClickListener(view12 -> ShowDatePicker());

        BusLineTimeDirectionIcon.setOnClickListener(view13 -> {
            if (TwoWay) {
                if (SelectedWay.equals("O")) {
                    SelectedWay = "V";
                    DescText.setText(WayDescStringBackwards);
                } else {
                    SelectedWay = "O";
                    DescText.setText(WayDescStringForward);
                }
            }
            ReloadSchedules(getView());
        });

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                () -> {
                    ReloadSchedules(view);

                    ScrollToCurrentHour(view);

                    swipeRefreshLayout.setRefreshing(false);
                }
        );

        swipeRefreshLayout.setVisibility(View.GONE);

        ReloadSchedules(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (Sbta != null) {
            ReloadSchedules(getView());
        }
    }

    private void LoadColorsAndBackgrounds() {
        TypedValue typedValue = new TypedValue();
        if (getContext() == null) {
            return;
        }

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        int colorPrimary = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        colorOnPrimary = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
        colorPrimaryContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimaryContainer, typedValue, true);
        colorOnPrimaryContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        colorSec = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        int colorOnSec = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true);
        colorSecContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true);
        colorOnSecContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true);
        int colorErr = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnError, typedValue, true);
        colorOnError = ContextCompat.getColor(getContext(), typedValue.resourceId);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int StrokeWidth = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2,
                displayMetrics
        ));

        MinuteBackground = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        if (MinuteBackground != null) {
            ((GradientDrawable)(((LayerDrawable)MinuteBackground).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
            ((GradientDrawable)(((LayerDrawable)MinuteBackground).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorOnSec);
        }


        MinuteBackgroundFull = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        if (MinuteBackgroundFull != null) {
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFull).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFull).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorOnSec);
        }


        MinuteBackgroundStarted = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        if (MinuteBackgroundStarted != null) {
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorPrimary);
        }

        MinuteBackgroundFullStarted = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        if (MinuteBackgroundFullStarted != null) {
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorPrimary);
        }

        MinuteBackgroundErr = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        if (MinuteBackgroundErr != null) {
            ((GradientDrawable) (((LayerDrawable) MinuteBackgroundErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
            ((GradientDrawable) (((LayerDrawable) MinuteBackgroundErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorErr);
        }


        MinuteBackgroundFullErr = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        if (MinuteBackgroundFullErr != null) {
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
            ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorErr);
        }
    }

    private void setColors(View view) {
        TextView NumText = view.findViewById(R.id.ScheduleBusLineNum);
        NumText.setTextColor(colorOnPrimary);

        View ScheduleBusTimeFirstBar = view.findViewById(R.id.ScheduleBusTimeFirstBar);
        ((GradientDrawable)ScheduleBusTimeFirstBar.getBackground()).setColor(colorPrimaryContainer);

        View ScheduleBusTimeSecBar = view.findViewById(R.id.ScheduleBusTimeSecBar);
        ((GradientDrawable)ScheduleBusTimeSecBar.getBackground()).setColor(colorPrimaryContainer);

        TextView ScheduleBusLineDesc = view.findViewById(R.id.ScheduleBusLineDesc);
        ScheduleBusLineDesc.setTextColor(colorOnPrimaryContainer);

        TextView ScheduleBusLineDate = view.findViewById(R.id.ScheduleBusLineDate);
        ScheduleBusLineDate.setTextColor(colorOnPrimaryContainer);
    }

    private void ShowDatePicker() {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        try {
            date = sdf.parse(SelectedDate);
        }catch (Exception ignored) {

        }

        assert date != null;
        MaterialDatePicker<Long> DatePicker = MaterialDatePicker.Builder.datePicker().setTitleText(getString(R.string.SelectDate)).setSelection(date.getTime()).build();
        DatePicker.addOnPositiveButtonClickListener(selection -> {
            SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf1.setTimeZone(TimeZone.getTimeZone("UTC"));
            SelectedDate = sdf1.format(new Date(selection));

            if (getView() != null) {
                TextView SelectedDateText = getView().findViewById(R.id.ScheduleBusLineDate);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd.", Locale.US);
                SelectedDateText.setText(formatter.format(new Date(selection)));

                ReloadSchedules(getView());
            }
        });
        DatePicker.show(getChildFragmentManager(), "DatePicker");
    }

    @SuppressLint("NotifyDataSetChanged")
    private void ReloadSchedules(View view) {
        final ScheduleBusTimeFragment Callback = this;

        new Thread(() -> {
            if (getContext() == null) {
                return;
            }
            DatabaseManager Dm = new DatabaseManager(getContext());

            ScheduleActivity scheduleActivity = (ScheduleActivity) getActivity();
            if (scheduleActivity == null) {
                return;
            }

            scheduleActivity.runOnUiThread(() -> {
                ImageView BusLineTimeDirectionIcon = view.findViewById(R.id.BusLineTimeDirectionIcon);
                if (TwoWay && SelectedWay.equals("O")) {
                    BusLineTimeDirectionIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DirectionForward));
                } else if (TwoWay && SelectedWay.equals("V")){
                    BusLineTimeDirectionIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DirectionBackwards));
                } else {
                    BusLineTimeDirectionIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DirectionOneWay));
                }
            });

            if (mStopId == -1) {
                CurrentLines = Dm.GetBusScheduleTimeFromStart(mLineNum, SelectedDate, SelectedWay);
            } else {
                CurrentLines = Dm.GetBusScheduleTimeFromStop(mLineNum, SelectedDate, SelectedWay, mStopId);

                for (BusScheduleTime busScheduleTime : CurrentLines) {
                    int StopDelta = Dm.GetBusLineStopTravelTimeById(Integer.toString(busScheduleTime.getLineId()), mStopId);
                    busScheduleTime.AdjustToStop(StopDelta);
                }
            }

            if(CurrentLines.length == 0) {

                if (getActivity() != null) {
                    getActivity().runOnUiThread(() -> {
                        try {
                            if (Dm.GetBusDatabaseValidDate(SelectedDate)) {
                                InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.EmptySchedule), -1);

                                getChildFragmentManager().beginTransaction()
                                        .replace(R.id.ScheduleTimeFragmentContainer, Fragment)
                                        .commit();
                            } else {
                                InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.DatabaseNotContain), -1);

                                getChildFragmentManager().beginTransaction()
                                        .replace(R.id.ScheduleTimeFragmentContainer, Fragment)
                                        .commit();
                            }

                            SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
                            swipeRefreshLayout.setVisibility(View.GONE);

                            FragmentContainerView fragmentContainerView = view.findViewById(R.id.ScheduleTimeFragmentContainer);
                            fragmentContainerView.setVisibility(View.VISIBLE);

                            Sbta = null;
                        } catch (Exception e) {

                        }
                    });
                }
                return;
            }

            List<Integer> HoursList = new ArrayList<>();

            int CurrentHour = -1;
            for (BusScheduleTime busScheduleTime : CurrentLines) {
                if (busScheduleTime.getHour() != CurrentHour) {
                    HoursList.add(busScheduleTime.getHour());
                    CurrentHour = busScheduleTime.getHour();
                }
            }

            CurrentHours = new int[HoursList.size()];

            for (int i = 0; i < HoursList.size(); i++) {
                CurrentHours[i] = HoursList.get(i);
            }

            CurrentMinutes = new int[HoursList.size()][];
            List<Integer> TempMinutes = new ArrayList<>();

            CurrentHour = -1;
            int HourIndex = -1;
            for (BusScheduleTime busScheduleTime : CurrentLines) {
                if (busScheduleTime.getHour() != CurrentHour) {
                    if (HourIndex != -1) {
                        CurrentMinutes[HourIndex] = new int[TempMinutes.size()];
                        for (int j = 0; j < TempMinutes.size(); j++) {
                            CurrentMinutes[HourIndex][j] = TempMinutes.get(j);
                        }
                    }
                    HourIndex++;

                    CurrentHour = busScheduleTime.getHour();
                    TempMinutes = new ArrayList<>();
                    TempMinutes.add(busScheduleTime.getMinute());
                } else {
                    TempMinutes.add(busScheduleTime.getMinute());
                }
            }

            if (TempMinutes.size() > 0) {
                CurrentMinutes[HourIndex] = new int[TempMinutes.size()];
                for (int j = 0; j < TempMinutes.size(); j++) {
                    CurrentMinutes[HourIndex][j] = TempMinutes.get(j);
                }
            }

            String[][] BusCodes = new String[CurrentHours.length][];

            int Counter = 0;
            for (int i = 0; i < CurrentHours.length; i++) {
                BusCodes[i] = new String[CurrentMinutes[i].length];
                for (int j = 0; j < CurrentMinutes[i].length; j++){
                    BusCodes[i][j] = CurrentLines[Counter].getLineCode().replace(mLineNum, "");
                    Counter++;
                }
            }

            DisplayMetrics displayMetrics = new DisplayMetrics();
            scheduleActivity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            int width = displayMetrics.widthPixels;

            int ndp = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    75,
                    displayMetrics
            ));

            int MinuteWidth = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    50,
                    displayMetrics
            ));

            RecyclerView Recv = view.findViewById(R.id.ScheduleTimeHoursRecView);

            scheduleActivity.runOnUiThread(() -> {
                SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefresh);

                if (Sbta == null) {
                    swipeRefreshLayout.setVisibility(View.VISIBLE);

                    FragmentContainerView fragmentContainerView = view.findViewById(R.id.ScheduleTimeFragmentContainer);
                    fragmentContainerView.setVisibility(View.GONE);

                    Sbta = new ScheduleBusTimeHourAdapter(CurrentHours, CurrentMinutes,BusCodes, Variations, (int) Math.floor((float)(width-ndp)/(float) MinuteWidth), colorSec, colorSecContainer, colorOnSecContainer, colorOnPrimary, colorOnError, MinuteBackground, MinuteBackgroundFull, MinuteBackgroundStarted, MinuteBackgroundFullStarted, MinuteBackgroundErr, MinuteBackgroundFullErr, getContext(), Callback);
                    RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(scheduleActivity);
                    Recv.setLayoutManager(mLayoutManager);
                    Recv.setAdapter(Sbta);

                    new Thread(() -> scheduleActivity.runOnUiThread(() -> ScrollToCurrentHour(view))).start();
                } else {
                    Sbta.UpdateData(CurrentHours, CurrentMinutes, BusCodes);
                    Sbta.notifyDataSetChanged();
                    new Thread(() -> scheduleActivity.runOnUiThread(() -> ScrollToCurrentHour(view))).start();
                }

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                Date date = new Date();
                if (SelectedDate.equals(formatter.format(date))) {
                    GetLiveData(CurrentLines);
                }else {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }).start();
    }

    private void ScrollToCurrentHour(View view) {
        Calendar Now = Calendar.getInstance();

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = new Date();

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
                return super.calculateSpeedPerPixel(displayMetrics) *2;
            }
        };

        int NowHour = Now.get(Calendar.HOUR_OF_DAY);
        smoothScroller.setTargetPosition(0);

        if (SelectedDate.equals(formatter.format(date))) {
            for (int i = 0; i < CurrentHours.length; i++) {
                if ( CurrentHours[i] >= NowHour) {
                    if (i > 0) {
                        smoothScroller.setTargetPosition(i-1);
                    }
                    break;
                }
            }
        }

        RecyclerView Recv = view.findViewById(R.id.ScheduleTimeHoursRecView);
        if (Recv.getLayoutManager() != null) {
            Recv.getLayoutManager().startSmoothScroll(smoothScroller);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void GetLiveData(BusScheduleTime[] Lines) {
        if (getContext() == null) {
            return;
        }

        Context ctx = getContext();
        new Thread(() -> {
            Calendar Now = Calendar.getInstance();

            int CurrentHour = Now.get(Calendar.HOUR_OF_DAY);
            int CurrentMinute = Now.get(Calendar.MINUTE);

            List<BusScheduleTime> StartedList = new ArrayList<>();
            List<BusScheduleTime> ErrNotStartedList = new ArrayList<>();

            for (BusScheduleTime busScheduleTime : Lines) {
                if (mStopId == -1) {
                    if ((busScheduleTime.getHour() == CurrentHour && CurrentMinute >= busScheduleTime.getMinute()) || busScheduleTime.getHour() + 1 == CurrentHour || busScheduleTime.getHour() + 2 == CurrentHour) {
                        TukeServerApi tukeServerApi = new TukeServerApi(ctx);
                        Boolean IsStarted = tukeServerApi.getIsBusHasStarted(busScheduleTime.getLineId());
                        if (IsStarted == null){
                            if(HelperProvider.displayOfflineText()){
                                Activity activity = getActivity();
                                if(activity != null) {
                                    activity.runOnUiThread(() -> Toast.makeText(activity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                                }
                                HelperProvider.setOfflineTextDisplayed();
                            }
                            continue;
                        }

                        if (IsStarted) {
                            StartedList.add(busScheduleTime);
                        } else {
                            DatabaseManager Dm = new DatabaseManager(ctx);
                            int TravelTimeMin = Dm.GetBusLineSumTravelTimeById(Integer.toString(busScheduleTime.getLineId()));

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date());
                            calendar.set(Calendar.HOUR_OF_DAY, busScheduleTime.getHour());
                            calendar.set(Calendar.MINUTE, busScheduleTime.getMinute());

                            calendar.add(Calendar.MINUTE, TravelTimeMin - 2);

                            if (calendar.after(Now) && ((busScheduleTime.getHour() == CurrentHour && CurrentMinute > busScheduleTime.getMinute()) || busScheduleTime.getHour() + 1 == CurrentHour || busScheduleTime.getHour() + 2 == CurrentHour)) {
                                ErrNotStartedList.add(busScheduleTime);
                            }
                        }
                    }
                } else {
                    if (busScheduleTime.getHour() == CurrentHour || busScheduleTime.getHour() + 1 == CurrentHour || busScheduleTime.getHour() - 1 == CurrentHour) {
                        TukeServerApi tukeServerApi = new TukeServerApi(ctx);
                        Boolean IsStarted = tukeServerApi.getIsBusHasStarted(busScheduleTime.getLineId());
                        if (IsStarted == null){
                            if(HelperProvider.displayOfflineText()){
                                Activity activity = getActivity();
                                if(activity != null) {
                                    activity.runOnUiThread(() -> Toast.makeText(activity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                                }
                                HelperProvider.setOfflineTextDisplayed();
                            }
                            continue;
                        }

                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(new Date());
                        calendar.set(Calendar.HOUR_OF_DAY, busScheduleTime.getHour());
                        calendar.set(Calendar.MINUTE, busScheduleTime.getMinute());

                        DatabaseManager Dm = new DatabaseManager(ctx);
                        int StopDelta = Dm.GetBusLineStopTravelTimeById(Integer.toString(busScheduleTime.getLineId()), mStopId);
                        calendar.add(Calendar.MINUTE, StopDelta * -1);

                        if (IsStarted) {
                            if (((calendar.get(Calendar.HOUR_OF_DAY) == CurrentHour && CurrentMinute >= calendar.get(Calendar.MINUTE) || calendar.get(Calendar.HOUR_OF_DAY) + 1 == CurrentHour || calendar.get(Calendar.HOUR_OF_DAY) + 2 == CurrentHour))) {
                                StartedList.add(busScheduleTime);
                            }
                        } else {

                            int TravelTimeMin = Dm.GetBusLineSumTravelTimeById(Integer.toString(busScheduleTime.getLineId()));
                            Calendar calendar1 = (Calendar) calendar.clone();
                            calendar1.add(Calendar.MINUTE, TravelTimeMin - 2);

                            if (calendar1.after(Now) && ((calendar.get(Calendar.HOUR_OF_DAY) == CurrentHour && CurrentMinute > calendar.get(Calendar.MINUTE) || calendar.get(Calendar.HOUR_OF_DAY) + 1 == CurrentHour || calendar.get(Calendar.HOUR_OF_DAY) + 2 == CurrentHour))) {
                                ErrNotStartedList.add(busScheduleTime);
                            }
                        }
                    }
                }
            }

            BusScheduleTime[] Started = new BusScheduleTime[StartedList.size()];
            BusScheduleTime[] ErrNotStarted = new BusScheduleTime[ErrNotStartedList.size()];

            StartedList.toArray(Started);
            ErrNotStartedList.toArray(ErrNotStarted);

            List<Integer> HoursList = new ArrayList<>();

            int LoopHour = -1;
            for (BusScheduleTime busScheduleTime : CurrentLines) {
                if (busScheduleTime.getHour() != LoopHour) {
                    HoursList.add(busScheduleTime.getHour());
                    LoopHour = busScheduleTime.getHour();
                }
            }

            Activity activity = getActivity();
            View view = getView();

            if(activity != null && Sbta != null && view != null) {
                activity.runOnUiThread(() -> {
                    Sbta.AttachLiveData(Started, ErrNotStarted);

                    for (BusScheduleTime busScheduleTime : Started) {
                        int PrevHour = -1;
                        for (int j = 0; j < HoursList.size(); j++) {
                            if (busScheduleTime.getHour() == HoursList.get(j) && PrevHour != j) {
                                Sbta.notifyItemChanged(j);
                                PrevHour = j;
                            }
                        }
                    }

                    for (BusScheduleTime busScheduleTime : ErrNotStarted) {
                        int PrevHour = -1;
                        for (int j = 0; j < HoursList.size(); j++) {
                            if (busScheduleTime.getHour() == HoursList.get(j) && PrevHour != j) {
                                Sbta.notifyItemChanged(j);
                                PrevHour = j;
                            }
                        }
                    }
                    SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
                    swipeRefreshLayout.setRefreshing(false);
                });
            }

        }).start();
    }

    public void OnScheduleClick(int Hour, int Minute) {
        ScheduleActivity scheduleActivity = (ScheduleActivity)getActivity();
        if (scheduleActivity == null) {
            return;
        }
        for (BusScheduleTime busScheduleTime : CurrentLines) {
            if (busScheduleTime.getHour() == Hour && busScheduleTime.getMinute() == Minute) {
                (scheduleActivity).OnSelectedSchedule(busScheduleTime.getLineId(), SelectedDate, SelectedWay);
                return;
            }
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    public void UpdateMaxPerLine() {
        Activity activity = getActivity();
        if(activity == null) {
            return;
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        activity.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        int width = displayMetrics.widthPixels;

        int ndp = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                75,
                displayMetrics
        ));

        int MinuteWidth = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50,
                displayMetrics
        ));

        int MaxPerLine = (int) Math.floor((float)(width-ndp)/(float) MinuteWidth);

        if (Sbta != null) {
            Sbta.updateMaxPerLine(MaxPerLine);
            Sbta.notifyDataSetChanged();
        }

    }
}