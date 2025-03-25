package hu.krisz768.bettertuke.models;

public class ScheduleBackStack {
    private final String LineNum;
    private final String Direction;
    private final String Date;
    private final String StopId;
    private final boolean PreSelected;

    public ScheduleBackStack(String lineNum, String direction, String date, String stopId, boolean PreSelected) {
        LineNum = lineNum;
        Direction = direction;
        Date = date;
        StopId = stopId;
        this.PreSelected = PreSelected;
    }

    public String getLineNum() {
        return LineNum;
    }

    public String getDirection() {
        return Direction;
    }

    public String getDate() {
        return Date;
    }

    public String getStopId() {
        return StopId;
    }

    public boolean isPreSelected() {
        return PreSelected;
    }
}
