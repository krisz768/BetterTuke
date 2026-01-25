package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;
import java.util.Calendar;
import java.util.Date;

public class IncomingBusRespModel implements Serializable {
    private final String LineNum;
    private String LineName;
    private Date ArriveTime;
    private final String LineId;
    private int RemainingMin;
    private boolean AtStop;
    private boolean Started;
    private boolean Miss;
    private String ArriveStop;

    public IncomingBusRespModel(String lineNum, String lineName, Date arriveTime, String LineId, int remainingMin, boolean AtStop, String ArriveStop) {
        LineNum = lineNum;
        LineName = lineName;
        ArriveTime = arriveTime;
        this.LineId = LineId;
        RemainingMin = remainingMin;
        this.AtStop = AtStop;
        this.ArriveStop = ArriveStop;
    }

    public void setStarted(boolean started) {
        Started = started;
    }

    public void setMiss(boolean miss) {
        Miss = miss;
    }

    public boolean isMiss() {
        return Miss;
    }

    public String getLineNum() {
        return LineNum;
    }

    public String getLineName() {
        return LineName;
    }

    public Date getArriveTime() {
        return ArriveTime;
    }

    public boolean isStarted() { return Started;}

    public String getLineId() {
        return LineId;
    }

    public int getRemainingMin() {
        return RemainingMin;
    }

    public boolean isAtStop() {
        return AtStop;
    }

    public void setDelay(int Delay) {
        RemainingMin += Delay;

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(ArriveTime);
        calendar.add(Calendar.MINUTE, Delay);

        ArriveTime = calendar.getTime();
    }

    public void setLineName(String lineName) {
        LineName = lineName;
    }

    public void setAtStop () {
        AtStop = true;
    }

    public String getArriveStop() {
        return ArriveStop;
    }
}
