package hu.krisz768.bettertuke.Database;

import java.util.Calendar;

public class BusScheduleTime {
    private int Hour;
    private int Minute;
    private final String LineId;

    public BusScheduleTime(int hour, int minute, String lineId) {
        Hour = hour;
        Minute = minute;
        LineId = lineId;
    }

    public int getHour() {
        return Hour;
    }

    public int getMinute() {
        return Minute;
    }

    public String getLineId() {
        return LineId;
    }

    public void AdjustToStop(int Minute) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, Hour);
        calendar.set(Calendar.MINUTE, this.Minute);

        calendar.add(Calendar.MINUTE, Minute);

        Hour = calendar.get(Calendar.HOUR_OF_DAY);
        this.Minute = calendar.get(Calendar.MINUTE);
    }
}
