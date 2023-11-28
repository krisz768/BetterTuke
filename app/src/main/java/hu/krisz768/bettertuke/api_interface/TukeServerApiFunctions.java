package hu.krisz768.bettertuke.api_interface;

import android.util.Log;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Map;

public class TukeServerApiFunctions<T> {
    protected final String API_URL = "http://menobusz.tukebusz.hu/";
    protected volatile T value;
    protected volatile boolean ErrorFlag = false;
    protected void log (String msg) {
        Log.i(this.getClass().getName(), msg);
    }
    public T getValue (){
        return value;
    }
    public boolean getError (){
        return ErrorFlag;
    }

    protected byte[] getParamsByte(Map<String, Object> params) {
        byte[] result = null;
        StringBuilder postData = new StringBuilder();
        for (Map.Entry<String, Object> param : params.entrySet()) {
            if (postData.length() != 0) {
                postData.append('&');
            }
            postData.append(this.encodeParam(param.getKey()));
            postData.append('=');
            postData.append(this.encodeParam(String.valueOf(param.getValue())));
        }
        try {
            result = postData.toString().getBytes(StandardCharsets.UTF_8);
        } catch (Exception e) {
            log("Encode Error: " + e);
        }
        return result;
    }

    protected String encodeParam(String data) {
        String result = "";
        try {
            result = URLEncoder.encode(data, "UTF-8");
        } catch (Exception e) {
            log("Encode Error: " + e);
        }
        return result;
    }

    protected Date DateParser(String DateString) {
        SimpleDateFormat Sdf = new SimpleDateFormat("LLL d y h:m:s:SSSa", Locale.US);

        try {
            return Sdf.parse(DateString);
        } catch (ParseException e) {
            log("Date parse Error: " + e);
        }

        return null;
    }
}
