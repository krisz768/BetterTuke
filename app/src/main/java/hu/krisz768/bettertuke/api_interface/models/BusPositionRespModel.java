package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;

public class BusPositionRespModel implements Serializable {
    private final float GpsLongitude;
    private final float GpsLatitude;
    private final String LineNum;
    private final String TripId;

    public BusPositionRespModel(float gpsLongitude, float gpsLatitude, String lineNum, String tripId) {
        GpsLongitude = gpsLongitude;
        GpsLatitude = gpsLatitude;
        LineNum = lineNum;
        TripId = tripId;
    }

    public float getGpsLongitude() {
        return GpsLongitude;
    }

    public float getGpsLatitude() {
        return GpsLatitude;
    }

    public String getLineNum() {
        return LineNum;
    }

    public String getTripId() {
        return TripId;
    }
}
