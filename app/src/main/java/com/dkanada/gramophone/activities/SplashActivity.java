package com.dkanada.gramophone.activities;

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
import com.dkanada.gramophone.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.model.Server;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.PreferenceUtil;

import org.jellyfin.apiclient.interaction.EmptyResponse;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.session.ClientCapabilities;
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
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.fade_delay);
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
                .setPositiveButton(R.string.disable, (dialog, id) -> openPowerSettings())
                .show();
    }

    @RequiresApi(api = Build.VERSION_CODES.M)
    private void openPowerSettings() {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        startActivity(intent);
    }

    public void login() {
        if (PreferenceUtil.getInstance(this).getServer().isEmpty()) {
            NavigationUtil.goToLogin(this);
        } else {
            final Context context = this;
            Server server = App.getDatabase().serverDao().getServer(PreferenceUtil.getInstance(this).getServer());

            App.getApiClient().ChangeServerLocation(server.url);
            App.getApiClient().SetAuthenticationInfo(server.token, server.user);
            App.getApiClient().GetSystemInfoAsync(new Response<SystemInfo>() {
                @Override
                public void onResponse(SystemInfo result) {
                    ClientCapabilities clientCapabilities = new ClientCapabilities();
                    clientCapabilities.setSupportsMediaControl(true);
                    clientCapabilities.setSupportsPersistentIdentifier(true);

                    App.getApiClient().ensureWebSocket();
                    App.getApiClient().ReportCapabilities(clientCapabilities, new EmptyResponse());

                    NavigationUtil.goToMain(context);
                }

                @Override
                public void onError(Exception exception) {
                    NavigationUtil.goToLogin(context);
                }
            });
        }
    }
}
