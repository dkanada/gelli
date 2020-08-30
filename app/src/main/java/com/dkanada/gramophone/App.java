package com.dkanada.gramophone;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import com.dkanada.gramophone.util.PreferenceUtil;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.shortcuts.DynamicShortcutManager;

import org.jellyfin.apiclient.interaction.AndroidDevice;
import org.jellyfin.apiclient.interaction.ApiClient;
import org.jellyfin.apiclient.interaction.ApiEventListener;
import org.jellyfin.apiclient.interaction.VolleyHttpClient;
import org.jellyfin.apiclient.interaction.device.IDevice;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.logging.AndroidLogger;
import org.jellyfin.apiclient.logging.ILogger;

public class App extends Application {
    private static App app;

    private static ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        app = this;
        apiClient = createApiClient(this);

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this).primaryColorRes(R.color.md_indigo_500).accentColorRes(R.color.md_pink_A400).commit();
        }

        // dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }
    }

    public static ApiClient createApiClient(Context context) {
        String appName = context.getString(R.string.app_name);
        String appVersion = BuildConfig.VERSION_NAME;

        @SuppressLint("HardwareIds")
        String deviceId = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String deviceName = android.os.Build.MODEL;
        String server = PreferenceUtil.getInstance(context).getServer();

        ILogger logger = new AndroidLogger(context.getClass().getName());
        IAsyncHttpClient httpClient = new VolleyHttpClient(logger, context);
        IDevice device = new AndroidDevice(deviceId, deviceName);
        ApiEventListener eventListener = new ApiEventListener();

        return new ApiClient(httpClient, logger, server, appName, appVersion, device, eventListener);
    }

    public static ApiClient getApiClient() {
        return apiClient;
    }

    public static App getInstance() {
        return app;
    }
}
