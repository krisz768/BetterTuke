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
import hu.krisz768.bettertuke.R;

public class IncomingBusStopSelectorAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final BusStops[] BusStopList;
    private final Context ctx;
    private int SelectedStop;
    private final BottomSheetIncomingBusFragment callback;
    private final String[] BusStopNames;

    public static class StopViewHolder extends RecyclerView.ViewHolder {
        private final Button button;

        public StopViewHolder(View view) {
            super(view);
            button = view.findViewById(R.id.StopButton);
        }

        public void setData(BusStops Data, int SelectedStop, BottomSheetIncomingBusFragment callback, String DirectionText, Context ctx) {
            if (DirectionText.equals("-") || DirectionText.equals("") || DirectionText.equals(" ")) {
                if (Data.getId() == -1) {
                    button.setText(Data.getStopNum().trim());
                } else {
                    button.setText(ctx.getString(R.string.TrackBusStopSelect, Data.getStopNum().trim()));
                }

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

    public IncomingBusStopSelectorAdapter(BusStops[] BusStopList, int SelectedStop, BottomSheetIncomingBusFragment callback, String[] busStopNames, Context ctx) {
        this.BusStopList = BusStopList;
        this.SelectedStop = SelectedStop;
        this.ctx = ctx;
        this.callback = callback;
        this.BusStopNames = busStopNames;
    }

    public void setSelectedStop(int SelectedStop) {
        this.SelectedStop = SelectedStop;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.incoming_bus_stop_list_recview, viewGroup, false);

        return new StopViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {
        if (position < BusStopList.length) {
            ((StopViewHolder)viewHolder).setData(BusStopList[position],SelectedStop, callback, BusStopNames[position], ctx);
        } else {
            ((StopViewHolder)viewHolder).setData(new BusStops(-1, "", 0, 0, ctx.getString(R.string.AllStopButton)),SelectedStop, callback, "", ctx);
        }
    }

    @Override
    public int getItemViewType(int position) {
        return 0;
    }

    @Override
    public int getItemCount() {
        return BusStopList.length+1;
    }
}
