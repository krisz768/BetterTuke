package hu.krisz768.bettertuke.NearStops;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.LineInfoRouteInfo;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.BusPositionRespModel;

public class NearBusStopListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final BusPlaces[] busPlaces;
    private final NearBusStopListFragment Callback;
    private final int FavCount;
    private final ArrayList<LineInfoRouteInfo> NearBuses;
    private int FavBusCount;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final ImageView icon;
        private final ImageView FavIcon;
        private final TextView StopName;
        private final View view;

        public ViewHolder(View view) {
            super(view);
            icon = view.findViewById(R.id.NearStopIcon);
            StopName = view.findViewById(R.id.NearBusStopName);
            FavIcon = view.findViewById(R.id.NearFavIcon);
            this.view = view;
        }

        public void setData(BusPlaces stop, NearBusStopListFragment Callback, boolean Fav) {
            icon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));
            if (Fav) {
                FavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                FavIcon.setVisibility(View.VISIBLE);
            } else {
                FavIcon.setVisibility(View.GONE);
            }

            StopName.setText(stop.getName());

            view.setOnClickListener(view -> Callback.OnStopClick(stop.getId()));
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
        }
    }

    public static class ViewHolderBus extends RecyclerView.ViewHolder {
        private final TextView Name;
        private final TextView Desc;
        private final ImageView FavIcon;
        private final View view;

        public ViewHolderBus(View view) {
            super(view);
            Name = view.findViewById(R.id.NearBusListArrNumText);
            Desc = view.findViewById(R.id.NearBusArrDesText);
            FavIcon = view.findViewById(R.id.NearBusShortcutFavIcon);
            this.view = view;
        }

        public void setData(LineInfoRouteInfo line, NearBusStopListFragment Callback, boolean Fav) {
            if (Fav) {
                FavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                FavIcon.setVisibility(View.VISIBLE);
            } else {
                FavIcon.setVisibility(View.GONE);
            }

            view.setOnClickListener(view -> Callback.OnBusClick(line.getId()));

            Name.setText(line.getLineNum());
            Desc.setText(line.getLineName());

            int WhiteColor = Color.rgb(255,255,255);
            Name.setTextColor(WhiteColor);

        }
    }

    public NearBusStopListAdapter(BusPlaces[] busPlaces, NearBusStopListFragment Callback, int FavCount, BusPositionRespModel[] NearBuses, int FavBusCount) {
        this.busPlaces = busPlaces;
        this.Callback = Callback;
        this.FavCount = FavCount;
        this.FavBusCount = FavBusCount;

        this.NearBuses = new ArrayList<>();

        if (Callback.getContext() == null)
            return;

        NewGTFSDatabase db = new NewGTFSDatabase(Callback.getContext());

        for (BusPositionRespModel nearBus : NearBuses) {
            LineInfoRouteInfo temp = db.GetBusLineRouteInfoById(nearBus.getTripId());

            NewGTFSDatabase NDm = new NewGTFSDatabase(Callback.getContext());

            String newName = NDm.GetBusNameByStop(nearBus.getTripId(), nearBus.getStopId());
            if (newName != null) {
                temp.setLineName(newName);
            }

            this.NearBuses.add(temp);
        }

        for (int i = 0; i < this.NearBuses.size(); i++) {
            if ( this.NearBuses.get(i) == null) {
                this.NearBuses.remove(i);
                if (this.FavBusCount > i) {
                    this.FavBusCount--;
                }
                i--;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.near_stop_recview, viewGroup, false);

            return new NearBusStopListAdapter.ViewHolder(view);
        } else if (viewType == 1) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recview_label, viewGroup, false);

            return new NearBusStopListAdapter.ViewHolderLabel(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.near_bus_list_recview, viewGroup, false);

            return new NearBusStopListAdapter.ViewHolderBus(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == 0) {
            ((ViewHolder)viewHolder).setData(busPlaces[position], Callback, position < FavCount);
        } else if (viewHolder.getItemViewType() == 1) {
            ((ViewHolderLabel)viewHolder).setData(Callback.getString(R.string.NearBuses));
        } else {
            ((ViewHolderBus)viewHolder).setData(NearBuses.get(position- (busPlaces.length+1)), Callback, (position- (busPlaces.length+1)) < FavBusCount);
        }

    }

    @Override
    public int getItemViewType(int position) {
        if (position < busPlaces.length) {
            return 0;
        } else if (position == busPlaces.length) {
            return 1;
        } else {
            return 2;
        }
    }

    @Override
    public int getItemCount() {
        return busPlaces.length + NearBuses.size() + (NearBuses.isEmpty() ? 0 : 1);
    }
}
