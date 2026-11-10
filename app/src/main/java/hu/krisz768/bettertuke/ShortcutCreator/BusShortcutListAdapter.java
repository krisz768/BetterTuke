package hu.krisz768.bettertuke.ShortcutCreator;

import android.content.Context;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.BusShortcutCreatorActivity;
import hu.krisz768.bettertuke.Database.BusNum;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.StopShortcutCreatorActivity;

public class BusShortcutListAdapter  extends RecyclerView.Adapter<BusShortcutListAdapter.ViewHolder>{
    private final BusNum[] buses;
    private final BusShortcutCreatorActivity Callback;
    private final int FavCount;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView BusNum;
        private final ImageView FavIcon;
        private final TextView BusDesc;
        private final View view;

        public ViewHolder(View view) {
            super(view);
            BusNum = view.findViewById(R.id.ShortcutLineNum);
            BusDesc = view.findViewById(R.id.ShortcutBusName);
            FavIcon = view.findViewById(R.id.ShortcutFavIcon);
            this.view = view;
        }

        public void setData(BusNum line, BusShortcutCreatorActivity Callback, boolean Fav) {
            if (Fav) {
                FavIcon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                FavIcon.setVisibility(View.VISIBLE);
            } else {
                FavIcon.setVisibility(View.GONE);
            }

            TypedValue typedValue = new TypedValue();
            Callback.getTheme().resolveAttribute(com.google.android.material.R.attr.colorOnPrimary, typedValue, true);

            BusNum.setTextColor(ContextCompat.getColor(Callback, typedValue.resourceId));

            BusNum.setText(line.getLineName());
            BusDesc.setText(line.getLineDesc());

            view.setOnClickListener(view -> Callback.OnBusClick(line.getLineName()));
        }
    }

    public BusShortcutListAdapter(BusNum[] buses, BusShortcutCreatorActivity Callback, int FavCount) {
        this.buses = buses;
        this.Callback = Callback;
        this.FavCount = FavCount;
    }

    @NonNull
    @Override
    public BusShortcutListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.shortcut_line_list_recview, viewGroup, false);

        return new BusShortcutListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusShortcutListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(buses[position], Callback, position < FavCount);
    }

    @Override
    public int getItemCount() {
        return buses.length;
    }
}