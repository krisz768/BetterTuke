package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.R;

public class ScheduleBusListAdapter extends RecyclerView.Adapter<ScheduleBusListAdapter.ViewHolder>{

    private BusLine[] BusLines;
    private Context ctx;
    private ScheduleBusListFragment Callback;

    public static class ViewHolder extends RecyclerView.ViewHolder {

        private TextView Number;
        private TextView Description;
        private View view;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Number = view.findViewById(R.id.ScheduleLineNum);
            Description = view.findViewById(R.id.ScheduleLineDesc);
            this.view = view;
        }

        public void setData(BusLine busLine, Context ctx, ScheduleBusListFragment Callback) {
            TypedValue typedValue = new TypedValue();
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

            Number.setTextColor(ContextCompat.getColor(ctx, typedValue.resourceId));
            Number.setText(busLine.getLineName());
            Description.setText(busLine.getLineDesc());

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnLineClick(busLine.getLineName());
                }
            });
        }
    }

    public ScheduleBusListAdapter(BusLine[] BusLines, Context ctx, ScheduleBusListFragment Callback) {
        this.BusLines = BusLines;
        this.ctx = ctx;
        this.Callback = Callback;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public ScheduleBusListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.schedule_line_list_recview, viewGroup, false);

        return new ScheduleBusListAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(ScheduleBusListAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        viewHolder.setData(BusLines[position], ctx, Callback);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return BusLines.length;
    }
}
