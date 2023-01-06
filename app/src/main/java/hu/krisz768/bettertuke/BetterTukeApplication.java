package hu.krisz768.bettertuke;

import android.app.Application;

import com.google.android.material.color.DynamicColors;

public class BetterTukeApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        DynamicColors.applyToActivitiesIfAvailable(this);
    }
}
