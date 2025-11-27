package hu.krisz768.bettertuke;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcelable;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.DrawableRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.pm.ShortcutInfoCompat;
import androidx.core.content.pm.ShortcutManagerCompat;
import androidx.core.graphics.Insets;
import androidx.core.graphics.drawable.IconCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputLayout;

import java.text.Normalizer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.NearStops.NearBusStopListAdapter;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.ScheduleFragment.ScheduleBusListFragment;
import hu.krisz768.bettertuke.ShortcutCreator.StopShortcodeSubListAdapter;
import hu.krisz768.bettertuke.ShortcutCreator.StopShortcutMainListAdapter;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.models.SearchResult;

public class StopShortcutCreatorActivity extends AppCompatActivity {

    private BusPlaces[] busPlacesList;
    private int FavPlaceCount;
    private BusPlaces[] searchedList;
    private int searchedFavPlaceCount;
    private Parcelable ScrollState = null;

    private BusStops[] selectedBusStops;
    private int selectedBusFavStopCount;

    HashMap<Integer, BusPlaces> busPlaces;
    HashMap<String, BusStops> busStops;

    boolean StopClicked = false;

    private RecyclerView.LayoutManager mLayoutManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();
        HelperProvider.RenderAllBitmap(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stop_shortcut_creator);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        SetupBackButton();

        RecyclerView recyclerView = findViewById(R.id.StopShortcutRec);
        mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        GetStopPlaces();
        ListPlaces();

        ((TextInputLayout)findViewById(R.id.StopShortcutSearch)).getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                OnSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }


    private void GetStopPlaces() {
        busPlaces = BusPlaces.getAllBusPlaces(this);
        busStops = BusStops.GetAllStops(this);

        List<BusPlaces> BusPlacesList = new ArrayList<>(busPlaces.values());

        List<BusPlaces> FavNearBusPlacesList = new ArrayList<>();

        UserDatabase userDatabase = new UserDatabase(this);

        for (int i = 0; i < BusPlacesList.size(); i++) {
            for (BusStops mStop : busStops.values()) {
                if (BusPlacesList.get(i).getId() == mStop.getPlace()) {
                    if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, mStop.getId())) {
                        FavNearBusPlacesList.add(BusPlacesList.get(i));
                        BusPlacesList.remove(i);
                        i--;
                        break;
                    }
                }
            }
        }

        BusPlacesList.addAll(0, FavNearBusPlacesList);

        BusPlaces[] finalbusPlaces = new BusPlaces[BusPlacesList.size()];
        BusPlacesList.toArray(finalbusPlaces);

        busPlacesList = finalbusPlaces;

        FavPlaceCount = FavNearBusPlacesList.size();
    }

    private void ListPlaces() {
        RecyclerView recyclerView = findViewById(R.id.StopShortcutRec);

        if (StopClicked) {
            StopShortcodeSubListAdapter BusStopList = new StopShortcodeSubListAdapter(selectedBusStops, this, selectedBusFavStopCount);
            recyclerView.setAdapter(BusStopList);
        } else {
            BusPlaces[] Data = searchedList == null ? busPlacesList : searchedList;
            int FavCount = searchedList == null ? FavPlaceCount : searchedFavPlaceCount;

            StopShortcutMainListAdapter BusStopList = new StopShortcutMainListAdapter(Data, this, FavCount);
            recyclerView.setAdapter(BusStopList);
        }
    }

    private void OnSearch(String input) {
        if (input.equals("")) {
            searchedList = null;
            searchedFavPlaceCount = 0;

            ListPlaces();
        } else {
            List<BusPlaces> ResultsList = new ArrayList<>();

            for (BusPlaces searchResult : busPlacesList) {
                if (Normalizer.normalize(searchResult.getName(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().contains(Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase())) {
                    ResultsList.add(searchResult);
                }
            }

            List<BusPlaces> FavNearBusPlacesList = new ArrayList<>();
            UserDatabase userDatabase = new UserDatabase(this);

            for (int i = 0; i < ResultsList.size(); i++) {
                for (BusStops mStop : busStops.values()) {
                    if (ResultsList.get(i).getId() == mStop.getPlace()) {
                        if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, mStop.getId())) {
                            FavNearBusPlacesList.add(ResultsList.get(i));
                            ResultsList.remove(i);
                            i--;
                            break;
                        }
                    }
                }
            }

            ResultsList.addAll(0, FavNearBusPlacesList);

            searchedList = new BusPlaces[ResultsList.size()];
            ResultsList.toArray(searchedList);
            searchedFavPlaceCount = FavNearBusPlacesList.size();

            ListPlaces();
        }
    }

    public void OnPlaceClick(int PlaceId) {
        if (getCurrentFocus() != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
        }

        mLayoutManager.onSaveInstanceState();

        ScrollState = mLayoutManager.onSaveInstanceState();

        StopClicked = true;

        findViewById(R.id.StopShortcutSearch).setVisibility(View.GONE);

        List<BusStops> ResultsList = new ArrayList<>();
        for (BusStops mStop : busStops.values()) {
            if (PlaceId == mStop.getPlace()) {
                ResultsList.add(mStop);
            }
        }

        UserDatabase userDatabase = new UserDatabase(this);
        List<BusStops> FavNearBusStopsList = new ArrayList<>();
        for (int i = 0; i < ResultsList.size(); i++) {
            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, ResultsList.get(i).getId())) {
                FavNearBusStopsList.add(ResultsList.get(i));
                ResultsList.remove(i);
                i--;
            }
        }

        ResultsList.addAll(0, FavNearBusStopsList);

        selectedBusStops = new BusStops[ResultsList.size()];
        ResultsList.toArray(selectedBusStops);

        selectedBusFavStopCount = FavNearBusStopsList.size();

        ListPlaces();
    }

    public void OnStopClick(String Id) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)){
            Intent intent = new Intent(this, SplashActivity.class).setAction(Intent.ACTION_MAIN);
            intent.putExtra("ShortcutType", 1);
            intent.putExtra("ShortcutId", Id);

            NewGTFSDatabase Dm = new NewGTFSDatabase(this);
            String StopName = Dm.GetStopName(Id);
            String StopNum = HelperProvider.GetStopDirectionString(this,Id);

            String label = getString(R.string.BusStopNameWithNum, StopName.trim(), StopNum);

            ShortcutInfoCompat sic =
                    new ShortcutInfoCompat.Builder(this, Id).setIntent(intent).setShortLabel(label).setIcon(IconCompat.createWithBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected))).build();
            ShortcutManagerCompat.requestPinShortcut(this, sic, null);

            finish();
        }
    }

    private void setTheme() {
        if (Build.VERSION.SDK_INT < 31) {
            setTheme(R.style.DefaultPre12);
        }
    }

    private void SetupBackButton() {
        OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (StopClicked) {
                    findViewById(R.id.StopShortcutSearch).setVisibility(View.VISIBLE);
                    StopClicked = false;
                    ListPlaces();
                    mLayoutManager.onRestoreInstanceState(ScrollState);
                } else {
                    finish();
                }
            }
        };

        getOnBackPressedDispatcher().addCallback(onBackPressedCallback);
    }
}