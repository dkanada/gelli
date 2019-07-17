package com.kabouzeid.gramophone.shortcuts.shortcuttype;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.pm.ShortcutInfo;
import android.os.Build;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.shortcuts.AppShortcutIconGenerator;
import com.kabouzeid.gramophone.shortcuts.AppShortcutLauncherActivity;

/**
 * @author Adrian Campos
 */
@TargetApi(Build.VERSION_CODES.N_MR1)
public final class FrequentShortcutType extends BaseShortcutType {
    public FrequentShortcutType(Context context) {
        super(context);
    }

    public static String getId() {
        return ID_PREFIX + "top_tracks";
    }

    public ShortcutInfo getShortcutInfo() {
        return new ShortcutInfo.Builder(context, getId())
                .setShortLabel(context.getString(R.string.app_shortcut_top_tracks_short))
                .setLongLabel(context.getString(R.string.my_top_tracks))
                .setIcon(AppShortcutIconGenerator.generateThemedIcon(context, R.drawable.ic_app_shortcut_top_tracks))
                .setIntent(getPlaySongsIntent(AppShortcutLauncherActivity.SHORTCUT_TYPE_FREQUENT))
                .build();
    }
}
