package hu.krisz768.bettertuke.IncomingBusFragment;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import hu.krisz768.bettertuke.BuildConfig;
import hu.krisz768.bettertuke.Database.BusLine;
import hu.krisz768.bettertuke.Database.BusPlaces;
import hu.krisz768.bettertuke.Database.BusStops;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.HelperProvider;
import hu.krisz768.bettertuke.InfoFragment;
import hu.krisz768.bettertuke.LoadingFragment;
import hu.krisz768.bettertuke.MainActivity;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.UserDatabase.UserDatabase;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;
import hu.krisz768.bettertuke.api_interface.models.IncomingBusRespModel;

public class BottomSheetIncomingBusFragment extends Fragment {
    private static final String PLACE = "Place";
    private static final String STOP = "Stop";
    private static final String PLACELIST = "PlaceList";
    private static final String STOPLIST = "StopList";

    private int mPlace;
    private volatile int mStop;
    private HashMap<Integer, BusPlaces> mPlaceList;
    private HashMap<Integer, BusStops> mStopList;

    private IncomingBusStopSelectorAdapter Ibssa;
    private IncomingBusListFragment InBusFragment;

    private ScheduledExecutorService UpdateLoop;

    public BottomSheetIncomingBusFragment() {

    }
    public static BottomSheetIncomingBusFragment newInstance(int Place, int Stop, HashMap<Integer, BusPlaces> PlaceList, HashMap<Integer, BusStops> StopList) {
        BottomSheetIncomingBusFragment fragment = new BottomSheetIncomingBusFragment();
        Bundle args = new Bundle();
        args.putInt(PLACE, Place);
        args.putInt(STOP, Stop);
        args.putSerializable(PLACELIST, PlaceList);
        args.putSerializable(STOPLIST, StopList);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mPlace = getArguments().getInt(PLACE);
            mStop = getArguments().getInt(STOP);
            mPlaceList = (HashMap<Integer, BusPlaces>) getArguments().getSerializable(PLACELIST);
            mStopList = (HashMap<Integer, BusStops>) getArguments().getSerializable(STOPLIST);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_bottom_sheet_incoming_bus_view, container, false);

        TextView BusStopName = view.findViewById(R.id.BusStopName);

