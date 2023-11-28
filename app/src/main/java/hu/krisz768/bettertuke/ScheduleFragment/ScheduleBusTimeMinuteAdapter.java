package hu.krisz768.bettertuke.ScheduleFragment;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.R;

public class ScheduleBusTimeMinuteAdapter extends RecyclerView.Adapter<ScheduleBusTimeMinuteAdapter.ViewHolder>{
    private final int Hour;
    private final int[] Minutes;
    private final String[] BusCodes;
    private final int MaxPerLine;
    private final int SecColor;
    private final int OnPrimColor;
    private final int OnErrColor;
    private final Drawable Background;
    private final Drawable FullBackground;
    private final BusScheduleTime[] Started;
    private final BusScheduleTime[] ErrNotStarted;
    private final Drawable MinBackgroundStarted;
    private final Drawable MinFullBackgroundStarted;
    private final Drawable MinBackgroundErr;
    private final Drawable MinFullBackgroundErr;
    private final ScheduleBusTimeFragment Callback;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView MinuteText;
        private final TextView BusCodeText;

        public ViewHolder(View view) {
            super(view);
            MinuteText = view.findViewById(R.id.ScheduleMinuteText);
            BusCodeText = view.findViewById(R.id.ScheduleMinuteCode);
        }

        public void setData(int Hour, int Minute, String Code, boolean FirstRow, int SecColor, int OnPrimColor, int OnErrColor, Drawable Background, Drawable FullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr,BusScheduleTime[] Started,BusScheduleTime[] ErrNotStarted, ScheduleBusTimeFragment Callback) {
            MinuteText.setText(String.format(Locale.US, "%02d", Minute));

            BusCodeText.setText(Code);

            boolean IsStarted = false;
            boolean IsErr = false;

            for (BusScheduleTime busScheduleTime : Started) {
                if (busScheduleTime.getHour() == Hour && busScheduleTime.getMinute() == Minute) {
                    IsStarted = true;
                    break;
                }
            }

            for (BusScheduleTime busScheduleTime : ErrNotStarted) {
                if (busScheduleTime.getHour() == Hour && busScheduleTime.getMinute() == Minute) {
                    IsErr = true;
                    break;
                }
            }

            if (IsStarted) {
                if (FirstRow) {
                    MinuteText.setBackground(MinFullBackgroundStarted);
                } else {
                    MinuteText.setBackground(MinBackgroundStarted);
                }
                MinuteText.setTextColor(OnPrimColor);
                BusCodeText.setTextColor(OnPrimColor);
            } else if (IsErr) {
                if (FirstRow) {
                    MinuteText.setBackground(MinFullBackgroundErr);
                } else {
                    MinuteText.setBackground(MinBackgroundErr);
                }
                MinuteText.setTextColor(OnErrColor);
                BusCodeText.setTextColor(OnErrColor);
            } else {
                if (FirstRow) {
                    MinuteText.setBackground(FullBackground);
                } else {
                    MinuteText.setBackground(Background);
                }
                MinuteText.setTextColor(SecColor);
                BusCodeText.setTextColor(SecColor);
            }

            MinuteText.setOnClickListener(view -> Callback.OnScheduleClick(Hour, Minute));
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

    @NonNull
    @Override
    public ScheduleBusTimeMinuteAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.schedule_bus_time_minute_recview, viewGroup, false);

        return new ScheduleBusTimeMinuteAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ScheduleBusTimeMinuteAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(Hour, Minutes[position], BusCodes[position], MaxPerLine > position, SecColor, OnPrimColor, OnErrColor, Background,FullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr,Started, ErrNotStarted, Callback);
    }

    @Override
    public int getItemCount() {
        return Minutes.length;
    }
}
