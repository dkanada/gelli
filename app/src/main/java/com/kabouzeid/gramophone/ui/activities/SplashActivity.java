package com.kabouzeid.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.ui.activities.base.AbsBaseActivity;

import org.jellyfin.apiclient.interaction.AndroidCredentialProvider;
import org.jellyfin.apiclient.interaction.ConnectionResult;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.interaction.VolleyHttpClient;
import org.jellyfin.apiclient.interaction.connectionmanager.ConnectionManager;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.logging.AndroidLogger;
import org.jellyfin.apiclient.model.logging.ILogger;
import org.jellyfin.apiclient.model.serialization.GsonJsonSerializer;
import org.jellyfin.apiclient.model.serialization.IJsonSerializer;

import butterknife.ButterKnife;

public class SplashActivity extends AbsBaseActivity {
    public static final String TAG = SplashActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        IJsonSerializer jsonSerializer = new GsonJsonSerializer();
        ILogger logger = new AndroidLogger(TAG);
        IAsyncHttpClient httpClient = new VolleyHttpClient(logger, this);

        AndroidCredentialProvider credentialProvider = new AndroidCredentialProvider(jsonSerializer, this, logger);
        if (credentialProvider.GetCredentials().getServers().size() == 0) {
            Intent intent = new Intent(this, LoginActivity.class);
            startActivity(intent);
        } else {
            final Context context = this;
            ConnectionManager connectionManager = App.getConnectionManager(this, jsonSerializer, logger, httpClient);
            connectionManager.Connect(credentialProvider.GetCredentials().getServers().get(0), new Response<ConnectionResult>() {
                @Override
                public void onResponse(ConnectionResult result) {
                    //if (result.getState() != ConnectionState.SignedIn) return;
                    App.setApiClient(result.getApiClient());
                    context.startActivity(new Intent(context, MainActivity.class));
                }
            });
        }
    }
}
