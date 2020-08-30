package com.dkanada.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
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
import com.dkanada.gramophone.util.PreferenceUtil;
import com.kabouzeid.appthemehelper.ThemeStore;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.system.SystemInfo;
import org.jellyfin.apiclient.model.users.AuthenticationResult;

import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends AbsBaseActivity implements View.OnClickListener {
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
            if (server.getText().toString().trim().length() == 0) {
                Toast.makeText(context, context.getResources().getString(R.string.error_login_empty_addr), Toast.LENGTH_SHORT).show();
                return;
            }

            if (username.getText().toString().trim().length() == 0) {
                Toast.makeText(context, context.getResources().getString(R.string.error_no_username), Toast.LENGTH_SHORT).show();
                return;
            }

            App.getApiClient().ChangeServerLocation(server.getText().toString());
            App.getApiClient().AuthenticateUserAsync(username.getText().toString(), password.getText().toString(), new Response<AuthenticationResult>() {
                @Override
                public void onResponse(AuthenticationResult result) {
                    if (result.getAccessToken() == null) return;
                    check(context, server.getText().toString(), result.getUser().getId(), result.getAccessToken());
                }

                @Override
                public void onError(Exception exception) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_login_credentials), Toast.LENGTH_SHORT).show();
                }
            });
        }
    }

    public void check(Context context, String server, String user, String token) {
        App.getApiClient().GetSystemInfoAsync(new Response<SystemInfo>() {
            @Override
            public void onResponse(SystemInfo result) {
                if (Integer.parseInt(result.getVersion().substring(0, 1)) == 1) {
                    PreferenceUtil.getInstance(context).setServer(server);
                    PreferenceUtil.getInstance(context).setUser(user);
                    PreferenceUtil.getInstance(context).setToken(token);

                    Intent intent = new Intent(context, MainActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    context.startActivity(intent);
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
