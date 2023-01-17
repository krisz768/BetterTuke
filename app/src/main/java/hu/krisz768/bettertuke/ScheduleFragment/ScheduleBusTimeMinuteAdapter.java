package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Calendar;
import java.util.Date;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

public class ScheduleBusTimeMinuteAdapter extends RecyclerView.Adapter<ScheduleBusTimeMinuteAdapter.ViewHolder>{

    private int Hour;
    private int[] Minutes;
    private String[] BusCodes;
    private int MaxPerLine;
    private int SecColor;
    private int OnPrimColor;
    private int OnErrColor;
    private Drawable Background;
    private Drawable FullBackground;

    private BusScheduleTime[] Started;
    private BusScheduleTime[] ErrNotStarted;

    private Drawable MinBackgroundStarted;
    private Drawable MinFullBackgroundStarted;
    private Drawable MinBackgroundErr;
    private Drawable MinFullBackgroundErr;

    private ScheduleBusTimeFragment Callback;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView MinuteText;
        private TextView BusCodeText;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            MinuteText = view.findViewById(R.id.ScheduleMinuteText);
            BusCodeText = view.findViewById(R.id.ScheduleMinuteCode);
        }

        public void setData(int Hour, int Minute, String Code, boolean Firstrow, int SecColor, int OnPrimColor, int OnErrColor, Drawable Background, Drawable FullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr,BusScheduleTime[] Started,BusScheduleTime[] ErrNotStarted, ScheduleBusTimeFragment Callback) {
            MinuteText.setText(String.format("%02d", Minute));

            BusCodeText.setText(Code);

            boolean IsStarted = false;
            boolean IsErr = false;

            for (int i = 0; i < Started.length; i++) {
                if (Started[i].getOra() == Hour && Started[i].getPerc() == Minute) {
                    IsStarted = true;
                }
            }

            for (int i = 0; i < ErrNotStarted.length; i++) {
                if (ErrNotStarted[i].getOra() == Hour && ErrNotStarted[i].getPerc() == Minute) {
                    IsErr = true;
                }
            }

            if (IsStarted) {
                if (Firstrow) {
                    MinuteText.setBackground(MinFullBackgroundStarted);
                } else {
                    MinuteText.setBackground(MinBackgroundStarted);
                }
                MinuteText.setTextColor(OnPrimColor);
                BusCodeText.setTextColor(OnPrimColor);
            } else if (IsErr) {
                if (Firstrow) {
                    MinuteText.setBackground(MinFullBackgroundErr);
                } else {
                    MinuteText.setBackground(MinBackgroundErr);
                }
                MinuteText.setTextColor(OnErrColor);
                BusCodeText.setTextColor(OnErrColor);
            } else {
                if (Firstrow) {
                    MinuteText.setBackground(FullBackground);
                } else {
                    MinuteText.setBackground(Background);
                }
                MinuteText.setTextColor(SecColor);
                BusCodeText.setTextColor(SecColor);
            }

            MinuteText.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnScheduleClick(Hour, Minute);
                }
            });

        }
    }

    public ScheduleBusTimeMinuteAdapter(int Hour, int[] Minutes, String[] BusCodes, int MaxPerLine,int SecColor, int OnPrimColor, int OnErrColor, Drawable Background, Drawable FullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr,BusScheduleTime[] Started,BusScheduleTime[] ErrNotStarted, ScheduleBusTimeFragment Callback) {
        this.Hour = Hour;
        this.Minutes = Minutes;
        this.MaxPerLine = MaxPerLine;
        this.SecColor = SecColor;
        this.OnPrimColor = OnPrimColor;
        this.OnErrColor = OnErrColor;
        this.Background = Background;
        this.FullBackground = FullBackground;
        this.Started = Started;
        this.ErrNotStarted = ErrNotStarted;
        this.BusCodes = BusCodes;

        this.MinBackgroundStarted = MinBackgroundStarted;
        this.MinFullBackgroundStarted = MinFullBackgroundStarted;
        this.MinBackgroundErr = MinBackgroundErr;
        this.MinFullBackgroundErr = MinFullBackgroundErr;

        this.Callback = Callback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScheduleBusTimeMinuteAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.schedule_bus_time_minute_recview, viewGroup, false);

        return new ScheduleBusTimeMinuteAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ScheduleBusTimeMinuteAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setData(Hour, Minutes[position], BusCodes[position], MaxPerLine > position, SecColor, OnPrimColor, OnErrColor, Background,FullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr,Started, ErrNotStarted, Callback);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Minutes.length;
    }
}
