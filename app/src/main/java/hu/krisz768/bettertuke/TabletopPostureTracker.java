package hu.krisz768.bettertuke;

import android.graphics.Rect;

import androidx.activity.ComponentActivity;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.util.Consumer;
import androidx.lifecycle.DefaultLifecycleObserver;
import androidx.lifecycle.LifecycleOwner;
import androidx.window.java.layout.WindowInfoTrackerCallbackAdapter;
import androidx.window.layout.DisplayFeature;
import androidx.window.layout.FoldingFeature;
import androidx.window.layout.WindowInfoTracker;
import androidx.window.layout.WindowLayoutInfo;

import java.util.Objects;

/**
 * Detects the tabletop posture of foldables ("Flex mode" on Samsung devices): the device is half
 * opened with a horizontal fold, the top half of the screen stands up, the bottom half lies flat.
 *
 * The posture can change without a configuration change (only the hinge angle changes), so it is
 * observed through Jetpack WindowManager while the activity is started.
 */
public class TabletopPostureTracker implements DefaultLifecycleObserver {
    public interface Listener {
        void OnTabletopPostureChanged();
    }

    private final ComponentActivity Activity;
    private final Listener mListener;
    private final WindowInfoTrackerCallbackAdapter Tracker;
    private final Consumer<WindowLayoutInfo> LayoutInfoConsumer = this::OnWindowLayoutInfo;

    @Nullable
    private Rect HingeBounds;

    public TabletopPostureTracker(@NonNull ComponentActivity activity, @NonNull Listener listener) {
        Activity = activity;
        mListener = listener;
        Tracker = new WindowInfoTrackerCallbackAdapter(WindowInfoTracker.getOrCreate(activity));
        activity.getLifecycle().addObserver(this);
    }

    @Override
    public void onStart(@NonNull LifecycleOwner owner) {
        Tracker.addWindowLayoutInfoListener(Activity, ContextCompat.getMainExecutor(Activity), LayoutInfoConsumer);
    }

    @Override
    public void onStop(@NonNull LifecycleOwner owner) {
        Tracker.removeWindowLayoutInfoListener(LayoutInfoConsumer);
    }

    public boolean IsTabletop() {
        return HingeBounds != null;
    }

    /** Bounds of the fold in window coordinates in tabletop posture, null otherwise */
    @Nullable
    public Rect GetHingeBounds() {
        return HingeBounds;
    }

    private void OnWindowLayoutInfo(WindowLayoutInfo layoutInfo) {
        Rect hinge = null;

        for (DisplayFeature feature : layoutInfo.getDisplayFeatures()) {
            if (feature instanceof FoldingFeature) {
                FoldingFeature fold = (FoldingFeature) feature;
                if (fold.getState() == FoldingFeature.State.HALF_OPENED && fold.getOrientation() == FoldingFeature.Orientation.HORIZONTAL) {
                    hinge = fold.getBounds();
                }
            }
        }

        if (Objects.equals(hinge, HingeBounds)) {
            return;
        }

        HingeBounds = hinge;
        mListener.OnTabletopPostureChanged();
    }
}
