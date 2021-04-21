package com.dkanada.gramophone;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.provider.Settings;

import androidx.room.Room;

import com.dkanada.gramophone.database.JellyDatabase;
import com.dkanada.gramophone.helper.EventListener;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.views.shortcuts.DynamicShortcutManager;
import com.melegy.redscreenofdeath.RedScreenOfDeath;

import org.jellyfin.apiclient.interaction.AndroidDevice;
import org.jellyfin.apiclient.interaction.ApiClient;
import org.jellyfin.apiclient.interaction.VolleyHttpClient;
import org.jellyfin.apiclient.interaction.device.IDevice;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.logging.AndroidLogger;
import org.jellyfin.apiclient.logging.ILogger;

public class App extends Application {
    private static App app;

    private static JellyDatabase database;
    private static ApiClient apiClient;

    @Override
    public void onCreate() {
        super.onCreate();

        if (BuildConfig.DEBUG) {
            RedScreenOfDeath.init(this);
        }

        app = this;
        database = createDatabase(this);
        apiClient = createApiClient(this);

        if (database.userDao().getUsers().size() == 0) {
            PreferenceUtil.getInstance(this).setServer(null);
            PreferenceUtil.getInstance(this).setUser(null);
        }

        if (!ThemeStore.isConfigured(this, 1)) {
            ThemeStore.editTheme(this).primaryColorRes(R.color.md_indigo_500).accentColorRes(R.color.md_pink_A400).commit();
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N_MR1) {
            new DynamicShortcutManager(this).initDynamicShortcuts();
        }
    }

    public static JellyDatabase createDatabase(Context context) {
        return Room.databaseBuilder(context, JellyDatabase.class, "database")
                .allowMainThreadQueries()
                .addMigrations(JellyDatabase.Migration2)
                .addMigrations(JellyDatabase.Migration3)
                .addMigrations(JellyDatabase.Migration4)
                .build();
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
        EventListener eventListener = new EventListener();

        return new ApiClient(httpClient, logger, server, appName, appVersion, device, eventListener);
    }

    public static JellyDatabase getDatabase() {
        return database;
    }

    public static ApiClient getApiClient() {
        return apiClient;
    }

    public static App getInstance() {
        return app;
    }
}
