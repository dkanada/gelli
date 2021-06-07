package com.dkanada.gramophone.activities.base;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.ColorInt;
import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.util.NavigationUtil;
import com.kabouzeid.appthemehelper.ATH;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialDialogsUtil;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.Util;

public abstract class AbsThemeActivity extends AppCompatActivity {
    private int currentTheme;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setTheme(PreferenceUtil.getInstance(this).getTheme().style);
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);

        if (!ThemeStore.coloredNavigationBar(this)) {
            ThemeStore.editTheme(this).coloredNavigationBar(true).commit();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // for some reason the recreate method has issues here
        if (PreferenceUtil.getInstance(this).getTheme().style != currentTheme) {
            NavigationUtil.startMain(this);
            overridePendingTransition(0, android.R.anim.fade_out);
            finish();
        }
    }

    @Override
    public void setTheme(int resId) {
        currentTheme = resId;

        super.setTheme(resId);
    }

    protected void setDrawUnderStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setAllowDrawUnderStatusBar(getWindow());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow());
        }
    }

    public void setStatusBarColor(int color) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            final View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
            if (statusBar != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    statusBar.setBackgroundColor(ColorUtil.darkenColor(color));
                    setLightStatusBarAuto(color);
                } else {
                    statusBar.setBackgroundColor(color);
                }
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                getWindow().setStatusBarColor(ColorUtil.darkenColor(color));
                setLightStatusBarAuto(color);
            }
        }
    }

    public void setStatusBarColorAuto() {
        // ignore status bar color because we are darkening the color on our own to support KitKat
        setStatusBarColor(ThemeStore.primaryColor(this));
    }

    public void setTaskDescriptionColor(@ColorInt int color) {
        ATH.setTaskDescriptionColor(this, color);
    }

    public void setTaskDescriptionColorAuto() {
        setTaskDescriptionColor(ThemeStore.primaryColor(this));
    }

    public void setNavigationBarColor(int color) {
        ATH.setNavigationbarColor(this, color);
    }

    public void setNavigationBarColorAuto() {
        setNavigationBarColor(ThemeStore.navigationBarColor(this));
    }

    public void setLightStatusBar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    public void setLightStatusBarAuto(int bgColor) {
        setLightStatusBar(ColorUtil.isColorLight(bgColor));
    }
}
