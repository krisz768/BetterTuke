package hu.krisz768.bettertuke.models;

import java.io.Serializable;

public class SearchResult implements Serializable {

    private final SearchType Type;
    private final String SearchText;
    private final Object Data;

    public SearchResult(SearchType type, String searchText, Object data) {
        Type = type;
        SearchText = searchText;
        Data = data;
    }

    public SearchType getType() {
        return Type;
    }

    public String getSearchText() {
        return SearchText;
    }

    public Object getData() {
        return Data;
    }

    public enum SearchType {
        Stop,
        FavStop,
        Line,
        Map
    }
}
