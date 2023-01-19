package hu.krisz768.bettertuke.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

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
        return Database.exists();
    }

    public boolean DeleteDatabase() {
        File Database = new File(DATABASEFILE);
        try {
            return Database.delete();
        } catch (Exception e) {
            log(e.toString());
            return false;
        }

    }

    public String GetDatabaseVersion() {
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

    public String GetStopName (int StopId) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT f.foldhely_nev FROM foldhelyek f INNER JOIN kocsiallasok AS k ON f.id_foldhelyek = k.id_foldhely WHERE k.id_kocsiallas = " + StopId +";", null);
            String Name = "";
            while(cursor.moveToNext()) {
                Name = cursor.getString(0);
            }
            cursor.close();

            return Name;

        } catch (Exception e) {
            log(e.toString());
            return "Err";

        }
    }

    public String GetStopNum (int StopId) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT k.kocsiallas_szam FROM kocsiallasok k WHERE k.id_kocsiallas = " + StopId +";", null);
            String Num = "";
            while(cursor.moveToNext()) {
                Num = Integer.toString(cursor.getInt(0));
            }
            cursor.close();

            return Num;

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
                String PlaceName = cursor.getString(1);
                if (!PlaceName.contains("KEDPLASMA plazma központ")) {
                    AllPlaces.add(new BusPlaces(cursor.getInt(0), cursor.getString(1), cursor.getFloat(2), cursor.getFloat(3)));
                }
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

    public BusLine GetBusLineById(int Id) {
        try
        {
            BusLine ret = null;
            Cursor cursor = Sld.rawQuery("SELECT * FROM jaratok WHERE id_jarat = " + Id + ";", null);
            while(cursor.moveToNext()) {
                ret = new BusLine(cursor.getInt(0), cursor.getInt(2),cursor.getInt(3), GetBusLineTravelTimeById(cursor.getInt(4)), GetBusLineRouteById(cursor.getInt(6)), GetBusLineRouteInfoById(cursor.getInt(6)));
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public LineInfoTravelTime[] GetBusLineTravelTimeById(int Id) {
        try
        {
            List<LineInfoTravelTime> TravelTime = new ArrayList<>();
            Cursor cursor = Sld.rawQuery("SELECT * FROM nyomvonal_tetelek WHERE id_menetido = " + Id + " ORDER BY sorrend ASC;", null);
            while(cursor.moveToNext()) {
                TravelTime.add(new LineInfoTravelTime(cursor.getInt(0), cursor.getInt(1), cursor.getInt(2),cursor.getInt(6)));
            }
            cursor.close();

            LineInfoTravelTime[] ret  = new LineInfoTravelTime[TravelTime.size()];
            TravelTime.toArray(ret);
            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public LineInfoRoute[] GetBusLineRouteById(int Id) {
        try
        {
            List<LineInfoRoute> Route = new ArrayList<>();
            Cursor cursor = Sld.rawQuery("SELECT * FROM onlineroute WHERE id_nyomvonal = " + Id + " ORDER BY szakasz_sorrend ASC;", null);
            while(cursor.moveToNext()) {
                Route.add(new LineInfoRoute(cursor.getInt(0), cursor.getFloat(1), cursor.getFloat(2)));
            }
            cursor.close();

            LineInfoRoute[] ret  = new LineInfoRoute[Route.size()];
            Route.toArray(ret);
            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public LineInfoRouteInfo GetBusLineRouteInfoById(int Id) {
        try
        {
            LineInfoRouteInfo ret = null;
            Cursor cursor = Sld.rawQuery("SELECT * FROM nyomvonalak WHERE id_nyomvonal = " + Id + ";", null);
            while(cursor.moveToNext()) {
                ret = new LineInfoRouteInfo(cursor.getInt(0), cursor.getString(1), cursor.getString(2));
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public BusNum[] GetActiveBusLines() {
        List<BusNum> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT DISTINCT v.vonal_nev, v.vonal_leiras FROM vonalak v INNER JOIN nyomvonalak AS n ON n.vonal_nev = v.vonal_nev INNER JOIN jaratok j ON  j.id_nyomvonal = n.id_nyomvonal ORDER BY '0' + v.vonal_nev;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusNum(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();

        BusNum[] ret  = new BusNum[Lines.size()];
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

    public int GetBusLineSumTravelTimeById(String LineId) {
        int TravelTime = 0;

        Cursor cursor = Sld.rawQuery("SELECT ny.osszegzett_menetido FROM nyomvonal_tetelek ny INNER JOIN jaratok as j ON j.id_menetido = ny.id_menetido WHERE j.id_jarat = \"" + LineId + "\" ORDER BY ny.sorrend DESC LIMIT 1;", null);
        while(cursor.moveToNext()) {
            TravelTime = cursor.getInt(0);
        }
        cursor.close();

        return TravelTime;
    }

    public BusScheduleTime[] GetBusScheduleTimeFromStop(String LineNum, String date, String Direction, int StopId) {
        List<BusScheduleTime> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT j.indulas_ora, j.indulas_perc, ny.nyomvonal_kod, j.id_jarat FROM jaratok j INNER JOIN nyomvonalak as ny ON j.id_nyomvonal = ny.id_nyomvonal INNER JOIN naptar AS n ON j.id_jarat = n.id_jarat INNER JOIN nyomvonal_tetelek AS nyt ON nyt.id_menetido = j.id_menetido WHERE nyt.id_kocsiallas = " + StopId + " AND ny.vonal_nev = \"" + LineNum + "\" AND ny.irany = \"" + Direction + "\" AND n.datum = \"" + date + "\" ORDER BY j.indulas_ora,j.indulas_perc;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusScheduleTime(cursor.getInt(0), cursor.getInt(1),cursor.getString(2), cursor.getInt(3)));
        }
        cursor.close();

        BusScheduleTime[] ret  = new BusScheduleTime[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    public BusVariation[] GetBusVariationsFromStop(String LineNum, int StopId) {
        List<BusVariation> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT DISTINCT ny.nyomvonal_nev, ny.irany, ny.nyomvonal_kod FROM nyomvonalak ny INNER JOIN jaratok AS j ON ny.id_nyomvonal = j.id_nyomvonal INNER JOIN nyomvonal_tetelek AS nyt ON nyt.id_menetido = j.id_menetido WHERE ny.vonal_nev = \""+ LineNum +"\" AND nyt.id_kocsiallas = " + StopId + " ORDER BY ny.nyomvonal_kod, ny.irany;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusVariation(cursor.getString(0), cursor.getString(1),cursor.getString(2)));
        }
        cursor.close();

        BusVariation[] ret  = new BusVariation[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    public BusNum[] GetActiveBusLinesFromStop(int StopId) {
        List<BusNum> Lines = new ArrayList<>();

        Cursor cursor = Sld.rawQuery("SELECT DISTINCT v.vonal_nev, v.vonal_leiras FROM vonalak v INNER JOIN nyomvonalak AS n ON n.vonal_nev = v.vonal_nev INNER JOIN jaratok j ON j.id_nyomvonal = n.id_nyomvonal INNER JOIN nyomvonal_tetelek nyt ON nyt.id_menetido = j.id_menetido WHERE nyt.id_kocsiallas = " + StopId + " ORDER BY '0' + v.vonal_nev;", null);
        while(cursor.moveToNext()) {
            Lines.add(new BusNum(cursor.getString(0), cursor.getString(1)));
        }
        cursor.close();

        BusNum[] ret  = new BusNum[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    public int GetBusLineStopTravelTimeById(String LineId, int StopId) {
        int TravelTime = 0;

        Cursor cursor = Sld.rawQuery("SELECT ny.osszegzett_menetido FROM nyomvonal_tetelek ny INNER JOIN jaratok as j ON j.id_menetido = ny.id_menetido WHERE j.id_jarat = \"" + LineId + "\" AND ny.id_kocsiallas = " + StopId + ";", null);
        while(cursor.moveToNext()) {
            TravelTime = cursor.getInt(0);
        }
        cursor.close();

        return TravelTime;
    }

    public IncomingBusRespModel[] GetOfflineDepartureTimes(int StopId) {
        List<IncomingBusRespModel> Lines = new ArrayList<>();
        Calendar now = Calendar.getInstance();
        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        String CurrentDate = formatter.format(now.getTime());

        Cursor cursor = Sld.rawQuery("SELECT j.id_jarat, ny.id_nyomvonal, ny.vonal_nev, ny.nyomvonal_nev, nyt.osszegzett_menetido, j.indulas_ora, j.indulas_perc FROM nyomvonalak ny INNER JOIN jaratok as j ON j.id_nyomvonal = ny.id_nyomvonal INNER JOIN nyomvonal_tetelek as nyt ON j.id_menetido = nyt.id_menetido INNER JOIN naptar as n ON j.id_jarat = n.id_jarat WHERE n.datum = \"" + CurrentDate + "\" AND nyt.id_kocsiallas = " + StopId + " ORDER BY j.indulas_ora, j.indulas_perc;", null);
        while(cursor.moveToNext()) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR_OF_DAY, cursor.getInt(5));
            calendar.set(Calendar.MINUTE, cursor.getInt(6));

            calendar.add(Calendar.MINUTE, cursor.getInt(4));

            Calendar MaxLimit = Calendar.getInstance();

            MaxLimit.add(Calendar.MINUTE, 90);



            if (calendar.after(now) && calendar.before(MaxLimit)) {
                long diff = calendar.getTime().getTime() - now.getTime().getTime();
                int RemainingMinute = (int)(diff / 1000)/ 60;

                Lines.add(new IncomingBusRespModel(cursor.getString(2), cursor.getString(3), calendar.getTime(), cursor.getInt(1), cursor.getInt(0), RemainingMinute, false));
            }
        }
        cursor.close();

        Collections.sort(Lines, new Comparator<IncomingBusRespModel>() {
            @Override
            public int compare(IncomingBusRespModel incomingBusRespModel, IncomingBusRespModel t1) {
                return incomingBusRespModel.getRemainingMin() - t1.getRemainingMin();
            }
        });

        IncomingBusRespModel[] ret  = new IncomingBusRespModel[Lines.size()];
        Lines.toArray(ret);
        return ret;
    }

    private void log (String msg) {
        Log.e("DatabaseManager", msg);
    }
}
