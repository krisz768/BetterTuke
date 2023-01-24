package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;
import java.util.HashMap;

public class BusPlaces implements Serializable {
    private final int Id;
    private final String Name;
    private final float GpsLongitude;
    private final float GpsLatitude;

    public BusPlaces(int id, String name, float gpsLongitude, float gpsLatitude) {
        Id = id;
        Name = name;
        GpsLongitude = gpsLongitude;
        GpsLatitude = gpsLatitude;
    }

    public int getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public float getGpsLongitude() {
        return GpsLongitude;
    }

    public float getGpsLatitude() {
        return GpsLatitude;
    }

    public static HashMap<Integer, BusPlaces> getAllBusPlaces(Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetAllBusPlaces();
    }
}
