package hu.krisz768.bettertuke.Gtfs;

public class GTFSBusLineData {
    private final String LineId;
    private final String DepartureTime;
    private final String StartStopId;

    public GTFSBusLineData(String lineId, String departureTime, String startStopId) {
        LineId = lineId;
        DepartureTime = departureTime;
        StartStopId = startStopId;
    }

    public String getLineId() {
        return LineId;
    }

    public String getDepartureTime() {
        return DepartureTime;
    }

    public String getStartStopId() {
        return StartStopId;
    }
}
