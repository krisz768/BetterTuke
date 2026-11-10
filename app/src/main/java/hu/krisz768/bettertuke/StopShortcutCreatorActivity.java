package hu.krisz768.bettertuke;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.content.res.Configuration;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.ShortcutCreator.StopShortcodeSubListAdapter;
import hu.krisz768.bettertuke.ShortcutCreator.StopShortcutMainListAdapter;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class StopShortcutCreatorActivity extends AppCompatActivity {

    private BusPlaces[] busPlacesList;
    private int FavPlaceCount;
    private BusPlaces[] searchedList;
    private int searchedFavPlaceCount;

    private BusStops[] selectedBusStops;
    private int selectedBusFavStopCount;

    HashMap<Integer, BusPlaces> busPlaces;
    HashMap<String, BusStops> busStops;

    // Place whose stops are shown in the detail pane, -1 if none
    private int SelectedPlaceId = -1;

    // Place list | stops of the selected place. Side by side on large screens, on phones the stops
    // are shown above the list and back (with predictive animation) returns to the list.
    private ListDetailController Panes;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme();
        HelperProvider.RenderAllBitmap(this);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_stop_shortcut_list_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Panes = new ListDetailController(this, findViewById(R.id.StopShortcutListPane), findViewById(R.id.StopShortcutDetailPane), findViewById(R.id.StopShortcutDivider), this::OnDetailClosed);
        Panes.SetOnLayoutModeChangedListener(this::UpdateSelection);

        RecyclerView recyclerView = findViewById(R.id.StopShortcutRec);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        RecyclerView stopRecyclerView = findViewById(R.id.StopShortcutStopRec);
        stopRecyclerView.setLayoutManager(new LinearLayoutManager(this));

        GetStopPlaces();
        ListPlaces();

        EditText editText = ((TextInputLayout)findViewById(R.id.StopShortcutSearch)).getEditText();

        if (editText == null)
            return;

        editText.addTextChangedListener(new TextWatcher() {
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

        BusPlaces[] Data = searchedList == null ? busPlacesList : searchedList;
        int FavCount = searchedList == null ? FavPlaceCount : searchedFavPlaceCount;

        StopShortcutMainListAdapter BusStopList = new StopShortcutMainListAdapter(Data, this, FavCount);
        recyclerView.setAdapter(BusStopList);
    }

    // Place highlighted in the list: only in two-pane mode, where the list and its stops are both visible
    public int GetHighlightedPlaceId() {
        return Panes.IsMultiPane() ? SelectedPlaceId : -1;
    }

    private void UpdateSelection() {
        findViewById(R.id.StopShortcutDetailPlaceholder).setVisibility(SelectedPlaceId == -1 ? View.VISIBLE : View.GONE);

        RecyclerView recyclerView = findViewById(R.id.StopShortcutRec);
        if (recyclerView.getAdapter() != null) {
            recyclerView.getAdapter().notifyItemRangeChanged(0, recyclerView.getAdapter().getItemCount());
        }
    }

    // The stops were closed with back on a phone
    private void OnDetailClosed() {
        SelectedPlaceId = -1;
        ((RecyclerView) findViewById(R.id.StopShortcutStopRec)).setAdapter(null);
        UpdateSelection();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);

        // E.g. folding / unfolding: rearrange the panes, the lists are kept
        Panes.OnConfigurationChanged(newConfig);
        UpdateSelection();
    }

    private void OnSearch(String input) {
        if (input.isEmpty()) {
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

        SelectedPlaceId = PlaceId;

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

        RecyclerView stopRecyclerView = findViewById(R.id.StopShortcutStopRec);
        stopRecyclerView.setAdapter(new StopShortcodeSubListAdapter(selectedBusStops, this, selectedBusFavStopCount));
        stopRecyclerView.scrollToPosition(0);

        Panes.ShowDetail(true);
        UpdateSelection();
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
}
