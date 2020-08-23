package com.dkanada.gramophone.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import androidx.annotation.RequiresApi;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.util.PreferenceUtil;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.system.SystemInfo;

public class SplashActivity extends AbsBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && detectBatteryOptimization()) {
            showBatteryOptimizationDialog();
        } else {
            login();
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private boolean detectBatteryOptimization() {
        String packageName = getPackageName();
        PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
        return !pm.isIgnoringBatteryOptimizations(packageName);
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void showBatteryOptimizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setMessage(R.string.battery_optimizations_message)
                .setTitle(R.string.battery_optimizations_title)
                .setNegativeButton(R.string.ignore, (dialog, id) -> login())
                .setPositiveButton(R.string.disable, (dialog, id) -> openPowerSettings(SplashActivity.this))
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openPowerSettings(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
    }

    public void login() {
        if (PreferenceUtil.getInstance(this).getToken() == null) {
            launchLoginActivity();
        } else {
            final Context context = this;

            App.getApiClient().ChangeServerLocation(PreferenceUtil.getInstance(this).getServer());
            App.getApiClient().SetAuthenticationInfo(PreferenceUtil.getInstance(this).getToken(), PreferenceUtil.getInstance(this).getUser());
            App.getApiClient().GetSystemInfoAsync(new Response<SystemInfo>() {
                @Override
                public void onResponse(SystemInfo result) {
                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                }

                @Override
                public void onError(Exception exception) {
                    launchLoginActivity();
                }
            });
        }
    }

    private void launchLoginActivity() {
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}
