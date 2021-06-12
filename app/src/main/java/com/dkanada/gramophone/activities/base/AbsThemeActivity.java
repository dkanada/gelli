package com.dkanada.gramophone.activities.base;

import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;

import com.kabouzeid.appthemehelper.ATH;
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
        setColor(PreferenceUtil.getInstance(this).getPrimaryColor());
        MaterialDialogsUtil.updateMaterialDialogsThemeSingleton(this);
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
        boolean light = ColorUtil.isColorLight(color);

        setLightStatusBar(light);
        setLightNavigationBar(light);
        setDrawUnderStatusBar();

        setStatusBarColor(color);
        setTaskDescriptionColor(color);
        setNavigationBarColor(color);
    }

    protected void setDrawUnderStatusBar() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            Util.setAllowDrawUnderStatusBar(getWindow());
        } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Util.setStatusBarTranslucent(getWindow());
        }
    }

    public void setStatusBarColor(int color) {
        View statusBar = getWindow().getDecorView().getRootView().findViewById(R.id.status_bar);
        int dark = ColorUtil.darkenColor(color);

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
        ATH.setTaskDescriptionColor(this, color);
    }

    public void setNavigationBarColor(int color) {
        ATH.setNavigationbarColor(this, color);
    }

    public void setLightStatusBar(boolean enabled) {
        ATH.setLightStatusbar(this, enabled);
    }

    public void setLightNavigationBar(boolean enabled) {
        ATH.setLightNavigationbar(this, enabled);
    }
}
