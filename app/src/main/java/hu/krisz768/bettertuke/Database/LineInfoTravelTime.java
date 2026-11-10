package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class LineInfoTravelTime implements Serializable {
    private final int Id;
    private final int Order;
    private final String StopId;
    private final String ArriveTime;

    public int getId() {
        return Id;
    }

    public int getOrder() {
        return Order;
    }

    public String getStopId() {
        return StopId;
    }

    public String getArriveTime() {
        return ArriveTime;
    }

    public LineInfoTravelTime(int id, int order, String stopId, String arriveTime) {
        Id = id;
        Order = order;
        StopId = stopId;
        ArriveTime = arriveTime;
    }
}
