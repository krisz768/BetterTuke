package hu.krisz768.bettertuke;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
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
import java.util.Arrays;
import java.util.List;

import hu.krisz768.bettertuke.Database.BusNum;
import hu.krisz768.bettertuke.NewGTFS.NewGTFSDatabase;
import hu.krisz768.bettertuke.ShortcutCreator.BusShortcutListAdapter;
import hu.krisz768.bettertuke.UserDatabase.Favorite;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;

public class BusShortcutCreatorActivity extends AppCompatActivity {

    private BusNum[] busNums;
    private int FavCount;

    private BusNum[] searchedList;
    private int searchedFavCount;

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

        RecyclerView recyclerView = findViewById(R.id.StopShortcutRec);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(mLayoutManager);

        GetBuses();
        ListBuses();

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

    private void OnSearch(String input) {
        if (input.isEmpty()) {
            searchedList = null;
            searchedFavCount = 0;

            ListBuses();
        } else {
            List<BusNum> ResultsList = new ArrayList<>();

            for (BusNum searchResult : busNums) {
                if (Normalizer.normalize(searchResult.getLineName(), Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase().contains(Normalizer.normalize(input, Normalizer.Form.NFD).replaceAll("\\p{InCombiningDiacriticalMarks}+", "").toLowerCase())) {
                    ResultsList.add(searchResult);
                }
            }

            UserDatabase userDatabase = new UserDatabase(this);
            Favorite[] favorites = userDatabase.GetFavorites(UserDatabase.FavoriteType.Line);

            List<BusNum> favoriteBusNumList = new ArrayList<>();

            for (Favorite favorite : favorites) {
                for (BusNum busNum : ResultsList) {
                    if (favorite.getData().equals(busNum.getLineName())) {
                        favoriteBusNumList.add(busNum);
                        break;
                    }
                }
            }

            for (BusNum busNum : favoriteBusNumList) {
                ResultsList.remove(busNum);
            }

            ResultsList.addAll(0,favoriteBusNumList);

            searchedList = new BusNum[ResultsList.size()];
            ResultsList.toArray(searchedList);

            searchedFavCount = favoriteBusNumList.size();

            ListBuses();
        }
    }

    private void GetBuses() {
        NewGTFSDatabase NDm = new NewGTFSDatabase(this);
        List<BusNum> Buses = new ArrayList<>(Arrays.asList(NDm.GetActiveBusLines()));

        UserDatabase userDatabase = new UserDatabase(this);
        Favorite[] favorites = userDatabase.GetFavorites(UserDatabase.FavoriteType.Line);

        List<BusNum> favoriteBusNumList = new ArrayList<>();

        for (Favorite favorite : favorites) {
            for (BusNum busNum : Buses) {
                if (favorite.getData().equals(busNum.getLineName())) {
                    favoriteBusNumList.add(busNum);
                    break;
                }
            }
        }

        for (BusNum busNum : favoriteBusNumList) {
            Buses.remove(busNum);
        }

        Buses.addAll(0,favoriteBusNumList);

        busNums = new BusNum[Buses.size()];
        Buses.toArray(busNums);

        FavCount = favoriteBusNumList.size();
    }

    private void ListBuses() {
        RecyclerView recyclerView = findViewById(R.id.StopShortcutRec);

        BusNum[] Data = searchedList == null ? busNums : searchedList;
        int FavCount = searchedList == null ? this.FavCount : searchedFavCount;

        BusShortcutListAdapter BusStopList = new BusShortcutListAdapter(Data, this, FavCount);
        recyclerView.setAdapter(BusStopList);
    }

    public void OnBusClick(String Id) {
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(this)){
            Intent intent = new Intent(this, SplashActivity.class).setAction(Intent.ACTION_MAIN);
            intent.putExtra("ShortcutType", 0);
            intent.putExtra("ShortcutId", Id);

            ShortcutInfoCompat sic =
                    new ShortcutInfoCompat.Builder(this, Id).setIntent(intent).setShortLabel(Id).setIcon(IconCompat.createWithBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapBus))).build();
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