package com.dkanada.gramophone.activities;

import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;

import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.databinding.ActivityAboutBinding;
import com.dkanada.gramophone.databinding.CardAboutAppBinding;
import com.dkanada.gramophone.databinding.CardAuthorBinding;
import com.dkanada.gramophone.databinding.CardSpecialThanksBinding;
import com.dkanada.gramophone.databinding.CardSupportDevelopmentBinding;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.base.AbsBaseActivity;

public class AboutActivity extends AbsBaseActivity implements View.OnClickListener {
    private ActivityAboutBinding binding;

    private CardAboutAppBinding aboutBinding;
    private CardAuthorBinding authorBinding;
    private CardSupportDevelopmentBinding supportBinding;
    private CardSpecialThanksBinding thanksBinding;

    private final static String GITHUB = "https://github.com/dkanada/gelli";

    private final static String TWITTER = "https://twitter.com/karimjabouzeid";
    private final static String WEBSITE = "https://github.com/dkanada";

    private final static String TRANSLATE = "https://phonograph.oneskyapp.com/collaboration/project?id=26521";
    private final static String RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.kabouzeid.gramophone";

    private final static String AIDAN_FOLLESTAD_GOOGLE_PLUS = "https://google.com/+AidanFollestad";
    private final static String AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad";

    private final static String MAARTEN_CORPEL_GOOGLE_PLUS = "https://google.com/+MaartenCorpel";

    private final static String ALEKSANDAR_TESIC_GOOGLE_PLUS = "https://google.com/+aleksandartešić";

    private final static String EUGENE_CHEUNG_GITHUB = "https://github.com/arkon";
    private final static String EUGENE_CHEUNG_WEBSITE = "https://echeung.me/";

    private final static String ADRIAN_TWITTER = "https://twitter.com/froschgames";
    private final static String ADRIAN_WEBSITE = "https://froschgames.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aboutBinding = CardAboutAppBinding.bind(findViewById(R.id.about_layout));
        authorBinding = CardAuthorBinding.bind(findViewById(R.id.author_layout));
        supportBinding = CardSupportDevelopmentBinding.bind(findViewById(R.id.support_layout));
        thanksBinding = CardSpecialThanksBinding.bind(findViewById(R.id.thanks_layout));

        setDrawUnderStatusBar();
        setStatusBarColorAuto();

        setNavigationBarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        binding.toolbar.setBackgroundColor(PreferenceUtil.getInstance(this).getPrimaryColor());
        setSupportActionBar(binding.toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpAppVersion() {
        aboutBinding.appVersion.setText(getCurrentVersionName(this));
    }

    private void setUpOnClickListeners() {
        authorBinding.followOnTwitter.setOnClickListener(this);
        aboutBinding.appSource.setOnClickListener(this);
        authorBinding.visitWebsite.setOnClickListener(this);
        supportBinding.reportBugs.setOnClickListener(this);
        supportBinding.translate.setOnClickListener(this);
        supportBinding.rateOnGooglePlay.setOnClickListener(this);
        supportBinding.donate.setOnClickListener(this);
        thanksBinding.aidanFollestadGooglePlus.setOnClickListener(this);
        thanksBinding.aidanFollestadGitHub.setOnClickListener(this);
        thanksBinding.maartenCorpelGooglePlus.setOnClickListener(this);
        thanksBinding.aleksandarTesicGooglePlus.setOnClickListener(this);
        thanksBinding.eugeneCheungGitHub.setOnClickListener(this);
        thanksBinding.eugeneCheungWebsite.setOnClickListener(this);
        thanksBinding.adrianTwitter.setOnClickListener(this);
        thanksBinding.adrianWebsite.setOnClickListener(this);
    }

    private static String getCurrentVersionName(@NonNull final Context context) {
        try {
            return context.getPackageManager().getPackageInfo(context.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }

        return "Unknown";
    }

    @Override
    public void onClick(View v) {
        if (v == authorBinding.followOnTwitter) {
            NavigationUtil.openUrl(this, TWITTER);
        } else if (v == aboutBinding.appSource) {
            NavigationUtil.openUrl(this, GITHUB);
        } else if (v == authorBinding.visitWebsite) {
            NavigationUtil.openUrl(this, WEBSITE);
        } else if (v == supportBinding.reportBugs) {
            NavigationUtil.openUrl(this, GITHUB);
        } else if (v == supportBinding.translate) {
            NavigationUtil.openUrl(this, TRANSLATE);
        } else if (v == supportBinding.rateOnGooglePlay) {
            NavigationUtil.openUrl(this, RATE_ON_GOOGLE_PLAY);
        } else if (v == supportBinding.donate) {
            NavigationUtil.openUrl(this, RATE_ON_GOOGLE_PLAY);
        } else if (v == thanksBinding.aidanFollestadGooglePlus) {
            NavigationUtil.openUrl(this, AIDAN_FOLLESTAD_GOOGLE_PLUS);
        } else if (v == thanksBinding.aidanFollestadGitHub) {
            NavigationUtil.openUrl(this, AIDAN_FOLLESTAD_GITHUB);
        } else if (v == thanksBinding.maartenCorpelGooglePlus) {
            NavigationUtil.openUrl(this, MAARTEN_CORPEL_GOOGLE_PLUS);
        } else if (v == thanksBinding.aleksandarTesicGooglePlus) {
            NavigationUtil.openUrl(this, ALEKSANDAR_TESIC_GOOGLE_PLUS);
        } else if (v == thanksBinding.eugeneCheungGitHub) {
            NavigationUtil.openUrl(this, EUGENE_CHEUNG_GITHUB);
        } else if (v == thanksBinding.eugeneCheungWebsite) {
            NavigationUtil.openUrl(this, EUGENE_CHEUNG_WEBSITE);
        } else if (v == thanksBinding.adrianTwitter) {
            NavigationUtil.openUrl(this, ADRIAN_TWITTER);
        } else if (v == thanksBinding.adrianWebsite) {
            NavigationUtil.openUrl(this, ADRIAN_WEBSITE);
        }
    }
}
