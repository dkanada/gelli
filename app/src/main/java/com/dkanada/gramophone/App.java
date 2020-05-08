package com.dkanada.gramophone;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.shortcuts.DynamicShortcutManager;

import org.jellyfin.apiclient.interaction.AndroidConnectionManager;
import org.jellyfin.apiclient.interaction.AndroidDevice;
import org.jellyfin.apiclient.interaction.ApiClient;
import org.jellyfin.apiclient.interaction.ApiEventListener;
import org.jellyfin.apiclient.interaction.connectionmanager.ConnectionManager;
import org.jellyfin.apiclient.interaction.device.IDevice;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.model.logging.ILogger;
import org.jellyfin.apiclient.model.serialization.IJsonSerializer;
import org.jellyfin.apiclient.model.session.ClientCapabilities;

public class App extends Application {
    private static App app;

    private static ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;

        // default theme
        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this).primaryColorRes(R.color.md_indigo_500).accentColorRes(R.color.md_pink_A400).commit();
        }

        // dynamic shortcuts
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }
    }

    public static ConnectionManager getConnectionManager(Context context, IJsonSerializer jsonSerializer, ILogger logger, IAsyncHttpClient httpClient) {
        String appName = context.getString(R.string.app_name);
        String appVersion = BuildConfig.VERSION_NAME;

        IDevice device = new AndroidDevice(context);
        ClientCapabilities capabilities = new ClientCapabilities();
        ApiEventListener eventListener = new ApiEventListener();

        return new AndroidConnectionManager(context, jsonSerializer, logger, httpClient, appName, appVersion, device, capabilities, eventListener);
    }

    public static ApiClient getApiClient() {
        return apiClient;
    }

    public static void setApiClient(ApiClient client) {
        apiClient = client;
    }

    public static App getInstance() {
        return app;
    }
}
