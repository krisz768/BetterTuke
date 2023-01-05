package hu.krisz768.bettertuke;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Build;
import android.os.Bundle;

import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusTimeFragment;

public class ScheduleActivity extends AppCompatActivity {

    private String SelectedLine;

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

    public void selectLine(String LineNum) {
        this.SelectedLine = LineNum;

        ScheduleBusTimeFragment Sbtf = ScheduleBusTimeFragment.newInstance(LineNum);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ScheduleFragmentContainer, Sbtf)
                .commit();
    }
}