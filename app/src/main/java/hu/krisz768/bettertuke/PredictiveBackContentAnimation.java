package hu.krisz768.bettertuke;

import android.util.TypedValue;
import android.view.View;

import androidx.activity.BackEventCompat;
import androidx.annotation.NonNull;

/**
 * Material "in-app" predictive back for content that is replaced in place (not through a fragment
 * back stack).
 *
 * While the user swipes back, the content shrinks and moves towards the swipe edge. When the
 * gesture is committed the content fades out, the caller swaps it to the previous state and the
 * new content fades in. When the gesture is cancelled the content returns to normal.
 */
public class PredictiveBackContentAnimation {
    private static final float MIN_SCALE = 0.9f;
    private static final float MIN_ALPHA = 0.6f;

    private final View Content;
    private final boolean PivotTop;
    private Runnable PendingSwap;

    /**
     * @param content  the view to animate
     * @param pivotTop scale towards the top edge (e.g. bottom sheet content) instead of the center
     */
    public PredictiveBackContentAnimation(@NonNull View content, boolean pivotTop) {
        Content = content;
        PivotTop = pivotTop;
    }

    public void Start() {
        Content.animate().cancel();
        Content.setPivotX(Content.getWidth() / 2f);
        Content.setPivotY(PivotTop ? 0f : Content.getHeight() / 2f);
    }

    public void Update(@NonNull BackEventCompat backEvent) {
        float progress = backEvent.getProgress();

        float maxShift = Math.max(0f, Content.getWidth() / 20f - TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8, Content.getResources().getDisplayMetrics()));
        float direction = backEvent.getSwipeEdge() == BackEventCompat.EDGE_LEFT ? 1f : -1f;
        float scale = 1f - (1f - MIN_SCALE) * progress;

        Content.setScaleX(scale);
        Content.setScaleY(scale);
        Content.setTranslationX(direction * maxShift * progress);
        Content.setAlpha(1f - (1f - MIN_ALPHA) * progress);
    }

    /**
     * The gesture was committed: fade the content out, then run swap (which should show the
     * previous state) and fade the new content in.
     */
    public void Finish(@NonNull Runnable swap) {
        PendingSwap = swap;

        Content.animate()
                .alpha(0f)
                .scaleX(MIN_SCALE)
                .scaleY(MIN_SCALE)
                .setStartDelay(0)
                .setDuration(100)
                .withEndAction(this::CompletePending)
                .start();
    }

    /**
     * Runs the swap of a committed gesture right away if its exit animation is still running
     * (e.g. a new back event arrives during it).
     *
     * @return true if there was a pending swap
     */
    public boolean CompletePending() {
        if (PendingSwap == null) {
            return false;
        }

        Runnable swap = PendingSwap;
        PendingSwap = null;

        Content.animate().cancel();
        swap.run();

        Content.setScaleX(1f);
        Content.setScaleY(1f);
        Content.setTranslationX(0f);
        Content.animate()
                .alpha(1f)
                .setStartDelay(0)
                .setDuration(200)
                .start();
        return true;
    }

    public void Cancel() {
        Content.animate()
                .alpha(1f)
                .scaleX(1f)
                .scaleY(1f)
                .translationX(0f)
                .setStartDelay(0)
                .setDuration(150)
                .start();
    }
}
