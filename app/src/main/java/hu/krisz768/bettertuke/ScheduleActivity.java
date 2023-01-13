package hu.krisz768.bettertuke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusTimeFragment;

public class ScheduleActivity extends AppCompatActivity {

    private String SelectedLine;

    private int StopId = -1;

    private ScheduleBusTimeFragment Sbtf;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();
        setContentView(R.layout.activity_schedule);
        Bundle b = getIntent().getExtras();
        if (b != null) {
            StopId = b.getInt("StopId");
        }

        ScheduleBusListFragment Sblf = ScheduleBusListFragment.newInstance(StopId);

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

        Sbtf = ScheduleBusTimeFragment.newInstance(LineNum, StopId);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.ScheduleFragmentContainer, Sbtf)
                .commit();
    }

    public void OnSelectedSchedule(int ScheduleId, String Date) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("ScheduleId",ScheduleId);
        returnIntent.putExtra("ScheduleDate",Date);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (SelectedLine == null) {
            finish();
        } else {
            SelectedLine = null;

            ScheduleBusListFragment Sblf = ScheduleBusListFragment.newInstance(StopId);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ScheduleFragmentContainer, Sblf)
                    .commit();

            Sbtf = null;
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Sbtf != null) {
            Sbtf.UpdateMaxPerLine();
        }
    }
}