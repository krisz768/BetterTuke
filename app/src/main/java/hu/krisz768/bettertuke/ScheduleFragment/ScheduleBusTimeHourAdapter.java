package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.BusVariation;
import hu.krisz768.bettertuke.R;

public class ScheduleBusTimeHourAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private int[] Hours;
    private int[][] Minutes;
    private String[][] BusCodes;
    private final BusVariation[] Variations;
    private final Context ctx;
    private int MaxPerLine;

    private final int SecColor;
    private final int SecContainerColor;
    private final int OnSecContainerColor;
    private final int OnPrimColor;
    private final int OnErrColor;
    private final Drawable MinBackground;
    private final Drawable MinFullBackground;
    private final Drawable MinBackgroundStarted;
    private final Drawable MinFullBackgroundStarted;
    private final Drawable MinBackgroundErr;
    private final Drawable MinFullBackgroundErr;

    private BusScheduleTime[] Started;
    private BusScheduleTime[] ErrNotStarted;

    private final ScheduleBusTimeFragment Callback;


    public static class ViewHolderHour extends RecyclerView.ViewHolder {

        private final TextView HourText;
        private final RecyclerView Recv;

        public ViewHolderHour(View view) {
            super(view);
            HourText = view.findViewById(R.id.ScheduleHourText);
            Recv = view.findViewById(R.id.ScheduleMinuteRecview);
        }

        public void setData(int Hour, int[] Minutes, String[] BusCodes, int MaxPerLine, int SecColor, int SecContainerColor, int OnSecContainerColor, int OnPrimColor, int OnErrColor, Drawable MinBackground, Drawable MinFullBackground, Drawable MinBackgroundStarted, Drawable MinFullBackgroundStarted, Drawable MinBackgroundErr, Drawable MinFullBackgroundErr,BusScheduleTime[] Started,BusScheduleTime[] ErrNotStarted, Context ctx, ScheduleBusTimeFragment Callback) {
            HourText.setText(String.format(Locale.US, "%02d", Hour));

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

        private final TextView Label;

        public ViewHolderLabel(View view) {
            super(view);
            Label = view.findViewById(R.id.labelText);
        }

        public void setData(String LabelText) {
            Label.setText(LabelText);
            Label.setTextSize(TypedValue.COMPLEX_UNIT_SP, 15);
        }
    }

    public static class ViewHolderLegend extends RecyclerView.ViewHolder {

        private final TextView Legend;
        private final TextView Desc;

        public ViewHolderLegend(View view) {
            super(view);

            Legend = view.findViewById(R.id.LegendCode);
            Desc = view.findViewById(R.id.LegendDesc);
        }

        public void setData(BusVariation variation, Context ctx) {
            Legend.setText(variation.getCode());
            if (variation.getDirection().equals("O")) {
                Drawable arrow = ContextCompat.getDrawable(ctx, R.drawable.right_arrow);
                if (arrow != null) {
                    arrow.setTint(Desc.getCurrentTextColor());
                }
                float ascent = Desc.getPaint().getFontMetrics().ascent;
                int h = (int) -ascent;
                if (arrow != null) {
                    arrow.setBounds(0,0,h,h);
                }

                SpannableString stringWithImage = new SpannableString(variation.getName() + " (*)");
                stringWithImage.setSpan(new ImageSpan(arrow, DynamicDrawableSpan.ALIGN_BASELINE), variation.getName().length()+2, variation.getName().length()+3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Desc.setText(stringWithImage);
            } else {
                Drawable arrow = ContextCompat.getDrawable(ctx, R.drawable.left_arrow);
                if (arrow != null) {
                    arrow.setTint(Desc.getCurrentTextColor());
                }
                float ascent = Desc.getPaint().getFontMetrics().ascent;
                int h = (int) -ascent;
                if (arrow != null) {
                    arrow.setBounds(0,0,h,h);
                }

                SpannableString stringWithImage = new SpannableString(variation.getName() + " (*)");
                stringWithImage.setSpan(new ImageSpan(arrow, DynamicDrawableSpan.ALIGN_BASELINE), variation.getName().length()+2, variation.getName().length()+3, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                Desc.setText(stringWithImage);
            }

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View HourView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.schedule_bus_time_hour_recview, viewGroup, false);

            return new ViewHolderHour(HourView);
        } else if (viewType == 1) {
            View LabelView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recview_label, viewGroup, false);

            return new ScheduleBusTimeHourAdapter.ViewHolderLabel(LabelView);
        } else {
            View LegendView = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.legend_recview, viewGroup, false);

            return new ScheduleBusTimeHourAdapter.ViewHolderLegend(LegendView);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        if (viewHolder.getItemViewType() == 0) {
            ((ViewHolderHour)viewHolder).setData(Hours[position], Minutes[position], BusCodes[position], MaxPerLine, SecColor, SecContainerColor, OnSecContainerColor, OnPrimColor, OnErrColor, MinBackground,MinFullBackground, MinBackgroundStarted, MinFullBackgroundStarted, MinBackgroundErr, MinFullBackgroundErr, Started, ErrNotStarted, ctx, Callback);
        } else if (viewHolder.getItemViewType() == 1){
            ((ViewHolderLabel)viewHolder).setData(ctx.getString(R.string.Legend));
        } else {
            ((ViewHolderLegend)viewHolder).setData(Variations[position-Hours.length-1], ctx);
        }

    }

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
