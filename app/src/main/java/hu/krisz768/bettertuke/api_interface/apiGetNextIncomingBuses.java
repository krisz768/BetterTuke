package hu.krisz768.bettertuke.api_interface;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

public class apiGetNextIncomingBuses extends TukeServerApiFunctions<IncomingBusRespModel[]> implements Runnable{
    private final int StopId;
    public apiGetNextIncomingBuses(int StopId) {
        this.StopId = StopId;
    }

    @Override
    public void run() {
        value = new IncomingBusRespModel[0];
        try {
            URL url = new URL(API_URL + "android/v3/handler.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            urlConnection.setConnectTimeout(10000);
            urlConnection.setReadTimeout(10000);

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("SMART", StopId);

            try {
                urlConnection.connect();

                byte[] postDataBytes = this.getParamsByte(queryParams);
                urlConnection.getOutputStream().write(postDataBytes);


                List<IncomingBusRespModel> BusList = new ArrayList<>();
                BufferedReader in = new BufferedReader(new InputStreamReader(urlConnection.getInputStream()));

                String line;
                while (true)
                {
                    line = in.readLine();
                    if (line != null ) {
                        String[] parts = line.split("\\|");

                        BusList.add(new IncomingBusRespModel(parts[0], parts[1], DateParser(parts[2]), Integer.parseInt(parts[5]), Integer.parseInt(parts[7]), parts[8].equals("1")));
                    } else {
                        break;
                    }
                }

                value = new IncomingBusRespModel[BusList.size()];
                BusList.toArray(value);

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            super.log("Error: " + e);
            ErrorFlag = true;
        }
    }
}