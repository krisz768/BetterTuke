package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ActiveBusTypeRespModel implements Serializable {
    private final String BusTypeName;
    private final ArrayList<String> TripIds;
    private final ArrayList<String> LPlates;
    private final ArrayList<String> StopIds;

    public ActiveBusTypeRespModel(String busTypeName, String TripId, String LPlate, String StopID) {
        BusTypeName = busTypeName;
        TripIds = new ArrayList<>();
        TripIds.add(TripId);

        LPlates = new ArrayList<>();
        LPlates.add(LPlate);

        StopIds = new ArrayList<>();
        StopIds.add(StopID);
    }

    public String getBusTypeName() {
        return BusTypeName;
    }

    public ArrayList<String> getTripIds() {
        return TripIds;
    }

    public ArrayList<String> getLPlates() {
        return LPlates;
    }

    public ArrayList<String> getStopIds() {
        return StopIds;
    }

    public void AddTripId(String TripId, String LPlate, String StopId) {
        TripIds.add(TripId);
        LPlates.add(LPlate);
        StopIds.add(StopId);
    }
}
