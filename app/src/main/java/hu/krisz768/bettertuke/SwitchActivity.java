package hu.krisz768.bettertuke;

import android.os.Build;
import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.SwitchFragment.Switchfragment;

public class SwitchActivity extends AppCompatActivity {

    private String TripID;
    private String StopID;
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
        Switchfragment Sf = Switchfragment.newInstance(TripID, StopID, Date);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.SwitchFragmentContainer, Sf)
                .commit();
    }
}