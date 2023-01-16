package hu.krisz768.bettertuke.models;

public class ScheduleBackStack {
    private String LineNum;
    private String Direction;
    private String Date;
    private int StopId;
    private boolean PreSelected;

    public ScheduleBackStack(String lineNum, String direction, String date, int stopId, boolean PreSelected) {
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

    public int getStopId() {
        return StopId;
    }

    public boolean isPreSelected() {
        return PreSelected;
    }
}
