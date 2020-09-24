package com.dkanada.gramophone.adapter.song;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;

import java.util.List;
import java.util.Locale;

public class AlbumSongAdapter extends SongAdapter {
    public AlbumSongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongAdapter.ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        final Song song = dataSet.get(position);

        if (holder.imageText != null) {
            final String trackNumber = song.trackNumber > 0 ? String.format(Locale.ENGLISH, "%02d", song.trackNumber) : null;
            final String discNumber = song.discNumber > 0 ? String.valueOf(song.discNumber) : null;

            if (trackNumber != null && discNumber != null) {
                holder.imageText.setText(String.format(Locale.ENGLISH, "%s.%s", discNumber, trackNumber));
            } else if (trackNumber != null) {
                holder.imageText.setText(String.format(Locale.ENGLISH, "%s", trackNumber));
            } else {
                holder.imageText.setText("-");
            }
        }
    }

    @Override
    protected String getSongText(Song song) {
        return MusicUtil.getReadableDurationString(song.duration);
    }

    public class ViewHolder extends SongAdapter.ViewHolder {

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (imageText != null) {
                imageText.setVisibility(View.VISIBLE);
            }

            if (image != null) {
                image.setVisibility(View.GONE);
            }
        }
    }

    @Override
    protected void loadAlbumCover(Song song, SongAdapter.ViewHolder holder) {
        // don't want to load it in this adapter
    }
}
