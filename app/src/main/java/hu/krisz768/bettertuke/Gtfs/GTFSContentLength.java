package hu.krisz768.bettertuke.Gtfs;

import java.net.HttpURLConnection;
import java.net.URL;

public class GTFSContentLength implements Runnable{
    String ContentLength = "";
    boolean Error = false;

    @Override
    public void run() {
        try {
            URL url = new URL( "https://mobilitas.biokom.hu/gtfs/");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);

            urlConnection.connect();
            ContentLength = Integer.toString(urlConnection.getContentLength());
        } catch (Exception e) {
            Error = true;
        }
    }
}
