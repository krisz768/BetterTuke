package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class LineInfoRouteInfo implements Serializable {
    private final String Id;
    private final String LineNum;
    private String LineName;

    public LineInfoRouteInfo(String id, String lineNum, String lineName) {
        Id = id;
        LineNum = lineNum;
        LineName = lineName;
    }

    public String getId() {
        return Id;
    }

    public String getLineNum() {
        return LineNum;
    }

    public String getLineName() {
        return LineName;
    }

    public void setLineName (String LineName) {
        this.LineName = LineName;
    }
}
