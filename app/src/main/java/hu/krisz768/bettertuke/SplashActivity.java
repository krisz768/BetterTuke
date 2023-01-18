package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

public class SplashActivity extends AppCompatActivity {

    private TextView Logs;
    private String LogText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true );

        setContentView(R.layout.activity_splash);

        Logs = findViewById(R.id.LogView);

        AddLog("init...");

        new Thread(this::InitTasks).start();
    }

    private void InitTasks() {
        AddLog("Check is database exist...\n(" + (new File(getFilesDir() + "/Database", "track.db")).getAbsolutePath() + ")");

        DatabaseManager Dm = new DatabaseManager(this);
        TukeServerApi serverApi = new TukeServerApi(this);


        if (Dm.IsDatabaseExist()) {
            AddLog("Database exist, checking for update...");
            String Version = Dm.GetDatabaseVersion();

            AddLog("Database version = " + Version);

            String OnlineVersion = serverApi.getServerDatabaseVersion();

            AddLog("Server database version = " + OnlineVersion);

            if (OnlineVersion.equals("Err") && !Version.equals("Err")) {
                AddLog("Server error, using existing database...");

                Context ctx = getApplicationContext();

                runOnUiThread(() -> Toast.makeText(ctx, "Nem sikerült frissíteni az adatbázist.", Toast.LENGTH_LONG).show());

                StartMain();
            } else {
                if (Version.equals(OnlineVersion)) {
                    AddLog("Database is up to date!");

                    //END
                    StartMain();
                } else {
                    AddLog("Database version does not match! Updating....");

                    if (!Dm.DeleteDatabase()){
                        AddLog("Database delete error. Continue anyway...");
                    }

                    if (serverApi.downloadDatabaseFile()) {
                        AddLog("Database downloaded successfully");

                        Dm.ReloadDatabase();
                        Version = Dm.GetDatabaseVersion();

                        AddLog("Database version = " + Version);

                        Context ctx = getApplicationContext();

                        runOnUiThread(() -> Toast.makeText(ctx, "Új menetrendre frissítve! Előfordulhatnak válzotások.", Toast.LENGTH_LONG).show());

                        //END
                        StartMain();
                    }else  {
                        AddLog("Database download fail!");

                        //FAIL
                    }
                }
            }
        } else  {
            AddLog("Database not found, attempt to download...");
            if (serverApi.downloadDatabaseFile()) {
                AddLog("Database downloaded successfully");
                Dm.ReloadDatabase();
                String Version = Dm.GetDatabaseVersion();

                AddLog("Database version = " + Version);

                //END
                StartMain();
            }else  {
                AddLog("Database download fail!");

                //FAIL
            }
        }
    }

    private void StartMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        startActivity(mainIntent);
    }


    private void AddLog(String LogText) {
        this.LogText += "> " + LogText + "\n"+"\n";
        Logs.setText(this.LogText);
    }

}