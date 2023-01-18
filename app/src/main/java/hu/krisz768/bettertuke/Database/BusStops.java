package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;

public class BusStops implements Serializable {
    private final int Id;
    private final String Place;
    private final float GpsLongitude;
    private final float GpsLatitude;
    private final String StopNum;

    public BusStops(int id, String place, float gpsLongitude, float gpsLatitude, String stopNum) {
        Id = id;
        Place = place;
        GpsLongitude = gpsLongitude;
        GpsLatitude = gpsLatitude;
        StopNum = stopNum;
    }

    public int getId() {
        return Id;
    }

    public int getPlace() {
        try
        {
            return Integer.parseInt(Place);
        } catch (Exception ignored) {

        }

        return -1;
    }

    public float getGpsLongitude() {
        return GpsLongitude;
    }

    public float getGpsLatitude() {
        return GpsLatitude;
    }

    public String getStopNum() {
        return StopNum;
    }

    public static BusStops[] GetAllStops(Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetAllBusStops();
    }
}
