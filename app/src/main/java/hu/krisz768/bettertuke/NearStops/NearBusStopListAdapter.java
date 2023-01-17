package hu.krisz768.bettertuke.NearStops;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListAdapter;
import hu.krisz768.bettertuke.IncomingBusFragment.IncomingBusListFragment;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.SearchFragment.SearchViewFragment;
import hu.krisz768.bettertuke.api_interface.models.IncommingBusRespModel;
import hu.krisz768.bettertuke.models.SearchResult;

public class NearBusStopListAdapter extends RecyclerView.Adapter<NearBusStopListAdapter.ViewHolder>{
    private BusPlaces[] busPlaces;
    private NearBusStopListFragment Callback;
    private int FavCount;

    /**
     * Provide a reference to the type of views that you are using
     * (custom ViewHolder)
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView icon;
        private ImageView FavIcon;
        private TextView StopName;
        private View view;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
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

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnStopClick(stop.getId());
                }
            });
        }
    }

    public NearBusStopListAdapter(BusPlaces[] busPlaces, NearBusStopListFragment Callback, int FavCount) {
        this.busPlaces = busPlaces;
        this.Callback = Callback;
        this.FavCount = FavCount;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public NearBusStopListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.near_stop_recview, viewGroup, false);

        return new NearBusStopListAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(NearBusStopListAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setData(busPlaces[position], Callback, position < FavCount);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return busPlaces.length;
    }
}
