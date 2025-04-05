package hu.krisz768.bettertuke.api_interface;

import android.content.Context;
import android.util.Log;

import androidx.annotation.Nullable;

import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;
import hu.krisz768.bettertuke.api_interface.models.TrackBusRespModel;

public class TukeServerApi {
    private final Context ctx;
    public TukeServerApi(Context ctx) {
        this.ctx = ctx;
    }


    @Nullable
    public IncomingBusRespModel[] getNextIncomingBuses(String StopId) {
        try {
            apiGetNextIncomingBuses data = new apiGetNextIncomingBuses(StopId);
            Thread thread = new Thread(data);
            thread.start();
            thread.join();
            if(data.getError()) {
                return null;
            }
            return data.getValue();
        } catch (Exception e) {
            log(e.toString());
        }

        return null;
    }

    public TrackBusRespModel getBusLocation(String LineId) {
        try {
            apiGetBusPosition data = new apiGetBusPosition(LineId);
            Thread thread = new Thread(data);
            thread.start();
            thread.join();
            return data.getValue();
        } catch (Exception e) {
            log(e.toString());
        }

        return null;
    }

    /*@Nullable
    public Boolean getIsBusHasStarted(String LineId) {
        try {
            apiGetIsBusHasStarted data = new apiGetIsBusHasStarted(2);
            Thread thread = new Thread(data);
            thread.start();
            thread.join();
            if (data.getError()) {
                return null;
            }
            return data.value;
        } catch (Exception e) {
            log(e.toString());
        }

        return null;
    }*/

    private void log (String msg) {
        Log.e("ApiInterface", msg);
    }
}
