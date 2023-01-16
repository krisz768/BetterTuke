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
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.models.SearchResult;

public class SearchAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{

    private SearchResult[] Results;
    private Context ctx;
    private SearchViewFragment Callback;

    public static class ViewHolderStop extends RecyclerView.ViewHolder {

        private ImageView icon;
        private TextView StopName;
        private View view;

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

    public SearchAdapter(SearchResult[] Results, Context ctx, SearchViewFragment Callback) {
        this.Results = Results;
        this.ctx = ctx;
        this.Callback = Callback;
    }

    public void UpdateResults(SearchResult[] Results) {
        this.Results = Results;
    }

    @Override
    public int getItemViewType(int position) {
        switch (Results[position].getType()) {
            case Stop:
                return 0;
            case Line:
                return 1;
            case Map:
                return 3;
        }

        return 0;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        switch (viewType) {
            case 0:
                View Stopview = LayoutInflater.from(viewGroup.getContext())
                        .inflate(R.layout.search_stop_recview, viewGroup, false);

                return new SearchAdapter.ViewHolderStop(Stopview);
            case 1:
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
            case 0:
                ((ViewHolderStop)viewHolder).setData((BusPlaces) Results[position].getData(), ctx, Callback);
                break;
            case 1:
                ((ViewHolderLine)viewHolder).setData((BusLine) Results[position].getData(), ctx, Callback);
                break;

        }
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return Results.length;
    }
}