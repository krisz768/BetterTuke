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
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class apiGetBusPosition extends TukeServerApiFunctions<TrackBusRespModel> implements Runnable{

    private int Jaratid;

    public apiGetBusPosition(int Jaratid) {
        this.Jaratid = Jaratid;
    }

    @Override
    public void run() {
        value = null;
        try {
            URL url = new URL(API_URL + "android/v3/handler.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("BLATE", Jaratid);

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

                        value = new TrackBusRespModel(parts[0], Integer.parseInt(parts[1]), parts[2],parts[3], Integer.parseInt(parts[4]), Integer.parseInt(parts[5]),parts[6].equals("8"), DateParser(parts[7]), Float.parseFloat(parts[8]), Float.parseFloat(parts[9]),Integer.parseInt(parts[10]),Integer.parseInt(parts[11]),Integer.parseInt(parts[12]));
                    } else {
                        break;
                    }
                }

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            super.log("Error :(" + e.toString());
            ErrorFlag = true;
        }
    }
}