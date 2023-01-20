package hu.krisz768.bettertuke;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.splashscreen.SplashScreen;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import java.io.File;
import java.util.Date;

import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

@SuppressLint("CustomSplashScreen")
public class SplashActivity extends AppCompatActivity {
    Date StartTime;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        StartTime = new Date();

        SplashScreen splashScreen = SplashScreen.installSplashScreen(this);

        super.onCreate(savedInstanceState);

        splashScreen.setKeepOnScreenCondition(() -> true );

        //setContentView(R.layout.activity_splash);

        AddLog("init...");

        new Thread(this::InitTasks).start();
    }

    private void InitTasks() {
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

                runOnUiThread(() -> Toast.makeText(ctx, "Nem sikerült frissíteni az adatbázist.", Toast.LENGTH_LONG).show());

                StartMain(false);
            } else {
                if (Version.equals(OnlineVersion) && !OnlineVersion.equals("Err") && !Version.equals("Err")) {
                    AddLog("Database is up to date!");

                    //END
                    StartMain(false);
                } else {
                    AddLog("Database version does not match! Updating....");

                    if (!DatabaseManager.DeleteDatabase(ctx)){
                        AddLog("Database delete error. Continue anyway...");
                    }

                    if (serverApi.downloadDatabaseFile()) {
                        AddLog("Database downloaded successfully");

                        Version = DatabaseManager.GetDatabaseVersion(ctx);

                        AddLog("Database version = " + Version);

                        runOnUiThread(() -> Toast.makeText(ctx, "Új menetrendre frissítve! Előfordulhatnak válzotások.", Toast.LENGTH_LONG).show());

                        //END
                        StartMain(false);
                    }else  {
                        AddLog("Database download fail!");
                        runOnUiThread(() -> Toast.makeText(ctx, "SIKERTELEN frissítés az új menetrendre", Toast.LENGTH_LONG).show());
                        StartMain(false);
                        //FAIL
                    }
                }
            }
        } else  {
            AddLog("Database not found, attempt to download...");
            if (serverApi.downloadDatabaseFile()) {
                AddLog("Database downloaded successfully");
                String Version = DatabaseManager.GetDatabaseVersion(ctx);

                AddLog("Database version = " + Version);

                //END
                StartMain(false);
            }else  {
                AddLog("Database download fail!");

                /*setContentView(R.layout.activity_splash);
                AlertDialog.Builder dlgAlert  = new AlertDialog.Builder(this);
                dlgAlert.setMessage("Az alkalmazás eső indításához internetkapcsolat szükséges.");
                dlgAlert.setTitle("Hiba");
                dlgAlert.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        finish();
                    }
                });
                dlgAlert.setCancelable(false);
                runOnUiThread(() -> dlgAlert.create().show());*/
                StartMain(true);
                //FAIL
            }
        }
    }

    private void StartMain(boolean Error) {
        Date FinishDate = new Date();
        if (FinishDate.getTime() - StartTime.getTime() < 500 && !(Build.VERSION.SDK_INT < 31)) {
            try {
                Thread.sleep(500 - (FinishDate.getTime() - StartTime.getTime()));
            }catch (Exception ignored) {

            }
        }



        Intent mainIntent = new Intent(this, MainActivity.class);
        mainIntent.putExtra("ERROR", Error);
        startActivity(mainIntent);
    }


    private void AddLog(String LogText) {

        Log.i("Init", LogText);
    }

}