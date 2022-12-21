package hu.krisz768.bettertuke.api_interface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.krisz768.bettertuke.api_interface.models.IncommingBusRespModel;

public class apiGetIsBusHasStarted extends TukeServerApiFunctions<Boolean> implements Runnable{

    private int jaratid;

    public apiGetIsBusHasStarted(int jaratid) {
        this.jaratid = jaratid;
    }

    @Override
    public void run() {
        value = false;
        try {
            URL url = new URL(API_URL + "android/v3/handler.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("ONWAY", jaratid);

            try {
                urlConnection.connect();

                byte[] postDataBytes = this.getParamsByte(queryParams);
                urlConnection.getOutputStream().write(postDataBytes);


                List<IncommingBusRespModel> BusList = new ArrayList<>();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while (true)
                {
                    line = in.readLine();
                    if (line != null ) {//|| line.length() != 0) {
                        String[] parts = line.split("\\|");

                        value = parts[0].equals("0") || parts[0].equals("8");
                    } else {
                        break;
                    }
                }

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            super.log("Error :(" + e.toString());
        }
    }
}
