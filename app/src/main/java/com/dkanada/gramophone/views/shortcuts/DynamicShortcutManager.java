package com.dkanada.gramophone.views.shortcuts;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ShortcutInfo;
import android.content.pm.ShortcutManager;
import android.graphics.drawable.Icon;
import android.os.Build;

import com.dkanada.gramophone.views.shortcuts.shortcuttype.LatestShortcutType;
import com.dkanada.gramophone.views.shortcuts.shortcuttype.ShuffleShortcutType;
import com.dkanada.gramophone.views.shortcuts.shortcuttype.FrequentShortcutType;

import java.util.Arrays;
import java.util.List;

@TargetApi(Build.VERSION_CODES.O)
public class DynamicShortcutManager {

    private final Context context;
    private final ShortcutManager shortcutManager;

    public DynamicShortcutManager(Context context) {
        this.context = context;
        shortcutManager = this.context.getSystemService(ShortcutManager.class);
    }

    public static ShortcutInfo createShortcut(Context context, String id, String shortLabel, String longLabel, Icon icon, Intent intent) {
        return new ShortcutInfo.Builder(context, id)
                .setShortLabel(shortLabel)
                .setLongLabel(longLabel)
                .setIcon(icon)
                .setIntent(intent)
                .build();
    }

    public void initDynamicShortcuts() {
        if (shortcutManager.getDynamicShortcuts().size() == 0) {
            shortcutManager.setDynamicShortcuts(getDefaultShortcuts());
        }
    }

    public void updateDynamicShortcuts() {
        shortcutManager.updateShortcuts(getDefaultShortcuts());
    }

    public List<ShortcutInfo> getDefaultShortcuts() {
        return Arrays.asList(
                new ShuffleShortcutType(context).getShortcutInfo(),
                new FrequentShortcutType(context).getShortcutInfo(),
                new LatestShortcutType(context).getShortcutInfo()
        );
    }

    public static void reportShortcutUsed(Context context, String shortcutId){
        context.getSystemService(ShortcutManager.class).reportShortcutUsed(shortcutId);
    }
}
