package hu.krisz768.bettertuke.NewApiInterface;

import static java.lang.Math.round;

import static hu.krisz768.bettertuke.HelperProvider.getBusAttributes;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import com.google.transit.realtime.GtfsRealtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.URL;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.LineInfoTravelTime;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;
import hu.krisz768.bettertuke.api_interface.models.BusPositionRespModel;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;
import hu.krisz768.bettertuke.models.BusAttributes;

public class GTFSRProvider {
    private final Context ctx;
    private final  String Link = "http://menobusz.tukebusz.hu:30080/mobilapp/GTFS/RTx_positions.x";
    private final  String Link2 = "http://menobusz.tukebusz.hu:30080/mobilapp/GTFS/RTx_updates.x";
    private static Calendar LastUpdate = null;

    private static GtfsRealtime.FeedMessage OnlineData;
    private static GtfsRealtime.FeedMessage OnlineDataUpdates;

    public GTFSRProvider(Context ctx) {
        this.ctx = ctx;
    }

    private void UpdateData () {
        Calendar Now = Calendar.getInstance();
        Now.add(Calendar.SECOND, -1);

        if (LastUpdate != null && !LastUpdate.before(Now)) {
            return;
        }

        Thread thr = new Thread(() -> {
            try {
                URL url = new URL(Link);
                OnlineData = GtfsRealtime.FeedMessage.parseFrom(url.openStream());

                URL url2 = new URL(Link2);
                OnlineDataUpdates = GtfsRealtime.FeedMessage.parseFrom(url2.openStream());

                LastUpdate = Calendar.getInstance();
            } catch (Exception e) {
                OnlineData = null;
                LastUpdate = null;
                log(e.toString());
            }
        });
        thr.start();
        try {
            thr.join();
        } catch (Exception ignored){

        }
    }

    public TrackBusRespModel getBusLocation(String LineId) {
        UpdateData();

        if (OnlineData == null || OnlineDataUpdates == null) {
            return null;
        }

        GtfsRealtime.FeedEntity entityUpdate = null;
        for (GtfsRealtime.FeedEntity entity : OnlineDataUpdates.getEntityList()) {
            if (entity.getTripUpdate().getTrip().getTripId().equals(LineId)) {
                entityUpdate = entity;
            }
        }

        int DelayMin = 0;
        int DelaySec = 0;
        Date PositionDate = new Date();

        if (entityUpdate != null) {
            DelayMin = entityUpdate.getTripUpdate().getDelay() / 60;
            DelaySec = entityUpdate.getTripUpdate().getDelay() % 60;
            PositionDate = new Date(entityUpdate.getTripUpdate().getTimestamp()*1000);
        }

        for (GtfsRealtime.FeedEntity entity : OnlineData.getEntityList()) {
            if (entity.getVehicle().getTrip().getTripId().equals(LineId)) {
                NewGTFSDatabase Ngd = new NewGTFSDatabase(ctx);
                String NewBusName = Ngd.GetBusNameByStop(LineId, entity.getVehicle().getStopId());
                return new TrackBusRespModel(entity.getVehicle().getVehicle().getLicensePlate().split("#")[0].replace("-",""), entity.getVehicle().getCurrentStopSequence(), entity.getVehicle().getStopId(), entity.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT, entity.getVehicle().getPosition().getLongitude(), entity.getVehicle().getPosition().getLatitude(), DelayMin, DelaySec, PositionDate, round(entity.getVehicle().getPosition().getSpeed()*3.6F), NewBusName);
            }
        }

        return null;
    }

    public BusPositionRespModel[] getALLBusLocation() {
        UpdateData();

        if (OnlineData == null) {
            return null;
        }

        List<BusPositionRespModel> Positions = new ArrayList<>();

        for (GtfsRealtime.FeedEntity entity : OnlineData.getEntityList()) {
            Positions.add(new BusPositionRespModel(entity.getVehicle().getPosition().getLongitude(), entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getTrip().getRouteId(),  entity.getVehicle().getTrip().getTripId(), entity.getVehicle().getStopId()));
        }

        BusPositionRespModel[] ret = new BusPositionRespModel[Positions.size()];

        Positions.toArray(ret);

        return ret;
    }

    public IncomingBusRespModel[] getNextIncomingBuses(String StopId) {
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
        SimpleDateFormat Sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
        String CurrentDate = sdf1.format(new Date());
        String CurrentTime = Sdf2.format(new Date());

        return getNextIncomingBuses(StopId, CurrentDate, CurrentTime);
    }

