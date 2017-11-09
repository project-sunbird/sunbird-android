package org.sunbird.ui;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;

/**
 * Created by sahil on 02/03/17.
 */

public class TabLayoutUtils {
    private static final int[] APPCOMPAT_CHECK_ATTRS = {
            android.support.v7.appcompat.R.attr.colorPrimary
    };

    public static void checkAppCompatTheme(Context context) {
        TypedArray a = context.obtainStyledAttributes(APPCOMPAT_CHECK_ATTRS);
        final boolean failed = !a.hasValue(0);
        if (a != null) {
            a.recycle();
        }
        if (failed) {
            throw new IllegalArgumentException("You need to use a Theme.AppCompat theme "
                    + "(or descendant) with the design library.");
        }
    }

    public static ValueAnimator createAnimator() {
        return new ValueAnimator();
    }

    /**
     * Linear interpolation between {@code startValue} and {@code endValue} by {@code fraction}.
     */
    public static float lerp(float startValue, float endValue, float fraction) {
        return startValue + (fraction * (endValue - startValue));
    }

    static int lerp(int startValue, int endValue, float fraction) {
        return startValue + Math.round(fraction * (endValue - startValue));
    }
}
