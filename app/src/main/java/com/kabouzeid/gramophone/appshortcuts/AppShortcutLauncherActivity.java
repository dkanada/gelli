package com.kabouzeid.gramophone.appshortcuts;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.kabouzeid.gramophone.appshortcuts.shortcuttype.LatestShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.ShuffleShortcutType;
import com.kabouzeid.gramophone.appshortcuts.shortcuttype.FrequentShortcutType;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.smartplaylist.LatestPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.FrequentPlaylist;
import com.kabouzeid.gramophone.model.smartplaylist.ShufflePlaylist;
import com.kabouzeid.gramophone.service.MusicService;

/**
 * @author Adrian Campos
 */

public class AppShortcutLauncherActivity extends Activity {
    public static final String KEY_SHORTCUT_TYPE = "com.kabouzeid.gramophone.appshortcuts.ShortcutType";

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
            //noinspection WrongConstant
            shortcutType = extras.getInt(KEY_SHORTCUT_TYPE, SHORTCUT_TYPE_NONE);
        }

        switch (shortcutType) {
            case SHORTCUT_TYPE_SHUFFLE:
                startServiceWithPlaylist(MusicService.SHUFFLE_MODE_SHUFFLE,
                        new ShufflePlaylist(getApplicationContext()));
                DynamicShortcutManager.reportShortcutUsed(this, ShuffleShortcutType.getId());
                break;
            case SHORTCUT_TYPE_FREQUENT:
                startServiceWithPlaylist(MusicService.SHUFFLE_MODE_NONE,
                        new FrequentPlaylist(getApplicationContext()));
                DynamicShortcutManager.reportShortcutUsed(this, FrequentShortcutType.getId());
                break;
            case SHORTCUT_TYPE_LATEST:
                startServiceWithPlaylist(MusicService.SHUFFLE_MODE_NONE,
                        new LatestPlaylist(getApplicationContext()));
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
        bundle.putInt(MusicService.INTENT_EXTRA_SHUFFLE_MODE, shuffleMode);

        intent.putExtras(bundle);

        startService(intent);
    }
}
