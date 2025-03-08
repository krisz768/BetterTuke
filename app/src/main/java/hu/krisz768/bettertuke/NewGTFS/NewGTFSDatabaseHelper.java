package hu.krisz768.bettertuke.NewGTFS;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class NewGTFSDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    private static final String DATABASECREATESQL =
            "CREATE TABLE \"routes\" (" +
                    "\"route_id\" TEXT PRIMARY KEY," +
                    "\"agency_id\" TEXT," +
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
                    "\"shape_dist_traveled\" INTEGER" +
                    ");";

    private static final String DATABASECREATESQL5 =
            "CREATE TABLE \"stop_times\" (" +
                    "\"trip_id\" TEXT," +
                    "\"arrival_time\" TEXT," +
                    "\"departure_time\" TEXT," +
                    "\"stop_id\" TEXT," +
                    "\"stop_sequence\" INTEGER," +
                    "\"stop_headsign\" TEXT" +
                    ");";

    private static final String DATABASECREATESQL6 =
            "CREATE TABLE \"stops\" (" +
                    "\"stop_id\" TEXT PRIMARY KEY," +
                    "\"stop_code\" TEXT," +
                    "\"stop_name\" TEXT," +
                    "\"stop_desc\" TEXT," +
                    "\"stop_lat\" TEXT," +
                    "\"stop_lon\" TEXT," +
                    "\"zone_id\" TEXT," +
                    "\"stop_url\" TEXT," +
                    "\"location_type\" INTEGER," +
                    "\"stop_timezone\" TEXT," +
                    "\"wheelchair_boarding\" TEXT" +
                    ");";

    private static final String DATABASECREATESQL7 =
            "CREATE TABLE \"trips\" (" +
                    "\"route_id\" TEXT," +
                    "\"service_id\" TEXT," +
                    "\"trip_id\" TEXT," +
                    "\"trip_headsign\" TEXT," +
                    "\"trip_short_name\" TEXT," +
                    "\"direction_id\" INTEGER," +
                    "\"shape_id\" INTEGER" +
                    ");";

    private static final String DATABASECREATESQL8 =
            "CREATE TABLE \"feed_info\" (" +
                    "\"feed_publisher_name\" TEXT," +
                    "\"feed_publisher_url\" TEXT," +
                    "\"feed_lang\" TEXT," +
                    "\"feed_start_date\" TEXT," +
                    "\"feed_end_date\" TEXT," +
                    "\"feed_version\" TEXT" +
                    ");";

    public NewGTFSDatabaseHelper(Context context, String DATABASEFILE) {
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