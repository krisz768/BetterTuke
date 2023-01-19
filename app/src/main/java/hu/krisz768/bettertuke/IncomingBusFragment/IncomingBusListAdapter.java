package hu.krisz768.bettertuke.IncomingBusFragment;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

public class IncomingBusListAdapter extends RecyclerView.Adapter<IncomingBusListAdapter.ViewHolder>{
    private IncomingBusRespModel[] BusList;
    private final Context ctx;
    private final IncomingBusListFragment ClickCallBack;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView number;
        private final TextView desc;
        private final TextView arrtime;
        private final View view;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            number = (TextView) view.findViewById(R.id.BusArrNumText);
            desc = (TextView) view.findViewById(R.id.BusArrDesText);
            arrtime = (TextView) view.findViewById(R.id.BusArrTime);
            this.view = view;
        }

        public void setData(IncomingBusRespModel Data, Context ctx, IncomingBusListFragment ClickCallBack) {

            view.setOnClickListener(view -> ClickCallBack.OnBusClick(Data.getLineId()));

            number.setText(Data.getLineNum());
            desc.setText(Data.getLineName());
            Date Arrtime = Data.getArriveTime();
            SimpleDateFormat Sdf = new SimpleDateFormat("HH:mm", Locale.US);
            if (Data.isAtStop()) {
                arrtime.setText(R.string.BusInStop);
            } else {
                arrtime.setText(Sdf.format(Arrtime) + " (" + Data.getRemainingMin() + " perc)");
            }

            int WhiteColor = Color.rgb(255,255,255);
            number.setTextColor(WhiteColor);

            if (Data.isStarted()) {
                number.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_number_background_active));
            } else  {
                number.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_number_background_inactive));
            }

        }
    }

    public void UpdateList(IncomingBusRespModel[] BusList) {
        this.BusList = BusList;
    }

    public IncomingBusListAdapter(IncomingBusRespModel[] BusList, Context ctx, IncomingBusListFragment ClickCallBack) {
        this.BusList = BusList;
        this.ctx = ctx;
        this.ClickCallBack = ClickCallBack;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.incoming_bus_list_recview, viewGroup, false);

        return new ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setData(BusList[position], ctx, ClickCallBack);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return BusList.length;
    }
}
