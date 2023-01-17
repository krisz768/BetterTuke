package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.BusVariation;
import hu.krisz768.bettertuke.R;

public class ScheduleBusTimeHourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private int[] Hours;
    private int[][] Minutes;
    private String[][] BusCodes;
    private BusVariation[] Variations;
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

    private ScheduleBusTimeFragment Callback;


    public static class ViewHolderHour extends RecyclerView.ViewHolder {

        private TextView HourText;
        private RecyclerView Recv;

        public ViewHolderHour(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            HourText = view.findViewById(R.id.ScheduleHourText);
            Recv = view.findViewById(R.id.ScheduleMinuteRecview);
        }

        public void setData(int Hour, int[] Minutes, String[] BusCodes, int MaxPerLine, int SecColor, int SecContainerColor, int OnSecContainerColor, int OnPrimColor, int OnErrColor, Drawable MinBackground, Drawable MinFullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr,BusScheduleTime[] Started,BusScheduleTime[] ErrNotStarted, Context ctx, ScheduleBusTimeFragment Callback) {
            HourText.setText(String.format("%02d", Hour));

            HourText.setTextColor(OnSecContainerColor);

            Drawable background = HourText.getBackground();
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(SecContainerColor);

            ScheduleBusTimeMinuteAdapter Sbta = new ScheduleBusTimeMinuteAdapter(Hour, Minutes, BusCodes, MaxPerLine, SecColor, OnPrimColor, OnErrColor, MinBackground, MinFullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr, Started, ErrNotStarted, Callback);
            GridLayoutManager mLayoutManager = new GridLayoutManager(ctx, MaxPerLine);
            Recv.setLayoutManager(mLayoutManager);
            Recv.setAdapter(Sbta);
        }
    }

    public static class ViewHolderLabel extends RecyclerView.ViewHolder {

        private TextView Label;

        public ViewHolderLabel(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Label = view.findViewById(R.id.labelText);
        }

        public void setData(String LabeText) {
            Label.setText(LabeText);
            Label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
    }

    public static class ViewHolderLegend extends RecyclerView.ViewHolder {

        private TextView Legend;
        private TextView Desc;

        public ViewHolderLegend(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Legend = view.findViewById(R.id.LegendCode);
            Desc = view.findViewById(R.id.LegendDesc);
        }

        public void setData(BusVariation variation) {
            Legend.setText(variation.getKod());
            Desc.setText(variation.getNev());
        }
    }

    public ScheduleBusTimeHourAdapter(int[] Hours, int[][] Minutes, String[][] BusCodes, BusVariation[] Variations, int MaxPerLine, int SecColor, int SecContainerColor, int OnSecContainerColor, int OnPrimColor, int OnErrColor, Drawable MinBackground, Drawable MinFullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr, Context ctx, ScheduleBusTimeFragment Callback) {
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
        this.BusCodes = BusCodes;
        this.Variations = Variations;

        Started = new BusScheduleTime[0];
        ErrNotStarted =new BusScheduleTime[0];

        this.Callback = Callback;
    }

    public void UpdateData(int[] Hours, int[][] Minutes, String[][] BusCodes) {
        this.Hours = Hours;
        this.Minutes = Minutes;
        this.BusCodes = BusCodes;

        Started = new BusScheduleTime[0];
        ErrNotStarted =new BusScheduleTime[0];
    }

    public void updateMaxPerLine(int MaxPerLine) {
        this.MaxPerLine = MaxPerLine;
    }

    public void AttachLiveData(BusScheduleTime[] Started, BusScheduleTime[] ErrNotStarted) {
        this.Started = Started;
        this.ErrNotStarted = ErrNotStarted;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.schedule_bus_time_hour_recview, viewGroup, false);

            return new ViewHolderHour(view);
        } else if (viewType == 1) {
            View Labelview = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recview_label, viewGroup, false);

            return new ScheduleBusTimeHourAdapter.ViewHolderLabel(Labelview);
        } else {
            View Labelview = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.legend_recview, viewGroup, false);

            return new ScheduleBusTimeHourAdapter.ViewHolderLegend(Labelview);
        }

    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder.getItemViewType() == 0) {
            ((ViewHolderHour)viewHolder).setData(Hours[position], Minutes[position], BusCodes[position], MaxPerLine, SecColor, SecContainerColor, OnSecContainerColor, OnPrimColor, OnErrColor, MinBackground,MinFullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr, Started, ErrNotStarted, ctx, Callback);
        } else if (viewHolder.getItemViewType() == 1){
            ((ViewHolderLabel)viewHolder).setData("Jelmagyarázat:");
        } else {
            ((ViewHolderLegend)viewHolder).setData(Variations[position-Hours.length-1]);
        }

    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Hours.length + Variations.length + 1;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < Hours.length) {
            return 0;
        }else if (position == Hours.length) {
            return 1;
        } else {
            return 2;
        }
    }
}
