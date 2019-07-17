package com.kabouzeid.gramophone;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import androidx.annotation.NonNull;

import com.anjlab.android.iab.v3.BillingProcessor;
import com.anjlab.android.iab.v3.TransactionDetails;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.gramophone.shortcuts.DynamicShortcutManager;

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

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class App extends Application {

    public static final String GOOGLE_PLAY_LICENSE_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAjMeADN5Ffnt/ml5SYxNPCn8kGcOYGpHEfNSCts99vVxqmCn6C01E94c17j7rUK2aeHur5uxphZylzopPlQ8P8l1fqty0GPUNRSo18FCJzfGH8HZAwZYOcnRFPaXdaq3InyFJhBiODh2oeAcVK/idH6QraQ4r9HIlzigAg6lgwzxl2wJKDh7X/GMdDntCyzDh8xDQ0wIawFgvgojHwqh2Ci8Gnq6EYRwPA9yHiIIksT8Q30QyM5ewl5QcnWepsls7enNqeHarhpmSibRUDgCsxHoOpny7SyuvZvUI3wuLckDR0ds9hrt614scHHqDOBp/qWCZiAgOPVAEQcURbV09qQIDAQAB";
    public static final String PRO_VERSION_PRODUCT_ID = "pro_version";

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
