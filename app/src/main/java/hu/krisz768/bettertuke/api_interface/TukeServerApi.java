package hu.krisz768.bettertuke.api_interface;

import android.content.Context;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class TukeServerApi {

    private final Context ctx;

    public TukeServerApi(Context ctx) {
        this.ctx = ctx;
    }

    public String getServerDatabaseVersion() {
        try {
            apiGetDatabaseVersion data = new apiGetDatabaseVersion();
            Thread thread = new Thread(data);
            thread.start();
            thread.join();
            String version = data.getValue();
            return version;
        } catch (Exception e) {
            log(e.toString());
        }

        return "Err";
    }

    public boolean downloadDatabaseFile() {
        try {
            apiGetDatabaseDownload data = new apiGetDatabaseDownload(ctx);
            Thread thread = new Thread(data);
            thread.start();
            thread.join();
            return  data.RetCode;
        } catch (Exception e) {
            log(e.toString());
            return false;
        }
    }


    private void log (String msg) {
        Log.e("ApiInterface", msg);
    }
}
