package com.kabouzeid.gramophone.adapter;

import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.kabouzeid.gramophone.adapter.base.MediaEntryViewHolder;
import com.kabouzeid.gramophone.dialogs.DeletePlaylistDialog;
import com.kabouzeid.gramophone.glide.CustomGlideRequest;
import com.kabouzeid.gramophone.glide.CustomPaletteTarget;
import com.kabouzeid.gramophone.helper.menu.PlaylistMenuHelper;
import com.kabouzeid.gramophone.helper.menu.SongsMenuHelper;
import com.kabouzeid.gramophone.interfaces.CabHolder;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.NavigationUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {
    protected final AppCompatActivity activity;
    protected List<Playlist> dataSet;
    protected int itemLayoutRes;

    public PlaylistAdapter(AppCompatActivity activity, List<Playlist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_playlists_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        setHasStableIds(true);
    }

    public List<Playlist> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(List<Playlist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    @Override
    @NonNull
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);
        return createViewHolder(view, viewType);
    }

    protected ViewHolder createViewHolder(View view, int viewType) {
        return new ViewHolder(view, viewType);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Playlist playlist = dataSet.get(position);

        holder.itemView.setActivated(isChecked(playlist));

        if (holder.title != null) {
            holder.title.setText(playlist.name);
        }

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        loadImage(playlist, holder);
    }

    protected void loadImage(Playlist playlist, final PlaylistAdapter.ViewHolder holder) {
        if (holder.image == null) return;

        CustomGlideRequest.Builder
                .from(Glide.with(activity), playlist.id)
                .generatePalette(activity).build()
                .into(new CustomPaletteTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                    }

                    @Override
                    public void onColorReady(int color) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Playlist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Playlist playlist) {
        return playlist.name;
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Playlist> selection) {
        switch (menuItem.getItemId()) {
            case R.id.action_delete_playlist:
                DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), "DELETE_PLAYLIST");
                break;
            default:
                SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
                break;
        }
    }

    @NonNull
    private List<Song> getSongList(@NonNull List<Playlist> playlists) {
        final List<Song> songs = new ArrayList<>();
        return songs;
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            if (menu != null) {
                menu.setOnClickListener(view -> {
                    final PopupMenu popupMenu = new PopupMenu(activity, view);

                    popupMenu.inflate(R.menu.menu_item_playlist);
                    popupMenu.setOnMenuItemClickListener(item -> PlaylistMenuHelper.handleMenuClick(activity, dataSet.get(getAdapterPosition()), item));

                    popupMenu.show();
                });
            }
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getAdapterPosition());
                NavigationUtil.goToPlaylist(activity, playlist);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
