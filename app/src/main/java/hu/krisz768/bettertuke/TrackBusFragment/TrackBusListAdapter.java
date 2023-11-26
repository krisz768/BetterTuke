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
import java.util.HashMap;
import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.LineInfoTravelTime;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class TrackBusListAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>{
    private final LineInfoTravelTime[] BusStopList;
    private final HashMap<Integer, BusPlaces> BusPlaceList;
    private final HashMap<Integer, BusStops> AllBusStopList;
    private final Context ctx;
    private final Calendar StartTime;
    private TrackBusRespModel BusPosition;
    private final TrackBusListFragment Callback;
    private final int CurrentStop;
    private final String Date;

    private final BusLine CTrip;
    private boolean IsViewLongClicked = false;

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView Name;
        private final TextView Time;
        private final TextView Delay;
        private final ImageView trackGraphic;
        private final View view;

        public ViewHolder(View view) {
            super(view);

            Name = view.findViewById(R.id.TrackBusStopName);
            Time = view.findViewById(R.id.TrackBusStopTime);
            Delay = view.findViewById(R.id.TrackBusDelayTime);
            trackGraphic = view.findViewById(R.id.trackGraphic);

            this.view = view;
        }

        public void setData(LineInfoTravelTime Data, int Pos, int max, int PrevStopId, Context ctx, Calendar StartTime, HashMap<Integer, BusPlaces> BusPlaceList, HashMap<Integer, BusStops> AllBusStopList, TrackBusRespModel BusPosition, TrackBusListFragment Callback, int CurrentStop, String Date, TrackBusListAdapter trackBusListAdapter) {
            int StopId = Data.getStopId();

            BusPlaces BusPlace;

            view.setOnClickListener(view -> Callback.OnStopClick(Data.getStopId()));
            view.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    trackBusListAdapter.IsViewLongClicked = true;
                    return true;
                }
            });

            BusStops busStops = AllBusStopList.get(StopId);

            int PlaceId;
            if (busStops != null) {
                PlaceId = busStops.getPlace();
            } else {
                PlaceId = -1;
            }


            BusPlace = BusPlaceList.get(PlaceId);

            if (BusPlace != null) {
                Name.setText(ctx.getString(R.string.TrackBusStopNameWithListNum, (Pos+1), BusPlace.getName()));
            } else {
                Name.setText(ctx.getString(R.string.TrackBusStopSelect, Integer.toString(Pos+1)));
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
                boolean ShowDelay = false;

                if (Pos == 0) {
                    if (BusPosition.getStopId() == Data.getStopId()) {
                        if (BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartHalf));
                            ShowDelay = true;
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartFull));
                            ShowDelay = true;
                        }
                    } else if (BusPosition.getStopNumber() > Data.getOrder()){
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartFull));
                    } else {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackStartEmpty));
                        ShowDelay = true;
                    }
                } else if(Pos+1 == max) {
                    if (BusPosition.getStopId() == Data.getStopId()) {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndHalf));
                    } else if (BusPosition.getStopNumber() < Data.getOrder()) {
                        if (PrevStopId == BusPosition.getStopNumber() && !BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndInc));
                            ShowDelay = true;
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndEmpty));
                            ShowDelay = true;
                        }
                    } else
                    {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackEndFull));
                    }
                } else {
                    if (BusPosition.getStopId() == Data.getStopId()) {
                        if (BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalHalf));
                            ShowDelay = true;
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalFull));
                            ShowDelay = true;
                        }
                    } else if (BusPosition.getStopNumber() > Data.getOrder()) {
                        trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalFull));
                    } else {
                        if (PrevStopId == BusPosition.getStopNumber() && !BusPosition.isAtStop()) {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalInc));
                            ShowDelay = true;
                        } else {
                            trackGraphic.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.TrackNormalEmpty));
                            ShowDelay = true;
                        }
                    }
                }

                if (ShowDelay) {
                    if (trackBusListAdapter.IsViewLongClicked) {
                        String SecoundText = "";
                        int DelaySec = BusPosition.getDelaySec();
                        int DelayMinute = DelaySec/60;
                        DelaySec = DelaySec%60;

                        if (BusPosition.getDelaySec() == 1) {
                            SecoundText = ctx.getString(R.string.DelayStringWithSecOneSecond, DelaySec);
                        } else {
                            SecoundText = ctx.getString(R.string.DelayStringWithSec, DelaySec);
                        }

                        if (BusPosition.getDelayMin() < 0) {
                            if(BusPosition.getDelayMin()==-1) {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSignOneMinute, "", DelayMinute) + SecoundText);
                            }
                            else {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSign, "", DelayMinute) + SecoundText);
                            }
                            Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#d60202")));
                        } else {
                            if(BusPosition.getDelayMin()==1) {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSignOneMinute, "+", DelayMinute) + SecoundText);
                            }
                            else {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSign, "+", DelayMinute) + SecoundText);
                            }
                            if (BusPosition.getDelayMin() == 0) {
                                Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#02de32")));
                            } else {
                                Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#ff7f1b")));
                            }
                        }
                    } else {
                        if (BusPosition.getDelayMin() < 0) {
                            if(BusPosition.getDelayMin()==-1) {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSignOneMinute, "", BusPosition.getDelayMin()));
                            }
                            else {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSign, "", BusPosition.getDelayMin()));
                            }
                            Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#d60202")));
                        } else {
                            if(BusPosition.getDelayMin()==1) {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSignOneMinute, "+", BusPosition.getDelayMin()));
                            }
                            else {
                                Delay.setText(ctx.getString(R.string.DelayStringWithPosSign, "+", BusPosition.getDelayMin()));
                            }
                            if (BusPosition.getDelayMin() == 0) {
                                Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#02de32")));
                            } else {
                                Delay.setTextColor(MaterialColors.harmonizeWithPrimary(ctx, Color.parseColor("#ff7f1b")));
                            }
                        }
                    }
                } else {
                    Delay.setText("");
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
                            Delay.setText(ctx.getString(R.string.Brackets, formatter2.format(ParsedDate)));
                        }
                    } catch (ParseException e) {
                        Delay.setText(ctx.getString(R.string.Brackets, Date));
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

    public static class CViewHolder extends RecyclerView.ViewHolder {
        private final TextView Num;
        private final TextView Desc;

        public CViewHolder(View view) {
            super(view);

            Num = view.findViewById(R.id.CTrackBusNumber);
            Desc = view.findViewById(R.id.CTrackBusName);
        }

        public void setData(BusLine busLine) {
            Num.setText(busLine.getRouteInfo().getLineNum());
            Desc.setText(busLine.getRouteInfo().getLineName());
        }
    }

    public void UpdateList(TrackBusRespModel TrackData) {
        this.BusPosition = TrackData;
    }

    public TrackBusListAdapter(LineInfoTravelTime[] BusStopList, HashMap<Integer, BusPlaces> BusPlaceList, HashMap<Integer, BusStops> AllBusStopList, Calendar StartTime, TrackBusRespModel BusPosition, int CurrentStop, TrackBusListFragment Callback, Context ctx, String Date, BusLine cTrip) {
        this.BusStopList = BusStopList;
        this.BusPlaceList = BusPlaceList;
        this.AllBusStopList = AllBusStopList;
        this.StartTime = StartTime;
        this.BusPosition = BusPosition;
        this.ctx = ctx;
        this.Callback = Callback;
        this.CurrentStop = CurrentStop;
        this.Date = Date;
        this.CTrip = cTrip;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        if (viewType == 0 || viewType == 2) {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.track_bus_list_recview, viewGroup, false);

            return new TrackBusListAdapter.ViewHolder(view);
        } else {
            View view = LayoutInflater.from(viewGroup.getContext())
                    .inflate(R.layout.track_bus_ctrip_label_recview, viewGroup, false);

            return new TrackBusListAdapter.CViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder viewHolder, final int position) {
        if (viewHolder.getItemViewType() == 0) {
            int max = BusStopList.length;

            int PrevStopId = position > 0 ? BusStopList[position-1].getOrder() : -1;

            ((TrackBusListAdapter.ViewHolder)viewHolder).setData(BusStopList[position],position, max, PrevStopId, ctx, StartTime, BusPlaceList, AllBusStopList, BusPosition, Callback, CurrentStop, Date, this);
        } else if (viewHolder.getItemViewType() == 2) {

            Calendar calendar = Calendar.getInstance();
            calendar.set(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH), CTrip.getDepartureHour(), CTrip.getDepartureMinute());

            ((TrackBusListAdapter.ViewHolder)viewHolder).setData(CTrip.getStops()[position-BusStopList.length-1],position-BusStopList.length-1, CTrip.getStops().length , -1, ctx, calendar, BusPlaceList, AllBusStopList, null, Callback, CurrentStop, Date, this);
        } else {
            ((TrackBusListAdapter.CViewHolder)viewHolder).setData(CTrip);
        }
    }

    @Override
    public int getItemCount() {
        return CTrip == null ? BusStopList.length : BusStopList.length+1+CTrip.getStops().length;
    }

    @Override
    public int getItemViewType(int position) {
        if (position < BusStopList.length) {
            return 0;
        } else if (position == BusStopList.length) {
            return 1;
        } else {
            return 2;
        }
    }
}
