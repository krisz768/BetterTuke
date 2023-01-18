package hu.krisz768.bettertuke.TrackBusFragment;

import android.content.Context;
import android.graphics.Color;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.color.MaterialColors;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.LineInfoTravelTime;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class TrackBusListAdapter extends RecyclerView.Adapter<TrackBusListAdapter.ViewHolder>{
    private final LineInfoTravelTime[] BusStopList;
    private final BusPlaces[] BusPlaceList;
    private final BusStops[] AllBusStopList;
    private final Context ctx;
    private final Calendar StartTime;
    private TrackBusRespModel BusPosition;
    private final TrackBusListFragment Callback;
    private final int CurrentStop;
    private final String Date;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView Name;
        private final TextView Time;
        private final TextView Delay;
        private final ImageView trackGraphic;
        private final View view;

        public ViewHolder(View view) {
            super(view);
            // Define click listener for the ViewHolder's View

            Name = (TextView) view.findViewById(R.id.TrackBusStopName);
            Time = (TextView) view.findViewById(R.id.TrackBusStopTime);
            Delay = (TextView) view.findViewById(R.id.TrackBusDelayTime);
            trackGraphic = view.findViewById(R.id.trackGraphic);

            this.view = view;
        }

        public void setData(LineInfoTravelTime Data, int Pos, int max, int PrevStopId, Context ctx, Calendar StartTime, BusPlaces[] BusPlaceList, BusStops[] AllBusStopList, TrackBusRespModel BusPosition, TrackBusListFragment Callback, int CurrentStop, String Date) {
            int StopId = Data.getStopId();

            BusPlaces BusPlace = null;

            view.setOnClickListener(view -> Callback.OnStopClick(Data.getStopId()));

            for (BusStops busStops : AllBusStopList) {
                if (StopId == busStops.getId()) {
                    int PlaceId = busStops.getPlace();
                    for (BusPlaces busPlaces : BusPlaceList) {
                        if (PlaceId == busPlaces.getId()) {
                            BusPlace = busPlaces;
                            break;
                        }
                    }
                    break;
                }
            }

            if (BusPlace != null) {
                Name.setText((Pos+1) + " - " + BusPlace.getName());
            } else {
                Name.setText((Pos+1) + " megálló");
            }



            TypedValue typedValue = new TypedValue();
            if (Data.getStopId() == CurrentStop) {
                ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.colorPrimary, typedValue, true);
                Name.setTextColor(ContextCompat.getColor(ctx, typedValue.resourceId));
            } else {
                ctx.getTheme().resolveAttribute(com.google.android.material.R.attr.color, typedValue, true);
                Name.setTextColor(Time.getTextColors());
            }

            Calendar ArriveTime = (Calendar) StartTime.clone();

            ArriveTime.add(Calendar.MINUTE, Data.getSumTravelTime());
            String TimeString = String.format(Locale.US, "%02d", ArriveTime.get(Calendar.HOUR_OF_DAY)) + ":" + String.format(Locale.US, "%02d", ArriveTime.get(Calendar.MINUTE));
            Time.setText(TimeString);

            if (BusPosition != null) {
                if (Pos == 0) {
                    if (BusPosition.getStopId() == Data.getStopId()) {
                        if (BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartHalf));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartFull));
                        }
                    } else if (BusPosition.getStopNumber() > Data.getOrder()){
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartFull));
                    } else {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartEmpty));
                    }
                } else if(Pos+1 == max) {
                    if (BusPosition.getStopId() == Data.getStopId()) {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndHalf));
                    } else if (BusPosition.getStopNumber() < Data.getOrder()) {
                        if (PrevStopId == BusPosition.getStopNumber() && !BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndInc));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndEmpty));
                        }
                    } else
                    {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndFull));
                    }
                } else {
                    if (BusPosition.getStopId() == Data.getStopId()) {
                        if (BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalHalf));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalFull));
                        }
                    } else if (BusPosition.getStopNumber() > Data.getOrder()) {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalFull));
                    } else {
                        if (PrevStopId == BusPosition.getStopNumber() && !BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalInc));
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalEmpty));
                        }
                    }
                }

                if (BusPosition.getDelayMin() < 0) {
                    Delay.setText(BusPosition.getDelayMin() + " perc");
                    Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#d60202")));
                }else {
                    Delay.setText("+" + BusPosition.getDelayMin() + " perc");
                    if (BusPosition.getDelayMin() == 0) {
                        Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#02de32")));
                    } else {
                        Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#a39b00")));
                    }
                }
            } else {
                if (Date == null) {
                    Delay.setText("");
                } else {
                    SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
                    SimpleDateFormat formatter2 = new SimpleDateFormat("yyyy. MM. dd.", Locale.US);
                    try {
                        java.util.Date ParsedDate = formatter.parse(Date);
                        if (ParsedDate != null) {
                            Delay.setText("(" + formatter2.format(ParsedDate) + ")");
                        }
                    } catch (ParseException e) {
                        Delay.setText("(" + Date + ")");
                    }
                }

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

    public TrackBusListAdapter(LineInfoTravelTime[] BusStopList, BusPlaces[] BusPlaceList, BusStops[] AllBusStopList, Calendar StartTime, TrackBusRespModel BusPosition, int CurrentStop, TrackBusListFragment Callback, Context ctx, String Date) {
        this.BusStopList = BusStopList;
        this.BusPlaceList = BusPlaceList;
        this.AllBusStopList = AllBusStopList;
        this.StartTime = StartTime;
        this.BusPosition = BusPosition;
        this.ctx = ctx;
        this.Callback = Callback;
        this.CurrentStop = CurrentStop;
        this.Date = Date;
    }

    // Create new views (invoked by the layout manager)
    @NonNull
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

        int PrevStopId = position > 0 ? BusStopList[position-1].getOrder() : -1;

        viewHolder.setData(BusStopList[position],position, max, PrevStopId, ctx, StartTime, BusPlaceList, AllBusStopList, BusPosition, Callback, CurrentStop, Date);
    }

    // Return the size of your dataset (invoked by the layout manager)
    @Override
    public int getItemCount() {
        return BusStopList.length;
    }
}
