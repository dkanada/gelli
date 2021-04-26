package com.dkanada.gramophone.activities;

import android.content.Context;
import android.os.Bundle;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.model.User;
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
        overridePendingTransition(0, R.anim.fade_quick);
    }

    @Override
    protected void onResume() {
        super.onResume();

        Context context = this;
        User user = App.getDatabase().userDao().getUser(PreferenceUtil.getInstance(this).getUser());

        if (user == null) {
            NavigationUtil.startLogin(this);
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

                NavigationUtil.startMain(context);
            }

            @Override
            public void onError(Exception exception) {
                NavigationUtil.startLogin(context);
            }
        });
    }
}
