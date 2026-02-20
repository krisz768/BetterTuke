package hu.krisz768.bettertuke.ActiveBusFragment;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import hu.krisz768.bettertuke.Database.LineInfoRouteInfo;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;

public class BusTypeRouteListAdapter  extends RecyclerView.Adapter<BusTypeRouteListAdapter.ViewHolder> {
    private final ActiveBusTypeRespModel BusData;
    private final BusTypeRouteListFragment ClickCallBack;
    private final ArrayList<LineInfoRouteInfo> Data;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView Name;
        private final TextView Desc;
        private final TextView Plate;
        private final View view;

        public ViewHolder(View view) {
            super(view);

            Name = view.findViewById(R.id.BusTypeListArrNumText);
            Desc = view.findViewById(R.id.BusTypeArrDesText);
            Plate = view.findViewById(R.id.BusTypeLicensePlate);

            this.view = view;
        }

        public void setData(LineInfoRouteInfo Data, String LPlate, BusTypeRouteListFragment ClickCallBack) {

            view.setOnClickListener(view -> ClickCallBack.OnBusClick(Data.getId()));

            Name.setText(Data.getLineNum());
            Desc.setText(Data.getLineName());
            Plate.setText(LPlate);

            int WhiteColor = Color.rgb(255,255,255);
            Name.setTextColor(WhiteColor);
        }
    }

    public BusTypeRouteListAdapter(ActiveBusTypeRespModel BusData, BusTypeRouteListFragment ClickCallBack) {
        this.BusData = BusData;
        this.ClickCallBack = ClickCallBack;

        Data = new ArrayList<>();

        if (ClickCallBack.getContext() == null)
            return;

        NewGTFSDatabase db = new NewGTFSDatabase(ClickCallBack.getContext());

        for (int i = 0; i < BusData.getTripIds().size(); i++) {
            LineInfoRouteInfo temp = db.GetBusLineRouteInfoById(BusData.getTripIds().get(i));

            String newName = db.GetBusNameByStop(BusData.getTripIds().get(i),BusData.getStopIds().get(i));
            if (newName != null) {
                temp.setLineName(newName);
            }

            Data.add(temp);
        }

        for (int i = 0; i < Data.size(); i++) {
            if ( Data.get(i) == null) {
                Data.remove(i);
                BusData.getTripIds().remove(i);
                BusData.getLPlates().remove(i);
                BusData.getStopIds().remove(i);
                i--;
            }
        }
    }

    @NonNull
    @Override
    public BusTypeRouteListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.bus_type_route_list_recview, viewGroup, false);

        return new BusTypeRouteListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusTypeRouteListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(Data.get(position), BusData.getLPlates().get(position) , ClickCallBack);
    }

    @Override
    public int getItemCount() {
        return BusData.getTripIds().size();
    }
}
