package hu.krisz768.bettertuke.UserDatabase;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

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

    public boolean IsFavorite(FavoriteType favoriteType, String Data) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT count(Id) FROM Favorites WHERE Type = " + TypeToInt(favoriteType) + " AND Data = \"" + Data + "\";", null);
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



    public void DeleteFavorite(FavoriteType favoriteType, String Data) {
        try {
            Sld.execSQL("DELETE FROM Favorites WHERE Data = \"" + Data + "\" AND Type = " + TypeToInt(favoriteType) + ";");
        } catch (Exception e) {
            log(e.toString());
        }
    }

    public void AddFavorite(FavoriteType favoriteType, String Data) {
        try {
            Sld.execSQL("INSERT INTO Favorites (Type, Data) VALUES (" + TypeToInt(favoriteType) + ",\"" + Data + "\");");
        } catch (Exception e) {
            log(e.toString());
        }
    }

    public enum FavoriteType {
        Stop,
        Line
    }

    private int TypeToInt(FavoriteType favoriteType) {
        switch (favoriteType) {
            case Line:
                return 0;
            case Stop:
                return 1;
        }

        return -1;
    }

    public Favorite[] GetFavorites (FavoriteType favoriteType) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT Id, Data FROM Favorites WHERE Type = " + TypeToInt(favoriteType) + " ORDER BY Id ASC;", null);
            List<Favorite> favoritesList = new ArrayList<>();
            while(cursor.moveToNext()) {
                favoritesList.add(new Favorite(cursor.getInt(0), cursor.getString(1)));
            }
            cursor.close();

            Favorite[] favorites = new Favorite[favoritesList.size()];

            favoritesList.toArray(favorites);

            return favorites;

        } catch (Exception e) {
            log(e.toString());
            return new Favorite[0];

        }
    }

    public void SwapId(int First, int Sec) {
        try {
            Sld.execSQL("update Favorites set Id = (case when Id = " + First + " then " + (Sec*-1) + " else " + (First*-1) + " end) where id in (" + First + "," + Sec + ");");
            Sld.execSQL("update Favorites set Id = - Id where Id < 0;");
        } catch (Exception e) {
            log(e.toString());
        }
    }

    public int GetId(String Data, FavoriteType favoriteType) {
        try
        {
            Cursor cursor = Sld.rawQuery("SELECT Id FROM Favorites WHERE Type = " + TypeToInt(favoriteType) + " AND Data = \"" + Data + "\";", null);
            int Id = -1;
            while(cursor.moveToNext()) {
                Id = cursor.getInt(0);
                break;
            }
            cursor.close();

            return Id;

        } catch (Exception e) {
            log(e.toString());
            return -1;

        }
    }

    private void log (String msg) {
        Log.e("UserDatabaseManager", msg);
    }
}
