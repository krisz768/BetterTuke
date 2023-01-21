package hu.krisz768.bettertuke.api_interface.models;

import java.io.Serializable;
import java.util.Date;

public class IncomingBusRespModel implements Serializable {
    private final String LineNum;
    private final String LineName;
    private final Date ArriveTime;
    private final int LineId;
    private final int RemainingMin;
    private final boolean AtStop;
    private boolean Started;

    public IncomingBusRespModel(String lineNum, String lineName, Date arriveTime, int LineId, int remainingMin, boolean AtStop) {
        LineNum = lineNum;
        LineName = lineName;
        ArriveTime = arriveTime;
        this.LineId = LineId;
        RemainingMin = remainingMin;
        this.AtStop = AtStop;
    }

    public void setStarted(boolean started) {
        Started = started;
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

    public int getLineId() {
        return LineId;
    }

    public int getRemainingMin() {
        return RemainingMin;
    }

    public boolean isAtStop() {
        return AtStop;
    }
}
