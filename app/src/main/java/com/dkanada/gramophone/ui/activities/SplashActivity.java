package com.dkanada.gramophone.ui.activities;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.provider.Settings;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;

import org.jellyfin.apiclient.interaction.AndroidCredentialProvider;
import org.jellyfin.apiclient.interaction.ConnectionResult;
import org.jellyfin.apiclient.interaction.EmptyResponse;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.interaction.VolleyHttpClient;
import org.jellyfin.apiclient.interaction.connectionmanager.ConnectionManager;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.logging.AndroidLogger;
import org.jellyfin.apiclient.model.apiclient.ConnectionState;
import org.jellyfin.apiclient.model.logging.ILogger;
import org.jellyfin.apiclient.model.serialization.GsonJsonSerializer;
import org.jellyfin.apiclient.model.serialization.IJsonSerializer;

public class SplashActivity extends AbsBaseActivity {
    public static final String TAG = SplashActivity.class.getSimpleName();

    public AndroidCredentialProvider credentialProvider;
    public ConnectionManager connectionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setDrawUnderStatusbar();

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        IJsonSerializer jsonSerializer = new GsonJsonSerializer();
        ILogger logger = new AndroidLogger(TAG);
        IAsyncHttpClient httpClient = new VolleyHttpClient(logger, this);

        credentialProvider = new AndroidCredentialProvider(jsonSerializer, this, logger);
        connectionManager = App.getConnectionManager(this, jsonSerializer, logger, httpClient);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (detectBatteryOptimization()) {
            showBatteryOptimizationDialog();
        } else {
            login();
        }
    }

    private boolean detectBatteryOptimization() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String packageName = getPackageName();
            PowerManager pm = (PowerManager) getSystemService(POWER_SERVICE);
            return !pm.isIgnoringBatteryOptimizations(packageName);
        }

        return false;
    }

    private void showBatteryOptimizationDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(SplashActivity.this);
        builder.setMessage(R.string.battery_optimizations_message)
                .setTitle(R.string.battery_optimizations_title)
                .setNegativeButton(R.string.ignore, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        login();
                    }
                })
                .setPositiveButton(R.string.disable, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        openPowerSettings(SplashActivity.this);
                    }
                })
                .show();
    }

    private void openPowerSettings(Context context) {
        Intent intent = new Intent();
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setAction(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);
        context.startActivity(intent);
    }

    public void login() {
        if (credentialProvider.GetCredentials().getServers().size() == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        } else {
            final Context context = this;
            connectionManager.Connect(credentialProvider.GetCredentials().getServers().get(0), new Response<ConnectionResult>() {
                @Override
                public void onResponse(ConnectionResult result) {
                    if (result.getState() != ConnectionState.SignedIn) {
                        connectionManager.DeleteServer(credentialProvider.GetCredentials().getServers().get(0).getId(), new EmptyResponse());

                        Intent intent = new Intent(context, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    } else {
                        App.setApiClient(result.getApiClient());

                        Intent intent = new Intent(context, MainActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        context.startActivity(intent);
                    }
                }
            });
        }
    }
}
