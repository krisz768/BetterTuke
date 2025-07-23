package hu.krisz768.bettertuke.NewApiInterface;

import static java.lang.Math.round;

import static hu.krisz768.bettertuke.HelperProvider.getBusAttributes;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.google.transit.realtime.GtfsRealtime;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.net.URL;
import java.util.Collections;
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

    //static int updatecount = 0;

    private void UpdateData () {
        Calendar Now = Calendar.getInstance();
        Now.add(Calendar.SECOND, -1);

        if (LastUpdate != null && !LastUpdate.before(Now)) {
            return;
        }

        /*log( "UPDATE! ");
        if (LastUpdate != null) {
            log( "UPDATE2! " + updatecount + " N:" + Now.get(Calendar.MINUTE) + ":" + Now.get(Calendar.SECOND)+ ":" + Now.get(Calendar.MILLISECOND) + " LU:" + LastUpdate.get(Calendar.MINUTE) + ":" + LastUpdate.get(Calendar.SECOND)+ ":" + LastUpdate.get(Calendar.MILLISECOND));
        }


        updatecount++;*/

        Thread thr = new Thread(() -> {
            try {
                URL url = new URL(Link);
                OnlineData = GtfsRealtime.FeedMessage.parseFrom(url.openStream());

                URL url2 = new URL(Link2);
                OnlineDataUpdates = GtfsRealtime.FeedMessage.parseFrom(url2.openStream());

                LastUpdate = Calendar.getInstance();
                /*for (GtfsRealtime.FeedEntity entity : feed.getEntityList()) {
                    Log.e("GTFSR", " " + entity.getId() + ": " + entity.getVehicle().getVehicle().getLabel() + " " + entity.getVehicle().getVehicle().getLicensePlate() + " " + entity.getVehicle().getCurrentStatus() + " " +  entity.getVehicle().getStopId() + " lat:" + entity.getVehicle().getPosition().getLatitude() + " lon:" + entity.getVehicle().getPosition().getLongitude() + " Tripid:" + entity.getVehicle().getTrip().getTripId());
                }*/
            } catch (Exception e) {
                OnlineData = null;
                LastUpdate = null;
                log(e.toString());
            }
        });
        thr.start();
        try {
            thr.join();
        } catch (Exception e){

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
                return new TrackBusRespModel(entity.getVehicle().getVehicle().getLicensePlate().split("#")[0].replace("-",""), entity.getVehicle().getCurrentStopSequence(), entity.getVehicle().getStopId(), entity.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT, entity.getVehicle().getPosition().getLongitude(), entity.getVehicle().getPosition().getLatitude(), DelayMin, DelaySec, PositionDate, round(entity.getVehicle().getPosition().getSpeed()*3.6F));
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
                Positions.add(new BusPositionRespModel(entity.getVehicle().getPosition().getLongitude(), entity.getVehicle().getPosition().getLatitude(), entity.getVehicle().getTrip().getRouteId(),  entity.getVehicle().getTrip().getTripId()));
        }

        BusPositionRespModel[] ret = new BusPositionRespModel[Positions.size()];

        Positions.toArray(ret);

        return ret;
    }

    public IncomingBusRespModel[] getNextIncomingBuses(String StopId) {
        UpdateData();

        if (OnlineData == null || OnlineDataUpdates == null) {
            return null;
        }

        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyyMMdd", Locale.US);
        SimpleDateFormat Sdf2 = new SimpleDateFormat("HH:mm", Locale.US);
        String CurrentDate = sdf1.format(new Date());
        String CurrentTime = Sdf2.format(new Date());

        NewGTFSDatabase NDm = new NewGTFSDatabase(ctx);
        List<IncomingBusRespModel> BusList = new ArrayList<>(List.of(NDm.GetOfflineDepartureTimes(StopId, CurrentDate, CurrentTime)));
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
                            log("Info: " + LineInfo.getRouteInfo().getLineNum() + " " + Litt.getOrder() + " / " + entity.getVehicle().getCurrentStopSequence());
                            if ( (Litt.getOrder() > entity.getVehicle().getCurrentStopSequence()) || ((Litt.getOrder() == entity.getVehicle().getCurrentStopSequence()) && entity.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT)) {
                                log("KÉSÉS: " + LineInfo.getRouteInfo().getLineNum());
                                Calendar ArriveTime = (Calendar) Calendar.getInstance();
                                String[] TimeParts = Litt.getArriveTime().split(":");
                                ArriveTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                                ArriveTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));

                                if (entityUpdate != null) {
                                    ArriveTime.add(Calendar.MINUTE, entityUpdate.getTripUpdate().getDelay()/60);
                                    log( "É:" + Litt.getArriveTime() + " D:" + entityUpdate.getTripUpdate().getDelay());
                                }


                                long diff = ArriveTime.getTime().getTime() - new Date().getTime();

                                int RemainingMin = (int) (diff / 1000) / 60;

                                BusList.add(new IncomingBusRespModel(LineInfo.getRouteInfo().getLineNum(), LineInfo.getRouteInfo().getLineName(), ArriveTime.getTime(), entity.getVehicle().getTrip().getTripId(), Math.max(RemainingMin, 0), (Litt.getOrder() == entity.getVehicle().getCurrentStopSequence()) && entity.getVehicle().getCurrentStatus() == GtfsRealtime.VehiclePosition.VehicleStopStatus.STOPPED_AT));
                                /*for (int i = 0; i < BusList.size(); i++) {

                                    //log("INS T:" + BusList.get(i).getRemainingMin()  + " / " +  (int) (diff / 1000) / 60);
                                    if (BusList.get(i).getRemainingMin() > (int) (diff / 1000) / 60) {

                                        break;
                                    }
                                }*/
                            }
                        }
                    }
                }
            }
        }

        BusList.removeAll(RemovableBusList);
        Collections.sort(BusList, new Comparator<IncomingBusRespModel>() {
            @Override
            public int compare(IncomingBusRespModel o1, IncomingBusRespModel o2) {
                return  o1.getRemainingMin() - o2.getRemainingMin();
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
            //Log.e("GTFSR", " " + entity.getId() + ": " + entity.getVehicle().getVehicle().getLabel() + " " + entity.getVehicle().getVehicle().getLicensePlate() + " " + entity.getVehicle().getCurrentStatus() + " " +  entity.getVehicle().getStopId() + " lat:" + entity.getVehicle().getPosition().getLatitude() + " lon:" + entity.getVehicle().getPosition().getLongitude() + " Tripid:" + entity.getVehicle().getTrip().getTripId());
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
                    Resp.get(i).AddTripId(entity.getVehicle().getTrip().getTripId(), busAttributes.getPlateNumber());
                    Found = true;
                    break;
                }
            }

            if (!Found) {
                Resp.add(new ActiveBusTypeRespModel(busAttributes.getType(), entity.getVehicle().getTrip().getTripId(), busAttributes.getPlateNumber()));
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
