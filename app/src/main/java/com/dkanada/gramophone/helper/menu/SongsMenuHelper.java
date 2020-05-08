package com.dkanada.gramophone.helper.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.dialogs.AddToPlaylistDialog;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.model.Song;

import java.util.List;

public class SongsMenuHelper {
    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull List<Song> songs, int menuItemId) {
        switch (menuItemId) {
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_queue:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
        }

        return false;
    }
}
