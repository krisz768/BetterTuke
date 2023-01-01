package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;
import java.util.Date;

public class TrackBusRespModel implements Serializable {
    private String Rendszam;
    private int Jaraid;
    private String Jaratszam;
    private String Jaratnev;
    private int MegalloSorszam;
    private int Megalloid;
    private boolean Megalloban;
    private Date UtolsoAdat;
    private float GPSx;
    private float GPSY;
    private int KesesMasodperc;
    private int KesesPerc;
    private int JarmuId;

    public TrackBusRespModel(String rendszam, int jaraid, String jaratszam, String jaratnev, int megalloSorszam, int megalloid, boolean megalloban, Date utolsoAdat, float GPSx, float GPSY, int kesesMasodperc, int kesesPerc, int jarmuId) {
        Rendszam = rendszam;
        Jaraid = jaraid;
        Jaratszam = jaratszam;
        Jaratnev = jaratnev;
        MegalloSorszam = megalloSorszam;
        Megalloid = megalloid;
        Megalloban = megalloban;
        UtolsoAdat = utolsoAdat;
        this.GPSx = GPSx;
        this.GPSY = GPSY;
        KesesMasodperc = kesesMasodperc;
        KesesPerc = kesesPerc;
        JarmuId = jarmuId;
    }

    public String getRendszam() {
        return Rendszam;
    }

    public int getJaraid() {
        return Jaraid;
    }

    public String getJaratszam() {
        return Jaratszam;
    }

    public String getJaratnev() {
        return Jaratnev;
    }

    public int getMegalloSorszam() {
        return MegalloSorszam;
    }

    public int getMegalloid() {
        return Megalloid;
    }

    public boolean isMegalloban() {
        return Megalloban;
    }

    public Date getUtolsoAdat() {
        return UtolsoAdat;
    }

    public float getGPSx() {
        return GPSx;
    }

    public float getGPSY() {
        return GPSY;
    }

    public int getKesesMasodperc() {
        return KesesMasodperc;
    }

    public int getKesesPerc() {
        return KesesPerc;
    }

    public int getJarmuId() {
        return JarmuId;
    }
}
