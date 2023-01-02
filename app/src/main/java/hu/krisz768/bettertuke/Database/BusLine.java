package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class BusLine implements Serializable {
    private String LineName;
    private String LineDesc;

    public BusLine(String lineName, String lineDesc) {
        LineName = lineName;
        LineDesc = lineDesc;
    }

    public String getLineName() {
        return LineName;
    }

    public String getLineDesc() {
        return LineDesc;
    }
}
