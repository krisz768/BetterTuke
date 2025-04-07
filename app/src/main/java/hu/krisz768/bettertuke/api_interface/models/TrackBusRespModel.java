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

    public TrackBusRespModel(String licensePlateNumber, int stopNumber, String stopId, boolean atStop, float GpsLongitude, float GpsLatitude, int delayMin, int delaySec, Date lastUpdate, int speed) {
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
}
