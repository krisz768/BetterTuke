package hu.krisz768.bettertuke.NewGTFS;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class NewGTFSOnlineVersion implements Runnable {
    String Version = "";
    boolean Error = false;

    @Override
    public void run() {
        try {
            URL url = new URL( "http://menobusz.tukebusz.hu:30080/mobilapp/GTFS/feed_info.txt");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("GET");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);

            urlConnection.connect();

            BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));
            boolean FirstLine = true;
            String VersionLine = null;
            String inputLine = null;

            while ((inputLine = in.readLine()) != null) {
                if (FirstLine) {
                    FirstLine = false;
                } else {
                    VersionLine = inputLine;
                }

            }
            in.close();

            if (VersionLine == null)
                return;

            String[] VersionLineParts = VersionLine.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)");

            Version = VersionLineParts[5];
        } catch (Exception e) {
            Error = true;
        }
    }
}
