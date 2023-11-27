package hu.krisz768.bettertuke;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusTimeFragment;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class ScheduleActivity extends AppCompatActivity {
    private String SelectedLine;
    private String Date;
    private int StopId = -1;

    private ScheduleBusTimeFragment Sbtf;

    private boolean PreSelected = false;

    private Parcelable ScrollState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();
        setContentView(R.layout.activity_schedule);
        Bundle b = getIntent().getExtras();

        String LineNum = null;
        String Direction = null;

        UserDatabase userDatabase = new UserDatabase(this);
        String AdEnabled = userDatabase.GetPreference("AdEnabled");
        AdView mAdView = findViewById(R.id.adView2);
        if (AdEnabled != null && AdEnabled.equals("true")){

            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }else {
            mAdView.setVisibility(View.GONE);
        }

        if (b != null) {
            StopId = b.getInt("StopId");
            LineNum = b.getString("LineNum");
            Direction = b.getString("Direction");
            Date = b.getString("Date");
            PreSelected = b.getBoolean("PreSelected");
        }

        if (LineNum != null) {
            this.SelectedLine = LineNum;

            Sbtf = ScheduleBusTimeFragment.newInstance(LineNum, StopId, Direction, Date);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ScheduleFragmentContainer, Sbtf)
                    .commit();
        } else {
            ScheduleBusListFragment Sblf = ScheduleBusListFragment.newInstance(StopId, ScrollState);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ScheduleFragmentContainer, Sblf)
                    .commit();
        }
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    public void selectLine(String LineNum, Parcelable ScrollState) {
        this.SelectedLine = LineNum;
        this.ScrollState = ScrollState;

        SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
        Date date = new Date();
        Sbtf = ScheduleBusTimeFragment.newInstance(LineNum, StopId, "O", formatter.format(date));

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out)
                .replace(R.id.ScheduleFragmentContainer, Sbtf)
                .commit();
    }

    public void OnSelectedSchedule(int ScheduleId, String Date, String Direction) {
        Intent returnIntent = new Intent();
        returnIntent.putExtra("ScheduleId",ScheduleId);
        returnIntent.putExtra("ScheduleDate",Date);

        returnIntent.putExtra("StopId",StopId);
        returnIntent.putExtra("LineNum",SelectedLine);
        returnIntent.putExtra("Direction",Direction);
        returnIntent.putExtra("PreSelected",PreSelected);

        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    @Override
    public void onBackPressed() {
        if (SelectedLine == null || PreSelected) {
            finish();
        } else {
            SelectedLine = null;

            ScheduleBusListFragment Sblf = ScheduleBusListFragment.newInstance(StopId,ScrollState);

            getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in,
                    R.anim.slide_out,
                    R.anim.slide_in,
                    R.anim.fade_out)
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