package com.dkanada.gramophone.adapter.song;

import android.view.MenuItem;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.dialogs.RemoveFromPlaylistDialog;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;

import java.util.List;

public class PlaylistSongAdapter extends AbsOffsetSongAdapter {
    public PlaylistSongAdapter(AppCompatActivity activity, @NonNull List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder, false);
        setMultiSelectMenuRes(R.menu.menu_select_playlist_song);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new PlaylistSongAdapter.ViewHolder(view);
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Song> selection) {
        if (menuItem.getItemId() == R.id.action_remove_from_playlist) {
            RemoveFromPlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), RemoveFromPlaylistDialog.TAG);
            return;
        }

        super.onMultipleItemAction(menuItem, selection);
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
            return R.menu.menu_item_playlist_song;
        }

        @Override
        protected boolean onSongMenuItemClick(MenuItem item) {
            if (item.getItemId() == R.id.action_remove_from_playlist) {
                RemoveFromPlaylistDialog.create(getSong()).show(activity.getSupportFragmentManager(), RemoveFromPlaylistDialog.TAG);
                return true;
            }

            return super.onSongMenuItemClick(item);
        }
    }
}
