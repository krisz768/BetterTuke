package hu.krisz768.bettertuke;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
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
    private static final String STATE_SELECTED_LINE = "SelectedLine";

    private String SelectedLine;
    private String Date;
    private String StopId = "-1";
    private boolean PreSelected = false;
    private ListDetailController Panes;
    // Opened on a line: the line list should scroll to it
    private boolean ScrollListToSelection = false;

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

        // Line list | schedule of the selected line. Side by side on large screens, on phones the
        // schedule is shown above the list and back (with predictive animation) returns to the list.
        Panes = new ListDetailController(this, findViewById(R.id.ScheduleFragmentContainer), findViewById(R.id.ScheduleDetailPane), findViewById(R.id.ScheduleDivider), this::OnDetailClosed);
        // Opened on a preselected line (e.g. from a shortcut): on phones back leaves the screen
        Panes.SetDetailIsRoot(PreSelected);
        Panes.SetOnLayoutModeChangedListener(() -> {
            UpdateSelection();
            UpdateAdPlacement();
        });

        // The height of the ad is known (or changes) only after it is laid out / loaded
        findViewById(R.id.adView2).addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom - top != oldBottom - oldTop) {
                v.post(this::UpdateAdPlacement);
            }
        });
        UpdateAdPlacement();

        if (savedInstanceState != null) {
            // The fragments are restored by the FragmentManager
            SelectedLine = savedInstanceState.getString(STATE_SELECTED_LINE);
            if (GetTimeFragment() != null) {
                Panes.ShowDetail(false);
            }
            UpdateSelection();
            return;
        }

        // The line list is always created, on large screens it is shown next to the schedule
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.ScheduleFragmentContainer, ScheduleBusListFragment.newInstance(StopId))
                .commit();

        if (LineNum != null) {
            ScrollListToSelection = true;
            ShowLine(LineNum, Direction, Date, false);
        } else {
            UpdateSelection();
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        // The fragments committed in onCreate exist by now
        UpdateSelection();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_SELECTED_LINE, SelectedLine);
    }

    private ScheduleBusTimeFragment GetTimeFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.ScheduleDetailContainer);
        return fragment instanceof ScheduleBusTimeFragment ? (ScheduleBusTimeFragment) fragment : null;
    }

    private ScheduleBusListFragment GetListFragment() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.ScheduleFragmentContainer);
        return fragment instanceof ScheduleBusListFragment ? (ScheduleBusListFragment) fragment : null;
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    public void selectLine(String LineNum) {
        if (getSupportFragmentManager().isStateSaved()) {
            return;
        }

        // The user picked a line from the list, so the list is not skipped by back anymore
        Panes.SetDetailIsRoot(false);

        if (LineNum.equals(SelectedLine) && GetTimeFragment() != null) {
            Panes.ShowDetail(true);
            return;
        }

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date date = new Date();
        ShowLine(LineNum, "V", formatter.format(date), true);
    }

    private void ShowLine(String LineNum, String Direction, String Date, boolean Animate) {
        SelectedLine = LineNum;

        ScheduleBusTimeFragment timeFragment = ScheduleBusTimeFragment.newInstance(LineNum, StopId, Direction, Date);
        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.ScheduleDetailContainer, timeFragment)
                .commit();

        Panes.ShowDetail(Animate);
        UpdateSelection();
    }

    // The schedule was closed with back on a phone
    private void OnDetailClosed() {
        SelectedLine = null;

        ScheduleBusTimeFragment timeFragment = GetTimeFragment();
        if (timeFragment != null && !getSupportFragmentManager().isStateSaved()) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .remove(timeFragment)
                    .commit();
        }

        UpdateSelection();
    }

    // Large screen (side by side): the ad is below the line list, the schedule reaches the bottom of
    // the screen. Otherwise (phone, tabletop) the ad is below the panes, across the whole width.
    private void UpdateAdPlacement() {
        View panes = findViewById(R.id.SchedulePanes);
        View ad = findViewById(R.id.adView2);
        View listPane = findViewById(R.id.ScheduleFragmentContainer);

        ConstraintLayout.LayoutParams panesParams = (ConstraintLayout.LayoutParams) panes.getLayoutParams();
        ConstraintLayout.LayoutParams adParams = (ConstraintLayout.LayoutParams) ad.getLayoutParams();

        if (Panes.IsTwoPane() && !Panes.IsTabletop()) {
            panesParams.bottomToTop = ConstraintLayout.LayoutParams.UNSET;
            panesParams.bottomToBottom = ConstraintLayout.LayoutParams.PARENT_ID;

            adParams.width = ListDetailController.GetListPaneWidth(panes, getResources().getConfiguration().screenWidthDp);
            adParams.endToEnd = ConstraintLayout.LayoutParams.UNSET;

            // The list ends above the ad
            listPane.setPadding(0, 0, 0, ad.getVisibility() == View.GONE ? 0 : ad.getHeight());
        } else {
            panesParams.bottomToBottom = ConstraintLayout.LayoutParams.UNSET;
            panesParams.bottomToTop = R.id.adView2;

            adParams.width = ConstraintLayout.LayoutParams.MATCH_CONSTRAINT;
            adParams.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;

            listPane.setPadding(0, 0, 0, 0);
        }

        panes.setLayoutParams(panesParams);
        ad.setLayoutParams(adParams);
    }

    private void UpdateSelection() {
        findViewById(R.id.ScheduleDetailPlaceholder).setVisibility(SelectedLine == null ? View.VISIBLE : View.GONE);

        ScheduleBusListFragment listFragment = GetListFragment();
        if (listFragment != null) {
            listFragment.SetSelectedLine(Panes.IsMultiPane() ? SelectedLine : null);

            if (ScrollListToSelection && Panes.IsMultiPane()) {
                ScrollListToSelection = false;
                listFragment.ScrollToSelectedLine();
            }
        }
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

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // E.g. folding / unfolding: rearrange the panes, the fragments are kept
        Panes.OnConfigurationChanged(newConfig);
        UpdateAdPlacement();
        UpdateSelection();
    }
}