package hu.krisz768.bettertuke.api_interface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class apiGetIsBusHasStarted extends TukeServerApiFunctions<Boolean> implements Runnable{
    private final int LineId;

    public apiGetIsBusHasStarted(int LineId) {
        this.LineId = LineId;
    }

    @Override
    public void run() {
        value = false;
        try {
            URL url = new URL(API_URL + "android/v3/handler.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(6000);
            urlConnection.setReadTimeout(6000);

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("ONWAY", LineId);

            try {
                urlConnection.connect();

                byte[] postDataBytes = this.getParamsByte(queryParams);
                urlConnection.getOutputStream().write(postDataBytes);

                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while (true)
                {
                    line = in.readLine();
                    if (line != null ) {
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
            ErrorFlag = true;
            super.log("Error :(" + e);
        }
    }
}
