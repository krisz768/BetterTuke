package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class JaratInfoNyomvonalInfo implements Serializable {
    private int Id;
    private String JaratSzam;
    private String JaratNev;
    private String Irany;
    private String NyomvonalKod;

    public JaratInfoNyomvonalInfo(int id, String jaratSzam, String jaratNev, String irany, String nyomvonalKod) {
        Id = id;
        JaratSzam = jaratSzam;
        JaratNev = jaratNev;
        Irany = irany;
        NyomvonalKod = nyomvonalKod;
    }

    public int getId() {
        return Id;
    }

    public String getJaratSzam() {
        return JaratSzam;
    }

    public String getJaratNev() {
        return JaratNev;
    }

    public String getIrany() {
        return Irany;
    }

    public String getNyomvonalKod() {
        return NyomvonalKod;
    }
}
