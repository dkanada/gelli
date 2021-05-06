package com.dkanada.gramophone.views.shortcuts.type;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.os.Build;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.views.shortcuts.AppShortcutIconGenerator;
import com.dkanada.gramophone.views.shortcuts.AppShortcutLauncherActivity;

@TargetApi(Build.VERSION_CODES.O)
public final class LatestShortcutType extends BaseShortcutType {
    public LatestShortcutType(Context context) {
        super(context);
    }

    public static String getId() {
        return PREFIX + ".latest";
    }

    @Override
    public ShortcutInfo getShortcutInfo() {
        return new ShortcutInfo.Builder(context, getId())
            .setShortLabel(context.getString(R.string.last_added))
            .setIcon(AppShortcutIconGenerator.generateThemedIcon(context, R.drawable.ic_app_shortcut_last_added))
            .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.SHORTCUT_TYPE_LATEST))
            .build();
    }
}
