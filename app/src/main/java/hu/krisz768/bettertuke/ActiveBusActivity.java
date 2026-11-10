package hu.krisz768.bettertuke;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.krisz768.bettertuke.ActiveBusFragment.BusTypeListFragment;
import hu.krisz768.bettertuke.ActiveBusFragment.BusTypeRouteListFragment;
import hu.krisz768.bettertuke.NewApiInterface.GTFSRProvider;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;

public class ActiveBusActivity extends AppCompatActivity {
    private static final String STATE_OPEN_BUS_TYPE = "OpenBusType";

    private ActiveBusTypeRespModel[] Data;
    private boolean OpenDirectly = false;
    private boolean PendingDataUpdate = false;
    private ListDetailController Panes;
    public String OpenBusType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setTheme();

        setContentView(R.layout.activity_active_bus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle b = getIntent().getExtras();

        if (b != null && b.getString("BusType") != null) {
            OpenBusType = b.getString("BusType");
            OpenDirectly = true;
        }

        if (savedInstanceState != null) {
            OpenBusType = savedInstanceState.getString(STATE_OPEN_BUS_TYPE, OpenBusType);
        }

        // Bus type list | buses of the selected type. Side by side on large screens, on phones the
        // buses are shown above the list and back (with predictive animation) returns to the list.
        Panes = new ListDetailController(this, findViewById(R.id.ActiveBusFragmentContainer), findViewById(R.id.ActiveBusDetailPane), findViewById(R.id.ActiveBusDivider), this::OnDetailClosed);
        Panes.SetOnLayoutModeChangedListener(this::UpdateSelection);

        if (savedInstanceState != null && GetRouteList() != null) {
            Panes.ShowDetail(false);
        }
        UpdateSelection();

        GetData();
    }

    @Override
    protected void onStart() {
        super.onStart();
        UpdateSelection();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(STATE_OPEN_BUS_TYPE, OpenBusType);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // E.g. folding / unfolding: rearrange the panes, the fragments are kept
        Panes.OnConfigurationChanged(newConfig);
        UpdateSelection();
    }

    private void GetData() {
        new Thread(() -> {
            GTFSRProvider gtfsrProvider = new GTFSRProvider(this);
            Data = gtfsrProvider.GetActiveBuses();

            runOnUiThread(this::OnDataLoaded);
        }).start();
    }

    @Override
    protected void onPostResume() {
        super.onPostResume();

        if (PendingDataUpdate) {
            OnDataLoaded();
        }
    }

    private void OnDataLoaded() {
        // The download may finish after the screen was closed
        if (isFinishing() || isDestroyed()) {
            return;
        }

        // Fragment transactions are not allowed after onSaveInstanceState, retry when resumed
        PendingDataUpdate = getSupportFragmentManager().isStateSaved();
        if (PendingDataUpdate) {
            return;
        }

        BusTypeListFragment typeList = GetTypeList();
        BusTypeRouteListFragment routeList = GetRouteList();

        if (typeList == null) {
            // First load
            if (Data == null) {
                // Download failed (no internet), instead of loading forever
                if (!(getSupportFragmentManager().findFragmentById(R.id.ActiveBusFragmentContainer) instanceof InfoFragment)) {
                    getSupportFragmentManager().beginTransaction()
                            .setReorderingAllowed(true)
                            .replace(R.id.ActiveBusFragmentContainer, InfoFragment.newInstance(getString(R.string.InternetError), -1))
                            .commit();
                }
                return;
            }

            // commitNow: the list has to exist when the selection is applied below
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .replace(R.id.ActiveBusFragmentContainer, BusTypeListFragment.newInstance(Data))
                    .commitNow();

            if (OpenDirectly) {
                ShowRoutes(OpenBusType, false);

                BusTypeListFragment list = GetTypeList();
                if (list != null && Panes.IsMultiPane()) {
                    list.ScrollToSelectedType();
                }
            } else {
                UpdateSelection();
            }
            return;
        }

        // Reload (swipe to refresh): update the screens in place
        if (Data == null) {
            typeList.StopRefreshing();
            if (routeList != null) {
                routeList.StopRefreshing();
            }
            return;
        }

        typeList.UpdateData(Data);

        if (routeList != null) {
            ActiveBusTypeRespModel selected = FindBusType(OpenBusType);
            if (selected != null) {
                routeList.UpdateData(selected);
            } else {
                // This bus type has no active buses anymore
                routeList.StopRefreshing();
                Panes.CloseDetail();
                OnDetailClosed();
            }
        }
    }

    private BusTypeListFragment GetTypeList() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.ActiveBusFragmentContainer);
        return fragment instanceof BusTypeListFragment ? (BusTypeListFragment) fragment : null;
    }

    private BusTypeRouteListFragment GetRouteList() {
        Fragment fragment = getSupportFragmentManager().findFragmentById(R.id.ActiveBusDetailContainer);
        return fragment instanceof BusTypeRouteListFragment ? (BusTypeRouteListFragment) fragment : null;
    }

    private ActiveBusTypeRespModel FindBusType(String Type) {
        if (Data == null || Type == null) {
            return null;
        }

        for (ActiveBusTypeRespModel busType : Data) {
            if (busType.getBusTypeName().equals(Type)) {
                return busType;
            }
        }
        return null;
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    public void ShowRoutes(String Type) {
        ShowRoutes(Type, true);
    }

    private void ShowRoutes(String Type, boolean Animate) {
        ActiveBusTypeRespModel selected = FindBusType(Type);
        if (selected == null || getSupportFragmentManager().isStateSaved()) {
            return;
        }

        if (Type.equals(OpenBusType) && GetRouteList() != null) {
            Panes.ShowDetail(Animate);
            return;
        }

        getSupportFragmentManager().beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.ActiveBusDetailContainer, BusTypeRouteListFragment.newInstance(selected))
                .commit();

        OpenBusType = Type;
        Panes.ShowDetail(Animate);
        UpdateSelection();
    }

    // The buses of a type were closed (back on a phone, or the type disappeared)
    private void OnDetailClosed() {
        OpenBusType = "";

        BusTypeRouteListFragment routeList = GetRouteList();
        if (routeList != null && !getSupportFragmentManager().isStateSaved()) {
            getSupportFragmentManager().beginTransaction()
                    .setReorderingAllowed(true)
                    .remove(routeList)
                    .commit();
        }

        UpdateSelection();
    }

    private void UpdateSelection() {
        findViewById(R.id.ActiveBusDetailPlaceholder).setVisibility(GetRouteList() == null && OpenBusType.isEmpty() ? View.VISIBLE : View.GONE);

        BusTypeListFragment typeList = GetTypeList();
        if (typeList != null) {
            typeList.SetSelectedType(Panes.IsMultiPane() && !OpenBusType.isEmpty() ? OpenBusType : null);
        }
    }

    public void OnBusClick(String TripID) {

        SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd", Locale.US);
        Date date = new Date();

        Intent returnIntent = new Intent();
        returnIntent.putExtra("ScheduleId",TripID);
        returnIntent.putExtra("ScheduleDate",formatter.format(date));
        returnIntent.putExtra("BusType",OpenBusType);
        setResult(Activity.RESULT_OK,returnIntent);
        finish();
    }

    public void OnReloadInit() {
        GetData();
    }

    public void OnReloadInitOnType() {
        GetData();
    }
}
