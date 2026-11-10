package hu.krisz768.bettertuke;

import android.content.res.Configuration;
import android.graphics.Rect;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.activity.BackEventCompat;
import androidx.activity.ComponentActivity;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.interpolator.view.animation.FastOutSlowInInterpolator;

/**
 * List-detail layout for large screens (tablets, unfolded foldables).
 *
 * The list pane and the detail pane are children of the same FrameLayout.
 * - Two-pane mode (window width >= TWO_PANE_MIN_WIDTH_DP): the list is on the start side with a
 *   fixed width, the detail fills the rest, both are always visible. Selecting an item only
 *   changes the detail, back is handled by the system (leaves the screen).
 * - Single pane (phones, folded): the detail is shown above the list. Back closes it with a
 *   predictive back animation: the detail slides out towards the end edge following the gesture,
 *   while the list fades in below it.
 *
 * - Tabletop posture of foldables ("Flex mode"): the detail is on the top half of the screen, the
 *   list (where the user chooses) on the bottom half, split at the fold. Both are always visible.
 *
 * Switching between the modes (e.g. folding / unfolding) only rearranges the panes, so the
 * fragments / views in them and their state are kept.
 */
public class ListDetailController {
    public static final int TWO_PANE_MIN_WIDTH_DP = 600;

    private static final long ANIMATION_DURATION = 300;

    public interface Callback {
        /** The detail was closed by back navigation in single pane mode */
        void OnDetailClosed();
    }

    private final View ListPane;
    private final View DetailPane;
    @Nullable
    private final View Divider;
    private final Callback mCallback;

    private final TabletopPostureTracker PostureTracker;
    @Nullable
    private Runnable LayoutModeChangedListener;

    private boolean TwoPane = false;
    private boolean DetailShown = false;
    private boolean DetailIsRoot = false;
    private boolean IsBackGestureInProgress = false;

    private final OnBackPressedCallback BackCallback = new OnBackPressedCallback(false) {
        @Override
        public void handleOnBackStarted(@NonNull BackEventCompat backEvent) {
            IsBackGestureInProgress = true;
            CancelAnimations();
            ListPane.setVisibility(View.VISIBLE);
            ListPane.setAlpha(0f);
        }

        @Override
        public void handleOnBackProgressed(@NonNull BackEventCompat backEvent) {
            float progress = backEvent.getProgress();
            DetailPane.setTranslationX(EndDirection() * DetailPane.getWidth() * progress);
            ListPane.setAlpha(progress);
        }

        @Override
        public void handleOnBackPressed() {
            if (!IsBackGestureInProgress) {
                // Button navigation: play the whole animation
                CancelAnimations();
                ListPane.setVisibility(View.VISIBLE);
                ListPane.setAlpha(0f);
            }
            IsBackGestureInProgress = false;

            float remaining = 1f - Math.abs(DetailPane.getTranslationX()) / Math.max(1, DetailPane.getWidth());

            ListPane.animate().alpha(1f).setDuration((long) (ANIMATION_DURATION * remaining)).setInterpolator(new FastOutSlowInInterpolator()).start();
            DetailPane.animate()
                    .translationX(EndDirection() * DetailPane.getWidth())
                    .setDuration((long) (ANIMATION_DURATION * remaining))
                    .setInterpolator(new FastOutSlowInInterpolator())
                    .withEndAction(() -> {
                        DetailShown = false;
                        ApplyLayout();
                        mCallback.OnDetailClosed();
                    })
                    .start();
        }

        @Override
        public void handleOnBackCancelled() {
            IsBackGestureInProgress = false;

            ListPane.animate().alpha(0f).setDuration(150).start();
            DetailPane.animate()
                    .translationX(0f)
                    .setDuration(150)
                    .withEndAction(ListDetailController.this::ApplyLayout)
                    .start();
        }
    };

