package com.dkanada.gramophone.adapter.song;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.NavigationUtil;

import java.util.List;

public class PlaylistSongAdapter extends AbsOffsetSongAdapter {
    public PlaylistSongAdapter(AppCompatActivity activity, @NonNull List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder, false);
        setMultiSelectMenuRes(R.menu.menu_cannot_delete_single_songs_playlist_songs_selection);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new PlaylistSongAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.ViewHolder holder, int position) {
        if (holder.getItemViewType() == OFFSET_ITEM) {
            int textColor = ThemeStore.textColorSecondary(activity);
            if (holder.title != null) {
                holder.title.setText(MusicUtil.getPlaylistInfoString(activity, dataSet));
                holder.title.setTextColor(textColor);
            }

            if (holder.text != null) {
                holder.text.setVisibility(View.GONE);
            }

            if (holder.menu != null) {
                holder.menu.setVisibility(View.GONE);
            }

            if (holder.image != null) {
                final int padding = activity.getResources().getDimensionPixelSize(R.dimen.default_item_margin) / 2;
                holder.image.setPadding(padding, padding, padding, padding);
                holder.image.setColorFilter(textColor);
                holder.image.setImageResource(R.drawable.ic_timer_white_24dp);
            }

            if (holder.dragView != null) {
                holder.dragView.setVisibility(View.GONE);
            }

            if (holder.separator != null) {
                holder.separator.setVisibility(View.VISIBLE);
            }

            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            super.onBindViewHolder(holder, position - 1);
        }
    }

    public class ViewHolder extends AbsOffsetSongAdapter.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        protected int getSongMenuRes() {
            return R.menu.menu_item_cannot_delete_single_songs_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_go_to_album) {
                Pair[] albumPairs = new Pair[]{Pair.create(image, activity.getString(R.string.transition_album_image))};
                NavigationUtil.goToAlbum(activity, new Album(dataSet.get(getBindingAdapterPosition() - 1)), albumPairs);
                return true;
            }

            return super.onSongMenuItemClick(item);
        }
    }
}
