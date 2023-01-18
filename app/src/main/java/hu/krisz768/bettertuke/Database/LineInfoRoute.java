package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class LineInfoRoute implements Serializable {
    private final int Id;
    private final float GpsLongitude;
    private final float GpsLatitude;

    public int getId() {
        return Id;
    }

    public float getGpsLongitude() {
        return GpsLongitude;
    }

    public float getGpsLatitude() {
        return GpsLatitude;
    }

    public LineInfoRoute(int id, float gpsLongitude, float gpsLatitude) {
        Id = id;
        GpsLongitude = gpsLongitude;
        GpsLatitude = gpsLatitude;
    }
}
