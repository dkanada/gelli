package com.dkanada.gramophone.service;

import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.IBinder;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.BuildConfig;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.User;
import com.dkanada.gramophone.util.PreferenceUtil;

import org.jellyfin.apiclient.interaction.EmptyResponse;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.session.ClientCapabilities;
import org.jellyfin.apiclient.model.system.SystemInfo;

public class LoginService extends Service {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final String STATE_POLLING = PACKAGE_NAME + ".unknown";
    public static final String STATE_ONLINE = PACKAGE_NAME + ".online";
    public static final String STATE_OFFLINE = PACKAGE_NAME + ".offline";

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        sendBroadcast(new Intent(STATE_POLLING));
        authenticate();

        return super.onStartCommand(intent, flags, startId);
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void authenticate() {
        User user = App.getDatabase().userDao().getUser(PreferenceUtil.getInstance(this).getUser());
        Context context = this;

        if (user == null) {
            Toast.makeText(this, context.getResources().getString(R.string.error_unexpected), Toast.LENGTH_SHORT).show();
            return;
        }

        App.getApiClient().ChangeServerLocation(user.server);
        App.getApiClient().SetAuthenticationInfo(user.token, user.id);
        App.getApiClient().GetSystemInfoAsync(new Response<SystemInfo>() {
            @Override
            public void onResponse(SystemInfo result) {
                ClientCapabilities clientCapabilities = new ClientCapabilities();
                clientCapabilities.setSupportsMediaControl(true);
                clientCapabilities.setSupportsPersistentIdentifier(true);

                App.getApiClient().ensureWebSocket();
                App.getApiClient().ReportCapabilities(clientCapabilities, new EmptyResponse());

                sendBroadcast(new Intent(STATE_ONLINE));
            }

            @Override
            public void onError(Exception exception) {
                sendBroadcast(new Intent(STATE_OFFLINE));
            }
        });
    }
}
