package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class JaratInfoMenetido implements Serializable {
    private int Id;
    private int Sorrend;
    private int KocsiallasId;
    private String Km;
    private String NyomvonalKm;
    private int MenetIdoPerc;
    private int OsszegzettMenetIdoPerc;

    public int getId() {
        return Id;
    }

    public int getSorrend() {
        return Sorrend;
    }

    public int getKocsiallasId() {
        return KocsiallasId;
    }

    public String getKm() {
        return Km;
    }

    public String getNyomvonalKm() {
        return NyomvonalKm;
    }

    public int getMenetIdoPerc() {
        return MenetIdoPerc;
    }

    public int getOsszegzettMenetIdoPerc() {
        return OsszegzettMenetIdoPerc;
    }

    public JaratInfoMenetido(int id, int sorrend, int kocsiallasId, String km, String nyomvonalKm, int menetIdoPerc, int osszegzettMenetIdoPerc) {
        Id = id;
        Sorrend = sorrend;
        KocsiallasId = kocsiallasId;
        Km = km;
        NyomvonalKm = nyomvonalKm;
        MenetIdoPerc = menetIdoPerc;
        OsszegzettMenetIdoPerc = osszegzettMenetIdoPerc;
    }
}
