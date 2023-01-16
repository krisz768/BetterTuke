package hu.krisz768.bettertuke.UserDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    public static final int DATABASE_VERSION = 1;
    private static final String DATABASECREATESQL =
            "CREATE TABLE \"Favorites\" (" +
            "\"Id\" INTEGER PRIMARY KEY AUTOINCREMENT," +
            "\"Type\" INTEGER," +
            "\"Data\" TEXT" +
            ");";

    public UserDatabaseHelper(Context context, String DATABASEFILE) {
        super(context, DATABASEFILE, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(DATABASECREATESQL);
    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int i, int i1) {

    }
}
