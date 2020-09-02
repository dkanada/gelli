package com.dkanada.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.databinding.ActivityLoginBinding;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.kabouzeid.appthemehelper.ThemeStore;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.system.SystemInfo;
import org.jellyfin.apiclient.model.users.AuthenticationResult;

public class LoginActivity extends AbsBaseActivity implements View.OnClickListener {
    private ActivityLoginBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setDrawUnderStatusbar();
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
        binding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpOnClickListeners() {
        binding.login.setOnClickListener(this);
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
        if (v == binding.login) {
            final Context context = this;
            if (binding.server.getText().toString().trim().length() == 0) {
                Toast.makeText(context, context.getResources().getString(R.string.error_login_empty_addr), Toast.LENGTH_SHORT).show();
                return;
            }

            if (binding.username.getText().toString().trim().length() == 0) {
                Toast.makeText(context, context.getResources().getString(R.string.error_no_username), Toast.LENGTH_SHORT).show();
                return;
            }

            App.getApiClient().ChangeServerLocation(binding.server.getText().toString());
            App.getApiClient().AuthenticateUserAsync(binding.username.getText().toString(), binding.password.getText().toString(), new Response<AuthenticationResult>() {
                @Override
                public void onResponse(AuthenticationResult result) {
                    if (result.getAccessToken() == null) return;
                    check(context, binding.server.getText().toString(), result.getUser().getId(), result.getAccessToken());
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
