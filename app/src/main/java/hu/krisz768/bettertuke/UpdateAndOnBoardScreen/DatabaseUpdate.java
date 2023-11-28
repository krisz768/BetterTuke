package hu.krisz768.bettertuke.UpdateAndOnBoardScreen;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.constraintlayout.helper.widget.Flow;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import hu.krisz768.bettertuke.BuildConfig;
import hu.krisz768.bettertuke.Database.DatabaseManager;
import hu.krisz768.bettertuke.Gtfs.GTFSDatabaseManager;
import hu.krisz768.bettertuke.R;
import hu.krisz768.bettertuke.api_interface.TukeServerApi;

public class DatabaseUpdate extends Fragment {
    private static final String UPDATEBASE= "UpdateBase";
    private static final String UPDATEGTFS= "UpdateGFTS";
    private static final String IMMEDIATESTART= "ImmediateStart";
    private boolean mUpdateBase;
    private boolean mUpdateGFTS;
    private boolean mImmediateStart;
    private static OnUpdateFinish onUpdateFinish;
    private static OnUpdateFail onUpdateFail;
    private View view;
    private ProgressBar progressBar;
    private TextView progressText;

    public DatabaseUpdate() {
    }

    public static DatabaseUpdate newInstance(boolean UpdateBase, boolean UpdateGFTS, boolean ImmediateStart, OnUpdateFinish evt1, OnUpdateFail evt2) {
        onUpdateFinish = evt1;
        onUpdateFail = evt2;
        DatabaseUpdate fragment = new DatabaseUpdate();
        Bundle args = new Bundle();
        args.putBoolean(UPDATEBASE, UpdateBase);
        args.putBoolean(UPDATEGTFS, UpdateGFTS);
        args.putBoolean(IMMEDIATESTART, ImmediateStart);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            mUpdateBase = getArguments().getBoolean(UPDATEBASE);
            mUpdateGFTS = getArguments().getBoolean(UPDATEGTFS);
            mImmediateStart = getArguments().getBoolean(IMMEDIATESTART);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_database_update, container, false);
        progressBar = view.findViewById(R.id.DownloadPregressbar);
        progressText = view.findViewById(R.id.DownloadProgressText);

        if (mImmediateStart) {
            StartUpdate();
        }
        return view;
    }

    public void StartUpdate() {
        new Thread(this::Update).start();
    }

    public interface OnUpdateFinish {
        void onFinish();
    }

    public interface OnUpdateFail {
        void onFail();
    }

    private void Update() {
        Activity activity = getActivity();
        Context context = getContext();

        if (activity == null || context == null) {
            return;
        }

        if (mUpdateBase) {
            TukeServerApi serverApi = new TukeServerApi(getContext());

            if (!DatabaseManager.DeleteDatabase(context)){
                AddLog("Database delete error. Continue anyway...");
            }

            if (serverApi.downloadDatabaseFile()) {
                AddLog("Database downloaded successfully");
            } else {
                AddLog("Database download fail");
                activity.runOnUiThread(() -> {
                    if (onUpdateFail != null) {
                        onUpdateFail.onFail();
                    }
                    SetPercentage(0);
                });
                return;
            }
        }

        activity.runOnUiThread(() -> SetPercentage(33));

        if (mUpdateGFTS) {
            GTFSDatabaseManager gtfsDatabaseManager = new GTFSDatabaseManager(context);

            if (gtfsDatabaseManager.ForceUpdate(Step -> activity.runOnUiThread(() -> SetPercentage(33+((66/9)*Step))))) {
                AddLog("GTFS Database downloaded successfully");
            } else {
                AddLog("GTFS Database download fail");
                activity.runOnUiThread(() -> {
                    if (onUpdateFail != null) {
                        onUpdateFail.onFail();
                    }
                    SetPercentage(0);
                });
                return;
            }
        }

        activity.runOnUiThread(() -> {
            if (onUpdateFinish != null) {
                onUpdateFinish.onFinish();
            }
            SetPercentage(100);
        });


    }

    @SuppressLint("SetTextI18n")
    private void SetPercentage(int Percent) {
        progressText.setText(Percent + "%");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            progressBar.setProgress(Percent, true);
        } else {
            progressBar.setProgress(Percent);
        }
    }

    @Override
    public void onConfigurationChanged (@NonNull Configuration config) {
        super.onConfigurationChanged(config);
        ((Flow)view.findViewById(R.id.flow4)).setWrapMode(Flow.WRAP_CHAIN);
    }

    private void AddLog(String LogText) {
        if (BuildConfig.DEBUG) {
            Log.i("Update", LogText);
        }
    }
}