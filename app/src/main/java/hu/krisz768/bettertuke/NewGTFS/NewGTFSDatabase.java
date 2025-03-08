package hu.krisz768.bettertuke.NewGTFS;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteStatement;
import android.os.Environment;
import android.util.Log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

import hu.krisz768.bettertuke.Database.TukeDatabaseHelper;
import hu.krisz768.bettertuke.Gtfs.GTFSDatabaseHelper;

public class NewGTFSDatabase {
    private static SQLiteDatabase Sld;


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

    public static void DeleteDatabase(Context Ctx) throws IOException {
        File Database = new File(Ctx.getFilesDir() + "/Database", "NewGTFS.db");
        if (Database.exists()) {
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
        }



        try {
            Database.delete();
        } catch (Exception e) {
            Log.e("DatabaseManager", e.toString());
        }
    }

    public NewGTFSDatabase (Context Ctx) {
        String DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "NewGTFS.db")).getAbsolutePath();

        if (Sld == null) {
            NewGTFSDatabaseHelper Dbh = new NewGTFSDatabaseHelper(Ctx, DATABASEFILE);
            Sld = Dbh.getWritableDatabase();
        }
    }

    public void InsertRoutes(String fileContent) {
        log("NewGTFS Start Routes");
        String[] Lines = fileContent.split("\n");
        Sld.beginTransaction();
        SQLiteStatement stmt = Sld.compileStatement("INSERT INTO routes (route_id, agency_id, route_short_name, route_long_name, route_type, route_color, route_text_color) VALUES (?, ?, ?, ?, ?, ?, ?);");
        for (int i = 1; i< Lines.length; i++) {
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", 0-1);

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
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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
            String[] Data= Lines[i].split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)", -1);

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

    private void log (String msg) {
        Log.e("NewGTFSDatabase", msg);
    }
}
