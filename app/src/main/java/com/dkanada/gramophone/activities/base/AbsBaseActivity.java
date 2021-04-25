package com.dkanada.gramophone.activities.base;

import android.app.AlertDialog;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;

import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.R;

import java.util.List;

public abstract class AbsBaseActivity extends AbsThemeActivity {
    private static final int PERMISSION_REQUEST = 100;

    private List<String> permissions;
    private boolean allowed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        permissions = getPermissionsToRequest();
        allowed = hasPermissions();
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
        if (hasPermissions() != allowed) {
            super.recreate();
        }
    }

    protected List<String> getPermissionsToRequest() {
        return null;
    }

    protected String getPermissionDeniedMessage() {
        return getString(R.string.permissions_denied);
    }

    protected void requestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && permissions != null) {
            requestPermissions(permissions.toArray(new String[0]), PERMISSION_REQUEST);
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
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] results) {
        super.onRequestPermissionsResult(requestCode, permissions, results);

        if (requestCode != PERMISSION_REQUEST) {
            return;
        }

        for (int i = 0; i < permissions.length; i++) {
            String permission = permissions[i];
            int result = results[i];

            if (result == PackageManager.PERMISSION_GRANTED) {
                continue;
            }

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(getPermissionDeniedMessage())
                .setTitle(R.string.permissions_denied)
                .setNegativeButton(R.string.ignore, (dialog, which) -> { })
                .setPositiveButton(R.string.action_settings, (dialog, id) -> NavigationUtil.openSettings(this));

            if (ActivityCompat.shouldShowRequestPermissionRationale(this, permission)) {
                builder.setPositiveButton(R.string.action_grant, (dialog, id) -> requestPermissions());
            }

            builder.show();
        }
    }
}
