package com.dkanada.gramophone.views.shortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.dkanada.gramophone.views.shortcuts.shortcuttype.LatestShortcutType;
import com.dkanada.gramophone.views.shortcuts.shortcuttype.ShuffleShortcutType;
import com.dkanada.gramophone.views.shortcuts.shortcuttype.FrequentShortcutType;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.service.MusicService;

public class AppShortcutLauncherActivity extends Activity {
    public static final String KEY_SHORTCUT_TYPE = "com.dkanada.gramophone.views.shortcuts.ShortcutType";

    public static final int SHORTCUT_TYPE_SHUFFLE = 0;
    public static final int SHORTCUT_TYPE_FREQUENT = 1;
    public static final int SHORTCUT_TYPE_LATEST = 2;
    public static final int SHORTCUT_TYPE_NONE = 3;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        int shortcutType = SHORTCUT_TYPE_NONE;

        // Set shortcutType from the intent extras
        Bundle extras = getIntent().getExtras();
        if (extras != null) {
            // noinspection WrongConstant
            shortcutType = extras.getInt(KEY_SHORTCUT_TYPE, SHORTCUT_TYPE_NONE);
        }

        switch (shortcutType) {
            case SHORTCUT_TYPE_SHUFFLE:
                DynamicShortcutManager.reportShortcutUsed(this, ShuffleShortcutType.getId());
                break;
            case SHORTCUT_TYPE_FREQUENT:
                DynamicShortcutManager.reportShortcutUsed(this, FrequentShortcutType.getId());
                break;
            case SHORTCUT_TYPE_LATEST:
                DynamicShortcutManager.reportShortcutUsed(this, LatestShortcutType.getId());
                break;
        }

        finish();
    }

    private void startServiceWithPlaylist(int shuffleMode, Playlist playlist) {
        Intent intent = new Intent(this, MusicService.class);
        intent.setAction(MusicService.ACTION_PLAY_PLAYLIST);

        Bundle bundle = new Bundle();
        bundle.putParcelable(MusicService.INTENT_EXTRA_PLAYLIST, playlist);
        bundle.putInt(MusicService.INTENT_EXTRA_SHUFFLE, shuffleMode);

        intent.putExtras(bundle);

        startService(intent);
    }
}
