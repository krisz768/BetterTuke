package hu.krisz768.bettertuke.Database;

public class BusVariation {
    private String Nev;
    private String Irany;
    private String Kod;

    public BusVariation(String nev, String irany, String kod) {
        Nev = nev;
        Irany = irany;
        Kod = kod;
    }

    public String getNev() {
        return Nev;
    }

    public String getIrany() {
        return Irany;
    }

    public String getKod() {
        return Kod;
    }
}
