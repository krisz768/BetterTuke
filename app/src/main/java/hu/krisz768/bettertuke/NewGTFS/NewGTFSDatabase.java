package hu.krisz768.bettertuke.NewGTFS;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusNum;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusScheduleTime;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.BusVariation;
import hu.krisz768.bettertuke.Database.LineInfoRoute;
import hu.krisz768.bettertuke.Database.LineInfoRouteInfo;
import hu.krisz768.bettertuke.Database.LineInfoTravelTime;
import hu.krisz768.bettertuke.Gtfs.GTFSBusLineData;
import hu.krisz768.bettertuke.Gtfs.GTFSDatabase;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

public class NewGTFSDatabase {
    private static SQLiteDatabase Sld;
    private final Context ctx;

    public NewGTFSDatabase (Context Ctx) {
        this.ctx = Ctx;
        String DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "NewGTFS.db")).getAbsolutePath();

        if (Sld == null) {
            NewGTFSDatabaseHelper Dbh = new NewGTFSDatabaseHelper(Ctx, DATABASEFILE);
            Sld = Dbh.getWritableDatabase();
        }
    }

    public HashMap<String, BusStops> GetAllBusStops () {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT * FROM stops WHERE 1", null);
            HashMap<String, BusStops> AllStops = new HashMap<>();
            while(cursor.moveToNext()) {
                AllStops.put(cursor.getString(0),new BusStops(cursor.getString(0), cursor.getString(1), cursor.getFloat(5), cursor.getFloat(4), cursor.getString(0).substring(cursor.getString(0).length()-1)));
            }
            cursor.close();

            return AllStops;

        } catch (Exception e) {
            log(e.toString());
            return new HashMap<>(0);
        }
    }

    public HashMap<Integer, BusPlaces> GetAllBusPlaces () {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT stop_code, stop_name, avg(stop_lat), avg(stop_lon) from stops GROUP BY stop_code;", null);
            HashMap<Integer, BusPlaces> AllPlaces = new HashMap<>();
            while(cursor.moveToNext()) {
                AllPlaces.put(cursor.getInt(0), new BusPlaces(cursor.getInt(0), cursor.getString(1), cursor.getFloat(3), cursor.getFloat(2)));
            }
            cursor.close();

            return AllPlaces;

        } catch (Exception e) {
            log(e.toString());
            return new HashMap<>(0);

        }
    }

    public String GetStopName (String StopId) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT stop_name FROM stops WHERE stop_id = '" + StopId +"';", null);
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

    public String GetDirectionName (String StopId) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT stop_desc FROM stops WHERE stop_id = '" + StopId +"';", null);
            String Name = null;
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

    public BusNum[] GetActiveBusLines() {
        try {
            List<BusNum> Lines = new ArrayList<>();

            Cursor cursor = Sld.rawQuery("SELECT DISTINCT route_short_name, route_long_name FROM routes ORDER BY '0' + route_short_name;", null);
            while (cursor.moveToNext()) {
                Lines.add(new BusNum(cursor.getString(0), cursor.getString(1)));
            }
            cursor.close();

            BusNum[] ret = new BusNum[Lines.size()];
            Lines.toArray(ret);
            return ret;
        } catch (Exception e) {
            log(e.toString());
            return new BusNum[0];
        }
    }

    public BusNum[] GetActiveBusLinesFromStop(String StopId) {
        try {
            List<BusNum> Lines = new ArrayList<>();

            Cursor cursor = Sld.rawQuery("SELECT DISTINCT r.route_short_name, r.route_long_name FROM routes as r INNER JOIN trips as t ON t.route_id = r.route_id INNER JOIN stop_times AS st ON st.trip_id = t.trip_id WHERE st.stop_id = '" + StopId + "' ORDER BY '0' + r.route_short_name;", null);
            while (cursor.moveToNext()) {
                Lines.add(new BusNum(cursor.getString(0), cursor.getString(1)));
            }
            cursor.close();

            BusNum[] ret = new BusNum[Lines.size()];
            Lines.toArray(ret);
            return ret;
        } catch (Exception e) {
            log(e.toString());
            return new BusNum[0];
        }
    }

    public BusVariation[] GetBusVariations(String LineNum) {
        try {
            List<BusVariation> Lines = new ArrayList<>();

            //SELECT GROUP_CONCAT(trip_headsign), direction_id FROM (SELECT DISTINCT substr(t.trip_headsign , " + (LineNum.length()+1) + ") as trip_headsign, t.direction_id FROM trips t INNER JOIN routes AS r ON t.route_id = r.route_id WHERE r.route_short_name = '" + LineNum + "' ORDER BY r.route_short_name, t.direction_id LIMIT 7) GROUP BY direction_id;
            Cursor cursor = Sld.rawQuery("SELECT GROUP_CONCAT(trip_headsign), direction_id FROM (SELECT DISTINCT substr(t.trip_headsign , " + (LineNum.length()+2) + ") as trip_headsign, t.direction_id FROM trips t INNER JOIN routes AS r ON t.route_id = r.route_id WHERE r.route_short_name = '" + LineNum + "' ORDER BY r.route_short_name, t.direction_id LIMIT 7) GROUP BY direction_id;", null);
            while (cursor.moveToNext()) {
                Lines.add(new BusVariation(cursor.getString(0), cursor.getString(1), ""));
            }
            cursor.close();

            BusVariation[] ret = new BusVariation[Lines.size()];
            Lines.toArray(ret);

            return ret;
        } catch (Exception e) {
            log(e.toString());
            return new BusVariation[0];
        }
    }

    public BusVariation[] GetBusVariationsFromStop(String LineNum, String StopId) {
        try {
            List<BusVariation> Lines = new ArrayList<>();

            Cursor cursor = Sld.rawQuery("SELECT GROUP_CONCAT(trip_headsign), direction_id FROM (SELECT DISTINCT substr(t.trip_headsign , " + (LineNum.length()+1) + ") as trip_headsign, t.direction_id FROM trips t INNER JOIN routes AS r ON t.route_id = r.route_id INNER JOIN stop_times AS st ON st.trip_id = t.trip_id WHERE st.stop_id = '" + StopId + "' AND r.route_short_name = '" + LineNum + "' ORDER BY r.route_short_name, t.direction_id LIMIT 7) GROUP BY direction_id;", null);
            while (cursor.moveToNext()) {
                Lines.add(new BusVariation(cursor.getString(0), cursor.getString(1), ""));
            }
            cursor.close();

            BusVariation[] ret = new BusVariation[Lines.size()];
            Lines.toArray(ret);
            return ret;
        } catch (Exception e) {
            log(e.toString());
            return new BusVariation[0];
        }
    }

    public BusScheduleTime[] GetBusScheduleTimeFromStart(String LineNum, String date, String Direction) {
        try {
            List<BusScheduleTime> Lines = new ArrayList<>();


            Cursor cursor = Sld.rawQuery("SELECT st.arrival_time, st.trip_id FROM stop_times AS st INNER JOIN trips AS t ON t.trip_id = st.trip_id INNER JOIN routes AS r ON r.route_id = t.route_id INNER JOIN calendar_dates AS cd ON cd.service_id = t.service_id WHERE st.stop_sequence = 1 AND r.route_short_name = '" + LineNum + "' AND t.direction_id = '" + (Direction.equals("O") ? "0" : "1") + "' AND cd.date = '" + date + "' ORDER BY st.arrival_time;", null);

            while (cursor.moveToNext()) {
                String[] TimeParts = (cursor.getString(0)).split(":");

                Lines.add(new BusScheduleTime(Integer.parseInt(TimeParts[0]), Integer.parseInt(TimeParts[1]), "", cursor.getString(1)));


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

    public BusScheduleTime[] GetBusScheduleTimeFromStop(String LineNum, String date, String Direction, String StopId) {
        try {
            List<BusScheduleTime> Lines = new ArrayList<>();

            Cursor cursor = Sld.rawQuery("SELECT st2.arrival_time, st.trip_id FROM stop_times AS st INNER JOIN trips AS t ON t.trip_id = st.trip_id INNER JOIN routes AS r ON r.route_id = t.route_id INNER JOIN calendar_dates AS cd ON cd.service_id = t.service_id INNER JOIN stop_times AS st2 ON t.trip_id = st2.trip_id WHERE st.stop_id = '" + StopId + "' AND st2.stop_sequence = 1 AND r.route_short_name = '" + LineNum + "' AND t.direction_id = '" + (Direction.equals("O") ? "0" : "1") + "' AND cd.date = '" + date + "' ORDER BY st.arrival_time;", null);
            while (cursor.moveToNext()) {
                String[] TimeParts = (cursor.getString(0)).split(":");

                Lines.add(new BusScheduleTime(Integer.parseInt(TimeParts[0]), Integer.parseInt(TimeParts[1]), "", cursor.getString(1)));
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

    public IncomingBusRespModel[] GetOfflineDepartureTimes(String StopId, String Date, String Time) {

        try {
            List<IncomingBusRespModel> Lines = new ArrayList<>();
            Calendar GetTime = Calendar.getInstance();
            GetTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(Time.split(":")[0]));
            GetTime.set(Calendar.MINUTE, Integer.parseInt(Time.split(":")[1]));


            Cursor cursor = Sld.rawQuery("SELECT t.trip_id, t.shape_id, r.route_short_name, substr(t.trip_headsign, length(r.route_short_name)+2) as trip_headsign, st.arrival_time, st2.arrival_time AS start_time FROM trips AS t INNER JOIN routes AS r ON t.route_id = r.route_id INNER JOIN stop_times AS st ON st.trip_id = t.trip_id INNER JOIN stop_times AS st2 ON st2.trip_id = t.trip_id INNER JOIN calendar_dates AS cd ON cd.service_id = t.service_id WHERE st.stop_id = '" + StopId + "' AND cd.date = '" + Date + "' AND st2.stop_sequence = 1 ORDER BY st.arrival_time;", null);

            //log("SELECT t.route_id, t.shape_id, r.route_short_name, t.trip_headsign, st.arrival_time, st2.arrival_time AS start_time FROM trips AS t INNER JOIN routes AS r ON t.route_id = r.route_id INNER JOIN stop_times AS st ON st.trip_id = t.trip_id INNER JOIN stop_times AS st2 ON st2.trip_id = t.trip_id INNER JOIN calendar_dates AS cd ON cd.service_id = t.service_id WHERE st.stop_id = '" + StopId + "' AND cd.date = '" + Date + "' AND st2.stop_sequence = 1 ORDER BY st.arrival_time;");
            while (cursor.moveToNext()) {

                Calendar calendar = Calendar.getInstance();

                String[] TimeParts = cursor.getString(4).split(":");

                calendar.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                calendar.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));

                //calendar.add(Calendar.MINUTE, cursor.getInt(4));

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

    public BusLine GetBusLineById(String Id, boolean GetGTFS, Date date) {
        try
        {
            BusLine ret = null;
            Cursor cursor = Sld.rawQuery("SELECT t.*, st.arrival_time FROM trips AS t INNER JOIN stop_times AS st ON st.trip_id = t.trip_id WHERE t.trip_id = '" + Id + "' AND st.stop_sequence = 1;", null);
            while(cursor.moveToNext()) {
                LineInfoTravelTime[] lineInfoTravelTimes = GetBusLineTravelTimeById(Id);
                LineInfoTravelTime StartStop = null;

                for (LineInfoTravelTime lineInfoTravelTime : lineInfoTravelTimes) {
                    if (StartStop == null || StartStop.getOrder() > lineInfoTravelTime.getOrder()) {
                        StartStop = lineInfoTravelTime;
                    }
                }

                String[] TimeParts = cursor.getString(7).split(":");
                int DepartureHour = Integer.parseInt(TimeParts[0]);
                int DepartureMinute = Integer.parseInt(TimeParts[1]);

                DateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");

                if (date == null) {
                    date = new Date();
                }

                LineInfoRouteInfo lineInfoRouteInfo = GetBusLineRouteInfoById(Id);

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

                                String CTripId = ConvertTripId(gtfsBusLineData.getStartStopId(), GTFSDepartureHour + ":" + Integer.parseInt(gtfsBusLineData.getDepartureTime().split(":")[1]) + ":00" , dateFormat.format(date), gtfsBusLineData.getLineId());
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


                ret = new BusLine(Id, DepartureHour,DepartureMinute, lineInfoTravelTimes, lineInfoRoute, lineInfoRouteInfo, CTrip);
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;

        }
    }

    public LineInfoTravelTime[] GetBusLineTravelTimeById(String Id) {
        try
        {
            List<LineInfoTravelTime> TravelTime = new ArrayList<>();
            Cursor cursor = Sld.rawQuery("SELECT * FROM stop_times WHERE trip_id = '" + Id + "' ORDER BY stop_sequence ASC;", null);
            while(cursor.moveToNext()) {
                TravelTime.add(new LineInfoTravelTime(cursor.getInt(4), cursor.getInt(4), cursor.getString(3),cursor.getString(1)));
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

    public LineInfoRouteInfo GetBusLineRouteInfoById(String Id) {
        try
        {
            LineInfoRouteInfo ret = null;
            Cursor cursor = Sld.rawQuery("SELECT r.route_short_name, substr(t.trip_headsign, length(r.route_short_name)+2) FROM trips AS t INNER JOIN routes AS r ON r.route_id = t.route_id WHERE t.trip_id = '" + Id + "';", null);
            while(cursor.moveToNext()) {
                ret = new LineInfoRouteInfo(Id, cursor.getString(0), cursor.getString(1));
            }
            cursor.close();

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
            Cursor cursor = Sld.rawQuery("SELECT * FROM shapes WHERE shape_id = " + Id + " ORDER BY shape_pt_sequence ASC;", null);
            while(cursor.moveToNext()) {
                Route.add(new LineInfoRoute(cursor.getInt(3), cursor.getFloat(2), cursor.getFloat(1)));
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

    public boolean IsStopInBusRoute(String trip_id, String StopId) {
        try {
            Cursor cursor = Sld.rawQuery("SELECT trip_id FROM stop_times WHERE trip_id = '" + trip_id + "' AND stop_id = '" + StopId + "';", null);
            while (cursor.moveToNext()) {
                return true;
            }
            cursor.close();

            return false;
        } catch (Exception e) {
            log(e.toString());
            return false;
        }
    }

    public String ConvertTripId (String StartingStopId, String DepartureTime, String Date, String TripName) {
        try
        {

            Cursor cursor = Sld.rawQuery("SELECT st.trip_id FROM stop_times as st INNER JOIN trips as t ON st.trip_id = t.trip_id INNER JOIN calendar_dates as cd ON t.service_id = cd.service_id INNER JOIN routes as r ON t.route_id = r.route_id WHERE r.route_short_name = '" + TripName + "' AND st.stop_sequence = 1 AND st.stop_id = 'SP" + StartingStopId + "' AND st.departure_time = '" + DepartureTime + "' AND cd.date = '" + Date + "' AND cd.exception_type = 1;", null);
            String TripId = "-1";
            while(cursor.moveToNext()) {
                TripId = cursor.getString(0);
            }
            cursor.close();
            return TripId;
        } catch (Exception e) {
            log(e.toString());
            return "-1";
        }
    }

    public int GetBusLineSumTravelTimeById(String LineId) {
        try {

            Calendar StartTime = Calendar.getInstance();
            StartTime.setTime(new Date());

            Calendar EndTime = Calendar.getInstance();
            EndTime.setTime(new Date());

            Cursor cursor = Sld.rawQuery("SELECT arrival_time FROM stop_times WHERE trip_id = '" + LineId + "' ORDER BY stop_sequence DESC LIMIT 1;", null);
            while (cursor.moveToNext()) {
                String[] TimeParts = cursor.getString(0).split(":");

                EndTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                EndTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));
            }
            cursor.close();

            cursor = Sld.rawQuery("SELECT arrival_time FROM stop_times WHERE trip_id = '" + LineId + "' ORDER BY stop_sequence ASC LIMIT 1;", null);
            while (cursor.moveToNext()) {
                String[] TimeParts = cursor.getString(0).split(":");

                StartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                StartTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));
            }
            cursor.close();

            long difference_In_Time = EndTime.getTime().getTime() - StartTime.getTime().getTime();

            return (int)((difference_In_Time / (1000 * 60)) % 60);
        } catch (Exception e) {
            log(e.toString());
            return 0;
        }
    }

    public int GetBusLineStopTravelTimeById(String LineId, String StopId) {
        try {

            Calendar StartTime = Calendar.getInstance();
            StartTime.setTime(new Date());

            Calendar EndTime = Calendar.getInstance();
            EndTime.setTime(new Date());

            Cursor cursor = Sld.rawQuery("SELECT arrival_time FROM stop_times WHERE trip_id = '" + LineId + "' AND stop_id = '" + StopId + "' ORDER BY stop_sequence DESC LIMIT 1;", null);
            while (cursor.moveToNext()) {
                String[] TimeParts = cursor.getString(0).split(":");

                EndTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                EndTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));
            }
            cursor.close();

            cursor = Sld.rawQuery("SELECT arrival_time FROM stop_times WHERE trip_id = '" + LineId + "' ORDER BY stop_sequence ASC LIMIT 1;", null);
            while (cursor.moveToNext()) {
                String[] TimeParts = cursor.getString(0).split(":");

                StartTime.set(Calendar.HOUR_OF_DAY, Integer.parseInt(TimeParts[0]));
                StartTime.set(Calendar.MINUTE, Integer.parseInt(TimeParts[1]));
            }
            cursor.close();

            long difference_In_Time = EndTime.getTime().getTime() - StartTime.getTime().getTime();

            return (int)((difference_In_Time / (1000 * 60)) % 60);
        } catch (Exception e) {
            log(e.toString());
            return 0;
        }
    }

    public boolean GetBusDatabaseValidDate(String Date) {
        try {
            int BusCount = 0;

            Cursor cursor = Sld.rawQuery("SELECT count(date) FROM calendar_dates WHERE date = '" + Date + "';", null);
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

    public static String GetDatabaseVersion(Context Ctx) {
        try
        {
            String DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "NewGTFS.db")).getAbsolutePath();
            NewGTFSDatabaseHelper Dbh = new NewGTFSDatabaseHelper(Ctx, DATABASEFILE);
            SQLiteDatabase Sld = Dbh.getReadableDatabase();

            Cursor cursor = Sld.rawQuery("SELECT feed_version FROM feed_info WHERE 1;", null);
            String version = "Err";
            while(cursor.moveToNext()) {
                version = cursor.getString(0);
            }
            cursor.close();

            Sld.close();
            Dbh.close();
            return version;
        } catch (Exception e) {
            Log.e("DatabaseManager", e.toString());
            return "Err";
        }
    }

    public static void DeleteDatabase(Context Ctx) {
        File Database = new File(Ctx.getFilesDir() + "/Database", "NewGTFS.db");
        /*if (Database.exists()) {
            File out = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS) + "/test.sql");
            int bytesum = 0;
            int byteread = 0;
            InputStream inStream = new FileInputStream(Database);
            FileOutputStream fs = new FileOutputStream(out);
            Log.d("ifExists", "copyFile: " + fs);
            byte[] buffer = new byte[1444];
            while ((byteread = inStream.read(buffer)) != -1) {
                bytesum += byteread;
                fs.write(buffer, 0, byteread);
            }
            inStream.close();
            fs.close();
        }*/



        try {
            Database.delete();
        } catch (Exception e) {
            Log.e("DatabaseManager", e.toString());
        }
    }

    public void InsertRoutes(String fileContent) {
        log("NewGTFS Start Routes");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_type, route_color, route_text_color) VALUES (?, ?, ?, ?, ?, ?, ?);");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
                stmt.bindString(6, Data[5]);
                stmt.bindString(7, Data[6]);

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertCalendar(String fileContent) {
        log("NewGTFS Start Calendar");
        String[] Lines = fileContent.split("\n");
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES (?,?,?,?,?,?,?,?,?,?)");
        Sld.beginTransaction();
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindLong(2, Long.parseLong(Data[1]));
                stmt.bindLong(3, Long.parseLong(Data[2]));
                stmt.bindLong(4, Long.parseLong(Data[3]));
                stmt.bindLong(5, Long.parseLong(Data[4]));
                stmt.bindLong(6, Long.parseLong(Data[5]));
                stmt.bindLong(7, Long.parseLong(Data[6]));
                stmt.bindLong(8, Long.parseLong(Data[7]));
                stmt.bindLong(9, Long.parseLong(Data[8]));
                stmt.bindLong(10, Long.parseLong(Data[9].trim()));

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertCalendarDates(String fileContent) {
        log("NewGTFS Start CalendarDates");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindLong(2, Long.parseLong(Data[1]));
                stmt.bindLong(3, Long.parseLong(Data[2].trim()));

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertShapes(String fileContent) {
        log("NewGTFS Start Shapes");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES (?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindLong(4, Long.parseLong(Data[3]));
                stmt.bindLong(5, Long.parseLong(Data[4].trim()));
                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }

        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertStopTimes(String fileContent) {
        log("NewGTFS Start StopTimes");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO stop_times (trip_id, arrival_time, departure_time, stop_id, stop_sequence, stop_headsign) VALUES (?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindLong(5, Long.parseLong(Data[4]));
                stmt.bindString(6, Data[5]);

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertStops(String fileContent) {
        log("NewGTFS Start Stops");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO stops (stop_id, stop_code, stop_name, stop_desc, stop_lat, stop_lon, zone_id, stop_url, location_type, stop_timezone, wheelchair_boarding) VALUES (?,?,?,?,?,?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", 0-1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
                stmt.bindString(6, Data[5]);
                stmt.bindString(7, Data[6]);
                stmt.bindString(8, Data[7]);
                stmt.bindLong(9, Long.parseLong(Data[8]));
                stmt.bindString(10, Data[9]);
                stmt.bindString(11, Data[10]);

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertFeedInfo(String fileContent) {
        log("NewGTFS Start FeedInfo");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO feed_info (feed_publisher_name, feed_publisher_url, feed_lang, feed_start_date, feed_end_date, feed_version) VALUES (?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
                stmt.bindString(6, Data[5]);

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public void InsertTrips(String fileContent) {
        log("NewGTFS Start Trips");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO trips (route_id, service_id, trip_id, trip_headsign, trip_short_name, direction_id, shape_id) VALUES (?,?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^']*'[^']*')*[^']*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
                stmt.bindLong(6, Long.parseLong(Data[5]));
                stmt.bindLong(7, Long.parseLong(Data[6].trim()));

                stmt.executeInsert();
                stmt.clearBindings();
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public String GetBusNameByStop(String TripId, String StopId) {
        try
        {
            String ret = null;
            Cursor cursor = Sld.rawQuery("SELECT substr(t.stop_headsign, length(r.route_short_name)+2) FROM stop_times AS t INNER JOIN trips AS tr ON t.trip_id = tr.trip_id INNER JOIN routes AS r ON r.route_id = tr.route_id WHERE t.trip_id = '" + TripId + "' AND t.stop_id = '" + StopId + "';", null);
            while(cursor.moveToNext()) {
                ret = cursor.getString(0);
            }
            cursor.close();

            return ret;

        } catch (Exception e) {
            log(e.toString());
            return null;
        }
    }

    private void log (String msg) {
        Log.e("NewGTFSDatabase", msg);
    }
}
