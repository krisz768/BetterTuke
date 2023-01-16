package hu.krisz768.bettertuke.UserDatabase;

public class Favorite {
    private int Id;
    private String Data;

    public Favorite(int id, String data) {
        Id = id;
        Data = data;
    }

    public int getId() {
        return Id;
    }

    public String getData() {
        return Data;
    }
}
