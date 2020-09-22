package com.dkanada.gramophone.ui.activities;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.bumptech.glide.Glide;
import com.dkanada.gramophone.adapter.song.SongAdapter;
import com.dkanada.gramophone.databinding.ActivityArtistDetailBinding;
import com.google.android.material.appbar.AppBarLayout;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.album.HorizontalAlbumAdapter;
import com.dkanada.gramophone.dialogs.AddToPlaylistDialog;
import com.dkanada.gramophone.dialogs.SleepTimerDialog;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.interfaces.PaletteColorHolder;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.model.querying.ItemQuery;

public class ArtistDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder, AppBarLayout.OnOffsetChangedListener {
    public static final String EXTRA_ARTIST = "extra_artist";

    private ActivityArtistDetailBinding binding;

    private MaterialCab cab;
    private int headerViewHeight;
    private int toolbarColor;

    private Artist artist;
    private HorizontalAlbumAdapter albumAdapter;
    private SongAdapter songAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDrawUnderStatusbar();
        setUpObservableListViewParams();
        setUpToolbar();
        setUpViews();

        usePalette = PreferenceUtil.getInstance(this).getAlbumArtistColoredFooters();

        Artist artist = getIntent().getExtras().getParcelable(EXTRA_ARTIST);
        loadArtistImage(artist.primary);
        setArtist(artist);

        ItemQuery albums = new ItemQuery();
        albums.setArtistIds(new String[]{artist.id});
        QueryUtil.getAlbums(albums, new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                artist.albums = (List<Album>) media;
                setArtist(artist);
            }
        });

        ItemQuery songs = new ItemQuery();
        songs.setArtistIds(new String[]{artist.id});
        QueryUtil.getSongs(songs, new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                artist.songs = (List<Song>) media;
                setArtist(artist);
            }
        });
    }

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int verticalOffset) {
        float headerAlpha = Math.max(0, Math.min(1, 1 + (2 * (float) verticalOffset / headerViewHeight)));
        binding.header.setAlpha(headerAlpha);
    }

    @Override
    protected View createContentView() {
        binding = ActivityArtistDetailBinding.inflate(getLayoutInflater());

        return wrapSlidingMusicPanel(binding.getRoot());
    }

    private boolean usePalette;

    private void setUpObservableListViewParams() {
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
    }

    private void setUpViews() {
        setUpSongListView();
        setUpAlbumRecyclerView();
        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor));
    }

    private void setUpSongListView() {
        binding.appBarLayout.addOnOffsetChangedListener(this);

        songAdapter = new SongAdapter(this, getArtist().songs, R.layout.item_list, false, this);

        binding.songs.setLayoutManager(new GridLayoutManager(this, 1));
        binding.songs.setAdapter(songAdapter);
    }

    private void setUpAlbumRecyclerView() {
        binding.albums.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        albumAdapter = new HorizontalAlbumAdapter(this, getArtist().albums, usePalette, this);
        binding.albums.setAdapter(albumAdapter);
        albumAdapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (albumAdapter.getItemCount() == 0) finish();
            }
        });
    }

    protected void setUsePalette(boolean usePalette) {
        albumAdapter.usePalette(usePalette);
        PreferenceUtil.getInstance(this).setAlbumArtistColoredFooters(usePalette);
        this.usePalette = usePalette;
    }

    private void loadArtistImage(String primary) {
        CustomGlideRequest.Builder
                .from(Glide.with(this), primary)
                .palette(this).build()
                .dontAnimate()
                .into(new CustomPaletteTarget(binding.image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setColors(int color) {
        toolbarColor = color;
        binding.appBarLayout.setBackgroundColor(color);

        setNavigationbarColor(color);
        setTaskDescriptionColor(color);

        binding.toolbar.setBackgroundColor(color);
        // needed to auto readjust the toolbar content color
        setSupportActionBar(binding.toolbar);
        setStatusbarColor(color);

        int secondaryTextColor = MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color));
        binding.durationIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        binding.songCountIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        binding.albumCountIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);

        binding.durationText.setTextColor(secondaryTextColor);
        binding.songCountText.setTextColor(secondaryTextColor);
        binding.albumCountText.setTextColor(secondaryTextColor);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_artist_detail, menu);
        menu.findItem(R.id.action_colored_footers).setChecked(usePalette);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final List<Song> songs = songAdapter.getDataSet();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
            case R.id.action_shuffle_artist:
                MusicPlayerRemote.openAndShuffleQueue(songs, true);
                return true;
            case R.id.action_play_next:
                MusicPlayerRemote.playNext(songs);
                return true;
            case R.id.action_add_to_queue:
                MusicPlayerRemote.enqueue(songs);
                return true;
            case R.id.action_add_to_playlist:
                AddToPlaylistDialog.create(songs).show(getSupportFragmentManager(), "ADD_PLAYLIST");
                return true;
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_colored_footers:
                item.setChecked(!item.isChecked());
                setUsePalette(item.isChecked());
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @NonNull
    @Override
    public MaterialCab openCab(int menuRes, @NonNull final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(ThemeUtil.shiftBackgroundColorForLightText(getPaletteColor()))
                .start(new MaterialCab.Callback() {
                    @Override
                    public boolean onCabCreated(MaterialCab materialCab, Menu menu) {
                        return callback.onCabCreated(materialCab, menu);
                    }

                    @Override
                    public boolean onCabItemClicked(MenuItem menuItem) {
                        return callback.onCabItemClicked(menuItem);
                    }

                    @Override
                    public boolean onCabFinished(MaterialCab materialCab) {
                        return callback.onCabFinished(materialCab);
                    }
                });
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) {
            cab.finish();
        } else {
            binding.albums.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void setStatusbarColor(int color) {
        super.setStatusbarColor(color);
        setLightStatusbar(false);
    }

    private void setArtist(Artist artist) {
        this.artist = artist;

        getSupportActionBar().setTitle(artist.name);
        binding.songCountText.setText(MusicUtil.getSongCountString(this, artist.songs.size()));
        binding.albumCountText.setText(MusicUtil.getAlbumCountString(this, artist.albums.size()));
        binding.durationText.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, artist.songs)));

        if (artist.songs.size() != 0) songAdapter.swapDataSet(artist.songs);
        if (artist.albums.size() != 0) albumAdapter.swapDataSet(artist.albums);
    }

    private Artist getArtist() {
        if (artist == null) artist = new Artist();
        return artist;
    }
}
