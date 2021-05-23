package com.dkanada.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.PlaylistUtil;
import com.dkanada.gramophone.util.QueryUtil;

import java.util.ArrayList;
import java.util.List;

public class AddToPlaylistDialog extends DialogFragment {
    @NonNull
    public static AddToPlaylistDialog create(Song song) {
        List<Song> list = new ArrayList<>();
        list.add(song);
        return create(list);
    }

    @NonNull
    public static AddToPlaylistDialog create(List<Song> songs) {
        AddToPlaylistDialog dialog = new AddToPlaylistDialog();

        Bundle args = new Bundle();
        args.putParcelableArrayList("songs", new ArrayList<>(songs));

        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        List<Playlist> playlists = new ArrayList<>();
        MaterialDialog dialog = new MaterialDialog.Builder(requireActivity())
                .title(R.string.action_add_to_playlist)
                .items(requireActivity().getResources().getString(R.string.action_new_playlist))
                .itemsCallback((materialDialog, view, i, charSequence) -> {
                    final List<Song> songs = getArguments().getParcelableArrayList("songs");
                    if (songs == null) return;

                    if (i == 0) {
                        materialDialog.dismiss();
                        CreatePlaylistDialog.create(songs).show(requireActivity().getSupportFragmentManager(), "ADD_TO_PLAYLIST");
                    } else {
                        materialDialog.dismiss();
                        PlaylistUtil.addItems(songs, playlists.get(i - 1).id);
                    }
                })
                .build();

        QueryUtil.getPlaylists(media -> {
            List<String> names = new ArrayList<>();

            names.add(requireActivity().getResources().getString(R.string.action_new_playlist));
            for (Playlist playlist : media) {
                names.add(playlist.name);
            }

            playlists.addAll(media);
            dialog.setItems(names.toArray(new String[0]));
        });

        return dialog;
    }
}
