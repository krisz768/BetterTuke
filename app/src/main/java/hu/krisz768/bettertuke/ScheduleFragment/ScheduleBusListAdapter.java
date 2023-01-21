package hu.krisz768.bettertuke.ScheduleFragment;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.Database.BusNum;
import hu.krisz768.bettertuke.R;

public class ScheduleBusListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final BusNum[] busNums;
    private final BusNum[] Favorites;
    private final Context ctx;
    private final ScheduleBusListFragment Callback;

    public static class ViewHolderLine extends RecyclerView.ViewHolder {
        private final TextView Number;
        private final TextView Description;
        private final View view;
        private BusNum busNum;

        public ViewHolderLine(View view) {
            super(view);

            Number = view.findViewById(R.id.ScheduleLineNum);
            Description = view.findViewById(R.id.SearchBusStopName);
            this.view = view;
        }

        public void setData(BusNum busNum, Context ctx, ScheduleBusListFragment Callback) {
            this.busNum = busNum;

            TypedValue typedValue = new TypedValue();
            ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

            Number.setTextColor(ContextCompat.getColor(ctx, typedValue.resourceId));
            Number.setText(busNum.getLineName());
            Description.setText(busNum.getLineDesc());

            view.setOnClickListener(view -> Callback.OnLineClick(busNum.getLineName()));
        }

        public String GetName() {
            return busNum.getLineName();
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

    public ScheduleBusListAdapter(BusNum[] busNums, BusNum[] Favorites, Context ctx, ScheduleBusListFragment Callback) {
        this.busNums = busNums;
        this.ctx = ctx;
        this.Callback = Callback;
        this.Favorites = Favorites;
    }

    @Override
    public int getItemViewType(int position) {
        if (Favorites.length == 0) {
            return 0;
        } else {
            if (position == 0 || position == Favorites.length+1) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        if (viewType == 0) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.schedule_line_list_recview, viewGroup, false);

            return new ScheduleBusListAdapter.ViewHolderLine(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.recview_label, viewGroup, false);

            return new ScheduleBusListAdapter.ViewHolderLabel(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, final int position) {

        if (Favorites.length == 0) {
            ((ViewHolderLine)viewHolder).setData(busNums[position], ctx, Callback);
        } else {
            if (position == 0 || position == Favorites.length+1) {
                if (position == 0) {
                    ((ViewHolderLabel)viewHolder).setData(ctx.getString(R.string.FavoriteText));
                } else {
                    ((ViewHolderLabel)viewHolder).setData(Callback.getString(R.string.SumText));
                }
            } else {
                if (position < Favorites.length+1) {
                    ((ViewHolderLine)viewHolder).setData(Favorites[position-1], ctx, Callback);
                } else {
                    ((ViewHolderLine)viewHolder).setData(busNums[position-Favorites.length-2], ctx, Callback);
                }
            }
        }
    }

    @Override
    public int getItemCount() {
        if (Favorites.length == 0) {
            return busNums.length;
        } else {
            return busNums.length + Favorites.length + 2;
        }
    }
}
