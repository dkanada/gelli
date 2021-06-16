package com.dkanada.gramophone.util;

import android.content.res.ColorStateList;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Color;

import androidx.annotation.ColorInt;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.palette.graphics.Palette;

import com.dkanada.gramophone.R;
import com.google.android.material.color.MaterialColors;

import java.util.Collections;
import java.util.Comparator;

public class ThemeUtil {
    public static ColorStateList getColorStateList(int normal, int active) {
        int[][] states = new int[][]{
            new int[]{-android.R.attr.state_checked},
            new int[]{android.R.attr.state_checked}
        };

        int[] colors = new int[]{
            normal,
            active
        };

        return new ColorStateList(states, colors);
    }

    @ColorInt
    public static int getColor(@Nullable Palette palette, int fallback) {
        if (palette != null) {
            if (palette.getVibrantSwatch() != null) {
                return palette.getVibrantSwatch().getRgb();
            } else if (palette.getMutedSwatch() != null) {
                return palette.getMutedSwatch().getRgb();
            } else if (palette.getDarkVibrantSwatch() != null) {
                return palette.getDarkVibrantSwatch().getRgb();
            } else if (palette.getDarkMutedSwatch() != null) {
                return palette.getDarkMutedSwatch().getRgb();
            } else if (palette.getLightVibrantSwatch() != null) {
                return palette.getLightVibrantSwatch().getRgb();
            } else if (palette.getLightMutedSwatch() != null) {
                return palette.getLightMutedSwatch().getRgb();
            } else if (!palette.getSwatches().isEmpty()) {
                return Collections.max(palette.getSwatches(), SwatchComparator.getInstance()).getRgb();
            }
        }

        return fallback;
    }

    @ColorInt
    public static int getPrimaryTextColor(Context context, boolean light) {
        return light
            ? ContextCompat.getColor(context, R.color.color_text_primary_light)
            : ContextCompat.getColor(context, R.color.color_text_primary_dark);
    }

    @ColorInt
    public static int getSecondaryTextColor(Context context, boolean light) {
        return light
            ? ContextCompat.getColor(context, R.color.color_text_secondary_light)
            : ContextCompat.getColor(context, R.color.color_text_secondary_dark);
    }

    @ColorInt
    public static int getPrimaryTextColor(Context context, int color) {
        return getPrimaryTextColor(context, MaterialColors.isColorLight(color));
    }

    @ColorInt
    public static int getSecondaryTextColor(Context context, int color) {
        return getSecondaryTextColor(context, MaterialColors.isColorLight(color));
    }

    @ColorInt
    public static int getColorResource(Context context, int resource, int alpha) {
        TypedArray array = context.obtainStyledAttributes(new int[]{resource});
        int color = array.getColor(0, ContextCompat.getColor(context, android.R.color.white));

        array.recycle();

        return MaterialColors.compositeARGBWithAlpha(color, alpha);
    }

    @ColorInt
    public static int getColorAlpha(Context context, int color, int alpha) {
        return MaterialColors.compositeARGBWithAlpha(ContextCompat.getColor(context, color), alpha);
    }

    @ColorInt
    public static int getColorDark(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;

        return Color.HSVToColor(hsv);
    }

    private static class SwatchComparator implements Comparator<Palette.Swatch> {
        private static SwatchComparator sInstance;

        private static SwatchComparator getInstance() {
            if (sInstance == null) {
                sInstance = new SwatchComparator();
            }

            return sInstance;
        }

        @Override
        public int compare(Palette.Swatch lhs, Palette.Swatch rhs) {
            return lhs.getPopulation() - rhs.getPopulation();
        }
    }
}
