package hu.krisz768.bettertuke.Gtfs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.krisz768.bettertuke.Database.LineInfoRoute;

public class GTFSDatabase {
    private static SQLiteDatabase Sld;

    public String GetStopName (int StopId) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT stop_desc FROM stops f WHERE stop_id = " + StopId +";", null);
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

    public String ConvertTripId (String StartingStopId, String DepartureTime, String Date, String TripName) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT st.trip_id FROM stop_times as st INNER JOIN trips as t ON st.trip_id = t.trip_id INNER JOIN calendar_dates as cd ON t.service_id = cd.service_id INNER JOIN routes as r ON t.route_id = r.route_id WHERE r.route_short_name = \"" + TripName + "\" AND st.stop_sequence = 0 AND st.stop_id = " + StartingStopId + " AND st.departure_time = \"" + DepartureTime + "\" AND cd.date = \"" + Date + "\" AND cd.exception_type = 1;", null);
            String TripId = null;
            while(cursor.moveToNext()) {
                TripId = cursor.getString(0);
            }
            cursor.close();
            return TripId;
        } catch (Exception e) {
            log(e.toString());
            return null;
        }
    }

    public LineInfoRoute[] GetGTFSGPSRoute(String Id){
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT s.shape_pt_lat, s.shape_pt_lon, s.shape_id, s.shape_pt_sequence FROM shapes as s INNER JOIN trips as t on t.shape_id = s.shape_id WHERE t.trip_id = \"" + Id + "\" ORDER BY s.shape_pt_sequence DESC;", null);
            List<LineInfoRoute> lineInfoRoutes = new ArrayList<>();
            while(cursor.moveToNext()) {
                lineInfoRoutes.add(new LineInfoRoute(cursor.getInt(2), Float.parseFloat(cursor.getString(1)), Float.parseFloat(cursor.getString(0))));
            }
            cursor.close();

            LineInfoRoute[] ret  = new LineInfoRoute[lineInfoRoutes.size()];
            lineInfoRoutes.toArray(ret);
            return ret;
        } catch (Exception e) {
            log(e.toString());
            return null;
        }
    }

    public String GetContinueTrip(String TripId, String CurrenStartTime){
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT block_id FROM trips WHERE trip_id = \"" + TripId + "\";", null);
            String CTripId = null;

            String BlockId = null;
            while(cursor.moveToNext()) {
                BlockId = cursor.getString(0);
            }
            cursor.close();

            if (BlockId != null && !BlockId.equals("")) {
                cursor = Sld.rawQuery("SELECT t.trip_id from trips as t INNER JOIN stop_times as st ON st.trip_id = t.trip_id WHERE t.block_id = " + BlockId + " AND st.arrival_time > \"" + CurrenStartTime + "\" AND st.stop_sequence = 0 ORDER BY st.arrival_time LIMIT 1;", null);
                while(cursor.moveToNext()) {
                    CTripId = cursor.getString(0);
                }
                cursor.close();
            }

            return CTripId;
        } catch (Exception e) {
            log(e.toString());
            return null;
        }
    }

    public GTFSBusLineData GetLineData (String TripId) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT r.route_short_name, st.departure_time, st.stop_id FROM stop_times as st INNER JOIN trips as t ON st.trip_id = t.trip_id INNER JOIN routes as r ON t.route_id = r.route_id WHERE st.stop_sequence = 0 AND st.trip_id = \"" + TripId + "\";", null);
            GTFSBusLineData busLineData = null;
            while(cursor.moveToNext()) {
                busLineData = new GTFSBusLineData(cursor.getString(0), cursor.getString(1),cursor.getString(2));
            }
            cursor.close();
            return busLineData;
        } catch (Exception e) {
            log(e.toString());
            return null;
        }
    }

    public static void DeleteDatabase(Context Ctx) {
        File Database = new File(Ctx.getFilesDir() + "/Database", "GTFS.db");
        try {
            Database.delete();
        } catch (Exception e) {
            Log.e("DatabaseManager", e.toString());
        }
    }

    public GTFSDatabase (Context Ctx) {
        String DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "GTFS.db")).getAbsolutePath();

        if (Sld == null) {
            GTFSDatabaseHelper Dbh = new GTFSDatabaseHelper(Ctx, DATABASEFILE);
            Sld = Dbh.getWritableDatabase();
        }
    }

    public void InsertRoutes(String fileContent) {
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO routes (route_id, route_short_name, route_long_name, route_type, route_color, route_text_color) VALUES (?, ?, ?, ?, ?, ?);");
       for (int i = 1; i< Lines.length; i++) {
           String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

           try {
               stmt.bindLong(1, Long.parseLong(Data[0]));
               stmt.bindString(2, Data[1]);
               stmt.bindString(3, Data[2].replaceAll("\"", ""));
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

    public void InsertCalendar(String fileContent) {
        String[] Lines = fileContent.split("\n");
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES (?,?,?,?,?,?,?,?,?,?)");
        Sld.beginTransaction();
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
                stmt.bindLong(10, Long.parseLong(Data[9]));

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
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindLong(2, Long.parseLong(Data[1]));
                stmt.bindLong(3, Long.parseLong(Data[2]));

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
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES (?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
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
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO stop_times (trip_id, arrival_time, departure_time, stop_id, stop_sequence, stop_headsign, pickup_type, drop_off_type) VALUES (?,?,?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindLong(4, Long.parseLong(Data[3]));
                stmt.bindLong(5, Long.parseLong(Data[4]));
                stmt.bindString(6, "");
                stmt.bindString(7, Data[6]);
                stmt.bindString(8, Data[7]);

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
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO stops (stop_id, stop_name, stop_desc, stop_lat, stop_lon, parent_station) VALUES (?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindString(2, Data[1].replaceAll("\"", ""));
                stmt.bindString(3, Data[2].replaceAll("\"", ""));
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

    public void InsertTransfers(String fileContent) {
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO transfers (from_stop_id, to_stop_id, from_route_id, to_route_id, transfer_type) VALUES (?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindLong(2, Long.parseLong(Data[1]));
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindLong(5, Long.parseLong(Data[4]));

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
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO trips (route_id, service_id, trip_id, shape_id, block_id, trip_headsign, bikes_allowed) VALUES (?,?,?,?,?,?,?)");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
                stmt.bindString(6, Data[5].replaceAll("\"", ""));
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

    private void log (String msg) {
        Log.e("GTFSDatabase", msg);
    }
}
