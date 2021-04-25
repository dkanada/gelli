package com.dkanada.gramophone.activities.base;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.R;
import com.google.android.material.snackbar.Snackbar;

public abstract class AbsBaseActivity extends AbsThemeActivity {
    private static final int PERMISSION_REQUEST = 100;

    private boolean hadPermissions;
    private String[] permissions;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissions = getPermissionsToRequest();
        hadPermissions = hasPermissions();
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);

        if (!hasPermissions()) {
            requestPermissions();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (hasPermissions() != hadPermissions) {
            super.recreate();
        }
    }

    protected String[] getPermissionsToRequest() {
        return null;
    }

    protected View getSnackBarContainer() {
        return getWindow().getDecorView();
    }

    protected String getPermissionDeniedMessage() {
        return getString(R.string.permissions_denied);
    }

    protected void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            requestPermissions(permissions, PERMISSION_REQUEST);
        }
    }

    protected boolean hasPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            for (String permission : permissions) {
                if (checkSelfPermission(permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }

        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST) {
            for (int grantResult : grantResults) {
                if (grantResult != PackageManager.PERMISSION_GRANTED) {
                    if (ActivityCompat.shouldShowRequestPermissionRationale(AbsBaseActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE)) {
                        Snackbar.make(getSnackBarContainer(), getPermissionDeniedMessage(), Snackbar.LENGTH_SHORT)
                            .setAction(R.string.action_grant, view -> requestPermissions())
                            .setActionTextColor(ThemeStore.accentColor(this))
                            .show();
                    } else {
                        Snackbar.make(getSnackBarContainer(), getPermissionDeniedMessage(), Snackbar.LENGTH_SHORT)
                            .setAction(R.string.action_settings, view -> NavigationUtil.openSettings(this))
                            .setActionTextColor(ThemeStore.accentColor(this))
                            .show();
                    }

                    return;
                }
            }

            super.recreate();
        }
    }
}
