package hu.krisz768.bettertuke.Database;

import android.content.Context;

public class BusPlaces {
    private int Id;
    private String Name;
    private float GpsX;
    private float GpsY;
    private int SequenceNumber;

    public BusPlaces(int id, String name, float gpsX, float gpsY, int sequenceNumber) {
        Id = id;
        Name = name;
        GpsX = gpsX;
        GpsY = gpsY;
        SequenceNumber = sequenceNumber;
    }

    public int getId() {
        return Id;
    }

    public String getName() {
        return Name;
    }

    public float getGpsX() {
        return GpsX;
    }

    public float getGpsY() {
        return GpsY;
    }

    public int getSequenceNumber() {
        return SequenceNumber;
    }

    public static BusPlaces[] getAllBusPlaces(Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetAllBusPlaces();
    }
}
