package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class LineInfoTravelTime implements Serializable {
    private final int Id;
    private final int Order;
    private final int StopId;
    private final int SumTravelTime;

    public int getId() {
        return Id;
    }

    public int getOrder() {
        return Order;
    }

    public int getStopId() {
        return StopId;
    }

    public int getSumTravelTime() {
        return SumTravelTime;
    }

    public LineInfoTravelTime(int id, int order, int stopId, int sumTravelTime) {
        Id = id;
        Order = order;
        StopId = stopId;
        SumTravelTime = sumTravelTime;
    }
}
