package com.dkanada.gramophone.ui.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.databinding.ActivityAboutBinding;
import com.dkanada.gramophone.databinding.CardAboutAppBinding;
import com.dkanada.gramophone.databinding.CardAuthorBinding;
import com.dkanada.gramophone.databinding.CardSpecialThanksBinding;
import com.dkanada.gramophone.databinding.CardSupportDevelopmentBinding;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.ui.activities.base.AbsBaseActivity;

@SuppressWarnings("FieldCanBeLocal")
public class AboutActivity extends AbsBaseActivity implements View.OnClickListener {
    ActivityAboutBinding binding;

    CardAboutAppBinding aboutBinding;
    CardAuthorBinding authorBinding;
    CardSupportDevelopmentBinding supportBinding;
    CardSpecialThanksBinding thanksBinding;

    private static String GITHUB = "https://github.com/dkanada/gelli";

    private static String TWITTER = "https://twitter.com/karimjabouzeid";
    private static String WEBSITE = "https://github.com/dkanada";

    private static String TRANSLATE = "https://phonograph.oneskyapp.com/collaboration/project?id=26521";
    private static String RATE_ON_GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=com.kabouzeid.gramophone";

    private static String AIDAN_FOLLESTAD_GOOGLE_PLUS = "https://google.com/+AidanFollestad";
    private static String AIDAN_FOLLESTAD_GITHUB = "https://github.com/afollestad";

    private static String MAARTEN_CORPEL_GOOGLE_PLUS = "https://google.com/+MaartenCorpel";

    private static String ALEKSANDAR_TESIC_GOOGLE_PLUS = "https://google.com/+aleksandartešić";

    private static String EUGENE_CHEUNG_GITHUB = "https://github.com/arkon";
    private static String EUGENE_CHEUNG_WEBSITE = "https://echeung.me/";

    private static String ADRIAN_TWITTER = "https://twitter.com/froschgames";
    private static String ADRIAN_WEBSITE = "https://froschgames.com/";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityAboutBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        aboutBinding = CardAboutAppBinding.bind(findViewById(R.id.about_layout));
        authorBinding = CardAuthorBinding.bind(findViewById(R.id.author_layout));
        supportBinding = CardSupportDevelopmentBinding.bind(findViewById(R.id.support_layout));
        thanksBinding = CardSpecialThanksBinding.bind(findViewById(R.id.thanks_layout));

        setDrawUnderStatusbar();
        setStatusbarColorAuto();

        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        setUpViews();
    }

    private void setUpViews() {
        setUpToolbar();
        setUpAppVersion();
        setUpOnClickListeners();
    }

    private void setUpToolbar() {
        binding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
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
        authorBinding.writeAnEmail.setOnClickListener(this);
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

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }

        return super.onOptionsItemSelected(item);
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
            openUrl(TWITTER);
        } else if (v == aboutBinding.appSource) {
            openUrl(GITHUB);
        } else if (v == authorBinding.visitWebsite) {
            openUrl(WEBSITE);
        } else if (v == supportBinding.reportBugs) {
            openUrl(GITHUB);
        } else if (v == authorBinding.writeAnEmail) {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:contact@kabouzeid.com"));
            intent.putExtra(Intent.EXTRA_EMAIL, "contact@kabouzeid.com");
            intent.putExtra(Intent.EXTRA_SUBJECT, "Phonograph");
            startActivity(Intent.createChooser(intent, "E-Mail"));
        } else if (v == supportBinding.translate) {
            openUrl(TRANSLATE);
        } else if (v == supportBinding.rateOnGooglePlay) {
            openUrl(RATE_ON_GOOGLE_PLAY);
        } else if (v == supportBinding.donate) {
            openUrl(RATE_ON_GOOGLE_PLAY);
        } else if (v == thanksBinding.aidanFollestadGooglePlus) {
            openUrl(AIDAN_FOLLESTAD_GOOGLE_PLUS);
        } else if (v == thanksBinding.aidanFollestadGitHub) {
            openUrl(AIDAN_FOLLESTAD_GITHUB);
        } else if (v == thanksBinding.maartenCorpelGooglePlus) {
            openUrl(MAARTEN_CORPEL_GOOGLE_PLUS);
        } else if (v == thanksBinding.aleksandarTesicGooglePlus) {
            openUrl(ALEKSANDAR_TESIC_GOOGLE_PLUS);
        } else if (v == thanksBinding.eugeneCheungGitHub) {
            openUrl(EUGENE_CHEUNG_GITHUB);
        } else if (v == thanksBinding.eugeneCheungWebsite) {
            openUrl(EUGENE_CHEUNG_WEBSITE);
        } else if (v == thanksBinding.adrianTwitter) {
            openUrl(ADRIAN_TWITTER);
        } else if (v == thanksBinding.adrianWebsite) {
            openUrl(ADRIAN_WEBSITE);
        }
    }

    private void openUrl(String url) {
        Intent i = new Intent(Intent.ACTION_VIEW);
        i.setData(Uri.parse(url));
        i.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(i);
    }
}
