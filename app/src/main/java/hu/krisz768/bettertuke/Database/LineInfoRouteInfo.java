package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class LineInfoRouteInfo implements Serializable {
    private final int Id;
    private final String LineNum;
    private final String LineName;

    public LineInfoRouteInfo(int id, String lineNum, String lineName) {
        Id = id;
        LineNum = lineNum;
        LineName = lineName;
    }

    public int getId() {
        return Id;
    }

    public String getLineNum() {
        return LineNum;
    }

    public String getLineName() {
        return LineName;
    }
}
