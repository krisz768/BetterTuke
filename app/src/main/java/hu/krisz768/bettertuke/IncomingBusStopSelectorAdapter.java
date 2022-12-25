package hu.krisz768.bettertuke;

import android.content.Context;
import android.graphics.Color;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;

import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.api_interface.models.IncommingBusRespModel;

public class IncomingBusStopSelectorAdapter extends RecyclerView.Adapter<IncomingBusStopSelectorAdapter.ViewHolder>{
    private BusStops[] BusStopList;
    private Context ctx;
    private int SelectedStop;
    BottomSheetIncomingBusFragment callback;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {

        Button button;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            button = view.findViewById(R.id.StopButton);
        }

        public void setData(BusStops Data, int SelectedStop, BottomSheetIncomingBusFragment callback, Context ctx) {
           button.setText(Data.getKocsiallasSzam().trim() + ". megálló");

           button.setOnClickListener(new View.OnClickListener() {
               @Override
               public void onClick(View view) {
                   callback.OnStopClick(Data.getId());
               }
           });

           if (Data.getId() != SelectedStop) {
               TypedValue typedValue = new TypedValue();
               ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
               int color = ContextCompat.getColor(ctx, typedValue.resourceId);
               button.setBackgroundColor(color);
           } else {
               TypedValue typedValue = new TypedValue();
               ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
               int color = ContextCompat.getColor(ctx, typedValue.resourceId);
               button.setBackgroundColor(color);
           }
        }
    }

    public IncomingBusStopSelectorAdapter(BusStops[] BusStopList, int SelectedStop, BottomSheetIncomingBusFragment callback, Context ctx) {
        this.BusStopList = BusStopList;
        this.SelectedStop = SelectedStop;
        this.ctx = ctx;
        this.callback = callback;
    }

    public void setSelectedStop(int SelectedStop) {
        this.SelectedStop = SelectedStop;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public IncomingBusStopSelectorAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.incoming_bus_stop_list_recview, viewGroup, false);

        return new IncomingBusStopSelectorAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(IncomingBusStopSelectorAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setData(BusStopList[position],SelectedStop, callback, ctx);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return BusStopList.length;
    }
}
