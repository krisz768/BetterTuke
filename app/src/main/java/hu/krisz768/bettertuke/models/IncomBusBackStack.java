package hu.krisz768.bettertuke.models;

import java.io.Serializable;

public class IncomBusBackStack implements Serializable {
    private final String Date;
    private final String Time;
    private final boolean CustomTime;

    public IncomBusBackStack(String date, String time, boolean customTime) {
        Date = date;
        Time = time;
        CustomTime = customTime;
    }

    public String getDate() {
        return Date;
    }

    public String getTime() {
        return Time;
    }

    public boolean isCustomTime() {
        return CustomTime;
    }
}