    public IncomingBusRespModel[] getNextIncomingBuses(String StopId, String SDate,  String STime) {
        UpdateData();

        if (OnlineData == null || OnlineDataUpdates == null) {
            return null;
        }

        NewGTFSDatabase NDm = new NewGTFSDatabase(ctx);
        List<IncomingBusRespModel> BusList = new ArrayList<>(List.of(NDm.GetOfflineDepartureTimes(StopId, SDate, STime)));
        List<IncomingBusRespModel> RemovableBusList = new ArrayList<>();


        for (IncomingBusRespModel Bus : BusList) {
            GtfsRealtime.FeedEntity entityOriginal = null;
            for (GtfsRealtime.FeedEntity entity : OnlineData.getEntityList()) {
                if (entity.getVehicle().getTrip().getTripId().equals((Bus.getLineId()))) {
                    entityOriginal = entity;
                }
            }

            for (GtfsRealtime.FeedEntity entity : OnlineDataUpdates.getEntityList()) {
                if (entity.getTripUpdate().getTrip().getTripId().equals(Bus.getLineId())) {
                    Bus.setDelay(entity.getTripUpdate().getDelay() / 60);
                    if (entityOriginal == null)
                        continue;
                    if (entityOriginal.getVehicle().getStopId().equals(StopId) && entityOriginal.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT) {
                        Bus.setAtStop();
                    }

                    BusLine LineInfo = BusLine.BusLinesByLineId(Bus.getLineId(), false, new Date(),ctx);
                    for (LineInfoTravelTime Litt : LineInfo.getStops()) {
                        if (Litt.getStopId().equals(StopId)){
                            if ( (Litt.getOrder() < entityOriginal.getVehicle().getCurrentStopSequence()) || ((Litt.getOrder() == entityOriginal.getVehicle().getCurrentStopSequence()) && entityOriginal.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.IN_TRANSIT_TO)) {
                                RemovableBusList.add(Bus);
                            }
                        }
                    }
                }
            }
        }

        for (GtfsRealtime.FeedEntity entity : OnlineData.getEntityList()) {
            boolean Found = false;

            for (IncomingBusRespModel Bus : BusList) {
                if (entity.getVehicle().getTrip().getTripId().equals((Bus.getLineId()))) {
                    Found = true;
                }
            }

            if (!Found) {
                if (NDm.IsStopInBusRoute(entity.getVehicle().getTrip().getTripId(), StopId)) {
                    GtfsRealtime.FeedEntity entityUpdate = null;
                    for (GtfsRealtime.FeedEntity entity1 : OnlineDataUpdates.getEntityList()) {
                        if (entity1.getTripUpdate().getTrip().getTripId().equals((entity.getVehicle().getTrip().getTripId()))) {
                            entityUpdate= entity1;
                        }
                    }

                    BusLine LineInfo = BusLine.BusLinesByLineId(entity.getVehicle().getTrip().getTripId(), false, new Date(),ctx);

                    for (LineInfoTravelTime Litt : LineInfo.getStops()) {
                        if (Litt.getStopId().equals(StopId)){
                            if ( (Litt.getOrder() > entity.getVehicle().getCurrentStopSequence()) || ((Litt.getOrder() == entity.getVehicle().getCurrentStopSequence()) && entity.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT)) {
                                Calendar ArriveTime = (Calendar) Calendar.getInstance();
                                String[] TimeParts = Litt.getArriveTime().split(":");
                                ArriveTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                                ArriveTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));

                                if (entityUpdate != null) {
                                    ArriveTime.add(Calendar.MINUTE, entityUpdate.getTripUpdate().getDelay()/60);
                                }

                                Calendar CompareTime = (Calendar) Calendar.getInstance();
                                String[] CompareTimeParts = STime.split(":");
                                CompareTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(CompareTimeParts[0]));
                                CompareTime.set(Calendar.MINUTE, Integer.parseInt(CompareTimeParts[1]));

                                long diff = ArriveTime.getTime().getTime() - CompareTime.getTime().getTime();

                                int RemainingMin = (int) (diff / 1000) / 60;

                                if (RemainingMin >= 0 && RemainingMin < 91) {
                                    BusList.add(new IncomingBusRespModel(LineInfo.getRouteInfo().getLineNum(), LineInfo.getRouteInfo().getLineName(), ArriveTime.getTime(), entity.getVehicle().getTrip().getTripId(), RemainingMin, (Litt.getOrder() == entity.getVehicle().getCurrentStopSequence()) && entity.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT, StopId));
                                }
                            }
                        }
                    }
                }
            }
        }

        BusList.removeAll(RemovableBusList);
        BusList.sort(new Comparator<IncomingBusRespModel>() {
            @Override
            public int compare(IncomingBusRespModel o1, IncomingBusRespModel o2) {
                return o1.getRemainingMin() - o2.getRemainingMin();
            }
        });

        IncomingBusRespModel[] ret = new IncomingBusRespModel[BusList.size()];
        BusList.toArray(ret);
        return ret;
    }

    @Nullable
    public Boolean getIsBusHasStarted(String LineId) {
        UpdateData();

        if (OnlineData == null) {
            return null;
        }

        for (GtfsRealtime.FeedEntity entity : OnlineData.getEntityList()) {
            if (entity.getVehicle().getTrip().getTripId().equals(LineId)) {
                return true;
            }
        }

        return false;
    }

    public ActiveBusTypeRespModel[] GetActiveBuses() {
        UpdateData();

        if (OnlineData == null) {
            return null;
        }

        ArrayList<ActiveBusTypeRespModel> Resp = new ArrayList<>();

        for (GtfsRealtime.FeedEntity entity : OnlineData.getEntityList()) {
            BusAttributes busAttributes = getBusAttributes(ctx, entity.getVehicle().getVehicle().getLicensePlate().split("#")[0].replace("-",""));
            boolean Found = false;

            for (int i = 0; i < Resp.size(); i++) {
                if (Resp.get(i).getBusTypeName().equals(busAttributes.getType())) {
                    Resp.get(i).AddTripId(entity.getVehicle().getTrip().getTripId(), busAttributes.getPlateNumber(), entity.getVehicle().getStopId());
                    Found = true;
                    break;
                }
            }

            if (!Found) {
                Resp.add(new ActiveBusTypeRespModel(busAttributes.getType(), entity.getVehicle().getTrip().getTripId(), busAttributes.getPlateNumber(), entity.getVehicle().getStopId()));
            }
        }

        ActiveBusTypeRespModel[] ret = new ActiveBusTypeRespModel[Resp.size()];
        Resp.toArray(ret);

        return ret;
    }

    private void log (String msg) {
        Log.e("GTFSR", msg);
    }
}
