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

import com.google.android.gms.ads.MobileAds;

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

        String AdEnabled = userDatabase.GetPreference("AdEnabled");

        String LaunchCounter = userDatabase.GetPreference("LaunchCounter");
        if (LaunchCounter == null) {
            userDatabase.SetPreference("LaunchCounter", "1");
        } else {
            try {
                int Count = Integer.parseInt(LaunchCounter);
                Count++;
                userDatabase.SetPreference("LaunchCounter", Integer.toString(Count));

                if (Count > 4 && AdEnabled == null) {
                    userDatabase.SetPreference("AdEnabled", "true");
                    AdEnabled = "true";
                }
            } catch (Exception e) {
                userDatabase.SetPreference("LaunchCounter", "1");
            }
        }


        if (AdEnabled != null && AdEnabled.equals("true")) {
            MobileAds.initialize(this, initializationStatus -> {
            });
        }

        String FirstStart = userDatabase.GetPreference("FirstStartComplete");
        if (FirstStart != null && FirstStart.equals("true")){
            new Thread(this::CheckForUpdates).start();
        } else {
            ShowSetupScreen();
        }
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

                StartMain();
            } else {
                if (Version.equals(OnlineVersion) && !OnlineVersion.equals("Err") && DatabaseManager.IsDatabaseValid(ctx)) {
                    GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
                    if (gtfsDatabaseManager.CheckForUpdate(this)){
                        AddLog("Updating gtfs...");
                        StartUpdate(false, true);
                    } else {
                        AddLog("Database is up to date!");
                        StartMain();
                    }
                } else {
                    AddLog("Database version does not match! Updating....");

                    GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
                    StartUpdate(true, gtfsDatabaseManager.CheckForUpdate(this));
                }
            }
        } else  {
            AddLog("Database not found, attempt to download...");
            GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(ctx);
            StartUpdate(true, gtfsDatabaseManager.CheckForUpdate(this));
        }
    }

    private void StartMain() {
        Date FinishDate = new Date();
        if (FinishDate.getTime() - StartTime.getTime() < 500 && !(Build.VERSION.SDK_INT < 31)) {
            try {
                Thread.sleep(500 - (FinishDate.getTime() - StartTime.getTime()));
            }catch (Exception ignored) {

            }
        }

        Intent mainIntent = new Intent(this, MainActivity.class);

        Bundle b = getIntent().getExtras();

        if (b != null) {
            mainIntent.putExtra("ShortcutType", b.getInt("ShortcutType"));
            mainIntent.putExtra("ShortcutId", b.getString("ShortcutId"));
        }

        startActivity(mainIntent);
    }

    private void StartUpdate(boolean BaseDB, boolean GTFSDB) {
        Date FinishDate = new Date();
        if (FinishDate.getTime() - StartTime.getTime() < 500 && !(Build.VERSION.SDK_INT < 31)) {
            try {
                Thread.sleep(500 - (FinishDate.getTime() - StartTime.getTime()));
            }catch (Exception ignored) {

            }
        }

        Intent updateIntent = new Intent(this, UpdateAndOnboarding.class);

        int Update = (BaseDB ? 1 : 0 ) + (GTFSDB ? 2 : 0 );

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
        Date FinishDate = new Date();
        if (FinishDate.getTime() - StartTime.getTime() < 500 && !(Build.VERSION.SDK_INT < 31)) {
            try {
                Thread.sleep(500 - (FinishDate.getTime() - StartTime.getTime()));
            }catch (Exception ignored) {

            }
        }

        Intent mainIntent = new Intent(this, UpdateAndOnboarding.class);
        startActivity(mainIntent);
    }

    private void AddLog(String LogText) {
        if (BuildConfig.DEBUG) {
            Log.i("Init", LogText);
        }
    }

}