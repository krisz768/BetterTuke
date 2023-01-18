package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;

import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {

    private TextView Logs;
    private String LogText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();

        setContentView(R.layout.activity_splash);

        Logs = findViewById(R.id.LogView);

        AddLog("init...");

        new Thread(new Runnable() {
            @Override
            public void run() {
                InitTasks();
            }
        }).start();
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    private void InitTasks() {
        AddLog("Check is database exist...\n(" + (new File(getFilesDir() + "/Database", "track.db")).getAbsolutePath() + ")");

        DatabaseManager Dm = new DatabaseManager(this);
        TukeServerApi serverApi = new TukeServerApi(this);


        if (Dm.IsDatabaseExist()) {
            AddLog("Database exist, checking for update...");
            String Verison = Dm.GetDatabaseVersion();

            AddLog("Database version = " + Verison);

            String OnlineVerison = serverApi.getServerDatabaseVersion();

            AddLog("Server database version = " + OnlineVerison);

            if (OnlineVerison.equals("Err") && !Verison.equals("Err")) {
                AddLog("Server error, using existing database...");

                Context ctx = getApplicationContext();

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(ctx, "Nem sikerült frissíteni az adatbázist.", Toast.LENGTH_LONG).show();
                    }
                });

                StartMain();
            } else {
                if (Verison.equals(OnlineVerison)) {
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
                        Verison = Dm.GetDatabaseVersion();

                        AddLog("Database version = " + Verison);

                        Context ctx = getApplicationContext();

                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Toast.makeText(ctx, "Új menetrendre frissítve! Előfordulhatnak válzotások.", Toast.LENGTH_LONG).show();
                            }
                        });

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
                String Verison = Dm.GetDatabaseVersion();

                AddLog("Database version = " + Verison);

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