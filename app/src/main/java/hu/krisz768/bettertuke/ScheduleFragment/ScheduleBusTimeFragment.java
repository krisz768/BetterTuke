package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;

import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.material.datepicker.MaterialDatePicker;
import com.google.android.material.datepicker.MaterialPickerOnPositiveButtonClickListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.TimeZone;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListAdapter;
import hu.krisz768.bettertuke.R;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ScheduleBusTimeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ScheduleBusTimeFragment extends Fragment {

    private static final String LINENUM= "lineNum";

    private String mLineNum;

    private String SelectedDate;

    private int colorPrimary;
    private int colorOnPrimary;
    private int colorPrimaryContainer;
    private int colorOnPrimaryContainer;

    private Drawable MinuteBackground;
    private Drawable MinuteBackgroundFull;

    public ScheduleBusTimeFragment() {
        // Required empty public constructor
    }

    public static ScheduleBusTimeFragment newInstance(String lineNum) {
        ScheduleBusTimeFragment fragment = new ScheduleBusTimeFragment();
        Bundle args = new Bundle();
        args.putString(LINENUM, lineNum);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mLineNum = getArguments().getString(LINENUM);
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd");
        Date date = new Date();
        SelectedDate = formatter.format(date);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_bus_time, container, false);

        LoadColorsAndBackgrounds();

        setColors(view);

        TextView SelectedDateText = view.findViewById(R.id.ScheduleBusLineDate);

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy. MM. dd.");
        Date date = new Date();
        SelectedDateText.setText(formatter.format(date));

        SelectedDateText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ShowDatePicker();
            }
        });

        ReloadSchedules(view);

        return view;
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

        DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        int StrokeWidth = Math.round(TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                2,
                displayMetrics
        ));

        MinuteBackground = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_background);
        ((GradientDrawable)(((LayerDrawable)MinuteBackground).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth,colorPrimaryContainer);

        MinuteBackgroundFull = ContextCompat.getDrawable(getContext(), R.drawable.bus_schedule_minute_fbackground);
        ((GradientDrawable)(((LayerDrawable)MinuteBackgroundFull).findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle))).setStroke(StrokeWidth,colorPrimaryContainer);
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
        TextView NumText = view.findViewById(R.id.ScheduleBusLineNum);
        NumText.setText(mLineNum);

        DatabaseManager Dm = new DatabaseManager(getContext());

        BusScheduleTime[] Jaratok = Dm.GetBusScheduleTimeFromStart(mLineNum, SelectedDate, "O");

        List<Integer> HoursList = new ArrayList<>();

        int CurrentHour = -1;
        for (int i = 0; i < Jaratok.length; i++) {
            if (Jaratok[i].getOra() != CurrentHour) {
                HoursList.add(Jaratok[i].getOra());
                CurrentHour = Jaratok[i].getOra();
            }
        }

        int[] Hours = new int[HoursList.size()];

        for (int i = 0; i < HoursList.size(); i++) {
            Hours[i] = HoursList.get(i);
        }

        int[][] Minutes = new int[HoursList.size()][];
        List<Integer> TempMinutes = new ArrayList<>();

        CurrentHour = -1;
        int HourIndex = -1;
        for (int i = 0; i < Jaratok.length; i++) {
            if (Jaratok[i].getOra() != CurrentHour) {
                if(HourIndex != -1) {
                    Minutes[HourIndex] = new int[TempMinutes.size()];
                    for (int j = 0; j < TempMinutes.size(); j++) {
                        Minutes[HourIndex][j] = TempMinutes.get(j);
                    }
                }

                HourIndex++;

                CurrentHour = Jaratok[i].getOra();
                TempMinutes = new ArrayList<>();
                TempMinutes.add(Jaratok[i].getPerc());
            } else {
                TempMinutes.add(Jaratok[i].getPerc());
            }
        }

        if (TempMinutes.size() > 0) {
            Minutes[HourIndex] = new int[TempMinutes.size()];
            for (int j = 0; j < TempMinutes.size(); j++) {
                Minutes[HourIndex][j] = TempMinutes.get(j);
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

        ScheduleBusTimeHourAdapter Sbta = new ScheduleBusTimeHourAdapter(Hours, Minutes, (int) Math.floor((float)(width-ndp)/(float) MinuteWidth), colorPrimary, colorPrimaryContainer, colorOnPrimaryContainer, MinuteBackground, MinuteBackgroundFull, getContext());
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        Recv.setLayoutManager(mLayoutManager);
        Recv.setAdapter(Sbta);
    }
}