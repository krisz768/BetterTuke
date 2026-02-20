package hu.krisz768.bettertuke.ActiveBusFragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;

public class BusTypeListAdapter extends RecyclerView.Adapter<BusTypeListAdapter.ViewHolder> {
    private final ActiveBusTypeRespModel[] BusList;
    private final BusTypeListFragment ClickCallBack;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView Name;
        private final ImageView Icon;
        private final View view;

        public ViewHolder(View view) {
            super(view);

            Icon = view.findViewById(R.id.BusTypeIcon);
            Name = view.findViewById(R.id.BusTypeName);
            this.view = view;
        }

        public void setData(ActiveBusTypeRespModel Data, BusTypeListFragment ClickCallBack) {

            view.setOnClickListener(view -> ClickCallBack.OnBusClick(Data.getBusTypeName()));
            Icon.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapBus));

            Name.setText(ClickCallBack.getString(R.string.BusTypeRec, Data.getBusTypeName(), Data.getTripIds().size()));
        }
    }

    public BusTypeListAdapter(ActiveBusTypeRespModel[] BusList, BusTypeListFragment ClickCallBack) {
        this.BusList = BusList;
        this.ClickCallBack = ClickCallBack;
    }

    @NonNull
    @Override
    public BusTypeListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.bus_type_list_recview, viewGroup, false);

        return new BusTypeListAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(BusTypeListAdapter.ViewHolder viewHolder, final int position) {
        viewHolder.setData(BusList[position], ClickCallBack);
    }

    @Override
    public int getItemCount() {
        return BusList.length;
    }
}
