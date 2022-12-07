package hu.krisz768.bettertuke.api_interface;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TukeServerApiFunctions<T> {
    protected final String API_URL = "http://menobusz.tukebusz.hu/";
    protected volatile T value;

    protected void log (String msg) {
        Log.i(this.getClass().getName(), msg);
    }

    public T getValue (){
        return value;
    }
}
