package com.example.reelshuffle;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.GestureDescription;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.widget.TextView;

import java.util.Random;

/** A manual Reels-only touch translator: enable it with the floating bubble. */
public class ShuffleAccessibilityService extends AccessibilityService {
    private static final String INSTAGRAM = "com.instagram.android";

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final DirectionEngine directions = new DirectionEngine(new Random());

    private WindowManager windowManager;
    private View shield;
    private TextView bubble;
    private WindowManager.LayoutParams shieldParams;
    private WindowManager.LayoutParams bubbleParams;
    private SharedPreferences prefs;

    private boolean instagramForeground = false;
    private boolean active = false;
    private boolean injecting = false;
    private float touchStartX, touchStartY;
    private long touchStartTime;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        windowManager = (WindowManager) getSystemService(WINDOW_SERVICE);
        prefs = getSharedPreferences("prefs", MODE_PRIVATE);
        handler.postDelayed(() -> {
            if (getRootInActiveWindow() != null) {
                CharSequence pkg = getRootInActiveWindow().getPackageName();
                instagramForeground = pkg != null && INSTAGRAM.contentEquals(pkg);
            }
            updateOverlays();
        }, 300);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        if (event == null) return;
        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED &&
                type != AccessibilityEvent.TYPE_WINDOWS_CHANGED) return;

        CharSequence pkg = event.getPackageName();
        // System UI (e.g. permission dialogs/notifications) should not be covered.
        if (type == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED && pkg != null) {
            instagramForeground = INSTAGRAM.contentEquals(pkg);
        } else if (getRootInActiveWindow() != null) {
            CharSequence rootPackage = getRootInActiveWindow().getPackageName();
            instagramForeground = rootPackage != null && INSTAGRAM.contentEquals(rootPackage);
        }
        if (!instagramForeground) {
            active = false;
            directions.reset();
        }
        if (!injecting) updateOverlays();
    }

    @Override
    public void onInterrupt() {
        active = false;
        updateOverlays();
    }

    @Override
    public void onDestroy() {
        handler.removeCallbacksAndMessages(null);
        removeShield();
        removeBubble();
        super.onDestroy();
    }

    private void toggle() {
        if (injecting) return;
        active = !active;
        if (active) {
            directions.reset();
            // Keep the floating control above the full-screen interception layer.
            removeBubble();
        }
        updateOverlays();
    }

    private void updateOverlays() {
        if (!instagramForeground || windowManager == null) {
            removeShield();
            removeBubble();
            return;
        }
        if (active) addShield(); else removeShield();
        addBubble();
        updateBubbleText();
    }

    private void addShield() {
        if (shield != null) return;
        shield = new View(this);
        shield.setBackgroundColor(Color.TRANSPARENT);
        shield.setOnTouchListener((v, event) -> onShieldTouch(event));
        shieldParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        shieldParams.gravity = Gravity.TOP | Gravity.LEFT;
        try {
            windowManager.addView(shield, shieldParams);
        } catch (RuntimeException exception) {
            shield = null;
            active = false;
        }
    }

    private void removeShield() {
        if (shield != null) {
            try { windowManager.removeView(shield); } catch (RuntimeException ignored) { }
            shield = null;
        }
    }

    private void addBubble() {
        if (bubble != null) return;
        bubble = new TextView(this);
        bubble.setTextSize(22f);
        bubble.setTextColor(Color.WHITE);
        bubble.setGravity(Gravity.CENTER);
        GradientDrawable bg = new GradientDrawable();
        bg.setColor(Color.argb(220, 31, 36, 50));
        bg.setCornerRadius(dp(35));
        bubble.setBackground(bg);
        bubble.setOnClickListener(v -> toggle());
        bubbleParams = new WindowManager.LayoutParams(
                dp(52), dp(52),
                WindowManager.LayoutParams.TYPE_ACCESSIBILITY_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE |
                        WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN,
                PixelFormat.TRANSLUCENT);
        bubbleParams.gravity = Gravity.RIGHT | Gravity.CENTER_VERTICAL;
        bubbleParams.x = dp(7);
        bubbleParams.y = -dp(100);
        try {
            windowManager.addView(bubble, bubbleParams);
        } catch (RuntimeException exception) {
            bubble = null;
        }
    }

    private void removeBubble() {
        if (bubble != null) {
            try { windowManager.removeView(bubble); } catch (RuntimeException ignored) { }
            bubble = null;
        }
    }

    private void updateBubbleText() {
        if (bubble == null) return;
        if (!active) {
            bubble.setText("↕");
            bubble.setContentDescription("릴스 랜덤 방향 켜기");
        } else {
            boolean show = prefs.getBoolean("show_direction", false);
            bubble.setText(show ? (directions.nextRequiresUp() ? "↑" : "↓") : "?");
            bubble.setContentDescription("릴스 랜덤 방향 사용 중. 종료하려면 누르세요.");
        }
        bubble.setAlpha(active ? 1f : 0.72f);
    }

    private boolean onShieldTouch(MotionEvent event) {
        if (!active || injecting) return true;
        switch (event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                touchStartX = event.getRawX();
                touchStartY = event.getRawY();
                touchStartTime = android.os.SystemClock.uptimeMillis();
                return true;
            case MotionEvent.ACTION_UP:
                float endX = event.getRawX();
                float endY = event.getRawY();
                float dx = endX - touchStartX;
                float dy = endY - touchStartY;
                long duration = Math.min(1000L,
                        Math.max(65L, android.os.SystemClock.uptimeMillis() - touchStartTime));
                if (Math.abs(dy) > dp(65) && Math.abs(dy) > Math.abs(dx) * 1.15f) {
                    handleVerticalSwipe(dy < 0);
                } else if (Math.abs(dx) > dp(42) || Math.abs(dy) > dp(42)) {
                    sendGesture(touchStartX, touchStartY, endX, endY, duration, null);
                } else {
                    // Forward taps, including play/pause and like.
                    sendGesture(endX, endY, endX, endY,
                            duration >= 450L ? duration : 65L, null);
                }
                return true;
            case MotionEvent.ACTION_CANCEL:
                return true;
            default:
                return true;
        }
    }

    private void handleVerticalSwipe(boolean fingerUp) {
        DirectionEngine.Move move = directions.classify(fingerUp);
        Point size = new Point();
        windowManager.getDefaultDisplay().getRealSize(size);
        float x = Math.max(dp(65), Math.min(size.x - dp(65), touchStartX));
        // Instagram's normal gesture: UP = next, DOWN = previous.
        float fromY = size.y * (move == DirectionEngine.Move.NEXT ? .75f : .28f);
        float toY = size.y * (move == DirectionEngine.Move.NEXT ? .28f : .75f);
        sendGesture(x, fromY, x, toY, 245L, () -> {
            directions.dispatched(move);
            updateBubbleText();
        });
    }

    private void sendGesture(float x1, float y1, float x2, float y2,
                             long duration, Runnable onCompleted) {
        if (injecting) return;
        injecting = true;
        // Make both accessibility overlays untouchable before injecting into Instagram.
        setOverlayTouchability(false);
        handler.postDelayed(() -> {
            Path path = new Path();
            path.moveTo(x1, y1);
            if (x1 != x2 || y1 != y2) path.lineTo(x2, y2);
            GestureDescription gesture = new GestureDescription.Builder()
                    .addStroke(new GestureDescription.StrokeDescription(path, 0,
                            Math.max(1L, duration))).build();
            boolean accepted = dispatchGesture(gesture, new GestureResultCallback() {
                @Override
                public void onCompleted(GestureDescription description) {
                    if (onCompleted != null) onCompleted.run();
                    restoreOverlaysSoon();
                }

                @Override
                public void onCancelled(GestureDescription description) {
                    restoreOverlaysSoon();
                }
            }, handler);
            if (!accepted) restoreOverlaysSoon();
        }, 80L);
    }

    private void restoreOverlaysSoon() {
        handler.postDelayed(() -> {
            injecting = false;
            setOverlayTouchability(true);
            updateOverlays();
        }, 130L);
    }

    private void setOverlayTouchability(boolean touchable) {
        if (shield != null) setTouchable(shield, shieldParams, touchable);
        if (bubble != null) setTouchable(bubble, bubbleParams, touchable);
    }

    private void setTouchable(View view, WindowManager.LayoutParams lp, boolean touchable) {
        if (touchable) {
            lp.flags &= ~WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        } else {
            lp.flags |= WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;
        }
        try { windowManager.updateViewLayout(view, lp); }
        catch (RuntimeException ignored) { }
    }

    private int dp(float value) {
        return (int) (value * getResources().getDisplayMetrics().density + .5f);
    }
}
