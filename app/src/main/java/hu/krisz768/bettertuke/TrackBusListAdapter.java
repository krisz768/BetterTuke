package hu.krisz768.bettertuke;

import android.app.admin.DelegatedAdminReceiver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.telecom.Call;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.util.Calendar;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.JaratInfoMenetido;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class TrackBusListAdapter extends RecyclerView.Adapter<TrackBusListAdapter.ViewHolder>{
    private JaratInfoMenetido[] BusStopList;
    private final BusPlaces[] BusPlaceList;
    private final BusStops[] AllBusStopList;
    private Context ctx;
    private Calendar StartTime;
    private TrackBusRespModel BusPosition;
    private TrackBusListFragment Callback;
    private int CurrentStop;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView Name;
        private TextView Time;
        private TextView Delay;
        private ImageView trackGraphic;
        private View view;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Name = (TextView) view.findViewById(R.id.TrackBusStopName);
            Time = (TextView) view.findViewById(R.id.TrackBusStopTime);
            Delay = (TextView) view.findViewById(R.id.TrackBusDelayTime);
            trackGraphic = view.findViewById(R.id.trackGraphic);

            this.view = view;
        }

        public void setData(JaratInfoMenetido Data, int Pos, int max, int PrevStopId, Context ctx,Calendar StartTime, BusPlaces[] BusPlaceList, BusStops[] AllBusStopList,TrackBusRespModel BusPosition, TrackBusListFragment Callback, int CurrentStop) {
            int Kocsiallasid = Data.getKocsiallasId();

            BusPlaces BusPlace = null;

            view.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    Callback.OnStopClick(Data.getKocsiallasId());
                }
            });

            for (int i = 0; i < AllBusStopList.length; i++) {
                if (Kocsiallasid == AllBusStopList[i].getId()) {
                    int Foldhelyid = AllBusStopList[i].getFoldhely();
                    for (int j = 0; j < BusPlaceList.length; j++) {
                        if (Foldhelyid == BusPlaceList[j].getId()) {
                            BusPlace = BusPlaceList[j];
                            break;
                        }
                    }
                    break;
                }
            }

            Name.setText((Pos+1) + " - " + BusPlace.getName());

            TypedValue typedValue = new TypedValue();
            if (Data.getKocsiallasId() == CurrentStop) {
                ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                Name.setTextColor(ContextCompat.getColor(ctx, typedValue.resourceId));
            } else {
                ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.color, typedValue, true);
                Name.setTextColor(Time.getTextColors());
            }

            Calendar ArriveTime = (Calendar) StartTime.clone();

            ArriveTime.add(Calendar.MINUTE, Data.getOsszegzettMenetIdoPerc());
            String TimeString = String.format("%02d", ArriveTime.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", ArriveTime.get(Calendar.MINUTE));
            Time.setText(TimeString);

            if (BusPosition != null) {
                if (Pos == 0) {
                    if (BusPosition.getMegalloid() == Data.getKocsiallasId()) {
                        if (BusPosition.isMegalloban()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartHalf));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartFull));
                        }
                    } else if (BusPosition.getMegalloSorszam() > Data.getSorrend()){
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartFull));
                    } else {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartEmpty));
                    }
                } else if(Pos+1 == max) {
                    if (BusPosition.getMegalloid() == Data.getKocsiallasId()) {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndHalf));
                    } else if (BusPosition.getMegalloSorszam() < Data.getSorrend()) {
                        if (PrevStopId == BusPosition.getMegalloSorszam() && !BusPosition.isMegalloban()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndInc));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndEmpty));
                        }
                    } else
                    {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndFull));
                    }
                } else {
                    if (BusPosition.getMegalloid() == Data.getKocsiallasId()) {
                        if (BusPosition.isMegalloban()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalHalf));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalFull));
                        }
                    } else if (BusPosition.getMegalloSorszam() > Data.getSorrend()) {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalFull));
                    } else {
                        if (PrevStopId == BusPosition.getMegalloSorszam() && !BusPosition.isMegalloban()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalInc));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalEmpty));
                        }
                    }
                }

                if (BusPosition.getKesesPerc() < 0) {
                    Delay.setText(BusPosition.getKesesPerc() + " perc");
                    Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#d60202")));
                }else {
                    Delay.setText("+" + BusPosition.getKesesPerc() + " perc");
                    if (BusPosition.getKesesPerc() == 0) {
                        Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#02de32")));
                    } else {
                        Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#a39b00")));
                    }
                }
            } else {
                Delay.setText("");
                if (Pos == 0) {
                    trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartEmpty));
                } else if(Pos+1 == max) {
                    trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndEmpty));
                } else {
                    trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalEmpty));
                }
            }




        }
    }

    public void UpdateList(TrackBusRespModel TrackData) {
        this.BusPosition = TrackData;
    }

    public TrackBusListAdapter(JaratInfoMenetido[] BusStopList, BusPlaces[] BusPlaceList, BusStops[] AllBusStopList, Calendar StartTime, TrackBusRespModel BusPosition, int CurrentStop, TrackBusListFragment Callback,Context ctx) {
        this.BusStopList = BusStopList;
        this.BusPlaceList = BusPlaceList;
        this.AllBusStopList = AllBusStopList;
        this.StartTime = StartTime;
        this.BusPosition = BusPosition;
        this.ctx = ctx;
        this.Callback = Callback;
        this.CurrentStop = CurrentStop;
    }

    // Create new views (invoked by the layout manager)
    @Override
    public TrackBusListAdapter.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        // Create a new view, which defines the UI of the list item
        View view = LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.track_bus_list_recview, viewGroup, false);

        return new TrackBusListAdapter.ViewHolder(view);
    }

    // Replace the contents of a view (invoked by the layout manager)
    @Override
    public void onBindViewHolder(TrackBusListAdapter.ViewHolder viewHolder, final int position) {

        // Get element from your dataset at this position and replace the
        // contents of the view with that element
        int max = BusStopList.length;

        int PrevStopId = position > 0 ? BusStopList[position-1].getSorrend() : -1;

        viewHolder.setData(BusStopList[position],position, max, PrevStopId, ctx, StartTime, BusPlaceList, AllBusStopList, BusPosition, Callback, CurrentStop);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return BusStopList.length;
    }
}
