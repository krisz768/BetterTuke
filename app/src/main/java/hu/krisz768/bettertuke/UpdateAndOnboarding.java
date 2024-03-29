package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;
import hu.krisz768.bettertuke.UpdateAndOnBoardScreen.AdDisableSetting;
import hu.krisz768.bettertuke.UpdateAndOnBoardScreen.DatabaseUpdate;
import hu.krisz768.bettertuke.UpdateAndOnBoardScreen.OnBoardMainFragment;

public class UpdateAndOnboarding extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();

        setContentView(R.layout.activity_update_and_onboarding);

        Bundle b = getIntent().getExtras();

        boolean AdSettingScreen = false;
        boolean Update = false;
        int UpdateType = 3;

        if (b != null) {
            Update = b.getBoolean("Update");
            UpdateType = b.getInt("UpdateType");
            AdSettingScreen = b.getBoolean("AdSettingScreen");
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
        } else if (AdSettingScreen) {
            AdDisableSetting adDisableSetting = new AdDisableSetting();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.updateOnBoardFragmentContainer, adDisableSetting)
                    .commit();
        }else {
            OnBoardMainFragment onBoardMainFragment = OnBoardMainFragment.newInstance();

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.updateOnBoardFragmentContainer, onBoardMainFragment)
                    .commit();
        }
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
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