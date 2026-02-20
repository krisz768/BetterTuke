package hu.krisz768.bettertuke;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hu.krisz768.bettertuke.SwitchFragment.Switchfragment;

public class SwitchActivity extends AppCompatActivity {

    private String TripID;
    private String StopID;
    private String CurrentStopID;
    private String Date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();

        Bundle b = getIntent().getExtras();

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_switch);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        if (b != null) {
            TripID = b.getString("TripId");
            StopID = b.getString("StopId");
            CurrentStopID = b.getString("CurrentStopId");
            Date = b.getString("Date");
        }

        InitFragment();
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    private void InitFragment() {
        Switchfragment Sf = Switchfragment.newInstance(TripID, StopID, CurrentStopID,Date);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.SwitchFragmentContainer, Sf)
                .commit();
    }

    public void TrackBus(String Id, String Date, String StopId, String CurrentStopId) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("TrackId",Id);
        returnIntent.putExtra("Date",Date);
        returnIntent.putExtra("StopId",StopId);
        returnIntent.putExtra("CurrentStopId",CurrentStopId);
        returnIntent.putExtra("TripId",TripID);

        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }
}