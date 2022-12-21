package hu.krisz768.bettertuke.api_interface.models;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.Serializable;
import java.util.Date;

import hu.krisz768.bettertuke.api_interface.TukeServerApi;

public class IncommingBusRespModel implements Serializable {
    private String Jaratszam;
    private String Jaratnev;
    private Date Erkezes;
    private String UnknownVar1;
    private int nyomvonalid;
    private int jaratid;
    private int HatralevoMasodperc;
    private int HatralevoPerc;
    private boolean Megalloban;
    private boolean Elindult;

    public IncommingBusRespModel(String jaratszam, String jaratnev, Date erkezes, String unknownVar1, int nyomvonalid, int jaratid, int HatralevoMasodperc, int hatralevoPerc, boolean Megalloban) {
        Jaratszam = jaratszam;
        Jaratnev = jaratnev;
        Erkezes = erkezes;
        UnknownVar1 = unknownVar1;
        this.nyomvonalid = nyomvonalid;
        this.jaratid = jaratid;
        this.HatralevoMasodperc = HatralevoMasodperc;
        HatralevoPerc = hatralevoPerc;
        this.Megalloban = Megalloban;
    }

    public void setElindult(boolean elindult) {
        Elindult = elindult;
    }

    public String getJaratszam() {
        return Jaratszam;
    }

    public String getJaratnev() {
        return Jaratnev;
    }

    public Date getErkezes() {
        return Erkezes;
    }

    public String getUnknownVar1() {
        return UnknownVar1;
    }

    public int getNyomvonalid() {
        return nyomvonalid;
    }

    public boolean isElindult() { return Elindult;}

    public int getJaratid() {
        return jaratid;
    }

    public int getHatralevoMasodperc() {
        return HatralevoMasodperc;
    }

    public int getHatralevoPerc() {
        return HatralevoPerc;
    }

    public boolean isMegalloban() {
        return Megalloban;
    }
}
