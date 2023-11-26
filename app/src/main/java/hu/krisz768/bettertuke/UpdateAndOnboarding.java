package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import java.util.Date;

import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusTimeFragment;
import hu.krisz768.bettertuke.UpdateAndOnBoardScreen.DatabaseUpdate;
import hu.krisz768.bettertuke.UpdateAndOnBoardScreen.OnBoardMainFragment;

public class UpdateAndOnboarding extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_and_onboarding);

        Bundle b = getIntent().getExtras();

        boolean Update = false;
        int UpdateType = 3;

        if (b != null) {
            Update = b.getBoolean("Update");
            UpdateType = b.getInt("UpdateType");
        }

        if (Update) {
            DatabaseUpdate databaseUpdate = DatabaseUpdate.newInstance(UpdateType == 1 || UpdateType == 3, UpdateType == 2 || UpdateType == 3, true, new DatabaseUpdate.OnUpdateFinish() {
                @Override
                public void onFinish() {
                    Toast.makeText(getApplicationContext(), R.string.NewDatabaseWarning, Toast.LENGTH_LONG).show();
                    StartMain();
                }
            }, new DatabaseUpdate.OnUpdateFail() {
                @Override
                public void onFail() {
                    Toast.makeText(getApplicationContext(), R.string.DatabaseUpdateError, Toast.LENGTH_LONG).show();
                }
            });

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.updateOnBoardFragmentContainer, databaseUpdate)
                    .commit();

            databaseUpdate.StartUpdate();
        } else {
            OnBoardMainFragment onBoardMainFragment = OnBoardMainFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.updateOnBoardFragmentContainer, onBoardMainFragment)
                    .commit();
        }
    }

    private void StartMain() {
        Intent mainIntent = new Intent(this, MainActivity.class);
        Bundle b = getIntent().getExtras();

        if (b != null) {
            mainIntent.putExtra("ShortcutType", b.getInt("ShortcutType"));
            mainIntent.putExtra("ShortcutId", b.getString("ShortcutId"));
        }

        startActivity(mainIntent);
    }
}