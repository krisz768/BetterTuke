package hu.krisz768.bettertuke.Database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class TukeDatabaseHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;

    public TukeDatabaseHelper(Context context, String DATABASEFILE) {
        super(context, DATABASEFILE, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db) {

    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
