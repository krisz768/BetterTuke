package hu.krisz768.bettertuke.Gtfs;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.util.Log;

import java.io.File;

import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.UserDatabase.UserDatabaseHelper;

public class GTFSDatabase {
    private static SQLiteDatabase Sld;

    private final Context Ctx;

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








    public static boolean DeleteDatabase(Context Ctx) {
        File Database = new File(Ctx.getFilesDir() + "/Database", "GTFS.db");
        try {
            return Database.delete();
        } catch (Exception e) {
            Log.e("DatabaseManager", e.toString());
            return false;
        }
    }

    public GTFSDatabase (Context Ctx) {
        this.Ctx = Ctx;
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
           String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

           try {
               stmt.bindLong(1, Long.parseLong(Data[0]));
               stmt.bindString(2, Data[1]);
               stmt.bindString(3, Data[2].replaceAll("\"", ""));
               stmt.bindString(4, Data[3]);
               stmt.bindString(5, Data[4]);
               stmt.bindString(6, Data[5]);

               stmt.executeInsert();
               stmt.clearBindings();
               //Sld.execSQL("INSERT INTO routes (route_id, route_short_name, route_long_name, route_type, route_color, route_text_color) VALUES (" + Data[0] + ",\"" + Data[1] + "\",\"" + Data[2].replaceAll("\"", "") + "\",\"" + Data[3] + "\",\"" + Data[4] + "\",\"" + Data[5] + "\"" + ");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

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
                //Sld.execSQL("INSERT INTO calendar (service_id, monday, tuesday, wednesday, thursday, friday, saturday, sunday, start_date, end_date) VALUES (\"" + Data[0] + "\"," + Data[1] + "," + Data[2] + "," + Data[3] + "," + Data[4] + "," + Data[5] + "," + Data[6] + "," + Data[7] + "," + Data[8] + "," + Data[9] + ");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindLong(2, Long.parseLong(Data[1]));
                stmt.bindLong(3, Long.parseLong(Data[2]));

                stmt.executeInsert();
                stmt.clearBindings();
                //Sld.execSQL("INSERT INTO calendar_dates (service_id, date, exception_type) VALUES (\"" + Data[0] + "\"," + Data[1] + "," + Data[2] + ");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

            try {

                //Sld.insert("shapes", null, )
                //Sld.execSQL("INSERT INTO shapes (shape_id, shape_pt_lat, shape_pt_lon, shape_pt_sequence, shape_dist_traveled) VALUES (" + Data[0] + ",\"" + Data[1] + "\",\"" + Data[2] + "\"," + Data[3] + ",\"" + Data[4] + "\");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

            try {
                stmt.bindString(1, Data[0]);
                stmt.bindString(2, Data[1]);
                stmt.bindString(3, Data[2]);
                stmt.bindLong(4, Long.parseLong(Data[3]));
                stmt.bindLong(5, Long.parseLong(Data[4]));
                stmt.bindString(6, Data[5].replaceAll("\"", ""));
                stmt.bindString(7, Data[6]);
                stmt.bindString(8, Data[7]);

                stmt.executeInsert();
                stmt.clearBindings();
                //Sld.execSQL("INSERT INTO stop_times (trip_id, arrival_time, departure_time, stop_id, stop_sequence, stop_headsign, pickup_type, drop_off_type) VALUES (\"" + Data[0] + "\",\"" + Data[1] + "\",\"" + Data[2] + "\"," + Data[3] + "," + Data[4] + ",\"" + Data[5].replaceAll("\"", "") + "\",\"" + Data[6] + "\",\"" + Data[7] + "\");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindString(2, Data[1].replaceAll("\"", ""));
                stmt.bindString(3, Data[2].replaceAll("\"", ""));
                stmt.bindString(4, Data[3]);
                stmt.bindString(5, Data[4]);
                stmt.bindString(6, Data[5]);

                stmt.executeInsert();
                stmt.clearBindings();
                //Sld.execSQL("INSERT INTO stops (stop_id, stop_name, stop_desc, stop_lat, stop_lon, parent_station) VALUES (" + Data[0] + ",\"" + Data[1].replaceAll("\"", "") + "\",\"" + Data[2].replaceAll("\"", "") + "\",\"" + Data[3] + "\"," + Data[4] + ",\"" + Data[5] + "\");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

            try {
                stmt.bindLong(1, Long.parseLong(Data[0]));
                stmt.bindLong(2, Long.parseLong(Data[1]));
                stmt.bindString(3, Data[2]);
                stmt.bindString(4, Data[3]);
                stmt.bindLong(5, Long.parseLong(Data[4]));

                stmt.executeInsert();
                stmt.clearBindings();
                //Sld.execSQL("INSERT INTO transfers (from_stop_id, to_stop_id, from_route_id, to_route_id, transfer_type) VALUES (" + Data[0] + "," + Data[1] + ",\"" + Data[2] + "\",\"" + Data[3] + "\"," + Data[4] + ");");
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
            String[] Data= Lines[i].split(",(?=(?:[^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);

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
                //Sld.execSQL("INSERT INTO trips (route_id, service_id, trip_id, shape_id, block_id, trip_headsign, bikes_allowed) VALUES (" + Data[0] + ",\"" + Data[1] + "\",\"" + Data[2] + "\"," + Data[3] + ",\"" + Data[4] + "\",\"" + Data[5].replaceAll("\"", "") + "\",\"" + Data[6] + "\");");
            } catch (Exception e) {
                log(e.toString());
            }
        }
        Sld.setTransactionSuccessful();
        Sld.endTransaction();
    }

    public boolean IsData() {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT count(route_id) FROM routes WHERE 1;", null);
            int count = 0;
            while(cursor.moveToNext()) {
                count = cursor.getInt(0);
            }
            cursor.close();

            return count>0;

        } catch (Exception e) {
            log(e.toString());
            return false;
        }
    }

    private void log (String msg) {
        Log.e("GTFSDatabase", msg);
    }
}
