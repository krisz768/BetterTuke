package hu.krisz768.bettertuke.Database;

import java.util.Date;

public class BusScheduleTime {
    int Ora;
    int Perc;
    String LineCode;

    public BusScheduleTime(int ora, int perc, String lineCode) {
        Ora = ora;
        Perc = perc;
        LineCode = lineCode;
    }

    public int getOra() {
        return Ora;
    }

    public int getPerc() {
        return Perc;
    }

    public String getLineCode() {
        return LineCode;
    }
}
