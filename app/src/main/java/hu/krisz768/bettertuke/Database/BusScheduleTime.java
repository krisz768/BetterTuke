package hu.krisz768.bettertuke.Database;

import java.util.Calendar;
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

    public void AdjustToStop(int Minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Ora);
        calendar.set(Calendar.MINUTE, Perc);

        calendar.add(Calendar.MINUTE, Minute);

        Ora = calendar.get(Calendar.HOUR_OF_DAY);
        Perc = calendar.get(Calendar.MINUTE);
    }
}
