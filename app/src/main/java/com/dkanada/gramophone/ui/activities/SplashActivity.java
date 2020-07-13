package com.dkanada.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.helper.NetworkConnectionHelper;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.appthemehelper.ThemeStore;

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

import butterknife.BindView;
import butterknife.ButterKnife;

public class SplashActivity extends AbsBaseActivity implements View.OnClickListener {
    public static final String TAG = SplashActivity.class.getSimpleName();

    public AndroidCredentialProvider credentialProvider;
    public ConnectionManager connectionManager;

    @BindView(R.id.splash_logo)
    ImageView splash_logo;
    @BindView(R.id.retry_connection)
    Button retry_connection;
    @BindView(R.id.text_area)
    LinearLayout text_area;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();

        IJsonSerializer jsonSerializer = new GsonJsonSerializer();
        ILogger logger = new AndroidLogger(TAG);
        IAsyncHttpClient httpClient = new VolleyHttpClient(logger, this);

        credentialProvider = new AndroidCredentialProvider(jsonSerializer, this, logger);
        connectionManager = App.getConnectionManager(this, jsonSerializer, logger, httpClient);

        tryConnect();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, 0);
    }

    private void setUpViews() {
        int primaryColor = ThemeStore.primaryColor(this);

        retry_connection.setBackgroundColor(primaryColor);

        setUpOnClickListeners();
    }

    private void setUpOnClickListeners() {
        retry_connection.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (view == retry_connection) {
            tryConnect();
        }
    }

    public void tryConnect() {
        if (NetworkConnectionHelper.checkNetworkConnection(this)) {
            login();
        } else {
            splash_logo.setVisibility(View.GONE);
            text_area.setVisibility(View.VISIBLE);
        }
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
