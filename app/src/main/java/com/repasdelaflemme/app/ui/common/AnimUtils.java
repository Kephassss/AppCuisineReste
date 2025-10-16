package com.repasdelaflemme.app.ui.common;

import android.animation.Keyframe;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.ValueAnimator;
import android.util.TypedValue;
import android.view.View;
import android.view.MotionEvent;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import com.repasdelaflemme.app.R;

public class AnimUtils {
    public static void slideInKeyframes(View v, long delayMs) {
        if (v.getTag(R.id.tag_animated) != null) return;

        float dp = v.getResources().getDisplayMetrics().density;
        float start = 20f * dp; // from: translateY(20px)
        float over = -4f * dp;
        float back = 2f * dp;

        Keyframe kf0 = Keyframe.ofFloat(0f, start);
        Keyframe kf1 = Keyframe.ofFloat(0.7f, over);
        Keyframe kf2 = Keyframe.ofFloat(0.85f, back);
        Keyframe kf3 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder pTrans = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y, kf0, kf1, kf2, kf3);

        Keyframe af0 = Keyframe.ofFloat(0f, 0f);
        Keyframe af3 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder pAlpha = PropertyValuesHolder.ofKeyframe(View.ALPHA, af0, af3);

        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, pTrans, pAlpha);
        anim.setDuration(300);
        anim.setStartDelay(Math.max(0, delayMs));
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
        v.setTag(R.id.tag_animated, Boolean.TRUE);
    }

    public static void slideInKeyframesX(View v, long delayMs) {
        if (v.getTag(R.id.tag_animated_x) != null) return;

        float dp = v.getResources().getDisplayMetrics().density;
        float startX = -20f * dp; // from translateX(-20px)
        float over = 4f * dp;     // slight overshoot to the right
        float back = -1.5f * dp;  // tiny back to left

        Keyframe kf0 = Keyframe.ofFloat(0f, startX);
        Keyframe kf1 = Keyframe.ofFloat(0.7f, over);
        Keyframe kf2 = Keyframe.ofFloat(0.85f, back);
        Keyframe kf3 = Keyframe.ofFloat(1f, 0f);
        PropertyValuesHolder pTrans = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_X, kf0, kf1, kf2, kf3);

        Keyframe af0 = Keyframe.ofFloat(0f, 0f);
        Keyframe af3 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder pAlpha = PropertyValuesHolder.ofKeyframe(View.ALPHA, af0, af3);

        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, pTrans, pAlpha);
        anim.setDuration(300);
        anim.setStartDelay(Math.max(0, delayMs));
        anim.setInterpolator(new DecelerateInterpolator());
        anim.start();
        v.setTag(R.id.tag_animated_x, Boolean.TRUE);
    }

    // Approximate CSS: transition: transform 0.2s ease, box-shadow 0.2s ease;
    // Press -> scale down slightly and raise translationZ, release -> reset.
    public static void attachPressAnimator(View v) {
        final float scalePressed = 0.98f;
        final long dur = 200L;
        final float dz = 8f * v.getResources().getDisplayMetrics().density;
        v.setOnTouchListener((view, ev) -> {
            switch (ev.getActionMasked()) {
                case MotionEvent.ACTION_DOWN:
                    view.animate()
                        .scaleX(scalePressed).scaleY(scalePressed)
                        .translationZ(dz)
                        .setDuration(dur)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                    break;
                case MotionEvent.ACTION_UP:
                case MotionEvent.ACTION_CANCEL:
                    view.animate()
                        .scaleX(1f).scaleY(1f)
                        .translationZ(0f)
                        .setDuration(dur)
                        .setInterpolator(new DecelerateInterpolator())
                        .start();
                    break;
            }
            return false;
        });
    }

    public static void fadeIn(View v, long durationMs, long delayMs) {
        if (v == null) return;
        v.setAlpha(0f);
        v.animate()
                .alpha(1f)
                .setDuration(Math.max(0, durationMs))
                .setStartDelay(Math.max(0, delayMs))
                .setInterpolator(new DecelerateInterpolator())
                .start();
    }

    public static void fadeInEaseIn(View v, long durationMs, long delayMs) {
        if (v == null) return;
        v.setAlpha(0f);
        v.animate()
                .alpha(1f)
                .setDuration(Math.max(0, durationMs))
                .setStartDelay(Math.max(0, delayMs))
                .setInterpolator(new AccelerateInterpolator())
                .start();
    }

    // CSS-like keyframes fadeIn: 0% -> 0, 60% -> 0.6, 100% -> 1 (ease-in)
    public static void fadeInKeyframes(View v, long durationMs, long delayMs) {
        if (v == null) return;
        Keyframe k0 = Keyframe.ofFloat(0f, 0f);
        Keyframe k1 = Keyframe.ofFloat(0.6f, 0.6f);
        Keyframe k2 = Keyframe.ofFloat(1f, 1f);
        PropertyValuesHolder pAlpha = PropertyValuesHolder.ofKeyframe(View.ALPHA, k0, k1, k2);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, pAlpha);
        anim.setDuration(Math.max(0, durationMs));
        anim.setStartDelay(Math.max(0, delayMs));
        anim.setInterpolator(new AccelerateInterpolator());
        // Ensure starting alpha visible path
        v.setAlpha(0f);
        anim.start();
    }

    // CSS @keyframes bounce equivalent on translateY
    // 0%,20%,50%,80%,100% -> 0 ; 40% -> -amp ; 60% -> -0.5*amp
    public static ObjectAnimator bounceKeyframes(View v, float amplitudeDp, long durationMs, boolean infinite) {
        float d = v.getResources().getDisplayMetrics().density;
        float amp = amplitudeDp * d;
        Keyframe k0 = Keyframe.ofFloat(0f, 0f);     // 0%
        Keyframe k20 = Keyframe.ofFloat(0.2f, 0f);  // 20%
        Keyframe k40 = Keyframe.ofFloat(0.4f, -amp);// 40%
        Keyframe k50 = Keyframe.ofFloat(0.5f, 0f);  // 50%
        Keyframe k60 = Keyframe.ofFloat(0.6f, -amp * 0.5f); // 60%
        Keyframe k80 = Keyframe.ofFloat(0.8f, 0f);  // 80%
        Keyframe k100 = Keyframe.ofFloat(1f, 0f);   // 100%
        PropertyValuesHolder pTY = PropertyValuesHolder.ofKeyframe(View.TRANSLATION_Y, k0, k20, k40, k50, k60, k80, k100);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, pTY);
        anim.setDuration(Math.max(0, durationMs));
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        if (infinite) {
            anim.setRepeatCount(ValueAnimator.INFINITE);
            anim.setRepeatMode(ValueAnimator.RESTART);
        }
        anim.start();
        return anim;
    }

    // Gentle pulse loop on scaleX/scaleY between [from..to]
    public static ObjectAnimator pulseLoop(View v, float fromScale, float toScale, long durationMs) {
        float fs = Math.max(0.90f, fromScale);
        float ts = Math.min(1.10f, toScale);
        PropertyValuesHolder sx = PropertyValuesHolder.ofFloat(View.SCALE_X, fs, ts);
        PropertyValuesHolder sy = PropertyValuesHolder.ofFloat(View.SCALE_Y, fs, ts);
        ObjectAnimator anim = ObjectAnimator.ofPropertyValuesHolder(v, sx, sy);
        anim.setDuration(Math.max(200L, durationMs));
        anim.setRepeatCount(ValueAnimator.INFINITE);
        anim.setRepeatMode(ValueAnimator.REVERSE);
        anim.setInterpolator(new AccelerateDecelerateInterpolator());
        anim.start();
        return anim;
    }
}
