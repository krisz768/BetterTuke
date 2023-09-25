package hu.krisz768.bettertuke.gtfs;

import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;

import hu.krisz768.bettertuke.api_interface.apiGetDatabaseVersion;

public class GTFSDatabaseManager {
    public void CheckForUpdate() {
        GTFSContentLength contentLength = new GTFSContentLength();
        try {
            apiGetDatabaseVersion data = new apiGetDatabaseVersion();
            Thread thread = new Thread(data);
            thread.start();
            thread.join();
            data.getValue();
        } catch (Exception e) {
            log(e.toString());
        }
    }

    public void ForceUpdate() {

    }

    private void log (String msg) {
        Log.e("GTFSInterface", msg);
    }
}