package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.BusVariation;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.ScheduleActivity;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleBusTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleBusTimeFragment extends Fragment {

    private static final String LINENUM= "lineNum";
    private static final String STOPID= "StopId";


    private String mLineNum;
    private int mStopId;

    private String SelectedDate;
    private String SelectedWay;
    private boolean TwoWay = false;
    private String WayDescStringForw = "";
    private String WayDescStringbackw = "";
    private BusScheduleTime[] CurrentJaratok;
    private int[] CurrentHours;
    private int[][] CurrentMinutes;

    private int colorPrimary;
    private int colorOnPrimary;
    private int colorPrimaryContainer;
    private int colorOnPrimaryContainer;
    private int colorSec;
    private int colorOnSec;
    private int colorOnSecContainer;
    private int colorSecContainer;
    private int colorErr;
    private int colorOnError;
    private Drawable MinuteBackground;
    private Drawable MinuteBackgroundFull;
    private Drawable MinuteBackgroundStarted;
    private Drawable MinuteBackgroundFullStarted;
    private Drawable MinuteBackgroundErr;
    private Drawable MinuteBackgroundFullErr;

    private ScheduleBusTimeHourAdapter Sbta;

    public ScheduleBusTimeFragment() {
        // Required empty public constructor
    }

    public static ScheduleBusTimeFragment newInstance(String lineNum, int StopId) {
        ScheduleBusTimeFragment fragment = new ScheduleBusTimeFragment();
        Bundle args = new Bundle();
        args.putString(LINENUM, lineNum);
        args.putInt(STOPID, StopId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLineNum = getArguments().getString(LINENUM);
            mStopId = getArguments().getInt(STOPID);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        SelectedDate = formatter.format(date);
        SelectedWay = "O";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_time, container, false);

        LoadColorsAndBackgrounds();

        setColors(view);

        TextView NumText = view.findViewById(R.id.ScheduleBusLineNum);
        NumText.setText(mLineNum);

        TextView SelectedDateText = view.findViewById(R.id.ScheduleBusLineDate);
        TextView DescText = view.findViewById(R.id.ScheduleBusLineDesc);

        DatabaseManager Dm = new DatabaseManager(getContext());
        BusVariation[] Variations;

        ImageView BusLineTimeDirectionIcon = view.findViewById(R.id.BusLineTimeDirectionIcon);

        if (mStopId == -1) {
            Variations = Dm.GetBusVariations(mLineNum);
        } else {
            Variations = Dm.GetBusVariationsFromStop(mLineNum, mStopId);
        }

        boolean IsForwWayDescTextSetted = false;
        boolean IsBackwWayDescTextSetted = false;
        for (int i = 0; i < Variations.length; i++) {
            if (Variations[i].getIrany().equals("V") && Variations.length > 1) {
                TwoWay = true;
            }
            if (!IsForwWayDescTextSetted && SelectedWay.equals(Variations[i].getIrany())) {
                IsForwWayDescTextSetted = true;
                WayDescStringForw = Variations[i].getNev();
                DescText.setText(Variations[i].getNev());
            }
            if (!IsBackwWayDescTextSetted && !SelectedWay.equals(Variations[i].getIrany())) {
                IsBackwWayDescTextSetted = true;
                WayDescStringbackw = Variations[i].getNev();
            }
        }

        if (!IsForwWayDescTextSetted && IsBackwWayDescTextSetted) {
            DescText.setText(WayDescStringbackw);
            SelectedWay = "V";
        }

        if (mStopId != -1 && !TwoWay) {
            BusLineTimeDirectionIcon.setVisibility(View.GONE);
        }

        ImageView BusLineTimeFavIcon = view.findViewById(R.id.BusLineTimeFavIcon);
        BusLineTimeFavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd.");
        Date date = new Date();
        SelectedDateText.setText(formatter.format(date));

        SelectedDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDatePicker();
            }
        });


        BusLineTimeDirectionIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (TwoWay) {
                    if (SelectedWay.equals("O")) {
                        SelectedWay = "V";
                        DescText.setText(WayDescStringbackw);
                    } else {
                        SelectedWay = "O";
                        DescText.setText(WayDescStringForw);
                    }
                }
                ReloadSchedules(getView());
            }
        });

        SwipeRefreshLayout swipeRefreshLayout = view.findViewById(R.id.swiperefresh);
        swipeRefreshLayout.setOnRefreshListener(
                new SwipeRefreshLayout.OnRefreshListener() {
                    @Override
                    public void onRefresh() {
                        ReloadSchedules(view);

                        RecyclerView Recv = view.findViewById(R.id.ScheduleTimeHoursRecView);

                        Calendar Now = Calendar.getInstance();
                        int NowHour = Now.get(Calendar.HOUR_OF_DAY);

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

                        smoothScroller.setTargetPosition(0);
                        for (int i = 0; i < CurrentHours.length; i++) {
                            if ( CurrentHours[i] >= NowHour) {
                                if (i > 0) {
                                    smoothScroller.setTargetPosition(i-1);
                                }

                                break;
                            }
                        }

                        Recv.getLayoutManager().startSmoothScroll(smoothScroller);

                        swipeRefreshLayout.setRefreshing(false);
                    }
                }
        );

        ReloadSchedules(view);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        ReloadSchedules(getView());
    }

    private void LoadColorsAndBackgrounds() {
        TypedValue typedValue = new TypedValue();
        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
        colorPrimary = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);
        colorOnPrimary = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
        colorPrimaryContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimaryContainer, typedValue, true);
        colorOnPrimaryContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
        colorSec = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondary, typedValue, true);
        colorOnSec = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondaryContainer, typedValue, true);
        colorSecContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnSecondaryContainer, typedValue, true);
        colorOnSecContainer = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorError, typedValue, true);
        colorErr = ContextCompat.getColor(getContext(), typedValue.resourceId);

        getContext().getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnError, typedValue, true);
        colorOnError = ContextCompat.getColor(getContext(), typedValue.resourceId);

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int StrokeWidth = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2,
                displayMetrics
        ));

        MinuteBackground = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        ((GradientDrawable)(((LayerDrawable)MinuteBackground).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
        ((GradientDrawable)(((LayerDrawable)MinuteBackground).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorOnSec);

        MinuteBackgroundFull = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFull).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFull).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorOnSec);

        MinuteBackgroundStarted = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorPrimary);

        MinuteBackgroundFullStarted = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullStarted).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorPrimary);

        MinuteBackgroundErr = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorErr);

        MinuteBackgroundFullErr = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth, colorSecContainer);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFullErr).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setColor(colorErr);
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
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
        Date date = new Date();
        try {
            date = sdf.parse(SelectedDate);
        }catch (Exception e) {

        }



        MaterialDatePicker DatePicker = MaterialDatePicker.Builder.datePicker().setTitleText("Válassz Dátumot:").setSelection(date.getTime()).build();
        DatePicker.addOnPositiveButtonClickListener(new MaterialPickerOnPositiveButtonClickListener() {
            @Override
            public void onPositiveButtonClick(Object selection) {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                SelectedDate = sdf.format(new Date((long)selection));

                TextView SelectedDateText = getView().findViewById(R.id.ScheduleBusLineDate);

                SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd.");
                SelectedDateText.setText(formatter.format(new Date((long)selection)));

                ReloadSchedules(getView());
            }
        });
        DatePicker.show(getChildFragmentManager(), "DatePicker");
    }

    private void ReloadSchedules(View view) {
        DatabaseManager Dm = new DatabaseManager(getContext());

        ImageView BusLineTimeDirectionIcon = view.findViewById(R.id.BusLineTimeDirectionIcon);
        if (TwoWay && SelectedWay.equals("O")) {
            BusLineTimeDirectionIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DirectionForward));
        } else if (TwoWay && SelectedWay.equals("V")){
            BusLineTimeDirectionIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DirectionBackwards));
        } else {
            BusLineTimeDirectionIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.DirectionOneWay));
        }




        if (mStopId == -1) {
            CurrentJaratok = Dm.GetBusScheduleTimeFromStart(mLineNum, SelectedDate, SelectedWay);
        } else {
            CurrentJaratok = Dm.GetBusScheduleTimeFromStop(mLineNum, SelectedDate, SelectedWay, mStopId);



            for (int i = 0; i < CurrentJaratok.length; i++) {
                int StopDelta = Dm.GetBusJaratStopMenetidoById(Integer.toString(CurrentJaratok[i].getJaratId()), mStopId);
                CurrentJaratok[i].AdjustToStop(StopDelta);
            }
        }


        List<Integer> HoursList = new ArrayList<>();

        int CurrentHour = -1;
        for (int i = 0; i < CurrentJaratok.length; i++) {
            if (CurrentJaratok[i].getOra() != CurrentHour) {
                HoursList.add(CurrentJaratok[i].getOra());
                CurrentHour = CurrentJaratok[i].getOra();
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
        for (int i = 0; i < CurrentJaratok.length; i++) {
            if (CurrentJaratok[i].getOra() != CurrentHour) {
                if(HourIndex != -1) {
                    CurrentMinutes[HourIndex] = new int[TempMinutes.size()];
                    for (int j = 0; j < TempMinutes.size(); j++) {
                        CurrentMinutes[HourIndex][j] = TempMinutes.get(j);
                    }
                }

                HourIndex++;

                CurrentHour = CurrentJaratok[i].getOra();
                TempMinutes = new ArrayList<>();
                TempMinutes.add(CurrentJaratok[i].getPerc());
            } else {
                TempMinutes.add(CurrentJaratok[i].getPerc());
            }
        }

        if (TempMinutes.size() > 0) {
            CurrentMinutes[HourIndex] = new int[TempMinutes.size()];
            for (int j = 0; j < TempMinutes.size(); j++) {
                CurrentMinutes[HourIndex][j] = TempMinutes.get(j);
            }
        }

        DisplayMetrics displayMetrics = new DisplayMetrics();
        getActivity().getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
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

        Calendar Now = Calendar.getInstance();

        if (Sbta == null) {
            Sbta = new ScheduleBusTimeHourAdapter(CurrentHours, CurrentMinutes, (int) Math.floor((float)(width-ndp)/(float) MinuteWidth), colorSec, colorSecContainer, colorOnSecContainer, colorOnPrimary, colorOnError, MinuteBackground, MinuteBackgroundFull, MinuteBackgroundStarted, MinuteBackgroundFullStarted, MinuteBackgroundErr, MinuteBackgroundFullErr, getContext(), this);
            RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
            Recv.setLayoutManager(mLayoutManager);
            Recv.setAdapter(Sbta);

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
            for (int i = 0; i < CurrentHours.length; i++) {
                if ( CurrentHours[i] >= NowHour) {
                    if (i > 0) {
                        smoothScroller.setTargetPosition(i-1);
                    }

                    break;
                }
            }

            Recv.getLayoutManager().startSmoothScroll(smoothScroller);
        } else {
            Sbta.UpdateData(CurrentHours, CurrentMinutes);
            Sbta.notifyDataSetChanged();
        }


        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        if (SelectedDate.equals(formatter.format(date))) {
            GetLiveData(CurrentJaratok);
        }
    }

    private void GetLiveData(BusScheduleTime[] Jaratok) {
        Context ctx = getContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Calendar Now = Calendar.getInstance();

                int CurrentHour = Now.get(Calendar.HOUR_OF_DAY);
                int CurrentMinute = Now.get(Calendar.MINUTE);

                List<BusScheduleTime> StartedList = new ArrayList<>();
                List<BusScheduleTime> ErrNotStartedList = new ArrayList<>();


                for (int i = 0; i < Jaratok.length; i++) {
                    if (mStopId == -1) {
                        if ((Jaratok[i].getOra() == CurrentHour && CurrentMinute >= Jaratok[i].getPerc()) || Jaratok[i].getOra() + 1 == CurrentHour || Jaratok[i].getOra() + 2 == CurrentHour) {
                            TukeServerApi tukeServerApi = new TukeServerApi(ctx);
                            boolean IsStarted = tukeServerApi.getIsBusHasStarted(Jaratok[i].getJaratId());

                            if (IsStarted) {
                                StartedList.add(Jaratok[i]);
                            } else {
                                DatabaseManager Dm = new DatabaseManager(ctx);
                                int MenetidoMin = Dm.GetBusJaratSumMenetidoById(Integer.toString(Jaratok[i].getJaratId()));



                                Calendar calendar = Calendar.getInstance();
                                calendar.setTime(new Date());
                                calendar.set(Calendar.HOUR_OF_DAY, Jaratok[i].getOra());
                                calendar.set(Calendar.MINUTE, Jaratok[i].getPerc());

                                calendar.add(Calendar.MINUTE, MenetidoMin-2);

                                if (calendar.after(Now) && ((Jaratok[i].getOra() == CurrentHour && CurrentMinute > Jaratok[i].getPerc()) || Jaratok[i].getOra() + 1 == CurrentHour || Jaratok[i].getOra() + 2 == CurrentHour)) {
                                    ErrNotStartedList.add(Jaratok[i]);
                                }
                            }
                        }
                    } else {
                        if (Jaratok[i].getOra() == CurrentHour || Jaratok[i].getOra() + 1 == CurrentHour || Jaratok[i].getOra() -1 == CurrentHour) {
                            TukeServerApi tukeServerApi = new TukeServerApi(ctx);
                            boolean IsStarted = tukeServerApi.getIsBusHasStarted(Jaratok[i].getJaratId());

                            Calendar calendar = Calendar.getInstance();
                            calendar.setTime(new Date());
                            calendar.set(Calendar.HOUR_OF_DAY, Jaratok[i].getOra());
                            calendar.set(Calendar.MINUTE, Jaratok[i].getPerc());

                            DatabaseManager Dm = new DatabaseManager(ctx);
                            int StopDelta = Dm.GetBusJaratStopMenetidoById(Integer.toString(Jaratok[i].getJaratId()), mStopId);
                            calendar.add(Calendar.MINUTE, StopDelta*-1);

                            if (IsStarted) {
                                if (((calendar.get(Calendar.HOUR_OF_DAY) == CurrentHour && CurrentMinute >= calendar.get(Calendar.MINUTE) || calendar.get(Calendar.HOUR_OF_DAY) + 1 == CurrentHour || calendar.get(Calendar.HOUR_OF_DAY) + 2 == CurrentHour))) {
                                    StartedList.add(Jaratok[i]);
                                }
                            } else {

                                int MenetidoMin = Dm.GetBusJaratSumMenetidoById(Integer.toString(Jaratok[i].getJaratId()));

                                Calendar calendar1 = (Calendar) calendar.clone();
                                calendar1.add(Calendar.MINUTE, MenetidoMin-2);

                                if (calendar1.after(Now) && ((calendar.get(Calendar.HOUR_OF_DAY) == CurrentHour && CurrentMinute > calendar.get(Calendar.MINUTE) || calendar.get(Calendar.HOUR_OF_DAY) + 1 == CurrentHour || calendar.get(Calendar.HOUR_OF_DAY) + 2 == CurrentHour))) {
                                    ErrNotStartedList.add(Jaratok[i]);
                                }
                            }
                        }
                    }
                }
                BusScheduleTime[] Started = new BusScheduleTime[StartedList.size()];
                BusScheduleTime[] ErrNotStarted = new BusScheduleTime[ErrNotStartedList.size()];

                StartedList.toArray(Started);
                ErrNotStartedList.toArray(ErrNotStarted);

                if(getActivity() != null && Sbta != null) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Sbta.AttachLiveData(Started, ErrNotStarted);
                            Sbta.notifyDataSetChanged();
                        }
                    });
                }
            }
        }).start();
    }

    public void OnScheduleClick(int Hour, int Minute) {
        for (int i = 0; i < CurrentJaratok.length; i++) {
            if (CurrentJaratok[i].getOra() == Hour && CurrentJaratok[i].getPerc() == Minute) {
                ((ScheduleActivity)getActivity()).OnSelectedSchedule(CurrentJaratok[i].getJaratId());
                return;
            }
        }
    }
}