        ImageView ScheduleButton = view.findViewById(R.id.StopScheduleButton);
        ScheduleButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.MapStopSelected));
        if (getActivity() != null){
            ScheduleButton.setOnClickListener(view1 -> ((MainActivity)getActivity()).ShowSchedule(mStop, null, null, null, false));
        }

        List<BusStops> SelectedPlaceStops = new ArrayList<>();

        BusPlaces busPlace = mPlaceList.get(mPlace);

        if (busPlace != null) {
            BusStopName.setText(busPlace.getName());

            for (BusStops busStops : mStopList.values()) {
                if (busStops.getPlace() == busPlace.getId()) {
                    SelectedPlaceStops.add(busStops);
                }
            }
        }

        BusStops[] SelectedPlaceStopsArray = new BusStops[SelectedPlaceStops.size()];
        SelectedPlaceStops.toArray(SelectedPlaceStopsArray);

        ImageView FavButton = view.findViewById(R.id.StopFavoriteButton);
        if (getContext() != null){
            UserDatabase userDatabase = new UserDatabase(getContext());

            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop))) {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
            } else {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
            }

            FavButton.setOnClickListener(view12 -> {
                if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop))) {
                    userDatabase.DeleteFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop));
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
                } else {
                    String StopNum = "1";
                    for (BusStops busStops : SelectedPlaceStopsArray) {
                        if (busStops.getId() == mStop) {
                            StopNum = busStops.getStopNum();
                            break;
                        }
                    }

                    userDatabase.AddFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop),  getString(R.string.BusStopNameWithNum, BusStopName.getText().toString().trim(), StopNum.trim()));
                    FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
                }
            });
        }

        Ibssa = new IncomingBusStopSelectorAdapter(SelectedPlaceStopsArray,mStop, this,getContext());

        RecyclerView StopSelectorRec = view.findViewById(R.id.BusStopListRecView);
        LinearLayoutManager mLayoutManager = new LinearLayoutManager(getActivity());
        mLayoutManager.setOrientation(LinearLayoutManager.HORIZONTAL);

        StopSelectorRec.setLayoutManager(mLayoutManager);
        StopSelectorRec.setAdapter(Ibssa);

        RecyclerView.SmoothScroller smoothScroller = new LinearSmoothScroller(getContext()) {
            @Override
            protected int getVerticalSnapPreference() {
                return LinearSmoothScroller.SNAP_TO_START;
            }

            @Override
            protected float calculateSpeedPerPixel(DisplayMetrics displayMetrics) {
                return super.calculateSpeedPerPixel(displayMetrics) *4;
            }
        };

        int scrollPosition = 0;
        for (int i = 0; i < SelectedPlaceStopsArray.length; i++) {
            if (SelectedPlaceStopsArray[i].getId() == mStop) {
                scrollPosition = i;
                break;
            }
        }

        smoothScroller.setTargetPosition(scrollPosition);
        if (StopSelectorRec.getLayoutManager() != null) {
            StopSelectorRec.getLayoutManager().startSmoothScroll(smoothScroller);
        }

        StopSelectorRec.setNestedScrollingEnabled(false);

        StartNewUpdateThread();

        return view;
    }

    private void StartNewUpdateThread() {
        if (UpdateLoop != null) {
            if (!UpdateLoop.isShutdown()){
                return;
            }
        }

        TukeServerApi serverApi = new TukeServerApi(this.getActivity());

        UpdateLoop = Executors.newScheduledThreadPool(1);
        UpdateLoop.scheduleAtFixedRate(() -> GetIncomingBuses(serverApi), 0, 10, TimeUnit.SECONDS);
    }

    @Override
    public void onPause() {
        super.onPause();

        UpdateLoop.shutdown();
    }

    @Override
    public void onResume() {
        super.onResume();

        ResetList();
        StartNewUpdateThread();
    }

    @Override
    public void onStop() {
        super.onStop();

        UpdateLoop.shutdown();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void OnStopClick(int Id) {
        if (getActivity() != null){
            ((MainActivity)getActivity()).ChangeStop(Id);
        }

        mStop = Id;
        Ibssa.setSelectedStop(mStop);
        Ibssa.notifyDataSetChanged();
        ResetList();

        if (getView() != null && getContext() != null){
            ImageView FavButton = getView().findViewById(R.id.StopFavoriteButton);

            UserDatabase userDatabase = new UserDatabase(getContext());
            if (userDatabase.IsFavorite(UserDatabase.FavoriteType.Stop, Integer.toString(mStop))) {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOn));
            } else {
                FavButton.setImageBitmap(HelperProvider.getBitmap(HelperProvider.Bitmaps.FaviconOff));
            }
        }

        new Thread(() -> {
            TukeServerApi serverApi = new TukeServerApi(getActivity());
            GetIncomingBuses(serverApi);
        }).start();
    }

    private void ResetList() {
        getChildFragmentManager().beginTransaction()
                .replace(R.id.BusListFragment, new LoadingFragment())
                .commit();
        InBusFragment = null;
    }

    private void GetIncomingBuses(TukeServerApi serverApi) {
        try {
            if (BuildConfig.DEBUG) {
                Log.i("Update", "Updating List");
            }

            final int SendStopId = mStop;

            IncomingBusRespModel[] BusList = serverApi.getNextIncomingBuses(mStop);

            MainActivity mainActivity = (MainActivity)getActivity();

            if (BusList == null) {
                if(mainActivity != null && HelperProvider.displayOfflineText()) {
                    mainActivity.runOnUiThread(() -> Toast.makeText(mainActivity,R.string.OfflineDataWarning, Toast.LENGTH_LONG).show());
                    HelperProvider.setOfflineTextDisplayed();
                }
                DatabaseManager Dm = new DatabaseManager(mainActivity);

                BusList = Dm.GetOfflineDepartureTimes(SendStopId);
            }

            Date currentTime = Calendar.getInstance().getTime();
            SimpleDateFormat Sdf = new SimpleDateFormat("H", Locale.US);
            SimpleDateFormat Sdf2 = new SimpleDateFormat("m", Locale.US);

            for (IncomingBusRespModel incomingBusRespModel : BusList) {
                BusLine Bj = BusLine.BusLinesByLineId(incomingBusRespModel.getLineId(), mainActivity);
                if (Bj.getDepartureHour() < Integer.parseInt(Sdf.format(currentTime)) || (Bj.getDepartureHour() == Integer.parseInt(Sdf.format(currentTime)) && Bj.getDepartureMinute() <= Integer.parseInt(Sdf2.format(currentTime)))) {
                    Boolean IsBusStarted = serverApi.getIsBusHasStarted(incomingBusRespModel.getLineId());
                    if (IsBusStarted == null) {
                        IsBusStarted = false;
                    }
                    incomingBusRespModel.setStarted(IsBusStarted);
                } else {
                    incomingBusRespModel.setStarted(false);
                }

            }

            if(SendStopId != mStop) {
                return;
            }

            if (BusList.length > 0) {
                if (InBusFragment == null) {
                    try {
                        InBusFragment = IncomingBusListFragment.newInstance(BusList);
                        getChildFragmentManager().beginTransaction()
                                .replace(R.id.BusListFragment, InBusFragment)
                                .commit();
                    } catch (Exception e) {
                        InBusFragment = null;
                    }
                } else {
                    if (mainActivity != null) {
                        IncomingBusRespModel[] finalBusList = BusList;
                        mainActivity.runOnUiThread(() -> {
                            if (InBusFragment != null) {
                                InBusFragment.UpdateList(finalBusList);
                            }

                        });
                    }
                }
            } else {
                InBusFragment = null;

                InfoFragment Fragment = InfoFragment.newInstance(getResources().getString(R.string.EmptyList), -1);
                getChildFragmentManager().beginTransaction()
                        .replace(R.id.BusListFragment, Fragment)
                        .commit();
            }
        } catch (Exception e) {
            Log.e("Update bus list error", e.toString());
            e.printStackTrace();
        }
    }
}