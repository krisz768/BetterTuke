package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class JaratInfoNyomvonal implements Serializable {
    private int Id;
    private float GpsX;
    private float GpsY;
    private float Sorrend;

    public int getId() {
        return Id;
    }

    public float getGpsX() {
        return GpsX;
    }

    public float getGpsY() {
        return GpsY;
    }

    public float getSorrend() {
        return Sorrend;
    }

    public JaratInfoNyomvonal(int id, float gpsX, float gpsY, float sorrend) {
        Id = id;
        GpsX = gpsX;
        GpsY = gpsY;
        Sorrend = sorrend;
    }
}
