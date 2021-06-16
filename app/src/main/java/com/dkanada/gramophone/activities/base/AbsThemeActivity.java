package com.dkanada.gramophone.activities.base;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;

import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.Util;
import com.google.android.material.color.MaterialColors;

public abstract class AbsThemeActivity extends AppCompatActivity {
    private int currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(PreferenceUtil.getInstance(this).getTheme().style);
        setColor(PreferenceUtil.getInstance(this).getPrimaryColor());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (PreferenceUtil.getInstance(this).getTheme().style != currentTheme) {
            recreate();
        }
    }

    @Override
    public void setTheme(int resId) {
        super.setTheme(resId);

        currentTheme = resId;
    }

    public void setColor(int color) {
        boolean light = MaterialColors.isColorLight(color);

        setLightStatusBar(light);
        setLightNavigationBar(light);
        setDrawUnderStatusBar();

        setStatusBarColor(color);
        setTaskDescriptionColor(color);
        setNavigationBarColor(color);
    }

    private void setDrawUnderStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setAllowDrawUnderStatusBar(getWindow());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow());
        }
    }

    public void setStatusBarColor(int color) {
        View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
        int dark = ThemeUtil.getColorDark(color);

        // KitKat through Lollipop will do this automatically
        if (statusBar != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            statusBar.setBackgroundColor(dark);
            getWindow().setStatusBarColor(getResources().getColor(android.R.color.transparent));
        } else if (statusBar != null) {
            statusBar.setBackgroundColor(color);
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            getWindow().setStatusBarColor(dark);
        }
    }

    public void setTaskDescriptionColor(int color) {
        int solid = MaterialColors.compositeARGBWithAlpha(color, 255);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setTaskDescription(new ActivityManager.TaskDescription(getTitle().toString(), null, solid));
        }
    }

    public void setNavigationBarColor(int color) {
        Window window = getWindow();
        int dark = ThemeUtil.getColorDark(color);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.setNavigationBarColor(dark);
        }
    }

    @SuppressLint("InlinedApi")
    public void setLightStatusBar(boolean enabled) {
        View view = getWindow().getDecorView();

        int flags = enabled
            ? view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
            : view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;

        view.setSystemUiVisibility(flags);
    }

    @SuppressLint("InlinedApi")
    public void setLightNavigationBar(boolean enabled) {
        View view = getWindow().getDecorView();

        int flags = enabled
            ? view.getSystemUiVisibility() | View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
            : view.getSystemUiVisibility() & ~View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;

        view.setSystemUiVisibility(flags);
    }
}
