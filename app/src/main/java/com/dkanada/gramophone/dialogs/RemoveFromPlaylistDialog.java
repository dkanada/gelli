package com.dkanada.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.Html;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.PlaylistSong;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.PlaylistUtil;

import java.util.ArrayList;
import java.util.List;

public class RemoveFromPlaylistDialog extends DialogFragment {
    @NonNull
    public static RemoveFromPlaylistDialog create(Song song) {
        List<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static RemoveFromPlaylistDialog create(List<Song> songs) {
        RemoveFromPlaylistDialog dialog = new RemoveFromPlaylistDialog();

        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", new ArrayList<>(songs));

        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final List<PlaylistSong> songs = getArguments().getParcelableArrayList("songs");

        int title;
        CharSequence content;
        if (songs.size() > 1) {
            title = R.string.remove_songs_from_playlist_title;
            content = Html.fromHtml(getString(R.string.remove_x_songs_from_playlist, songs.size()));
        } else {
            title = R.string.remove_song_from_playlist_title;
            content = Html.fromHtml(getString(R.string.remove_song_x_from_playlist, songs.get(0).title));
        }

        return new MaterialDialog.Builder(requireActivity())
                .title(title)
                .content(content)
                .positiveText(R.string.remove_action)
                .negativeText(android.R.string.cancel)
                .onPositive((dialog, which) -> {
                    if (getActivity() == null) return;

                    PlaylistSong song = songs.get(0);
                    PlaylistUtil.deleteItems(songs, song.playlistId);
                })
                .build();
    }
}
