package com.dkanada.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;
import com.google.android.material.snackbar.Snackbar;
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
import org.jellyfin.apiclient.model.system.SystemInfo;
import org.jellyfin.apiclient.model.users.AuthenticationResult;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AbsBaseActivity implements View.OnClickListener {
    public String TAG = LoginActivity.class.getSimpleName();
    public AndroidCredentialProvider credentialProvider;

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
        checkNetworkConnection();
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
        if (v == login && checkNetworkConnection()) {
            String mUsername = username.getText().toString().trim();
            String mPassword = password.getText().toString().trim();
            String mServer = server.getText().toString().trim();

            if (validate(mUsername, mPassword, mServer)) {
                final Context context = this;
                IJsonSerializer jsonSerializer = new GsonJsonSerializer();
                ILogger logger = new AndroidLogger(TAG);
                IAsyncHttpClient httpClient = new VolleyHttpClient(logger, this);

                credentialProvider = new AndroidCredentialProvider(jsonSerializer, this, logger);
                ConnectionManager connectionManager = App.getConnectionManager(context, jsonSerializer, logger, httpClient);
                connectionManager.Connect(server.getText().toString(), new Response<ConnectionResult>() {
                    @Override
                    public void onResponse(ConnectionResult result) {
                        App.setApiClient(result.getApiClient());
                        ServerCredentials serverCredentials = new ServerCredentials();
                        List<ServerInfo> servers = result.getServers();

                        if (servers.size() < 1) {
                            Toast.makeText(context, R.string.server_unreachable, Toast.LENGTH_SHORT).show();
                            return;
                        }

                        serverCredentials.AddOrUpdateServer(servers.get(0));
                        App.getApiClient().AuthenticateUserAsync(username.getText().toString(), password.getText().toString(), new Response<AuthenticationResult>() {
                            @Override
                            public void onResponse(AuthenticationResult result) {
                                if (result.getAccessToken() == null) return;
                                check(context, serverCredentials, result);
                            }

                            @Override
                            public void onError(Exception exception) {
                                Log.e(TAG, exception.getMessage());
                                Toast.makeText(LoginActivity.this, R.string.authentication_failed, Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });
            }
        }
    }

    public boolean validate(String mUsername, String mPassword, String mServerAddres) {
        boolean isValid = true;

        if (TextUtils.isEmpty(mUsername)) {
            username.setError(getString(R.string.error_credential_validation));
            isValid = false;
        }

        if (TextUtils.isEmpty(mPassword)) {
            password.setError(getString(R.string.error_credential_validation));
            isValid = false;
        }

        if (TextUtils.isEmpty(mServerAddres)) {
            server.setError(getString(R.string.error_credential_validation));
            isValid = false;
        }

        return isValid;
    }

    public void check(Context context, ServerCredentials serverCredentials, AuthenticationResult authenticationResult) {
        App.getApiClient().GetSystemInfoAsync(new Response<SystemInfo>() {
            @Override
            public void onResponse(SystemInfo result) {
                if (Integer.parseInt(result.getVersion().substring(0, 1)) == 1) {
                    serverCredentials.GetServer(authenticationResult.getServerId()).setAccessToken(authenticationResult.getAccessToken());
                    credentialProvider.SaveCredentials(serverCredentials);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private boolean checkNetworkConnection() {
        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        boolean isConnected = connectivityManager.getActiveNetworkInfo() != null && connectivityManager.getActiveNetworkInfo().isConnected();

        if (!isConnected) {
            Snackbar snackbar = Snackbar.make(findViewById(android.R.id.content), R.string.No_network_connection_available, Snackbar.LENGTH_LONG);
            snackbar.show();
            return false;
        }
        return true;
    }
}
