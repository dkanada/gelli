package com.dkanada.gramophone.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.dkanada.gramophone.adapter.base.MediaEntryViewHolder;
import com.dkanada.gramophone.dialogs.DeletePlaylistDialog;
import com.dkanada.gramophone.dialogs.RenamePlaylistDialog;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.helper.menu.SongsMenuHelper;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.util.QueryUtil;
import com.dkanada.gramophone.util.NavigationUtil;

import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.List;

public class PlaylistAdapter extends AbsMultiSelectAdapter<PlaylistAdapter.ViewHolder, Playlist> {
    protected final AppCompatActivity activity;
    protected List<Playlist> dataSet;
    protected int itemLayoutRes;

    public PlaylistAdapter(AppCompatActivity activity, List<Playlist> dataSet, @LayoutRes int itemLayoutRes, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_select_playlist);

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

        if (holder.getBindingAdapterPosition() == getItemCount() - 1) {
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
                .from(activity, playlist.primary, playlist.blurHash)
                .palette().build()
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
        if (menuItem.getItemId() == R.id.action_delete_playlist) {
            DeletePlaylistDialog.create(selection).show(activity.getSupportFragmentManager(), DeletePlaylistDialog.TAG);
        } else {
            for (Playlist playlist : selection) {
                ItemQuery songs = new ItemQuery();
                songs.setParentId(playlist.id);

                QueryUtil.getSongs(songs, (media) -> {
                    SongsMenuHelper.handleMenuClick(activity, media, menuItem.getItemId());
                });
            }
        }
    }

    public class ViewHolder extends MediaEntryViewHolder implements PopupMenu.OnMenuItemClickListener {
        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            if (menu != null) {
                menu.setOnClickListener(view -> {
                    final PopupMenu popupMenu = new PopupMenu(activity, view);

                    popupMenu.inflate(R.menu.menu_item_playlist);
                    popupMenu.setOnMenuItemClickListener(this);

                    popupMenu.show();
                });
            }
        }

        @Override
        public boolean onMenuItemClick(MenuItem item) {
            Playlist playlist = dataSet.get(getBindingAdapterPosition());

            if (item.getItemId() == R.id.action_rename_playlist) {
                RenamePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), RenamePlaylistDialog.TAG);
                return true;
            } else if (item.getItemId() == R.id.action_delete_playlist) {
                DeletePlaylistDialog.create(playlist).show(activity.getSupportFragmentManager(), DeletePlaylistDialog.TAG);
                return true;
            }

            ItemQuery songs = new ItemQuery();
            songs.setParentId(playlist.id);

            QueryUtil.getSongs(songs, (media) -> {
                SongsMenuHelper.handleMenuClick(activity, media, item.getItemId());
            });

            return true;
        }

        @Override
        public void onClick(View view) {
            if (isInQuickSelectMode()) {
                toggleChecked(getBindingAdapterPosition());
            } else {
                Playlist playlist = dataSet.get(getBindingAdapterPosition());
                NavigationUtil.startPlaylist(activity, playlist, null);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getBindingAdapterPosition());
            return true;
        }
    }
}
