package hu.krisz768.bettertuke;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import hu.krisz768.bettertuke.ActiveBusFragment.BusTypeListFragment;
import hu.krisz768.bettertuke.ActiveBusFragment.BusTypeRouteListFragment;
import hu.krisz768.bettertuke.NewApiInterface.GTFSRProvider;
import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.api_interface.models.ActiveBusTypeRespModel;

public class ActiveBusActivity extends AppCompatActivity {

    private ActiveBusTypeRespModel[] Data;
    private boolean IsBusOpen = false;
    public String OpenBusType = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        setTheme();

        SetupBack();

        setContentView(R.layout.activity_active_bus);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Bundle b = getIntent().getExtras();

        if (b != null) {
            OpenBusType = b.getString("BusType");
            IsBusOpen = true;
        }

        GetData();
    }

    private void GetData() {
        new Thread(() -> {
            GTFSRProvider gtfsrProvider = new GTFSRProvider(this);
            Data = gtfsrProvider.GetActiveBuses();

            if (IsBusOpen) {
                runOnUiThread(() -> ShowRoutes(OpenBusType));
            } else {
                runOnUiThread(this::ListBuses);
            }
        }).start();
    }

    private void ListBuses () {
        if (Data == null) {

        } else {
            BusTypeListFragment Fragment = BusTypeListFragment.newInstance(Data);
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.ActiveBusFragmentContainer, Fragment)
                    .commit();
        }
    }

    private void SetupBack() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (!IsBusOpen) {
                    finish();
                } else {
                    IsBusOpen = false;

                    BusTypeListFragment Fragment = BusTypeListFragment.newInstance(Data);

                    getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.fade_in,
                                    R.anim.slide_out,
                                    R.anim.slide_in,
                                    R.anim.fade_out)
                            .replace(R.id.ActiveBusFragmentContainer, Fragment)
                            .commit();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    public void ShowRoutes(String Type) {
        ActiveBusTypeRespModel Selected = null;
        for (ActiveBusTypeRespModel list : Data) {
            if (list.getBusTypeName().equals(Type)) {
                Selected = list;
                break;
            }
        }

        if (Selected != null) {
            BusTypeRouteListFragment Btrl = BusTypeRouteListFragment.newInstance(Selected);

            if (IsBusOpen) {
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.ActiveBusFragmentContainer, Btrl)
                        .commit();
            } else {
                getSupportFragmentManager().beginTransaction().setCustomAnimations(R.anim.slide_in,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.slide_out)
                        .replace(R.id.ActiveBusFragmentContainer, Btrl)
                        .commit();
            }
        }

        IsBusOpen = true;
        OpenBusType = Type;
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