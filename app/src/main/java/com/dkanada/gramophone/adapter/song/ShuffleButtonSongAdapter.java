package com.dkanada.gramophone.adapter.song;

import android.graphics.Typeface;
import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.ShortcutUtil;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Song;

import java.util.List;

public class ShuffleButtonSongAdapter extends AbsOffsetSongAdapter {
    private boolean isFavorite = false;

    public ShuffleButtonSongAdapter(AppCompatActivity activity, List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new ShuffleButtonSongAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final SongAdapter.ViewHolder holder, int position) {
        if (holder.getItemViewType() == OFFSET_ITEM) {
            int accentColor = PreferenceUtil.getInstance(activity).getAccentColor();

            if (holder.title != null) {
                holder.title.setText(activity.getResources().getString(R.string.action_shuffle_all).toUpperCase());
                holder.title.setTextColor(accentColor);
                holder.title.setTypeface(Typeface.create("sans-serif-medium", Typeface.NORMAL));
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
                holder.image.setColorFilter(accentColor);
                holder.image.setImageResource(R.drawable.ic_shuffle_white_24dp);
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

    public void setFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public class ViewHolder extends AbsOffsetSongAdapter.ViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View v) {
            if (getItemViewType() == OFFSET_ITEM) {
                ShortcutUtil.getShuffle((media) -> MusicPlayerRemote.openAndShuffleQueue(media, true), isFavorite);
            }

            super.onClick(v);
        }
    }
}
