package hu.krisz768.bettertuke.Database;

import android.content.Context;

import java.io.Serializable;

public class BusJaratok implements Serializable {

    private int Jaratid;
    private String Jaratnev;
    private int IndulasOra;
    private int IndulasPerc;
    private JaratInfoMenetido[] Megallok;
    private String Kozlekedesi_jel_id;
    private JaratInfoNyomvonal[] Nyomvonal;
    private JaratInfoNyomvonalInfo NyomvonalInfo;
    private String Date;

    public JaratInfoNyomvonalInfo getNyomvonalInfo() {
        return NyomvonalInfo;
    }

    public int getJaratid() {
        return Jaratid;
    }

    public String getJaratnev() {
        return Jaratnev;
    }

    public int getIndulasOra() {
        return IndulasOra;
    }

    public int getIndulasPerc() {
        return IndulasPerc;
    }

    public JaratInfoMenetido[] getMegallok() {
        return Megallok;
    }

    public String getKozlekedesi_jel_id() {
        return Kozlekedesi_jel_id;
    }

    public JaratInfoNyomvonal[] getNyomvonal() {
        return Nyomvonal;
    }

    public BusJaratok(int jaratid, String jaratnev, int indulasOra, int indulasPerc, JaratInfoMenetido[] megallok, String kozlekedesi_jel_id, JaratInfoNyomvonal[] nyomvonal, JaratInfoNyomvonalInfo nyomvonalInfo) {
        Jaratid = jaratid;
        Jaratnev = jaratnev;
        IndulasOra = indulasOra;
        IndulasPerc = indulasPerc;
        Megallok = megallok;
        Kozlekedesi_jel_id = kozlekedesi_jel_id;
        Nyomvonal = nyomvonal;
        NyomvonalInfo = nyomvonalInfo;
    }

    public static BusJaratok BusJaratokByJaratid (int Id, Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetBusJaratById(Id);
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }
}
