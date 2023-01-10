package hu.krisz768.bettertuke.Database;

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

    public void ReloadDatabase () {
        TukeDatabaseHelper Dbh = new TukeDatabaseHelper(Ctx, DATABASEFILE);
        Sld = Dbh.getReadableDatabase();
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
                ret = new BusJaratok(cursor.getInt(0), cursor.getString(1), cursor.getInt(2),cursor.getInt(3),GetBusJaratMenetidoById(cursor.getInt(4)), cursor.getString(5),GetBusJaratNyomvonalById(cursor.getInt(6)), GetBusJaratNyomvonalInfoById(cursor.getInt(6)));
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public JaratInfoMenetido[] GetBusJaratMenetidoById (int Id) {
        try
        {
            List<JaratInfoMenetido> Menetido = new ArrayList<>();
            Cursor cursor = Sld.rawQuery("SELECT * FROM nyomvonal_tetelek WHERE id_menetido = " + Id + " ORDER BY sorrend ASC;", null);
            while(cursor.moveToNext()) {
                Menetido.add(new JaratInfoMenetido(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),cursor.getString(3),cursor.getString(4), cursor.getInt(5),cursor.getInt(6)));
            }
            cursor.close();

            JaratInfoMenetido[] ret  = new JaratInfoMenetido[Menetido.size()];
            Menetido.toArray(ret);
            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public JaratInfoNyomvonal[] GetBusJaratNyomvonalById (int Id) {
        try
        {
            List<JaratInfoNyomvonal> Nyomvonal = new ArrayList<>();
            Cursor cursor = Sld.rawQuery("SELECT * FROM onlineroute WHERE id_nyomvonal = " + Id + " ORDER BY szakasz_sorrend ASC;", null);
            while(cursor.moveToNext()) {
                Nyomvonal.add(new JaratInfoNyomvonal(cursor.getInt(0), cursor.getFloat(1), cursor.getFloat(2),cursor.getFloat(3)));
            }
            cursor.close();

            JaratInfoNyomvonal[] ret  = new JaratInfoNyomvonal[Nyomvonal.size()];
            Nyomvonal.toArray(ret);
            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public JaratInfoNyomvonalInfo GetBusJaratNyomvonalInfoById (int Id) {
        try
        {
            JaratInfoNyomvonalInfo ret = null;
            Cursor cursor = Sld.rawQuery("SELECT * FROM nyomvonalak WHERE id_nyomvonal = " + Id + ";", null);
            while(cursor.moveToNext()) {
                ret = new JaratInfoNyomvonalInfo(cursor.getInt(0), cursor.getString(1), cursor.getString(2),cursor.getString(3), cursor.getString(7));
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public BusLine[] GetActiveBusLines() {
        List<BusLine> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT DISTINCT v.vonal_nev, v.vonal_leiras FROM vonalak v INNER JOIN nyomvonalak AS n ON n.vonal_nev = v.vonal_nev INNER JOIN jaratok j ON  j.id_nyomvonal = n.id_nyomvonal ORDER BY '0' + v.vonal_nev;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusLine(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();

        BusLine[] ret  = new BusLine[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    public BusScheduleTime[] GetBusScheduleTimeFromStart(String LineNum, String date, String Direction) {
        List<BusScheduleTime> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT j.indulas_ora, j.indulas_perc, ny.nyomvonal_kod, j.id_jarat FROM jaratok j INNER JOIN nyomvonalak as ny ON j.id_nyomvonal = ny.id_nyomvonal INNER JOIN naptar AS n ON j.id_jarat = n.id_jarat WHERE ny.vonal_nev = \"" + LineNum + "\" AND ny.irany = \"" + Direction + "\" AND n.datum = \"" + date + "\" ORDER BY j.indulas_ora,j.indulas_perc;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusScheduleTime(cursor.getInt(0), cursor.getInt(1),cursor.getString(2), cursor.getInt(3)));
        }
        cursor.close();

        BusScheduleTime[] ret  = new BusScheduleTime[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    public BusVariation[] GetBusVariations(String LineNum) {
        List<BusVariation> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT DISTINCT ny.nyomvonal_nev, ny.irany, ny.nyomvonal_kod FROM nyomvonalak ny INNER JOIN jaratok AS j ON ny.id_nyomvonal = j.id_nyomvonal WHERE ny.vonal_nev = \""+ LineNum +"\" ORDER BY ny.nyomvonal_kod, ny.irany;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusVariation(cursor.getString(0), cursor.getString(1),cursor.getString(2)));
        }
        cursor.close();

        BusVariation[] ret  = new BusVariation[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    public int GetBusJaratSumMenetidoById(String JaratId) {
        int Menetido = 0;

        Cursor cursor = Sld.rawQuery("SELECT ny.osszegzett_menetido FROM nyomvonal_tetelek ny INNER JOIN jaratok as j ON j.id_menetido = ny.id_menetido WHERE j.id_jarat = \"" + JaratId + "\" ORDER BY ny.sorrend DESC LIMIT 1;", null);
        while(cursor.moveToNext()) {
            Menetido = cursor.getInt(0);
        }
        cursor.close();

        return Menetido;
    }

    private void log (String msg) {
        Log.e("DatabaseManager", msg);
    }
}
