package com.dkanada.gramophone.dialogs;

import android.app.Dialog;
import android.os.Bundle;
import android.text.InputType;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.util.PlaylistUtil;

public class RenamePlaylistDialog extends DialogFragment {
    private static final String PLAYLIST_ID = "playlist_id";

    @NonNull
    public static RenamePlaylistDialog create(Playlist playlist) {
        RenamePlaylistDialog dialog = new RenamePlaylistDialog();

        Bundle args = new Bundle();
        args.putString(PLAYLIST_ID, playlist.id);

        dialog.setArguments(args);
        return dialog;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        String playlistId = getArguments().getString(PLAYLIST_ID);
        return new MaterialDialog.Builder(requireContext())
                .title(R.string.rename_playlist_title)
                .positiveText(R.string.rename_action)
                .negativeText(android.R.string.cancel)
                .inputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PERSON_NAME | InputType.TYPE_TEXT_FLAG_CAP_WORDS)
                .input(getString(R.string.name), "", false,
                        (materialDialog, charSequence) -> {
                            final String name = charSequence.toString().trim();

                            if (!name.isEmpty()) {
                                String id = getArguments().getString(PLAYLIST_ID);
                                PlaylistUtil.renamePlaylist(id, name);
                            }
                        })
                .build();
    }
}