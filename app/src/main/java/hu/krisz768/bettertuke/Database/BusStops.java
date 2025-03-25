package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;

public class BusStops implements Serializable {
    private final String Id;
    private final String Place;
    private final float GpsLongitude;
    private final float GpsLatitude;
    private final String StopNum;

    public BusStops(String id, String place, float gpsLongitude, float gpsLatitude, String stopNum) {
        Id = id;
        Place = place;
        GpsLongitude = gpsLongitude;
        GpsLatitude = gpsLatitude;
        StopNum = stopNum;
    }

    public String getId() {
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

    public static HashMap<String, BusStops> GetAllStops(Context ctx) {
        NewGTFSDatabase Dm = new NewGTFSDatabase(ctx);

        return Dm.GetAllBusStops();
    }
}
