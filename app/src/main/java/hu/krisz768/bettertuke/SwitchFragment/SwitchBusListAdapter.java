package hu.krisz768.bettertuke.SwitchFragment;

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

import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListAdapter;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListFragment;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

public class SwitchBusListAdapter extends RecyclerView.Adapter<SwitchBusListAdapter.ViewHolder>{
    private IncomingBusRespModel[] BusList;
    private final Context ctx;
    private final SwitchBusListFregment ClickCallBack;
    private final String Date;
    private final String StopID;
    private final String CurrentStopID;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView number;
        private final TextView desc;
        private final TextView arrtime;
        private final View view;

        public ViewHolder(View view) {
            super(view);

            number = view.findViewById(R.id.BusArrNumText);
            desc = view.findViewById(R.id.BusArrDesText);
            arrtime = view.findViewById(R.id.BusArrTime);
            this.view = view;
        }

        public void setData(IncomingBusRespModel Data, Context ctx, SwitchBusListFregment ClickCallBack, String Date, String StopID, String CurrentStopID) {

            /*if (Custom) {
                view.setOnClickListener(view -> ClickCallBack.OnBusClick(Data.getLineId(), Date));
            } else  {*/
                view.setOnClickListener(view -> ClickCallBack.OnBusClick(Data.getLineId(), Date, StopID, CurrentStopID));
            //}


            number.setText(Data.getLineNum());
            desc.setText(Data.getLineName());
            Date Arrtime = Data.getArriveTime();
            SimpleDateFormat Sdf = new SimpleDateFormat("HH:mm", Locale.US);
            SimpleDateFormat Sdf2 = new SimpleDateFormat("yyyyMMdd", Locale.US);
            if (Data.isAtStop()) {
                arrtime.setText(R.string.BusInStop);
            } else if (Data.getRemainingMin()==1){
                arrtime.setText(ctx.getString(R.string.TrackTimeStringOneMinute, Sdf.format(Arrtime), Data.getRemainingMin()));
            } else {
                arrtime.setText(ctx.getString(R.string.TrackTimeString, Sdf.format(Arrtime), Data.getRemainingMin()));
            } /*else if (!Date.equals(Sdf2.format(new Date()))) {

                StringBuilder str = new StringBuilder(Date);

                str.insert(6, ". ");
                str.insert(4, ". ");

                arrtime.setText(ctx.getString(R.string.IncBusAnotherDay, str + ". " + Sdf.format(Arrtime)));
            } else {
                arrtime.setText(Sdf.format(Arrtime));
            }*/


            int WhiteColor = Color.rgb(255,255,255);
            number.setTextColor(WhiteColor);

            if (Data.isStarted()) {
                number.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_number_background_active));
            } else if (Data.isMiss()){
                number.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_number_background_miss));
            } else {
                number.setBackground(ContextCompat.getDrawable(ctx, R.drawable.bus_number_background_inactive));
            }
        }
    }

    public void UpdateList(IncomingBusRespModel[] BusList) {
        this.BusList = BusList;
    }

    public SwitchBusListAdapter(IncomingBusRespModel[] BusList, String Date, Context ctx, SwitchBusListFregment ClickCallBack, String StopID, String CurrentStopID) {
        this.BusList = BusList;
        this.ctx = ctx;
        this.ClickCallBack = ClickCallBack;
        this.Date = Date;
        this.StopID = StopID;
        this.CurrentStopID = CurrentStopID;
    }

    @NonNull
    @Override
    public SwitchBusListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.incoming_bus_list_recview, viewGroup, false);

        return new SwitchBusListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(SwitchBusListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(BusList[position], ctx, ClickCallBack, Date, StopID, CurrentStopID);
    }

    @Override
    public int getItemCount() {
        return BusList.length;
    }
}