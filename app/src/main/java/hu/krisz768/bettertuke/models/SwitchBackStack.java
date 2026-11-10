package hu.krisz768.bettertuke.models;

import java.io.Serializable;

public class SwitchBackStack implements Serializable {
    private final String TripID;
    private final String StopID;
    private final String CurrentStopID;
    private final String Date;

    public SwitchBackStack(String tripID, String stopID, String currentStopID, String date) {
        TripID = tripID;
        StopID = stopID;
        CurrentStopID = currentStopID;
        Date = date;
    }

    public String getTripID() {
        return TripID;
    }

    public String getStopID() {
        return StopID;
    }

    public String getCurrentStopID() {
        return CurrentStopID;
    }

    public String getDate() {
        return Date;
    }
}
