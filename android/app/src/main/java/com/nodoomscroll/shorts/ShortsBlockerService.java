package com.nodoomscroll.shorts;

import android.accessibilityservice.AccessibilityService;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.util.ArrayDeque;
import java.util.List;

/**
 * Watches the YouTube app (and only it, see res/xml/shorts_blocker_service.xml).
 * When the full-screen Shorts player is on screen, it presses Back to leave it.
 */
public class ShortsBlockerService extends AccessibilityService {

    private static final String YT = "com.google.android.youtube";

    /** View IDs YouTube uses for the Shorts player. Checked first because lookups by ID are fast. */
    private static final String[] SHORTS_PLAYER_IDS = {
            YT + ":id/reel_recycler",
            YT + ":id/reel_player_page_container",
            YT + ":id/reel_watch_player",
            YT + ":id/reel_watch_fragment_root",
            YT + ":id/reel_progress_bar",
            YT + ":id/reel_time_bar",
    };

    /** Fallback: any large view whose ID mentions "reel" (YouTube's internal name for Shorts). */
    private static final String REEL_ID_MARKER = ":id/reel";

    private static final long CHECK_DELAY_MS = 120;
    private static final long COOLDOWN_MS = 1200;
    private static final int MAX_NODES_SCANNED = 600;

    private final Handler handler = new Handler(Looper.getMainLooper());
    private boolean checkScheduled = false;
    private long lastBackAt = 0;

    private final Runnable check = () -> {
        checkScheduled = false;
        checkForShorts();
    };

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null || !YT.contentEquals(event.getPackageName() == null ? "" : event.getPackageName())) return;
        // YouTube fires many events per second; check at most once per short window.
        if (!checkScheduled) {
            checkScheduled = true;
            handler.postDelayed(check, CHECK_DELAY_MS);
        }
    }

    private void checkForShorts() {
        if (Prefs.isPaused(this)) return;
        if (SystemClock.uptimeMillis() - lastBackAt < COOLDOWN_MS) return;

        AccessibilityNodeInfo root = getRootInActiveWindow();
        if (root == null) return;
        if (root.getPackageName() == null || !YT.contentEquals(root.getPackageName())) return;

        if (isShortsPlayerOpen(root)) {
            lastBackAt = SystemClock.uptimeMillis();
            performGlobalAction(GLOBAL_ACTION_BACK);
            Prefs.incrementBlocked(this);
            Toast.makeText(this, "Shorts blocked", Toast.LENGTH_SHORT).show();
            // If one Back wasn't enough (e.g. Shorts opened from the bottom tab), check again.
            handler.postDelayed(check, COOLDOWN_MS + 50);
        }
    }

    private boolean isShortsPlayerOpen(AccessibilityNodeInfo root) {
        for (String id : SHORTS_PLAYER_IDS) {
            List<AccessibilityNodeInfo> found = root.findAccessibilityNodeInfosByViewId(id);
            if (found == null) continue;
            for (AccessibilityNodeInfo n : found) {
                if (isLargeAndVisible(n) || id.endsWith("reel_progress_bar") || id.endsWith("reel_time_bar")) {
                    if (n.isVisibleToUser()) return true;
                }
            }
        }
        // Fallback scan in case YouTube renames its IDs.
        ArrayDeque<AccessibilityNodeInfo> queue = new ArrayDeque<>();
        queue.add(root);
        int scanned = 0;
        while (!queue.isEmpty() && scanned++ < MAX_NODES_SCANNED) {
            AccessibilityNodeInfo n = queue.poll();
            String id = n.getViewIdResourceName();
            if (id != null && id.contains(REEL_ID_MARKER) && isLargeAndVisible(n)) return true;
            for (int i = 0; i < n.getChildCount(); i++) {
                AccessibilityNodeInfo child = n.getChild(i);
                if (child != null) queue.add(child);
            }
        }
        return false;
    }

    /**
     * The Shorts player fills the screen. Shorts shelves in the home feed are small,
     * so requiring a near-full-screen view avoids pressing Back on the home page.
     */
    private boolean isLargeAndVisible(AccessibilityNodeInfo n) {
        if (!n.isVisibleToUser()) return false;
        Rect r = new Rect();
        n.getBoundsInScreen(r);
        DisplayMetrics dm = getResources().getDisplayMetrics();
        return r.width() >= dm.widthPixels * 0.9f && r.height() >= dm.heightPixels * 0.6f;
    }

    @Override
    public void onInterrupt() {}

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        super.onDestroy();
    }
}
