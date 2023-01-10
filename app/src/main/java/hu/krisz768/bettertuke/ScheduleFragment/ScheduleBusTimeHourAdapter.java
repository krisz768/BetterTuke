package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.R;

public class ScheduleBusTimeHourAdapter extends RecyclerView.Adapter<ScheduleBusTimeHourAdapter.ViewHolder>{

    private int[] Hours;
    private int[][] Minutes;
    private Context ctx;
    private int MaxPerLine;

    private int SecColor;
    private int SecContainerColor;
    private int OnSecContainerColor;
    private int OnPrimColor;
    private int OnErrColor;
    private Drawable MinBackground;
    private Drawable MinFullBackground;
    private Drawable MinBackgroundStarted;
    private Drawable MinFullBackgroundStarted;
    private Drawable MinBackgroundErr;
    private Drawable MinFullBackgroundErr;

    private BusScheduleTime[] Started;
    private BusScheduleTime[] ErrNotStarted;


    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView HourText;
        private RecyclerView Recv;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            HourText = view.findViewById(R.id.ScheduleHourText);
            Recv = view.findViewById(R.id.ScheduleMinuteRecview);
        }

        public void setData(int Hour, int[] Minutes, int MaxPerLine, int SecColor, int SecContainerColor, int OnSecContainerColor, int OnPrimColor, int OnErrColor, Drawable MinBackground, Drawable MinFullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr,BusScheduleTime[] Started,BusScheduleTime[] ErrNotStarted, Context ctx) {
            HourText.setText(String.format("%02d", Hour));

            HourText.setTextColor(OnSecContainerColor);

            Drawable background = HourText.getBackground();
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(SecContainerColor);

            ScheduleBusTimeMinuteAdapter Sbta = new ScheduleBusTimeMinuteAdapter(Hour, Minutes, MaxPerLine, SecColor, OnPrimColor, OnErrColor, MinBackground, MinFullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr, Started, ErrNotStarted);
            GridLayoutManager mLayoutManager = new GridLayoutManager(ctx, MaxPerLine);
            Recv.setLayoutManager(mLayoutManager);
            Recv.setAdapter(Sbta);
        }
    }

    public ScheduleBusTimeHourAdapter(int[] Hours, int[][] Minutes, int MaxPerLine, int SecColor, int SecContainerColor, int OnSecContainerColor, int OnPrimColor, int OnErrColor, Drawable MinBackground, Drawable MinFullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr, Context ctx) {
        this.Hours = Hours;
        this.Minutes = Minutes;
        this.ctx = ctx;
        this.MaxPerLine = MaxPerLine;
        this.SecColor = SecColor;
        this.SecContainerColor = SecContainerColor;
        this.OnSecContainerColor = OnSecContainerColor;
        this.OnPrimColor = OnPrimColor;
        this.OnErrColor = OnErrColor;
        this.MinBackground = MinBackground;
        this.MinFullBackground = MinFullBackground;
        this.MinBackgroundStarted = MinBackgroundStarted;
        this.MinFullBackgroundStarted = MinFullBackgroundStarted;
        this.MinBackgroundErr = MinBackgroundErr;
        this.MinFullBackgroundErr = MinFullBackgroundErr;

        Started = new BusScheduleTime[0];
        ErrNotStarted =new BusScheduleTime[0];
    }

    public void UpdateData(int[] Hours, int[][] Minutes) {
        this.Hours = Hours;
        this.Minutes = Minutes;

        Started = new BusScheduleTime[0];
        ErrNotStarted =new BusScheduleTime[0];
    }

    public void AttachLiveData(BusScheduleTime[] Started, BusScheduleTime[] ErrNotStarted) {
        this.Started = Started;
        this.ErrNotStarted = ErrNotStarted;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScheduleBusTimeHourAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.schedule_bus_time_hour_recview, viewGroup, false);

        return new ScheduleBusTimeHourAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ScheduleBusTimeHourAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setData(Hours[position], Minutes[position], MaxPerLine, SecColor, SecContainerColor, OnSecContainerColor, OnPrimColor, OnErrColor, MinBackground,MinFullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr, Started, ErrNotStarted, ctx);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Hours.length;
    }
}
