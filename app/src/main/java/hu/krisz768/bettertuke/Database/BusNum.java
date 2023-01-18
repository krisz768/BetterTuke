package hu.krisz768.bettertuke.Database;

import java.io.Serializable;

public class BusNum implements Serializable {
    private final String LineName;
    private final String LineDesc;

    public BusNum(String lineName, String lineDesc) {
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
