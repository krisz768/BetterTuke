package hu.krisz768.bettertuke.Database;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;

public class DatabaseManager {

    private final String DATABASEFILE;
    private final Context Ctx;

    public  DatabaseManager (Context Ctx) {
        this.Ctx = Ctx;
        DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "track.db")).getAbsolutePath();
    }

    public boolean IsDatabaseExist () {
        File Database = new File(DATABASEFILE);

        if (Database.exists()) {

            return true;
        }

        return false;
    }

    public void DeleteDatabase() {
        File Database = new File(DATABASEFILE);
        try {
            Database.delete();
        } catch (Exception e) {
            log(e.toString());
        }

    }

    public String GetDatabaseVerison () {
        try
        {
            TukeDatabaseHelper Dbh = new TukeDatabaseHelper(Ctx, DATABASEFILE);
            SQLiteDatabase db = Dbh.getReadableDatabase();

            Cursor cursor = db.rawQuery("SELECT lastupdate FROM syncron WHERE 1", null);
            String version = "Err";
            while(cursor.moveToNext()) {
                version = cursor.getString(0);
            }
            cursor.close();

            return version;

        } catch (Exception e) {
            log(e.toString());
            return "Err";

        }

    }

    private void log (String msg) {
        Log.e("DatabaseManager", msg);
    }
}
