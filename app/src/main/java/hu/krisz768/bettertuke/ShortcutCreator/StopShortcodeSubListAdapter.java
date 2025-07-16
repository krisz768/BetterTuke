package hu.krisz768.bettertuke.ShortcutCreator;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.StopShortcutCreatorActivity;

public class StopShortcodeSubListAdapter  extends RecyclerView.Adapter<StopShortcodeSubListAdapter.ViewHolder>{
    private final BusStops[] busStops;
    private final StopShortcutCreatorActivity Callback;
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

        public void setData(BusStops stop, StopShortcutCreatorActivity Callback, boolean Fav) {
            icon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));

            if (Fav) {
                FavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                FavIcon.setVisibility(View.VISIBLE);
            } else {
                FavIcon.setVisibility(View.GONE);
            }
            NewGTFSDatabase Dm = new NewGTFSDatabase(Callback);
            String StopName = Dm.GetStopName(stop.getId());
            String StopNum = HelperProvider.GetStopDirectionString(Callback,stop.getId());

            this.StopName.setText(Callback.getString(R.string.BusStopNameWithNum, StopName.trim(), StopNum));

            view.setOnClickListener(view -> Callback.OnStopClick(stop.getId()));
        }
    }

    public StopShortcodeSubListAdapter(BusStops[] busStops, StopShortcutCreatorActivity Callback, int FavCount) {
        this.busStops = busStops;
        this.Callback = Callback;
        this.FavCount = FavCount;
    }

    @NonNull
    @Override
    public StopShortcodeSubListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.near_stop_recview, viewGroup, false);

        return new StopShortcodeSubListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(StopShortcodeSubListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(busStops[position], Callback, position < FavCount);
    }

    @Override
    public int getItemCount() {
        return busStops.length;
    }
}