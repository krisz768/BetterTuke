package hu.krisz768.bettertuke.Database;

import android.app.Application;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {

    private final String DATABASEFILE;
    private final Context Ctx;

    private static SQLiteDatabase Sld;

    public  DatabaseManager (Context Ctx) {
        this.Ctx = Ctx;
        DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "track.db")).getAbsolutePath();

        if (Sld == null) {
            TukeDatabaseHelper Dbh = new TukeDatabaseHelper(Ctx, DATABASEFILE);
            Sld = Dbh.getReadableDatabase();
        }


    }

    public boolean IsDatabaseExist () {
        File Database = new File(DATABASEFILE);
        Sld = null;
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
            Cursor cursor = Sld.rawQuery("SELECT lastupdate FROM syncron WHERE 1", null);
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

    public BusStops[] GetAllBusStops () {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT * FROM kocsiallasok WHERE 1", null);
            List<BusStops> AllStops = new ArrayList<>();
            while(cursor.moveToNext()) {
                AllStops.add(new BusStops(cursor.getInt(0), cursor.getString(1), cursor.getFloat(2), cursor.getFloat(3), cursor.getString(5)));
            }
            cursor.close();

            BusStops[] ret  = new BusStops[AllStops.size()];
            AllStops.toArray(ret);
            return ret;

        } catch (Exception e) {
            log(e.toString());
            return new BusStops[0];

        }
    }

    public BusPlaces[] GetAllBusPlaces () {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT * FROM foldhelyek WHERE 1", null);
            List<BusPlaces> AllPlaces = new ArrayList<>();
            while(cursor.moveToNext()) {
                AllPlaces.add(new BusPlaces(cursor.getInt(0), cursor.getString(1), cursor.getFloat(2), cursor.getFloat(3), cursor.getInt(5)));
            }
            cursor.close();

            BusPlaces[] ret  = new BusPlaces[AllPlaces.size()];
            AllPlaces.toArray(ret);
            return ret;

        } catch (Exception e) {
            log(e.toString());
            return new BusPlaces[0];

        }
    }

    public BusJaratok GetBusJaratById (int Id) {
        try
        {
            BusJaratok ret = null;
            Cursor cursor = Sld.rawQuery("SELECT * FROM jaratok WHERE id_jarat = " + Id + ";", null);
            while(cursor.moveToNext()) {
                ret = new BusJaratok(cursor.getInt(0), cursor.getString(1), cursor.getInt(2),cursor.getInt(3),cursor.getInt(4), cursor.getString(5),cursor.getInt(6));
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    private void log (String msg) {
        Log.e("DatabaseManager", msg);
    }
}
