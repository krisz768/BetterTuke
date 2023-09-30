package hu.krisz768.bettertuke.Gtfs;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class GTFSDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASECREATESQL =
            "CREATE TABLE \"routes\" (" +
                    "\"route_id\" INTEGER PRIMARY KEY," +
                    "\"route_short_name\" TEXT," +
                    "\"route_long_name\" TEXT," +
                    "\"route_type\" TEXT," +
                    "\"route_color\" TEXT," +
                    "\"route_text_color\" TEXT" +
                    ");";

    private static final String DATABASECREATESQL2 =
            "CREATE TABLE \"calendar\" (" +
                    "\"service_id\" TEXT PRIMARY KEY," +
                    "\"monday\" INTEGER," +
                    "\"tuesday\" INTEGER," +
                    "\"wednesday\" INTEGER," +
                    "\"thursday\" INTEGER," +
                    "\"friday\" INTEGER," +
                    "\"saturday\" INTEGER," +
                    "\"sunday\" INTEGER," +
                    "\"start_date\" INTEGER," +
                    "\"end_date\" INTEGER" +
                    ");";

    private static final String DATABASECREATESQL3 =
            "CREATE TABLE \"calendar_dates\" (" +
                    "\"service_id\" TEXT," +
                    "\"date\" INTEGER," +
                    "\"exception_type\" INTEGER" +
                    ");";

    private static final String DATABASECREATESQL4 =
            "CREATE TABLE \"shapes\" (" +
                    "\"shape_id\" INTEGER," +
                    "\"shape_pt_lat\" TEXT," +
                    "\"shape_pt_lon\" TEXT," +
                    "\"shape_pt_sequence\" INTEGER," +
                    "\"shape_dist_traveled\" TEXT" +
                    ");";

    private static final String DATABASECREATESQL5 =
            "CREATE TABLE \"stop_times\" (" +
                    "\"trip_id\" TEXT," +
                    "\"arrival_time\" TEXT," +
                    "\"departure_time\" TEXT," +
                    "\"stop_id\" INTEGER," +
                    "\"stop_sequence\" INTEGER," +
                    "\"stop_headsign\" TEXT," +
                    "\"pickup_type\" TEXT," +
                    "\"drop_off_type\" TEXT" +
                    ");";

    private static final String DATABASECREATESQL6 =
            "CREATE TABLE \"stops\" (" +
                    "\"stop_id\" INTEGER PRIMARY KEY," +
                    "\"stop_name\" TEXT," +
                    "\"stop_desc\" TEXT," +
                    "\"stop_lat\" TEXT," +
                    "\"stop_lon\" TEXT," +
                    "\"location_type\" INTEGER," +
                    "\"parent_station\" TEXT" +
                    ");";

    private static final String DATABASECREATESQL7 =
            "CREATE TABLE \"transfers\" (" +
                    "\"from_stop_id\" INTEGER," +
                    "\"to_stop_id\" INTEGER," +
                    "\"from_route_id\" TEXT," +
                    "\"to_route_id\" TEXT," +
                    "\"transfer_type\" INTEGER" +
                    ");";

    private static final String DATABASECREATESQL8 =
            "CREATE TABLE \"trips\" (" +
                    "\"route_id\" INTEGER," +
                    "\"service_id\" TEXT," +
                    "\"trip_id\" TEXT," +
                    "\"shape_id\" INTEGER," +
                    "\"block_id\" TEXT," +
                    "\"trip_headsign\" TEXT," +
                    "\"bikes_allowed\" TEXT" +
                    ");";

    public GTFSDatabaseHelper(Context context, String DATABASEFILE) {
        super(context, DATABASEFILE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASECREATESQL);
        sqLiteDatabase.execSQL(DATABASECREATESQL2);
        sqLiteDatabase.execSQL(DATABASECREATESQL3);
        sqLiteDatabase.execSQL(DATABASECREATESQL4);
        sqLiteDatabase.execSQL(DATABASECREATESQL5);
        sqLiteDatabase.execSQL(DATABASECREATESQL6);
        sqLiteDatabase.execSQL(DATABASECREATESQL7);
        sqLiteDatabase.execSQL(DATABASECREATESQL8);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}