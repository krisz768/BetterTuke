package hu.krisz768.bettertuke.Database;

public class BusVariation {
    private final String Name;
    private final String Direction;
    private final String Code;

    public BusVariation(String name, String direction, String code) {
        Name = name;
        Direction = direction;
        Code = code;
    }

    public String getName() {
        return Name;
    }

    public String getDirection() {
        return Direction;
    }

    public String getCode() {
        return Code;
    }
}
