package com.dkanada.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;
import com.kabouzeid.appthemehelper.ThemeStore;

import org.jellyfin.apiclient.interaction.AndroidCredentialProvider;
import org.jellyfin.apiclient.interaction.ConnectionResult;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.interaction.VolleyHttpClient;
import org.jellyfin.apiclient.interaction.connectionmanager.ConnectionManager;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.logging.AndroidLogger;
import org.jellyfin.apiclient.model.apiclient.ServerCredentials;
import org.jellyfin.apiclient.model.apiclient.ServerInfo;
import org.jellyfin.apiclient.model.logging.ILogger;
import org.jellyfin.apiclient.model.serialization.GsonJsonSerializer;
import org.jellyfin.apiclient.model.serialization.IJsonSerializer;
import org.jellyfin.apiclient.model.users.AuthenticationResult;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AbsBaseActivity implements View.OnClickListener {
    public static final String TAG = SplashActivity.class.getSimpleName();

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.username)
    EditText username;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.server)
    EditText server;
    @BindView(R.id.login)
    Button login;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpOnClickListeners() {
        login.setOnClickListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onClick(View v) {
        if (v == login) {
            final Context context = this;
            IJsonSerializer jsonSerializer = new GsonJsonSerializer();
            ILogger logger = new AndroidLogger(TAG);
            IAsyncHttpClient httpClient = new VolleyHttpClient(logger, this);

            AndroidCredentialProvider credentialProvider = new AndroidCredentialProvider(jsonSerializer, this, logger);
            ConnectionManager connectionManager = App.getConnectionManager(context, jsonSerializer, logger, httpClient);
            connectionManager.Connect(server.getText().toString(), new Response<ConnectionResult>() {
                @Override
                public void onResponse(ConnectionResult result) {
                    App.setApiClient(result.getApiClient());
                    ServerCredentials serverCredentials = new ServerCredentials();
                    List<ServerInfo> servers = result.getServers();

                    if (servers.size() < 1) {
                        return;
                    }

                    serverCredentials.AddOrUpdateServer(servers.get(0));
                    App.getApiClient().AuthenticateUserAsync(username.getText().toString(), password.getText().toString(), new Response<AuthenticationResult>() {
                        @Override
                        public void onResponse(AuthenticationResult result) {
                            if (result.getAccessToken() == null) return;
                            serverCredentials.GetServer(result.getServerId()).setAccessToken(result.getAccessToken());
                            credentialProvider.SaveCredentials(serverCredentials);

                            Intent intent = new Intent(context, MainActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            context.startActivity(intent);
                        }
                    });
                }
            });
        }
    }
}
