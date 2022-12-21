package hu.krisz768.bettertuke.api_interface;

import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import hu.krisz768.bettertuke.api_interface.models.IncommingBusRespModel;

public class apiGetNextIncommingBuses extends TukeServerApiFunctions<IncommingBusRespModel[]> implements Runnable{

    private int kocsiallas;

    public apiGetNextIncommingBuses(int kocsiallas) {
        this.kocsiallas = kocsiallas;
    }

    @Override
    public void run() {
        value = new IncommingBusRespModel[0];
        try {
            URL url = new URL(API_URL + "android/v3/handler.php");
            HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");

            Map<String, Object> queryParams = new HashMap<>();
            queryParams.put("SMART", kocsiallas);

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

                        BusList.add(new IncommingBusRespModel(parts[0], parts[1], DateParser(parts[2]),parts[3], Integer.parseInt(parts[4]), Integer.parseInt(parts[5]), Integer.parseInt(parts[6]), Integer.parseInt(parts[7]), parts[8].equals("1")));
                    } else {
                        break;
                    }
                }

                value = new IncommingBusRespModel[BusList.size()];
                BusList.toArray(value);

            } finally {
                urlConnection.disconnect();
            }
        } catch (Exception e) {
            super.log("Error :(" + e.toString());
        }
    }
}