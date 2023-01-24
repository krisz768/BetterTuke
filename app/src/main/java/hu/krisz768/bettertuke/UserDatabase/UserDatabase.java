package hu.krisz768.bettertuke.UserDatabase;

import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.drawable.IconCompat;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.SplashActivity;

public class UserDatabase {
    private static SQLiteDatabase Sld;

    private final Context Ctx;

    public UserDatabase (Context Ctx) {
        this.Ctx = Ctx;
        String DATABASEFILE = (new File(Ctx.getFilesDir() + "/Database", "user.db")).getAbsolutePath();

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

    public void AddFavorite(FavoriteType favoriteType, String Data, String HumanReadable) {

        Intent intent = new Intent(Ctx, SplashActivity.class);
        intent.setAction(Intent.ACTION_MAIN);
        intent.putExtra("ShortcutId", Data);

        if (favoriteType == FavoriteType.Line) {
            intent.putExtra("ShortcutType", 0);

            ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(Ctx, favoriteType + Data)
                    .setShortLabel(HumanReadable)
                    .setLongLabel(Ctx.getString(R.string.BusText, HumanReadable))
                    .setIcon(IconCompat.createWithBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapBus)))
                    .setIntent(intent)
                    .build();

            ShortcutManagerCompat.pushDynamicShortcut(Ctx, shortcut);
        } else if (favoriteType == FavoriteType.Stop) {
            intent.putExtra("ShortcutType", 1);

            ShortcutInfoCompat shortcut = new ShortcutInfoCompat.Builder(Ctx, favoriteType + Data)
                    .setShortLabel(HumanReadable)
                    .setLongLabel(HumanReadable)
                    .setIcon(IconCompat.createWithBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected)))
                    .setIntent(intent)
                    .build();

            ShortcutManagerCompat.pushDynamicShortcut(Ctx, shortcut);
        }

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
            if (cursor.moveToNext()) {
                Id = cursor.getInt(0);
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
