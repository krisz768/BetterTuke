package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;
import java.util.Date;

public class TrackBusRespModel implements Serializable {
    private final String LicensePlateNumber;
    private final int StopNumber;
    private final String StopId;
    private final boolean AtStop;
    private final float GpsLongitude;
    private final float GpsLatitude;
    private final int DelayMin;
    private final int DelaySec;
    private final int Speed;
    private final Date LastUpdate;
    private final String BusNameByStop;

    public TrackBusRespModel(String licensePlateNumber, int stopNumber, String stopId, boolean atStop, float GpsLongitude, float GpsLatitude, int delayMin, int delaySec, Date lastUpdate, int speed, String busNameByStop) {
        LicensePlateNumber = licensePlateNumber;
        StopNumber = stopNumber;
        StopId = stopId;
        AtStop = atStop;
        this.GpsLongitude = GpsLongitude;
        this.GpsLatitude = GpsLatitude;
        DelayMin = delayMin;
        DelaySec = delaySec;
        LastUpdate = lastUpdate;
        Speed = speed;
        BusNameByStop = busNameByStop;
    }

    public String getLicensePlateNumber() {
        return LicensePlateNumber;
    }

    public int getStopNumber() {
        return StopNumber;
    }

    public String getStopId() {
        return StopId;
    }

    public boolean isAtStop() {
        return AtStop;
    }

    public float getGpsLongitude() {
        return GpsLongitude;
    }

    public float getGpsLatitude() {
        return GpsLatitude;
    }

    public int getDelayMin() {
        return DelayMin;
    }

    public int getDelaySec() {
        return DelaySec;
    }

    public Date getLastUpdate() {
        return LastUpdate;
    }

    public int getSpeed() {
        return Speed;
    }

    public String getBusNameByStop() {
        return BusNameByStop;
    }
}
