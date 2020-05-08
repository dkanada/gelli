package com.dkanada.gramophone.adapter.artist;

import android.graphics.drawable.Drawable;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.base.AbsMultiSelectAdapter;
import com.dkanada.gramophone.adapter.base.MediaEntryViewHolder;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.helper.menu.SongsMenuHelper;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ArtistAdapter extends AbsMultiSelectAdapter<ArtistAdapter.ViewHolder, Artist> implements FastScrollRecyclerView.SectionedAdapter {
    protected final AppCompatActivity activity;
    protected List<Artist> dataSet;

    protected int itemLayoutRes;

    protected boolean usePalette;

    public ArtistAdapter(@NonNull AppCompatActivity activity, List<Artist> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, cabHolder, R.menu.menu_media_selection);
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
        this.usePalette = usePalette;
        setHasStableIds(true);
    }

    public void swapDataSet(List<Artist> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    public List<Artist> getDataSet() {
        return dataSet;
    }

    public void usePalette(boolean usePalette) {
        this.usePalette = usePalette;
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
        return createViewHolder(view);
    }

    protected ViewHolder createViewHolder(View view) {
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        final Artist artist = dataSet.get(position);

        boolean isChecked = isChecked(artist);
        holder.itemView.setActivated(isChecked);

        if (holder.getAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.title != null) {
            holder.title.setText(artist.getName());
        }

        if (holder.text != null) {
            holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
        }

        holder.itemView.setActivated(isChecked(artist));
        loadArtistImage(artist, holder);
    }

    private void setColors(int color, ViewHolder holder) {
        if (holder.paletteColorContainer != null) {
            holder.paletteColorContainer.setBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }

            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }
    }

    protected void loadArtistImage(Artist artist, final ViewHolder holder) {
        if (holder.image == null) return;

        CustomGlideRequest.Builder
                .from(Glide.with(activity), artist.primary)
                .generatePalette(activity).build()
                .into(new CustomPaletteTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getDefaultFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette) {
                            setColors(color, holder);
                        } else {
                            setColors(getDefaultFooterColor(), holder);
                        }
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @Override
    protected Artist getIdentifier(int position) {
        return dataSet.get(position);
    }

    @Override
    protected String getName(Artist artist) {
        return artist.getName();
    }

    @Override
    protected void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Artist> selection) {
        SongsMenuHelper.handleMenuClick(activity, getSongList(selection), menuItem.getItemId());
    }

    @NonNull
    private List<Song> getSongList(@NonNull List<Artist> artists) {
        final List<Song> songs = new ArrayList<>();
        for (Artist artist : artists) {
            songs.addAll(artist.getSongs());
        }

        return songs;
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        return MusicUtil.getSectionName(dataSet.get(position).getName());
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            setImageTransitionName(activity.getString(R.string.transition_artist_image));
            if (menu != null) {
                menu.setVisibility(View.GONE);
            }
        }

        @Override
        public void onClick(View v) {
            if (isInQuickSelectMode()) {
                toggleChecked(getAdapterPosition());
            } else {
                Pair[] artistPairs = new Pair[]{Pair.create(image, activity.getResources().getString(R.string.transition_artist_image))};
                NavigationUtil.goToArtist(activity, dataSet.get(getAdapterPosition()), artistPairs);
            }
        }

        @Override
        public boolean onLongClick(View view) {
            toggleChecked(getAdapterPosition());
            return true;
        }
    }
}
