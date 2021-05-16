package com.dkanada.gramophone.adapter.song;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;

import com.afollestad.materialcab.MaterialCab;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.helper.menu.SongMenuHelper;
import com.dkanada.gramophone.helper.menu.SongsMenuHelper;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.NavigationUtil;

import java.util.ArrayList;
import java.util.List;

public class ArtistSongAdapter extends ArrayAdapter<Song> implements MaterialCab.Callback {
    @Nullable
    private final CabHolder cabHolder;
    private MaterialCab cab;
    private List<Song> dataSet;
    private List<Song> checked;

    @NonNull
    private final AppCompatActivity activity;

    public ArtistSongAdapter(@NonNull AppCompatActivity activity, @NonNull List<Song> dataSet, @Nullable CabHolder cabHolder) {
        super(activity, R.layout.item_list, dataSet);
        this.activity = activity;
        this.cabHolder = cabHolder;
        this.dataSet = dataSet;
        checked = new ArrayList<>();
    }

    public List<Song> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(List<Song> dataSet) {
        this.dataSet = dataSet;
        clear();
        addAll(dataSet);
        notifyDataSetChanged();
    }

    @Override
    @NonNull
    public View getView(final int position, View convertView, @NonNull ViewGroup parent) {
        final Song song = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(R.layout.item_list, parent, false);
        }

        final TextView songTitle = convertView.findViewById(R.id.title);
        final TextView songInfo = convertView.findViewById(R.id.text);
        final ImageView albumArt = convertView.findViewById(R.id.image);
        final View shortSeparator = convertView.findViewById(R.id.short_separator);

        if (position == getCount() - 1) {
            if (shortSeparator != null) {
                shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (shortSeparator != null) {
                shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        songTitle.setText(song.title);
        songInfo.setText(song.albumName);

        CustomGlideRequest.Builder
                .from(activity, song.primary, song.blurHash)
                .build().into(albumArt);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            albumArt.setTransitionName(activity.getString(R.string.transition_album_image));
        }

        final ImageView overflowButton = convertView.findViewById(R.id.menu);
        overflowButton.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
            @Override
            public Song getSong() {
                return song;
            }

            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if (item.getItemId() == R.id.action_go_to_album) {
                    Pair[] albumPairs = new Pair[]{Pair.create(albumArt, activity.getResources().getString(R.string.transition_album_image))};
                    NavigationUtil.startAlbum(activity, new Album(song), albumPairs);
                    return true;
                }

                return super.onMenuItemClick(item);
            }
        });

        convertView.setActivated(isChecked(song));
        convertView.setOnClickListener(view -> {
            if (isInQuickSelectMode()) {
                toggleChecked(song);
            } else {
                MusicPlayerRemote.openQueue(dataSet, position, true);
            }
        });

        convertView.setOnLongClickListener(view -> {
            toggleChecked(song);
            return true;
        });

        return convertView;
    }

    private void onMultipleItemAction(@NonNull MenuItem menuItem, @NonNull List<Song> selection) {
        SongsMenuHelper.handleMenuClick(activity, selection, menuItem.getItemId());
    }

    protected void toggleChecked(Song song) {
        if (cabHolder != null) {
            openCabIfNecessary();

            if (!checked.remove(song)) checked.add(song);
            notifyDataSetChanged();

            final int size = checked.size();
            if (size <= 0) cab.finish();
            else if (size == 1) cab.setTitle(checked.get(0).title);
            else if (size > 1) cab.setTitle(String.valueOf(size));
        }
    }

    private void openCabIfNecessary() {
        if (cabHolder != null) {
            if (cab == null || !cab.isActive()) {
                cab = cabHolder.openCab(R.menu.menu_select_media, this);
            }
        }
    }

    private void unCheckAll() {
        checked.clear();
        notifyDataSetChanged();
    }

    protected boolean isChecked(Song song) {
        return checked.contains(song);
    }

    protected boolean isInQuickSelectMode() {
        return cab != null && cab.isActive();
    }

    @Override
    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
        return true;
    }

    @Override
    public boolean onCabItemClicked(@NonNull MenuItem menuItem) {
        onMultipleItemAction(menuItem, new ArrayList<>(checked));
        cab.finish();
        unCheckAll();
        return true;
    }

    @Override
    public boolean onCabFinished(MaterialCab materialCab) {
        unCheckAll();
        return true;
    }
}
