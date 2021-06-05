package com.dkanada.gramophone.helper.menu;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.appcompat.app.AppCompatActivity;

import android.view.MenuItem;
import android.view.View;
import android.widget.PopupMenu;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.dialogs.AddToPlaylistDialog;
import com.dkanada.gramophone.dialogs.SongDetailDialog;
import com.dkanada.gramophone.dialogs.SongShareDialog;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.NavigationUtil;

import java.util.Collections;

public class SongMenuHelper {
    public static final int MENU_RES = R.menu.menu_item_song;

    public static boolean handleMenuClick(@NonNull FragmentActivity activity, @NonNull Song song, int menuItemId) {
        switch (menuItemId) {
            case R.id.action_share:
                SongShareDialog.create(song).show(activity.getSupportFragmentManager(), SongShareDialog.TAG);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(song).show(activity.getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(song);
                return true;
            case R.id.action_add_to_queue:
                MusicPlayerRemote.enqueue(song);
                return true;
            case R.id.action_details:
                SongDetailDialog.create(song).show(activity.getSupportFragmentManager(), SongDetailDialog.TAG);
                return true;
            case R.id.action_download:
                NavigationUtil.startDownload(activity, Collections.singletonList(song));
                return true;
            case R.id.action_go_to_album:
                NavigationUtil.startAlbum(activity, new Album(song), null);
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.startArtist(activity, new Artist(song), null);
                return true;
        }

        return false;
    }

    public static abstract class OnClickSongMenu implements View.OnClickListener, PopupMenu.OnMenuItemClickListener {
        private final AppCompatActivity activity;

        public OnClickSongMenu(@NonNull AppCompatActivity activity) {
            this.activity = activity;
        }

        public int getMenuRes() {
            return MENU_RES;
        }

        @Override
        public void onClick(View v) {
            PopupMenu popupMenu = new PopupMenu(activity, v);

            popupMenu.inflate(getMenuRes());
            popupMenu.setOnMenuItemClickListener(this);
            popupMenu.show();
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            return handleMenuClick(activity, getSong(), item.getItemId());
        }

        public abstract Song getSong();
    }
}
