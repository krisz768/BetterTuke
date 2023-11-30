package hu.krisz768.bettertuke.Gtfs;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class GTFSDatabaseManager {

    private final Context ctx;

    public GTFSDatabaseManager (Context ctx) {
        this.ctx = ctx;
    }

    public boolean CheckForUpdate() {
        GTFSContentLength contentLength = new GTFSContentLength();
        try {
            Thread thread = new Thread(contentLength);
            thread.start();
            thread.join();
            UserDatabase userDatabase = new UserDatabase(ctx);
            String Current = userDatabase.GetPreference("GTFS-cl");
            String Online = contentLength.ContentLength;

            if (Current == null) {
                return true;
            } else return !Current.equals(Online);
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

            return gtfsDatabaseDownload.RetCode;
        } catch (Exception e){
            log(e.toString());
            return false;
        }
    }

    private void log (String msg) {
        Log.e("GTFSInterface", msg);
    }
}