package hu.krisz768.bettertuke.models;

public class MarkerDescriptor {
    private final Types Type;
    private final String Id;

    public MarkerDescriptor(Types type, String id) {
        Type = type;
        Id = id;
    }

    public Types getType() {
        return Type;
    }

    public String getId() {
        return Id;
    }

    public enum Types {
        Place,
        Stop,
        Bus,
        PinPoint
    }
}
