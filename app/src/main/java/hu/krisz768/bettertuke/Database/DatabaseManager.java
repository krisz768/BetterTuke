package hu.krisz768.bettertuke.Database;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import hu.krisz768.bettertuke.Gtfs.GTFSBusLineData;
import hu.krisz768.bettertuke.Gtfs.GTFSDatabase;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

public class DatabaseManager {
    private static SQLiteDatabase Sld;
    private final Context ctx;

    public  DatabaseManager (Context Ctx) {
        this.ctx = Ctx;
        String DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "track.db")).getAbsolutePath();

        if (Sld == null) {
            TukeDatabaseHelper Dbh = new TukeDatabaseHelper(Ctx, DATABASEFILE);
            Sld = Dbh.getReadableDatabase();
        }

    }

    public BusLine GetBusLineById(String Id, boolean GetGTFS, Date date) {
        try
        {
            BusLine ret = null;
            Cursor cursor = Sld.rawQuery("SELECT * FROM jaratok WHERE id_jarat = " + Id + ";", null);
            while(cursor.moveToNext()) {
                LineInfoTravelTime[] lineInfoTravelTimes = GetBusLineTravelTimeById(cursor.getInt(4));
                LineInfoTravelTime StartStop = null;

                for (LineInfoTravelTime lineInfoTravelTime : lineInfoTravelTimes) {
                    if (StartStop == null || StartStop.getOrder() > lineInfoTravelTime.getOrder()) {
                        StartStop = lineInfoTravelTime;
                    }
                }

                int DepartureHour = cursor.getInt(2);
                int DepartureMinute = cursor.getInt(3);

                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
                DateFormat dateFormat2 = new SimpleDateFormat("yyyy-MM-dd");

                if (date == null) {
                    date = new Date();
                }

                LineInfoRouteInfo lineInfoRouteInfo = GetBusLineRouteInfoById(cursor.getInt(6));

                GTFSDatabase gtfsDatabase = new GTFSDatabase(this.ctx);
                LineInfoRoute[] lineInfoRoute = null;

                BusLine CTrip = null;

                if (GetGTFS && StartStop != null) {
                    String GTFSId = gtfsDatabase.ConvertTripId(StartStop.getStopId(), String.format("%02d", DepartureHour) + ":" + String.format("%02d", DepartureMinute) + ":00", dateFormat.format(date), lineInfoRouteInfo.getLineNum());
                    
                    if (GTFSId == null) {
                        GTFSId = gtfsDatabase.ConvertTripId(StartStop.getStopId(), String.format("%02d", DepartureHour+24) + ":" + String.format("%02d", DepartureMinute) + ":00", dateFormat.format(date), lineInfoRouteInfo.getLineNum());
                    }

                    if (GTFSId != null) {
                        lineInfoRoute = gtfsDatabase.GetGTFSGPSRoute(GTFSId);
                        String CTripIdGTFS = gtfsDatabase.GetContinueTrip(GTFSId, String.format("%02d", DepartureHour) + ":" + String.format("%02d", DepartureMinute) + ":00");
                        if (CTripIdGTFS != null) {
                            GTFSBusLineData gtfsBusLineData = gtfsDatabase.GetLineData(CTripIdGTFS);
                            if (gtfsBusLineData != null) {
                                int GTFSDepartureHour = Integer.parseInt(gtfsBusLineData.getDepartureTime().split(":")[0]);
                                if (GTFSDepartureHour > 23) {
                                    GTFSDepartureHour-=24;
                                }

                                String CTripId = ConvertTripId(gtfsBusLineData.getStartStopId(), GTFSDepartureHour,Integer.parseInt(gtfsBusLineData.getDepartureTime().split(":")[1]) , dateFormat2.format(date), gtfsBusLineData.getLineId());
                                if (!CTripId.equals("-1"))
                                {
                                    CTrip = GetBusLineById(CTripId, false, null);
                                }
                            }
                        }
                    }
                }

                if (lineInfoRoute == null) {
                    lineInfoRoute = GetBusLineRouteById(cursor.getInt(6));
                }


                ret = new BusLine(cursor.getString(0), DepartureHour,DepartureMinute, lineInfoTravelTimes, lineInfoRoute, lineInfoRouteInfo, CTrip);
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
                TravelTime.add(new LineInfoTravelTime(cursor.getInt(0), cursor.getInt(1), cursor.getString(2),cursor.getInt(6)));
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

    public int GetBusLineSumTravelTimeById(String LineId) {
        try {
            int TravelTime = 0;

            Cursor cursor = Sld.rawQuery("SELECT ny.osszegzett_menetido FROM nyomvonal_tetelek ny INNER JOIN jaratok as j ON j.id_menetido = ny.id_menetido WHERE j.id_jarat = \"" + LineId + "\" ORDER BY ny.sorrend DESC LIMIT 1;", null);
            while (cursor.moveToNext()) {
                TravelTime = cursor.getInt(0);
            }
            cursor.close();

            return TravelTime;
        } catch (Exception e) {
            log(e.toString());
            return 0;
        }
    }

    public BusScheduleTime[] GetBusScheduleTimeFromStop(String LineNum, String date, String Direction, String StopId) {
        try {
            List<BusScheduleTime> Lines = new ArrayList<>();

            Cursor cursor = Sld.rawQuery("SELECT j.indulas_ora, j.indulas_perc, ny.nyomvonal_kod, j.id_jarat FROM jaratok j INNER JOIN nyomvonalak as ny ON j.id_nyomvonal = ny.id_nyomvonal INNER JOIN naptar AS n ON j.id_jarat = n.id_jarat INNER JOIN nyomvonal_tetelek AS nyt ON nyt.id_menetido = j.id_menetido WHERE nyt.id_kocsiallas = " + StopId + " AND ny.vonal_nev = \"" + LineNum + "\" AND ny.irany = \"" + Direction + "\" AND n.datum = \"" + date + "\" ORDER BY j.indulas_ora,j.indulas_perc;", null);
            while (cursor.moveToNext()) {
                Lines.add(new BusScheduleTime(cursor.getInt(0), cursor.getInt(1), cursor.getString(2), cursor.getString(3)));
            }
            cursor.close();

            BusScheduleTime[] ret = new BusScheduleTime[Lines.size()];
            Lines.toArray(ret);
            return ret;
        } catch (Exception e) {
            log(e.toString());
            return new BusScheduleTime[0];
        }
    }

    public int GetBusLineStopTravelTimeById(String LineId, String StopId) {
        try {
            int TravelTime = 0;

            Cursor cursor = Sld.rawQuery("SELECT ny.osszegzett_menetido FROM nyomvonal_tetelek ny INNER JOIN jaratok as j ON j.id_menetido = ny.id_menetido WHERE j.id_jarat = \"" + LineId + "\" AND ny.id_kocsiallas = " + StopId + ";", null);
            while (cursor.moveToNext()) {
                TravelTime = cursor.getInt(0);
            }
            cursor.close();

            return TravelTime;
        } catch (Exception e) {
            log(e.toString());
            return 0;
        }
    }

    public IncomingBusRespModel[] GetOfflineDepartureTimes(String StopId, String Date, String Time) {
        try {
            List<IncomingBusRespModel> Lines = new ArrayList<>();
            Calendar GetTime = Calendar.getInstance();
            GetTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Time.split(":")[0]));
            GetTime.set(Calendar.MINUTE, Integer.parseInt(Time.split(":")[1]));


            Cursor cursor = Sld.rawQuery("SELECT j.id_jarat, ny.id_nyomvonal, ny.vonal_nev, ny.nyomvonal_nev, nyt.osszegzett_menetido, j.indulas_ora, j.indulas_perc FROM nyomvonalak ny INNER JOIN jaratok as j ON j.id_nyomvonal = ny.id_nyomvonal INNER JOIN nyomvonal_tetelek as nyt ON j.id_menetido = nyt.id_menetido INNER JOIN naptar as n ON j.id_jarat = n.id_jarat WHERE n.datum = \"" + Date + "\" AND nyt.id_kocsiallas = " + StopId + " ORDER BY j.indulas_ora, j.indulas_perc;", null);
            while (cursor.moveToNext()) {
                Calendar calendar = Calendar.getInstance();
                calendar.set(Calendar.HOUR_OF_DAY, cursor.getInt(5));
                calendar.set(Calendar.MINUTE, cursor.getInt(6));

                calendar.add(Calendar.MINUTE, cursor.getInt(4));

                Calendar MaxLimit = (Calendar) GetTime.clone();
                MaxLimit.add(Calendar.MINUTE, 90);

                if (calendar.after(GetTime) && calendar.before(MaxLimit)) {
                    long diff = calendar.getTime().getTime() - GetTime.getTime().getTime();
                    int RemainingMinute = (int) (diff / 1000) / 60;

                    Lines.add(new IncomingBusRespModel(cursor.getString(2), cursor.getString(3), calendar.getTime(), cursor.getString(0), RemainingMinute, false));
                }
            }
            cursor.close();

            Collections.sort(Lines, (incomingBusRespModel, t1) -> incomingBusRespModel.getRemainingMin() - t1.getRemainingMin());

            IncomingBusRespModel[] ret = new IncomingBusRespModel[Lines.size()];
            Lines.toArray(ret);
            return ret;
        } catch (Exception e) {
            log(e.toString());
            return new IncomingBusRespModel[0];
        }
    }

    public String ConvertTripId (String StartingStopId, int DepartureTimeHour, int DepartureTimeMinute, String Date, String TripName) {
        try {
            String TripId = "-1";

            Cursor cursor = Sld.rawQuery("SELECT j.id_jarat FROM nyomvonalak as ny INNER JOIN jaratok AS j ON j.id_nyomvonal = ny.id_nyomvonal INNER JOIN nyomvonal_tetelek AS nyt ON nyt.id_menetido = j.id_menetido INNER JOIN naptar AS n ON n.id_jarat = j.id_jarat WHERE nyt.sorrend = 1 AND nyt.id_kocsiallas = " + StartingStopId + " AND j.indulas_ora = " + DepartureTimeHour + " AND j.indulas_perc = " + DepartureTimeMinute + " AND n.datum = \"" + Date + "\" AND ny.vonal_nev = \"" + TripName + "\";", null);
            while (cursor.moveToNext()) {
                TripId = cursor.getString(0);
            }
            cursor.close();

            return TripId;
        } catch (Exception e) {
            log(e.toString());
            return "-1";
        }
    }

    public boolean GetBusDatabaseValidDate(String Date) {
        try {
            int BusCount = 0;

            Cursor cursor = Sld.rawQuery("SELECT count(datum) FROM naptar WHERE datum = \"" + Date + "\";", null);
            while (cursor.moveToNext()) {
                BusCount = cursor.getInt(0);
            }
            cursor.close();

            return BusCount > 0;
        } catch (Exception e) {
            log(e.toString());
            return false;
        }
    }

    private void log (String msg) {
        Log.e("DatabaseManager", msg);
    }
}
