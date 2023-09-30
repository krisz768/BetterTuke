package hu.krisz768.bettertuke.UserDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 2;
    private static final String DATABASECREATESQL =
            "CREATE TABLE \"Favorites\" (" +
            "\"Id\" INTEGER PRIMARY KEY AUTOINCREMENT," +
            "\"Type\" INTEGER," +
            "\"Data\" TEXT" +
            ");";

    private static final String DATABASEUPGRADEV2 =
            "CREATE TABLE \"Preferences\" (" +
                    "\"Id\" TEXT PRIMARY KEY," +
                    "\"Value\" TEXT" +
                    ");";

    public UserDatabaseHelper(Context context, String DATABASEFILE) {
        super(context, DATABASEFILE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASECREATESQL);
        sqLiteDatabase.execSQL(DATABASEUPGRADEV2);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {
        if (i == 1 && i1 == 2) {
            sqLiteDatabase.execSQL(DATABASEUPGRADEV2);
        }
    }
}
