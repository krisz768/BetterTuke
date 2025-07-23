package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;
import java.util.ArrayList;

public class ActiveBusTypeRespModel implements Serializable {
    private final String BusTypeName;
    private final ArrayList<String> TripIds;
    private final ArrayList<String> LPlates;

    public ActiveBusTypeRespModel(String busTypeName, String TripId, String LPlate) {
        BusTypeName = busTypeName;
        TripIds = new ArrayList<>();
        TripIds.add(TripId);

        LPlates = new ArrayList<>();
        LPlates.add(LPlate);
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

    public void AddTripId(String TripId, String LPlate) {
        TripIds.add(TripId);
        LPlates.add(LPlate);
    }
}
