package hu.krisz768.bettertuke.api_interface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

public class apiGetDatabaseVersion extends TukeServerApiFunctions<String> implements Runnable{

    @Override
    public void run() {
        value = "Err";
        try {
            URL url = new URL(API_URL + "android/update.php?id=2");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setConnectTimeout(3000);
            urlConnection.setReadTimeout(3000);

            try {
                urlConnection.connect();

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                value = in.readLine();
            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            super.log("Error :(" + e);

            ErrorFlag = true;
        }
    }
}

