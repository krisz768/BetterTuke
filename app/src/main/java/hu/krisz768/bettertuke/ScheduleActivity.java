package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentContainer;

import android.app.Fragment;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

public class ScheduleActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();
        setContentView(R.layout.activity_schedule);

        ScheduleBusListFragment Sblf = ScheduleBusListFragment.newInstance();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ScheduleFragmentContainer, Sblf)
                .commit();
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }
}