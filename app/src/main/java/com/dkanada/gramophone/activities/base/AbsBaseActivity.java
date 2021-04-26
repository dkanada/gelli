package com.dkanada.gramophone.activities.base;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.PowerManager;
import android.provider.Settings;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;

import com.dkanada.gramophone.activities.MainActivity;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.R;
import com.google.android.material.snackbar.Snackbar;
import com.kabouzeid.appthemehelper.ThemeStore;

import java.util.ArrayList;
import java.util.List;

public abstract class AbsBaseActivity extends AbsThemeActivity {
    private static final int PERMISSION_REQUEST = 100;

    private List<String> permissions;
    private boolean allowed;

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissions = getPermissionRequest();
        allowed = checkPermissions();
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!getClass().isInstance(MainActivity.class)) {
            return;
        }

        AlertDialog.Builder builder = new AlertDialog.Builder(this)
            .setNegativeButton(R.string.ignore, (dialog, id) -> showWarning());

        if (!checkBatteryOptimization()) {
            builder.setMessage(R.string.battery_optimizations_message)
                .setTitle(R.string.battery_optimizations_title)
                .setPositiveButton(R.string.disable, (dialog, id) -> requestBatteryOptimization());

            new Handler().postDelayed(builder::show, 2000);
        } else if (permissions.size() != 0 && ActivityCompat.shouldShowRequestPermissionRationale(this, permissions.get(0))) {
            builder.setMessage(getPermissionMessage())
                .setTitle(R.string.permissions_denied)
                .setPositiveButton(R.string.action_grant, (dialog, id) -> requestPermissions());

            new Handler().postDelayed(builder::show, 2000);
        } else if (!checkPermissions()) {
            builder.setMessage(getPermissionMessage())
                .setTitle(R.string.permissions_denied)
                .setPositiveButton(R.string.action_settings, (dialog, id) -> NavigationUtil.openSettings(this));

            new Handler().postDelayed(builder::show, 2000);
        }
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    protected void onResume() {
        super.onResume();

        if (checkPermissions() != allowed) {
            super.recreate();
        }
    }

    protected View getPermissionWindow() {
        return getWindow().getDecorView();
    }

    protected List<String> getPermissionRequest() {
        return new ArrayList<>();
    }

    protected String getPermissionMessage() {
        return getString(R.string.permissions_denied);
    }

    private void showWarning() {
        Snackbar.make(getPermissionWindow(), getPermissionMessage(), Snackbar.LENGTH_SHORT)
            .setAction(R.string.ignore, view -> { })
            .setActionTextColor(ThemeStore.accentColor(this))
            .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestBatteryOptimization() {
        Intent intent = new Intent();

        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);

        startActivity(intent);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkBatteryOptimization() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);

        return pm.isIgnoringBatteryOptimizations(packageName);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void requestPermissions() {
        requestPermissions(permissions.toArray(new String[0]), PERMISSION_REQUEST);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean checkPermissions() {
        for (String permission : permissions) {
            if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                return false;
            }
        }

        return true;
    }

    @Override
    @TargetApi(Build.VERSION_CODES.M)
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode != PERMISSION_REQUEST) {
            return;
        }

        for (int result : results) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                showWarning();
                return;
            }
        }
    }
}
