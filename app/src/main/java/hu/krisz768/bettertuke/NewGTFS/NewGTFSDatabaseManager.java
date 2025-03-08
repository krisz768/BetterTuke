package hu.krisz768.bettertuke.NewGTFS;

import android.content.Context;
import android.util.Log;

import hu.krisz768.bettertuke.Gtfs.GTFSContentLength;
import hu.krisz768.bettertuke.Gtfs.GTFSDatabaseDownload;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class NewGTFSDatabaseManager {
    private final Context ctx;

    public NewGTFSDatabaseManager (Context ctx) {
        this.ctx = ctx;
    }

    public boolean ForceUpdate(NewGTFSDatabaseDownload.OnProgressChange onProgressChange) {
        try {
            NewGTFSDatabaseDownload gtfsDatabaseDownload = new NewGTFSDatabaseDownload(ctx, onProgressChange);
            Thread thread = new Thread(gtfsDatabaseDownload);
            thread.start();
            thread.join();

            return gtfsDatabaseDownload.RetCode;
        } catch (Exception e){
            log(e.toString());
            return false;
        }
    }

    public boolean CheckForUpdate() {
        NewGTFSOnlineVersion OnlineVersion = new NewGTFSOnlineVersion();
        try {
            Thread thread = new Thread(OnlineVersion);
            thread.start();
            thread.join();
            String Current = NewGTFSDatabase.GetDatabaseVersion(ctx).trim();
            String Online = OnlineVersion.Version.trim();

            if (Current == null || Current.equals("Err")) {
                return true;
            } else return !Current.equals(Online);
        } catch (Exception e) {
            log(e.toString());
            return false;
        }
    }

    private void log (String msg) {
        Log.e("NewGTFSInterface", msg);
    }
}
