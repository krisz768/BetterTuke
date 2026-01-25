package hu.krisz768.bettertuke;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.View;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusTimeFragment;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class ScheduleActivity extends AppCompatActivity {
    private String SelectedLine;
    private String Date;
    private String StopId = "-1";
    private ScheduleBusTimeFragment Sbtf;
    private boolean PreSelected = false;
    private Parcelable ScrollState;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_schedule);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.frameLayout2), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle b = getIntent().getExtras();

        SetupBack();

        String LineNum = null;
        String Direction = null;

        UserDatabase userDatabase = new UserDatabase(this);
        String AdEnabled = userDatabase.GetPreference("AdEnabled");
        AdView mAdView = findViewById(R.id.adView2);
        if (AdEnabled != null && AdEnabled.equals("true") && HelperProvider.IsAdConsentOk()){

            AdRequest adRequest = new AdRequest.Builder().build();
            mAdView.loadAd(adRequest);
        }else {
            mAdView.setVisibility(View.GONE);
        }

        if (b != null) {
            StopId = b.getString("StopId");
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

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date date = new Date();
        Sbtf = ScheduleBusTimeFragment.newInstance(LineNum, StopId, "V", formatter.format(date));

        getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in,
                R.anim.fade_out,
                R.anim.fade_in,
                R.anim.slide_out)
                .replace(R.id.ScheduleFragmentContainer, Sbtf)
                .commit();
    }

    public void OnSelectedSchedule(String ScheduleId, String Date, String Direction) {
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

    private void SetupBack() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (SelectedLine == null || PreSelected) {
                    finish();
                } else {
                    SelectedLine = null;

                    ScheduleBusListFragment Sblf = ScheduleBusListFragment.newInstance(StopId, ScrollState);

                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in,
                                    R.anim.slide_out,
                                    R.anim.slide_in,
                                    R.anim.fade_out)
                            .replace(R.id.ScheduleFragmentContainer, Sblf)
                            .commit();

                    Sbtf = null;
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        if (Sbtf != null) {
            Sbtf.UpdateMaxPerLine();
        }
    }
}