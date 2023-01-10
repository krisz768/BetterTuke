package hu.krisz768.bettertuke.Database;

import java.util.Date;

public class BusScheduleTime {
    int Ora;
    int Perc;
    String LineCode;
    int JaratId;

    public BusScheduleTime(int ora, int perc, String lineCode, int jaratId) {
        Ora = ora;
        Perc = perc;
        LineCode = lineCode;
        JaratId = jaratId;
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

    public int getJaratId() {
        return JaratId;
    }
}
