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

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.R;

public class ScheduleBusTimeHourAdapter extends RecyclerView.Adapter<ScheduleBusTimeHourAdapter.ViewHolder>{

    private int[] Hours;
    private int[][] Minutes;
    private Context ctx;
    private int MaxPerLine;

    private int PrimaryColor;
    private int PrimaryContainerColor;
    private int OnPrimaryContainerColor;
    private Drawable MinBackground;
    private Drawable MinFullBackground;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView HourText;
        private RecyclerView Recv;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            HourText = view.findViewById(R.id.ScheduleHourText);
            Recv = view.findViewById(R.id.ScheduleMinuteRecview);
        }

        public void setData(int Hour, int[] Minutes, int MaxPerLine, int PrimaryColor, int PrimaryContainerColor, int OnPrimaryContainerColor, Drawable MinBackground, Drawable MinFullBackground, Context ctx) {
            HourText.setText(String.format("%02d", Hour));

            HourText.setTextColor(OnPrimaryContainerColor);

            Drawable background = HourText.getBackground();
            GradientDrawable gradientDrawable = (GradientDrawable) background;
            gradientDrawable.setColor(PrimaryContainerColor);

            ScheduleBusTimeMinuteAdapter Sbta = new ScheduleBusTimeMinuteAdapter(Hour, Minutes, MaxPerLine, PrimaryColor, MinBackground, MinFullBackground);
            GridLayoutManager mLayoutManager = new GridLayoutManager(ctx, MaxPerLine);
            Recv.setLayoutManager(mLayoutManager);
            Recv.setAdapter(Sbta);
        }
    }

    public ScheduleBusTimeHourAdapter(int[] Hours, int[][] Minutes, int MaxPerLine,int PrimaryColor, int PrimaryContainerColor, int OnPrimaryContainerColor, Drawable MinBackground, Drawable MinFullBackground, Context ctx) {
        this.Hours = Hours;
        this.Minutes = Minutes;
        this.ctx = ctx;
        this.MaxPerLine = MaxPerLine;
        this.PrimaryColor = PrimaryColor;
        this.PrimaryContainerColor = PrimaryContainerColor;
        this.OnPrimaryContainerColor = OnPrimaryContainerColor;
        this.MinBackground = MinBackground;
        this.MinFullBackground = MinFullBackground;
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
        viewHolder.setData(Hours[position], Minutes[position], MaxPerLine, PrimaryColor, PrimaryContainerColor, OnPrimaryContainerColor, MinBackground,MinFullBackground, ctx);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Hours.length;
    }
}
