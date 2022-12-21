package hu.krisz768.bettertuke.models;

public class MarkerDescriptor {
    private Types Type;
    private int Id;

    public MarkerDescriptor(Types type, int id) {
        Type = type;
        Id = id;
    }

    public Types getType() {
        return Type;
    }

    public int getId() {
        return Id;
    }

    public enum Types {
        Place,
        Stop
    }
}
