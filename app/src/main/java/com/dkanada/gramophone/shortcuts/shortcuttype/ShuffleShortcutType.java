package com.dkanada.gramophone.shortcuts.shortcuttype;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.os.Build;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.shortcuts.AppShortcutIconGenerator;
import com.dkanada.gramophone.shortcuts.AppShortcutLauncherActivity;

@TargetApi(Build.VERSION_CODES.N_MR1)
public final class ShuffleShortcutType extends BaseShortcutType {
    public ShuffleShortcutType(Context context) {
        super(context);
    }

    public static String getId() {
        return ID_PREFIX + "shuffle_all";
    }

    public ShortcutInfo getShortcutInfo() {
        return new ShortcutInfo.Builder(context, getId())
                .setShortLabel(context.getString(R.string.action_shuffle))
                .setIcon(AppShortcutIconGenerator.generateThemedIcon(context, R.drawable.ic_app_shortcut_shuffle_all))
                .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.SHORTCUT_TYPE_SHUFFLE))
                .build();
    }
}
