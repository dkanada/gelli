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

    private static String getSampleRateString(int sampleRate) {
        return sampleRate + " Hz";
    }

    private static String getBitRateString(int bitRate) {
        int bitRateInKB = bitRate / 1000;
        return bitRateInKB + " kb/s";
    }

    private static String getBitDepthString(int bitDepth) {
        return bitDepth + "-bit";
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
        final TextView path = dialogView.findViewById(R.id.file_path);
        final TextView name = dialogView.findViewById(R.id.file_name);
        final TextView size = dialogView.findViewById(R.id.file_size);
        final TextView format = dialogView.findViewById(R.id.file_format);
        final TextView length = dialogView.findViewById(R.id.track_length);
        final TextView sampleRate = dialogView.findViewById(R.id.sample_rate);
        final TextView bitRate = dialogView.findViewById(R.id.bit_rate);
        final TextView bitDepth = dialogView.findViewById(R.id.bit_depth);
        final TextView channels = dialogView.findViewById(R.id.channels);

        path.setText(makeTextWithTitle(context, R.string.label_file_path, "-"));
        name.setText(makeTextWithTitle(context, R.string.label_file_name, "-"));
        size.setText(makeTextWithTitle(context, R.string.label_file_size, "-"));
        format.setText(makeTextWithTitle(context, R.string.label_file_format, "-"));
        length.setText(makeTextWithTitle(context, R.string.label_track_length, "-"));
        sampleRate.setText(makeTextWithTitle(context, R.string.label_sample_rate, "-"));
        bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, "-"));
        bitDepth.setText(makeTextWithTitle(context, R.string.label_bit_depth, "-"));
        channels.setText(makeTextWithTitle(context, R.string.label_channels, "-"));

        if (song != null) {
            path.setText(makeTextWithTitle(context, R.string.label_file_path, song.path));
            name.setText(makeTextWithTitle(context, R.string.label_file_name, song.title));
            size.setText(makeTextWithTitle(context, R.string.label_file_size, getFileSizeString(song.size)));

            if (song.container.equals(song.codec)) {
                format.setText(makeTextWithTitle(context, R.string.label_file_format, song.container.toUpperCase()));
            } else {
                format.setText(makeTextWithTitle(context, R.string.label_file_format, song.container.toUpperCase() + ":" + song.codec.toUpperCase()));
            }

            length.setText(makeTextWithTitle(context, R.string.label_track_length, MusicUtil.getReadableDurationString(song.duration)));
            sampleRate.setText(makeTextWithTitle(context, R.string.label_sample_rate, getSampleRateString(song.sampleRate)));
            bitRate.setText(makeTextWithTitle(context, R.string.label_bit_rate, getBitRateString(song.bitRate)));
            bitDepth.setText(makeTextWithTitle(context, R.string.label_bit_depth, getBitDepthString(song.bitDepth)));
            channels.setText(makeTextWithTitle(context, R.string.label_channels, Integer.toString(song.channels)));

            if (song.bitDepth == 0) {
                bitDepth.setVisibility(View.GONE);
            }
        }

        return dialog;
    }
}
