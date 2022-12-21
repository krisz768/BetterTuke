package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;

public class BusStops {
    private int Id;
    private String Foldhely;
    private float GpsX;
    private float GpsY;
    private String KocsiallasSzam;

    public BusStops(int id, String foldhely, float gpsX, float gpsY, String kocsiallasSzam) {
        Id = id;
        Foldhely = foldhely;
        GpsX = gpsX;
        GpsY = gpsY;
        KocsiallasSzam = kocsiallasSzam;
    }

    public int getId() {
        return Id;
    }

    public int getFoldhely() {
        try
        {
            return Integer.parseInt(Foldhely);
        } catch (Exception e) {

        }

        return -1;
    }

    public float getGpsX() {
        return GpsX;
    }

    public float getGpsY() {
        return GpsY;
    }

    public String getKocsiallasSzam() {
        return KocsiallasSzam;
    }

    public static BusStops[] GetAllStops(Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetAllBusStops();
    }
}
