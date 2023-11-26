package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.Gtfs.GTFSDatabaseManager;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    private Date StartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        StartTime = new Date();

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true );

        AddLog("init...");

        UserDatabase userDatabase = new UserDatabase(this);
        String FirstStart = userDatabase.GetPreference("FirstStartComplete");
        if (FirstStart != null && FirstStart.equals("true")){
            //StartMain(false,false);
            new Thread(this::CheckForUpdates).start();
        } else {
            ShowSetupScreen();
        }

        //new Thread(this::InitTasks).start();

    }

    private void CheckForUpdates() {
        AddLog("Check is database exist...\n(" + (new File(getFilesDir() + "/Database", "track.db")).getAbsolutePath() + ")");

        TukeServerApi serverApi = new TukeServerApi(this);
        Context ctx = getApplicationContext();

        if (DatabaseManager.IsDatabaseExist(this)) {
            AddLog("Database exist, checking for update...");
            String Version = DatabaseManager.GetDatabaseVersion(ctx);

            AddLog("Database version = " + Version);

            String OnlineVersion = serverApi.getServerDatabaseVersion();

            AddLog("Server database version = " + OnlineVersion);

            if (OnlineVersion.equals("Err") && !Version.equals("Err")) {
                AddLog("Server error, using existing database...");

                runOnUiThread(() -> Toast.makeText(ctx, R.string.DatabaseVersionCheckFail, Toast.LENGTH_LONG).show());

                StartMain(false,false);
            } else {
                if (Version.equals(OnlineVersion) && !OnlineVersion.equals("Err") && DatabaseManager.IsDatabaseValid(ctx)) {
                    AddLog("Database is up to date!");

                    GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
                    if (gtfsDatabaseManager.CheckForUpdate(this)){
                        StartUpdate(false, true);
                    } else {
                        StartMain(false,false);
                    }


                } else {
                    AddLog("Database version does not match! Updating....");

                    GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
                    if (gtfsDatabaseManager.CheckForUpdate(this)){
                        StartUpdate(true, true);
                    } else {
                        StartUpdate(true, false);
                    }
                    /*if (!DatabaseManager.DeleteDatabase(ctx)){
                        AddLog("Database delete error. Continue anyway...");
                    }

                    if (serverApi.downloadDatabaseFile()) {
                        AddLog("Database downloaded successfully");

                        Version = DatabaseManager.GetDatabaseVersion(ctx);

                        AddLog("Database version = " + Version);

                        GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
                        runOnUiThread(() -> Toast.makeText(ctx, "Adatbázis frissítése, kérem várjon...", Toast.LENGTH_LONG).show());
                        //gtfsDatabaseManager.ForceUpdate();
                        gtfsDatabaseManager.CheckForUpdate(this); //NEM VÉGLEGES
                        runOnUiThread(() -> Toast.makeText(ctx, R.string.NewDatabaseWarning, Toast.LENGTH_LONG).show());

                        StartMain(false,false);
                    }else  {
                        AddLog("Database download fail!");
                        runOnUiThread(() -> Toast.makeText(ctx, R.string.DatabaseUpdateError, Toast.LENGTH_LONG).show());
                        StartMain(false,false);
                    }*/
                }
            }
        } else  {
            AddLog("Database not found, attempt to download...");
            /*if (serverApi.downloadDatabaseFile()) {
                AddLog("Database downloaded successfully");
                String Version = DatabaseManager.GetDatabaseVersion(ctx);

                AddLog("Database version = " + Version);

                GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
                runOnUiThread(() -> Toast.makeText(ctx, "Adatbázis frissítése, kérem várjon...", Toast.LENGTH_LONG).show());
                //gtfsDatabaseManager.ForceUpdate();

                StartMain(false,true);
            }else  {
                AddLog("Database download fail!");
                StartMain(true, true);
            }*/

            GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
            if (gtfsDatabaseManager.CheckForUpdate(this)){
                StartUpdate(true, true);
            } else {
                StartUpdate(true, false);
            }
        }
    }

    private void StartMain(boolean Error, boolean FirstStart) {
        Date FinishDate = new Date();
        if (FinishDate.getTime() - StartTime.getTime() < 500 && !(Build.VERSION.SDK_INT < 31)) {
            try {
                Thread.sleep(500 - (FinishDate.getTime() - StartTime.getTime()));
            }catch (Exception ignored) {

            }
        }

        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("ERROR", Error);
        mainIntent.putExtra("FirstStart", FirstStart);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            mainIntent.putExtra("ShortcutType", b.getInt("ShortcutType"));
            mainIntent.putExtra("ShortcutId", b.getString("ShortcutId"));
        }

        startActivity(mainIntent);
    }

    private void StartUpdate(boolean BaseDB, boolean GTFSDB) {
        Intent updateIntent = new Intent(this, UpdateAndOnboarding.class);

        int Update = (BaseDB == true ? 1 : 0 ) + (GTFSDB == true ? 2 : 0 );

        updateIntent.putExtra("Update", true);
        updateIntent.putExtra("UpdateType", Update);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            updateIntent.putExtra("ShortcutType", b.getInt("ShortcutType"));
            updateIntent.putExtra("ShortcutId", b.getString("ShortcutId"));
        }

        startActivity(updateIntent);
    }

    private void ShowSetupScreen() {
        Intent mainIntent = new Intent(this, UpdateAndOnboarding.class);
        startActivity(mainIntent);
    }

    private void AddLog(String LogText) {
        if (BuildConfig.DEBUG) {
            Log.i("Init", LogText);
        }
    }

}