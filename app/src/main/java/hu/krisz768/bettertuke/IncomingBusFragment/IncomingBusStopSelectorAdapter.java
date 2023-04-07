package hu.krisz768.bettertuke.IncomingBusFragment;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;

public class IncomingBusStopSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final BusStops[] BusStopList;
    private final Context ctx;
    private int SelectedStop;
    private final BottomSheetIncomingBusFragment callback;

    public static class StopViewHolder extends RecyclerView.ViewHolder {

        private final Button button;

        public StopViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.StopButton);
        }

        public void setData(BusStops Data, int SelectedStop, BottomSheetIncomingBusFragment callback, Context ctx) {
            String DirectionText =  HelperProvider.GetStopDirectionString(ctx,Data.getId());
            if (DirectionText.equals("-") || DirectionText.equals("") || DirectionText.equals(" ")) {
                button.setText(ctx.getString(R.string.TrackBusStopSelect, Data.getStopNum().trim()));
            } else {
                button.setText(DirectionText);
            }


           button.setOnClickListener(view -> callback.OnStopClick(Data.getId()));

            TypedValue typedValue = new TypedValue();
            if (Data.getId() != SelectedStop) {
                ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOutline, typedValue, true);
               int color = ContextCompat.getColor(ctx, typedValue.resourceId);
               button.setBackgroundColor(color);
           } else {
                ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
               int color = ContextCompat.getColor(ctx, typedValue.resourceId);
               button.setBackgroundColor(color);
           }
        }
    }

    public static class DateViewHolder extends RecyclerView.ViewHolder {

        private final Button button;

        public DateViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.SDateButton);
        }

        public void setData(BottomSheetIncomingBusFragment callback, Context ctx) {

            button.setText("2023. 04. 05. 15:23");


            //button.setOnClickListener(view -> callback.OnStopClick(Data.getId()));

            TypedValue typedValue = new TypedValue();
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorSecondary, typedValue, true);
            int color = ContextCompat.getColor(ctx, typedValue.resourceId);
            button.setBackgroundColor(color);

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

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        RecyclerView.ViewHolder result;
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.incoming_bus_stop_list_recview, viewGroup, false);

            result = new StopViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.incoming_bus_stop_list_datetime_recview, viewGroup, false);

            result = new DateViewHolder(view);
        }
        return result;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == 0) {
            ((StopViewHolder)viewHolder).setData(BusStopList[position],SelectedStop, callback, ctx);
        } else {
            ((DateViewHolder)viewHolder).setData(callback, ctx);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (position < BusStopList.length) {
            return 0;
        }

        return 1;
    }

    @Override
    public int getItemCount() {
        return BusStopList.length+1;
    }
}
