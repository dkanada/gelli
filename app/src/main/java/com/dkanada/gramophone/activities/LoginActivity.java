package com.dkanada.gramophone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Toast;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.databinding.ActivityLoginBinding;
import com.dkanada.gramophone.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.model.User;
import com.dkanada.gramophone.util.PreferenceUtil;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.system.SystemInfo;
import org.jellyfin.apiclient.model.users.AuthenticationResult;

public class LoginActivity extends AbsBaseActivity implements View.OnClickListener {
    private ActivityLoginBinding binding;
    private int primaryColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setUpViews();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.fade_quick);
    }

    private void setUpViews() {
        primaryColor = PreferenceUtil.getInstance(this).getPrimaryColor();

        setUpToolbar();
        setUpOnClickListeners();

        binding.login.setBackgroundColor(primaryColor);
    }

    private void setUpToolbar() {
        binding.toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(binding.toolbar);
    }

    private void setUpOnClickListeners() {
        binding.login.setOnClickListener(this);
        binding.select.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if (v == binding.select) {
            startActivity(new Intent(this, SelectActivity.class));
            return;
        }

        String username = binding.username.getText().toString();
        String password = binding.password.getText().toString();
        String server = binding.server.getText().toString();
        Context context = this;

        if (TextUtils.isEmpty(username)) {
            Toast.makeText(this, getResources().getString(R.string.error_empty_username), Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(server)) {
            Toast.makeText(this, getResources().getString(R.string.error_empty_server), Toast.LENGTH_SHORT).show();
            return;
        }

        binding.login.setEnabled(false);
        binding.login.setBackgroundColor(getResources().getColor(android.R.color.darker_gray));

        App.getApiClient().ChangeServerLocation(server);
        App.getApiClient().AuthenticateUserAsync(username, password, new Response<AuthenticationResult>() {
            @Override
            public void onResponse(AuthenticationResult authenticationResult) {
                if (authenticationResult.getAccessToken() != null) {
                    check(authenticationResult, server);
                }
            }

            @Override
            public void onError(Exception exception) {
                binding.login.setEnabled(true);
                binding.login.setBackgroundColor(primaryColor);

                if (exception.getMessage().contains("AuthFailureError")) {
                    Toast.makeText(context, context.getResources().getString(R.string.error_login_credentials), Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(context, context.getResources().getString(R.string.error_unreachable_server), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void check(AuthenticationResult authenticationResult, String server) {
        App.getApiClient().GetSystemInfoAsync(new Response<SystemInfo>() {
            @Override
            public void onResponse(SystemInfo result) {
                if (result.getVersion().charAt(0) == '1') {
                    User user = new User(authenticationResult, server);

                    App.getDatabase().userDao().insertUser(user);
                    PreferenceUtil.getInstance(LoginActivity.this).setServer(user.server);
                    PreferenceUtil.getInstance(LoginActivity.this).setUser(user.id);

                    Intent intent = new Intent(LoginActivity.this, SplashActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                } else {
                    binding.login.setEnabled(true);
                    binding.login.setBackgroundColor(primaryColor);

                    Toast.makeText(LoginActivity.this, getResources().getString(R.string.error_version), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
}
