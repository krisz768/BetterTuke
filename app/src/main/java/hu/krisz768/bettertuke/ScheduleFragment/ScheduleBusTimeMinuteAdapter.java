package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.graphics.drawable.InsetDrawable;
import android.graphics.drawable.LayerDrawable;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.R;

public class ScheduleBusTimeMinuteAdapter extends RecyclerView.Adapter<ScheduleBusTimeMinuteAdapter.ViewHolder>{

    private int Hour;
    private int[] Minutes;
    private int MaxPerLine;
    private Context ctx;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView MinuteText;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            MinuteText = view.findViewById(R.id.ScheduleMinuteText);
        }

        public void setData(int Hour, int Minute, boolean Firstrow, Context ctx) {
            MinuteText.setText(String.format("%02d", Minute));
            if (Firstrow) {
                MinuteText.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_schedule_minute_fbackground));
            } else {
                MinuteText.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_schedule_minute_background));
            }

            TypedValue typedValue = new TypedValue();
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
            int OutlineColor = ContextCompat.getColor(ctx, typedValue.resourceId);

            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimaryContainer, typedValue, true);
            int PrimaryColor = ContextCompat.getColor(ctx, typedValue.resourceId);

            DisplayMetrics displayMetrics = ctx.getResources().getDisplayMetrics();
            int StrokeWidth = Math.round(TypedValue.applyDimension(
                    TypedValue.COMPLEX_UNIT_DIP,
                    2,
                    displayMetrics
            ));

            Drawable background = MinuteText.getBackground();
            LayerDrawable gradientDrawable = (LayerDrawable) background;
            GradientDrawable gradient = (GradientDrawable) gradientDrawable.findDrawableByLayerId(R.id.busScheduleMinuteBackgroundRectangle);
            gradient.setStroke(StrokeWidth,PrimaryColor);
            //gradient.setColor(OutlineColor);

            MinuteText.setTextColor(OutlineColor);


        }
    }

    public ScheduleBusTimeMinuteAdapter(int Hour, int[] Minutes, int MaxPerLine,Context ctx) {
        this.Hour = Hour;
        this.Minutes = Minutes;
        this.MaxPerLine = MaxPerLine;
        this.ctx = ctx;
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
        viewHolder.setData(Hour, Minutes[position], MaxPerLine > position, ctx);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Minutes.length;
    }
}