    public ListDetailController(@NonNull ComponentActivity activity, @NonNull View listPane, @NonNull View detailPane, @Nullable View divider, @NonNull Callback callback) {
        ListPane = listPane;
        DetailPane = detailPane;
        Divider = divider;
        mCallback = callback;

        activity.getOnBackPressedDispatcher().addCallback(activity, BackCallback);

        PostureTracker = new TabletopPostureTracker(activity, () -> {
            CancelAnimations();
            ApplyLayout();
            if (LayoutModeChangedListener != null) {
                LayoutModeChangedListener.run();
            }
        });

        // In tabletop posture the split follows the fold, which has to be re-calculated when the
        // position or the size of the panes changes
        View parent = (View) listPane.getParent();
        parent.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (IsTabletop() && (top != oldTop || bottom - top != oldBottom - oldTop)) {
                v.post(this::ApplyLayout);
            }
        });

        TwoPane = IsTwoPane(activity.getResources().getConfiguration());
        ApplyLayout();
    }

    /** Called when the arrangement changes without a configuration change (tabletop posture) */
    public void SetOnLayoutModeChangedListener(@Nullable Runnable listener) {
        LayoutModeChangedListener = listener;
    }

    public static boolean IsTwoPane(@NonNull Configuration configuration) {
        return configuration.screenWidthDp >= TWO_PANE_MIN_WIDTH_DP;
    }

    /** Width of the list pane in two-pane mode */
    public static int GetListPaneWidth(@NonNull View parent, int windowWidthDp) {
        float density = parent.getResources().getDisplayMetrics().density;
        float widthDp = Math.max(320, Math.min(420, windowWidthDp * 0.4f));
        return Math.round(widthDp * density);
    }

    /** Call from onConfigurationChanged */
    public void OnConfigurationChanged(@NonNull Configuration newConfig) {
        TwoPane = IsTwoPane(newConfig);
        CancelAnimations();
        ApplyLayout();
    }

    public boolean IsTwoPane() {
        return TwoPane;
    }

    public boolean IsTabletop() {
        return PostureTracker.IsTabletop();
    }

    /** Whether the list and the detail are visible at the same time (two-pane or tabletop) */
    public boolean IsMultiPane() {
        return TwoPane || IsTabletop();
    }

    public boolean IsDetailShown() {
        return DetailShown;
    }

    /**
     * In single pane mode the detail has nothing below it (e.g. the screen was opened directly on
     * an item and should be left with back), so the list is never shown behind it.
     */
    public void SetDetailIsRoot(boolean detailIsRoot) {
        DetailIsRoot = detailIsRoot;
        ApplyLayout();
    }

    public void ShowDetail(boolean animate) {
        boolean wasShown = DetailShown;
        DetailShown = true;
        CancelAnimations();

        if (!animate || IsMultiPane() || wasShown) {
            ApplyLayout();
            return;
        }

        // Single pane: the detail slides in from the end edge while the list fades out
        View parent = (View) DetailPane.getParent();
        DetailPane.setVisibility(View.VISIBLE);
        DetailPane.setTranslationX(EndDirection() * parent.getWidth());
        DetailPane.animate()
                .translationX(0f)
                .setDuration(ANIMATION_DURATION)
                .setInterpolator(new FastOutSlowInInterpolator())
                .withEndAction(this::ApplyLayout)
                .start();
        ListPane.animate().alpha(0f).setDuration(ANIMATION_DURATION).start();
        BackCallback.setEnabled(!DetailIsRoot);
    }

    /** Closes the detail without back navigation (e.g. its data is gone) */
    public void CloseDetail() {
        DetailShown = false;
        CancelAnimations();
        ApplyLayout();
    }

    private void CancelAnimations() {
        ListPane.animate().cancel();
        DetailPane.animate().cancel();
    }

    private float EndDirection() {
        return DetailPane.getLayoutDirection() == View.LAYOUT_DIRECTION_RTL ? -1f : 1f;
    }

    private void ApplyLayout() {
        View parent = (View) ListPane.getParent();
        int windowWidthDp = parent.getResources().getConfiguration().screenWidthDp;

        FrameLayout.LayoutParams listParams = (FrameLayout.LayoutParams) ListPane.getLayoutParams();
        FrameLayout.LayoutParams detailParams = (FrameLayout.LayoutParams) DetailPane.getLayoutParams();

        ListPane.setTranslationX(0f);
        DetailPane.setTranslationX(0f);
        ListPane.setAlpha(1f);
        DetailPane.setAlpha(1f);

        listParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        detailParams.height = ViewGroup.LayoutParams.MATCH_PARENT;
        detailParams.gravity = Gravity.NO_GRAVITY;

        Rect hinge = PostureTracker.GetHingeBounds();

        if (hinge != null) {
            // Tabletop: detail above the fold, list below it
            int[] location = new int[2];
            parent.getLocationInWindow(location);
            int height = parent.getHeight();
            int splitTop = Math.max(0, Math.min(height, hinge.top - location[1]));
            int splitBottom = Math.max(splitTop, Math.min(height, hinge.bottom - location[1]));

            listParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            listParams.height = height - splitBottom;
            listParams.gravity = Gravity.BOTTOM;
            detailParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            detailParams.height = splitTop;
            detailParams.gravity = Gravity.TOP;
            detailParams.setMarginStart(0);

            ListPane.setVisibility(View.VISIBLE);
            DetailPane.setVisibility(View.VISIBLE);

            if (Divider != null) {
                Divider.setVisibility(View.GONE);
            }
        } else if (TwoPane) {
            int listWidth = GetListPaneWidth(parent, windowWidthDp);
            int dividerWidth = Math.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1, parent.getResources().getDisplayMetrics()));

            listParams.width = listWidth;
            listParams.gravity = Gravity.START;
            detailParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            detailParams.setMarginStart(listWidth + dividerWidth);

            ListPane.setVisibility(View.VISIBLE);
            DetailPane.setVisibility(View.VISIBLE);

            if (Divider != null) {
                FrameLayout.LayoutParams dividerParams = (FrameLayout.LayoutParams) Divider.getLayoutParams();
                dividerParams.width = dividerWidth;
                dividerParams.gravity = Gravity.START;
                dividerParams.setMarginStart(listWidth);
                Divider.setLayoutParams(dividerParams);
                Divider.setVisibility(View.VISIBLE);
            }
        } else {
            listParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            listParams.gravity = Gravity.NO_GRAVITY;
            detailParams.width = ViewGroup.LayoutParams.MATCH_PARENT;
            detailParams.setMarginStart(0);

            if (DetailShown) {
                // Keep the list laid out (INVISIBLE), so it can be shown right away for back
                ListPane.setVisibility(DetailIsRoot ? View.GONE : View.INVISIBLE);
                DetailPane.setVisibility(View.VISIBLE);
            } else {
                ListPane.setVisibility(View.VISIBLE);
                DetailPane.setVisibility(View.GONE);
            }

            if (Divider != null) {
                Divider.setVisibility(View.GONE);
            }
        }

        ListPane.setLayoutParams(listParams);
        DetailPane.setLayoutParams(detailParams);

        BackCallback.setEnabled(!IsMultiPane() && DetailShown && !DetailIsRoot);
    }
}
