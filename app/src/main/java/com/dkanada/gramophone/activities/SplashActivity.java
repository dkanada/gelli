package com.dkanada.gramophone.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.model.User;
import com.dkanada.gramophone.service.LoginService;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.List;

public class SplashActivity extends AbsBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_splash);
        setDrawUnderStatusBar();

        setStatusBarColorAuto();
        setNavigationBarColorAuto();
        setTaskDescriptionColorAuto();
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.fade_delay);
    }

    @Override
    protected void onResume() {
        super.onResume();

        User user = App.getDatabase().userDao().getUser(PreferenceUtil.getInstance(this).getUser());
        List<User> available = App.getDatabase().userDao().getUsers();

        if (user == null && available.size() != 0) {
            NavigationUtil.startSelect(this);
        } else if (user == null) {
            NavigationUtil.startLogin(this);
        } else {
            startService(new Intent(this, LoginService.class));
            new Handler().postDelayed(() -> NavigationUtil.startMain(this), 1000);
        }
    }
}
