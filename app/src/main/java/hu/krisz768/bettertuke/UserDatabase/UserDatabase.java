package hu.krisz768.bettertuke.UserDatabase;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

import hu.krisz768.bettertuke.Database.TukeDatabaseHelper;

public class UserDatabase {
    private static SQLiteDatabase Sld;

    private final String DATABASEFILE;
    private final Context Ctx;

    public  UserDatabase (Context Ctx) {
        this.Ctx = Ctx;
        DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "user.db")).getAbsolutePath();

        if (Sld == null) {
            UserDatabaseHelper Dbh = new UserDatabaseHelper(Ctx, DATABASEFILE);
            Sld = Dbh.getWritableDatabase();
        }


    }
}
