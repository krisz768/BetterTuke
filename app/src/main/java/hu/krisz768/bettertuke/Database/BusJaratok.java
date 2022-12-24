package hu.krisz768.bettertuke.Database;

import android.content.Context;

public class BusJaratok {

    private int Jaratid;
    private String Jaratnev;
    private int IndulasOra;
    private int IndulasPerc;
    private int MenetIdoId;
    private String KozlekedesiJelek;
    private int NyomvonalId;

    public BusJaratok(int jaratid, String jaratnev, int indulasOra, int indulasPerc, int menetIdoId, String kozlekedesiJelek, int nyomvonalId) {
        Jaratid = jaratid;
        Jaratnev = jaratnev;
        IndulasOra = indulasOra;
        IndulasPerc = indulasPerc;
        MenetIdoId = menetIdoId;
        KozlekedesiJelek = kozlekedesiJelek;
        NyomvonalId = nyomvonalId;
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

    public int getMenetIdoId() {
        return MenetIdoId;
    }

    public String getKozlekedesiJelek() {
        return KozlekedesiJelek;
    }

    public int getNyomvonalId() {
        return NyomvonalId;
    }

    public static BusJaratok BusJaratokByJaratid (int Id, Context ctx) {
        DatabaseManager Dm = new DatabaseManager(ctx);

        return Dm.GetBusJaratById(Id);
    }
}
