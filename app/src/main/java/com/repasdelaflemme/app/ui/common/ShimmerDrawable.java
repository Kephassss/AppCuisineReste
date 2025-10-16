package com.repasdelaflemme.app.ui.common;

import android.animation.ValueAnimator;
import android.graphics.Canvas;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.view.animation.LinearInterpolator;

/**
 * Lightweight shimmer highlight drawable drawn over skeleton blocks.
 */
public class ShimmerDrawable extends Drawable {
    private final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private float animFrac = 0f;
    private LinearGradient gradient;
    private final RectF rect = new RectF();
    private ValueAnimator animator;

    @Override
    public void draw(Canvas canvas) {
        Rect b = getBounds();
        if (b.isEmpty()) return;
        rect.set(b.left, b.top, b.right, b.bottom);
        int w = b.width();
        int h = b.height();
        if (w <= 0 || h <= 0) return;

        float gradientWidth = Math.max(0.2f * w, 60f);
        float startX = -gradientWidth;
        float endX = w + gradientWidth;
        float x = startX + (endX - startX) * animFrac;

        int base = 0x00FFFFFF; // transparent
        int mid = 0x26FFFFFF;  // ~15% white
        gradient = new LinearGradient(
                x - gradientWidth, 0,
                x + gradientWidth, 0,
                new int[]{base, mid, base},
                new float[]{0f, 0.5f, 1f},
                Shader.TileMode.CLAMP);
        paint.setShader(gradient);
        canvas.drawRoundRect(rect, 16f, 16f, paint);
    }

    @Override public void setAlpha(int alpha) { paint.setAlpha(alpha); }
    @Override public void setColorFilter(android.graphics.ColorFilter cf) { paint.setColorFilter(cf); }
    @Override public int getOpacity() { return android.graphics.PixelFormat.TRANSLUCENT; }

    public void start(View host) {
        stop();
        animator = ValueAnimator.ofFloat(0f, 1f);
        animator.setDuration(1500L);
        animator.setInterpolator(new LinearInterpolator());
        animator.setRepeatCount(ValueAnimator.INFINITE);
        animator.addUpdateListener(a -> {
            animFrac = (float) a.getAnimatedValue();
            invalidateSelf();
            if (host != null) host.invalidate();
        });
        animator.start();
    }

    public void stop() {
        if (animator != null) {
            try { animator.cancel(); } catch (Exception ignored) {}
            animator = null;
        }
    }
}

