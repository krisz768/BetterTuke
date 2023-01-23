package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;

public class TrackBusRespModel implements Serializable {
    private final String LicensePlateNumber;
    private final int StopNumber;
    private final int StopId;
    private final boolean AtStop;
    private final float GpsLongitude;
    private final float GpsLatitude;
    private final int DelayMin;

    public TrackBusRespModel(String licensePlateNumber, int stopNumber, int stopId, boolean atStop, float GpsLongitude, float GpsLatitude, int delayMin) {
        LicensePlateNumber = licensePlateNumber;
        StopNumber = stopNumber;
        StopId = stopId;
        AtStop = atStop;
        this.GpsLongitude = GpsLongitude;
        this.GpsLatitude = GpsLatitude;
        DelayMin = delayMin;
    }

    public String getLicensePlateNumber() {
        return LicensePlateNumber;
    }

    public int getStopNumber() {
        return StopNumber;
    }

    public int getStopId() {
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
}
