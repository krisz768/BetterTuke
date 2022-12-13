package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import java.io.File;

import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

public class SplashActivity extends AppCompatActivity {

    private TextView Logs;
    private String LogText = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
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

    private void InitTasks() {
        AddLog("Check is database exist...\n(" + (new File(getFilesDir() + "/Database", "track.db")).getAbsolutePath() + ")");

        DatabaseManager Dm = new DatabaseManager(this);
        TukeServerApi serverApi = new TukeServerApi(this);

        if (Dm.IsDatabaseExist()) {
            AddLog("Database exist, checking for update...");
            Dm = new DatabaseManager(this);
            String Verison = Dm.GetDatabaseVerison();

            AddLog("Database version = " + Verison);

            String OnlineVerison = serverApi.getServerDatabaseVersion();

            AddLog("Server database version = " + OnlineVerison);

            if (Verison.equals(OnlineVerison)) {
                AddLog("Database is up to date!");

                //END
                StartMain();
            } else {
                AddLog("Database version does not match! Updating....");

                Dm.DeleteDatabase();

                if (serverApi.downloadDatabaseFile()) {
                    AddLog("Database downloaded successfully");
                    Dm = new DatabaseManager(this);
                    Verison = Dm.GetDatabaseVerison();

                    AddLog("Database version = " + Verison);

                    //END
                    StartMain();
                }else  {
                    AddLog("Database download fail!");

                    //FAIL
                }
            }
        } else  {
            AddLog("Database not found, attempt to download...");
            if (serverApi.downloadDatabaseFile()) {
                AddLog("Database downloaded successfully");
                Dm = new DatabaseManager(this);
                String Verison = Dm.GetDatabaseVerison();

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
        Intent myIntent = new Intent(this, MainActivity.class);
        startActivity(myIntent);
    }


    private void AddLog(String LogText) {
        this.LogText += "> " + LogText + "\n"+"\n";
        Logs.setText(this.LogText);
    }

}