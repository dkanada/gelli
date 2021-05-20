package com.dkanada.gramophone.activities;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.base.AbsBaseActivity;
import com.dkanada.gramophone.adapter.SelectAdapter;
import com.dkanada.gramophone.databinding.ActivitySelectBinding;
import com.dkanada.gramophone.model.User;
import com.kabouzeid.appthemehelper.ThemeStore;

import java.util.List;

public class SelectActivity extends AbsBaseActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Context context = this;
        ActivitySelectBinding binding = ActivitySelectBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        List<User> users = App.getDatabase().userDao().getUsers();
        SelectAdapter adapter = new SelectAdapter(this, users);

        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        binding.add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(context, LoginActivity.class));
            }
        });

        setDrawUnderStatusBar();
        setStatusBarColorAuto();

        setNavigationBarColorAuto();
        setTaskDescriptionColorAuto();

        int primaryColor = ThemeStore.primaryColor(this);

        binding.add.setBackgroundColor(primaryColor);
        binding.toolbar.setBackgroundColor(primaryColor);
        setSupportActionBar(binding.toolbar);
    }

    @Override
    public void onPause() {
        super.onPause();
        overridePendingTransition(0, R.anim.fade_quick);
    }
}
