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
    private int PrimaryColor;
    private Drawable Background;
    private Drawable FullBackground;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView MinuteText;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            MinuteText = view.findViewById(R.id.ScheduleMinuteText);
        }

        public void setData(int Hour, int Minute, boolean Firstrow, int PrimaryColor, Drawable Background, Drawable FullBackground) {
            MinuteText.setText(String.format("%02d", Minute));
            if (Firstrow) {
                MinuteText.setBackground(FullBackground);
            } else {
                MinuteText.setBackground(Background);
            }

            MinuteText.setTextColor(PrimaryColor);


        }
    }

    public ScheduleBusTimeMinuteAdapter(int Hour, int[] Minutes, int MaxPerLine,int PrimaryColor, Drawable Background, Drawable FullBackground) {
        this.Hour = Hour;
        this.Minutes = Minutes;
        this.MaxPerLine = MaxPerLine;
        this.PrimaryColor = PrimaryColor;
        this.Background = Background;
        this.FullBackground = FullBackground;
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
        viewHolder.setData(Hour, Minutes[position], MaxPerLine > position, PrimaryColor, Background,FullBackground);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Minutes.length;
    }
}
