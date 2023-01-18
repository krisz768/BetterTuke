package hu.krisz768.bettertuke.SearchFragment;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.models.SearchResult;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private SearchResult[] Results;
    private Context ctx;
    private SearchViewFragment Callback;
    private boolean Fav;

    public static class ViewHolderStop extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView StopName;
        private View view;

        private String StopId;

        public ViewHolderStop(View view) {
            super(view);
            // Define click listener for the ViewHolder's View
            icon = view.findViewById(R.id.SearchStopIcon);
            StopName = view.findViewById(R.id.SearchBusStopName);
            this.view = view;
        }

        public void setData(BusPlaces stop, Context ctx, SearchViewFragment Callback) {
            icon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));

            StopName.setText(stop.getName());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnResultClick(new SearchResult(SearchResult.SearchType.Stop, "", stop));
                }
            });
        }

        public void setFavData(String StopId, Context ctx, SearchViewFragment Callback) {
            this.StopId = StopId;
            icon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));

            DatabaseManager Dm = new DatabaseManager(ctx);

            String StopName = Dm.GetStopName(Integer.parseInt(StopId));
            String StopNum = Dm.GetStopNum(Integer.parseInt(StopId));

            this.StopName.setText(StopName.trim() + " (" + StopNum + ")");

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnResultClick(new SearchResult(SearchResult.SearchType.FavStop, "", Integer.parseInt(StopId)));
                }
            });
        }

        public String GetStopId() {
            return StopId;
        }
    }

    public static class ViewHolderLine extends RecyclerView.ViewHolder {


        private TextView Number;
        private TextView Description;
        private View view;

        public ViewHolderLine(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Number = view.findViewById(R.id.ScheduleLineNum);
            Description = view.findViewById(R.id.SearchBusStopName);
            this.view = view;
        }

        public void setData(BusLine busLine, Context ctx, SearchViewFragment Callback) {
            TypedValue typedValue = new TypedValue();
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

            Number.setTextColor(ContextCompat.getColor(ctx, typedValue.resourceId));
            Number.setText(busLine.getLineName());
            Description.setText(busLine.getLineDesc());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnResultClick(new SearchResult(SearchResult.SearchType.Line, "", busLine));
                }
            });
        }
    }

    public static class ViewHolderLabel extends RecyclerView.ViewHolder {

        private TextView Label;

        public ViewHolderLabel(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Label = view.findViewById(R.id.labelText);
        }

        public void setData(String LabeText) {
            Label.setText(LabeText);
        }
    }

    public SearchAdapter(SearchResult[] Results, Context ctx, SearchViewFragment Callback) {
        this.Results = Results;
        this.ctx = ctx;
        this.Callback = Callback;
        this.Fav = true;
    }

    public void UpdateResults(SearchResult[] Results, boolean Fav) {
        this.Results = Results;
        this.Fav = Fav;
    }

    @Override
    public int getItemViewType(int position) {
        if (Fav) {
            if (position == 0) {
                return -1;
            } else {
                return 1;
            }
        } else {
            switch (Results[position].getType()) {
                case Stop:
                    return 0;
                case Line:
                    return 2;
                case Map:
                    return 3;
            }
        }

        return 0;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case -1:
                View Labelview = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.recview_label, viewGroup, false);

                return new SearchAdapter.ViewHolderLabel(Labelview);
            case 0:
            case 1:
                View Stopview = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.search_stop_recview, viewGroup, false);

                return new SearchAdapter.ViewHolderStop(Stopview);

            case 2:
                View Lineview = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.schedule_line_list_recview, viewGroup, false);

                return new SearchAdapter.ViewHolderLine(Lineview);
        }

        return null;
    }



    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element,

        switch (viewHolder.getItemViewType()) {
            case -1:
                ((ViewHolderLabel)viewHolder).setData("Kedvencek:");
                break;
            case 0:
                ((ViewHolderStop)viewHolder).setData((BusPlaces) Results[position].getData(), ctx, Callback);
                break;
            case 1:
                ((ViewHolderStop)viewHolder).setFavData((String)Results[position-1].getData(), ctx, Callback);
                break;
            case 2:
                ((ViewHolderLine)viewHolder).setData((BusLine) Results[position].getData(), ctx, Callback);
                break;

        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        if (Fav) {
            return Results.length+1;
        } else {
            return Results.length;
        }

    }
}