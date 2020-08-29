package com.dkanada.gramophone.dialogs;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;
import android.text.Html;
import android.text.Spanned;
import android.view.View;
import android.widget.TextView;

import com.afollestad.materialdialogs.MaterialDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;

public class SongDetailDialog extends DialogFragment {
    @NonNull
    public static SongDetailDialog create(Song song) {
        SongDetailDialog dialog = new SongDetailDialog();
        Bundle args = new Bundle();
        args.putParcelable("song", song);
        dialog.setArguments(args);
        return dialog;
    }

    private static Spanned makeTextWithTitle(@NonNull Context context, int titleResId, String text) {
        return Html.fromHtml("<b>" + context.getResources().getString(titleResId) + ": " + "</b>" + text);
    }

    private static String getFileSizeString(long sizeInBytes) {
        long fileSizeInKB = sizeInBytes / 1024;
        long fileSizeInMB = fileSizeInKB / 1024;
        return fileSizeInMB + " MB";
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Activity context = getActivity();
        final Song song = getArguments().getParcelable("song");

        MaterialDialog dialog = new MaterialDialog.Builder(context)
                .customView(R.layout.dialog_file_details, true)
                .title(context.getResources().getString(R.string.label_details))
                .positiveText(android.R.string.ok)
                .build();

        View dialogView = dialog.getCustomView();
        final TextView filePath = dialogView.findViewById(R.id.file_path);
        final TextView fileName = dialogView.findViewById(R.id.file_name);
        final TextView fileSize = dialogView.findViewById(R.id.file_size);
        final TextView fileFormat = dialogView.findViewById(R.id.file_format);
        final TextView trackLength = dialogView.findViewById(R.id.track_length);
        final TextView bitRate = dialogView.findViewById(R.id.bit_rate);
        final TextView sampleRate = dialogView.findViewById(R.id.sample_rate);

        filePath.setText(makeTextWithTitle(context, R.string.label_file_path, "-"));
        fileName.setText(makeTextWithTitle(context, R.string.label_file_name, "-"));
        fileSize.setText(makeTextWithTitle(context, R.string.label_file_size, "-"));
        fileFormat.setText(makeTextWithTitle(context, R.string.label_file_format, "-"));
        trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, "-"));
        bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, "-"));
        sampleRate.setText(makeTextWithTitle(context, R.string.label_sample_rate, "-"));

        if (song != null) {
            fileName.setText(makeTextWithTitle(context, R.string.label_file_name, song.title));
            trackLength.setText(makeTextWithTitle(context, R.string.label_track_length, MusicUtil.getReadableDurationString(song.duration)));
        }

        return dialog;
    }
}
