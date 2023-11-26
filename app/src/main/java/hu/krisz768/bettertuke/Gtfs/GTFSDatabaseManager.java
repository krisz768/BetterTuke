package hu.krisz768.bettertuke.Gtfs;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.apiGetDatabaseVersion;

public class GTFSDatabaseManager {

    private Context ctx;

    public GTFSDatabaseManager (Context ctx) {
        this.ctx = ctx;
    }

    public boolean CheckForUpdate(Activity UiThread) {
        GTFSContentLength contentLength = new GTFSContentLength();
        try {
            Thread thread = new Thread(contentLength);
            thread.start();
            thread.join();
            UserDatabase userDatabase = new UserDatabase(ctx);
            String Current = userDatabase.GetPreference("GTFS-cl");
            String Online = contentLength.ContentLength;

            UiThread.runOnUiThread(() -> Toast.makeText(ctx, "Current: " + Current, Toast.LENGTH_SHORT).show()); ///
            UiThread.runOnUiThread(() -> Toast.makeText(ctx, "Online: " + Online, Toast.LENGTH_SHORT).show()); ///
            if (Current == null) {
                UiThread.runOnUiThread(() -> Toast.makeText(ctx, "Adatbázis frissítése, kérem várjon...", Toast.LENGTH_LONG).show());
                //ForceUpdate();
                return true;
            } else if (!Current.equals(Online)) {
                UiThread.runOnUiThread(() -> Toast.makeText(ctx, "Adatbázis frissítése, kérem várjon...", Toast.LENGTH_LONG).show());
                //ForceUpdate();
                return true;
            }
            UiThread.runOnUiThread(() -> Toast.makeText(ctx, "Nincs update", Toast.LENGTH_SHORT).show());
            return false;
        } catch (Exception e) {
            log(e.toString());
            return false;
        }
    }

    public boolean ForceUpdate(GTFSDatabaseDownload.OnProgressChange onProgressChange) {
        try {
            GTFSDatabaseDownload gtfsDatabaseDownload = new GTFSDatabaseDownload(ctx, onProgressChange);
            Thread thread = new Thread(gtfsDatabaseDownload);
            thread.start();
            thread.join();

            boolean RetCode = gtfsDatabaseDownload.RetCode;
            return RetCode;
        } catch (Exception e){
            log(e.toString());
            return false;
        }
    }

    private void log (String msg) {
        Log.e("GTFSInterface", msg);
    }
}