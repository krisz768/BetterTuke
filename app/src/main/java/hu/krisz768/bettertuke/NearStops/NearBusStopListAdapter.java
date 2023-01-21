package hu.krisz768.bettertuke.NearStops;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;

public class NearBusStopListAdapter extends RecyclerView.Adapter<NearBusStopListAdapter.ViewHolder>{
    private final BusPlaces[] busPlaces;
    private final NearBusStopListFragment Callback;
    private final int FavCount;

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
            } else {
                FavIcon.setVisibility(View.GONE);
            }

            StopName.setText(stop.getName());

            view.setOnClickListener(view -> Callback.OnStopClick(stop.getId()));
        }
    }

    public NearBusStopListAdapter(BusPlaces[] busPlaces, NearBusStopListFragment Callback, int FavCount) {
        this.busPlaces = busPlaces;
        this.Callback = Callback;
        this.FavCount = FavCount;
    }

    @NonNull
    @Override
    public NearBusStopListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.near_stop_recview, viewGroup, false);

        return new NearBusStopListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NearBusStopListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(busPlaces[position], Callback, position < FavCount);
    }

    @Override
    public int getItemCount() {
        return busPlaces.length;
    }
}
