package com.dkanada.gramophone.activities.details;

import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.dkanada.gramophone.BuildConfig;
import com.dkanada.gramophone.activities.base.AbsMusicContentActivity;
import com.dkanada.gramophone.databinding.ActivityAlbumDetailBinding;
import com.google.android.material.appbar.AppBarLayout;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.song.AlbumSongAdapter;
import com.dkanada.gramophone.dialogs.AddToPlaylistDialog;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.interfaces.PaletteColorHolder;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.List;

public class AlbumDetailActivity extends AbsMusicContentActivity implements PaletteColorHolder, CabHolder, AppBarLayout.OnOffsetChangedListener {
    public static final String EXTRA_ALBUM = BuildConfig.APPLICATION_ID + ".extra.album";

    private ActivityAlbumDetailBinding binding;

    private MaterialCab cab;
    private int headerViewHeight;
    private int toolbarColor;

    private Album album;
    private AlbumSongAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        album = getIntent().getParcelableExtra(EXTRA_ALBUM);

        super.onCreate(savedInstanceState);

        setDrawUnderStatusBar();
        setUpObservableListViewParams();
        setUpToolbar();
        setUpViews();

        loadAlbumCover(album);
        setAlbum(album);
    }

    @Override
    public void onStateOnline() {
        ItemQuery query = new ItemQuery();
        query.setParentId(album.id);
        query.setSortBy(new String[]{"ParentIndexNumber", "IndexNumber"});

        QueryUtil.getSongs(query, media -> {
            album.songs = media;
            setAlbum(album);
        });
    }

    @Override
    public void onOffsetChanged (AppBarLayout appBarLayout, int verticalOffset) {
        float headerAlpha = Math.max(0, Math.min(1, 1 + (2 * (float) verticalOffset / headerViewHeight)));
        binding.header.setAlpha(headerAlpha);
    }

    @Override
    protected View createContentView() {
        binding = ActivityAlbumDetailBinding.inflate(getLayoutInflater());

        return wrapSlidingMusicPanel(binding.getRoot());
    }

    private void setUpObservableListViewParams() {
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
    }

    private void setUpViews() {
        setUpRecyclerView();
        setUpSongsAdapter();
        binding.artistText.setOnClickListener(v -> {
            NavigationUtil.startArtist(AlbumDetailActivity.this, new Artist(album), null);
        });

        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor));
    }

    private void loadAlbumCover(Album album) {
        CustomGlideRequest.Builder
                .from(this, album.primary, album.blurHash)
                .palette().build().dontAnimate()
                .into(new CustomPaletteTarget(binding.image) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    private void setColors(int color) {
        toolbarColor = color;
        binding.appBarLayout.setBackgroundColor(color);

        setNavigationBarColor(color);
        setTaskDescriptionColor(color);

        binding.toolbar.setBackgroundColor(color);
        // needed to auto readjust the toolbar content color
        setSupportActionBar(binding.toolbar);
        setStatusBarColor(color);

        int secondaryTextColor = MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color));
        binding.artistIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        binding.durationIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        binding.songCountIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        binding.albumYearIcon.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);

        binding.artistText.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
        binding.durationText.setTextColor(secondaryTextColor);
        binding.songCountText.setTextColor(secondaryTextColor);
        binding.albumYearText.setTextColor(secondaryTextColor);
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setUpRecyclerView() {
        binding.appBarLayout.addOnOffsetChangedListener(this);
    }

    private void setUpToolbar() {
        setSupportActionBar(binding.toolbar);
        binding.toolbar.setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSongsAdapter() {
        adapter = new AlbumSongAdapter(this, album.songs, R.layout.item_list, false, this);
        binding.list.setLayoutManager(new GridLayoutManager(this, 1));
        binding.list.setAdapter(adapter);
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                if (adapter.getItemCount() == 0) finish();
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_detail_album, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final List<Song> songs = adapter.getDataSet();
        switch (id) {
            case R.id.action_shuffle_album:
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
            case R.id.action_go_to_artist:
                NavigationUtil.startArtist(this, new Artist(album), null);
                return true;
            case R.id.action_download:
                NavigationUtil.startDownload(this, songs);
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
                .setBackgroundColor(getPaletteColor())
                .start(callback);
        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) {
            cab.finish();
        } else {
            binding.list.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void setStatusBarColor(int color) {
        super.setStatusBarColor(color);
        setLightStatusBar(false);
    }

    private void setAlbum(Album album) {
        this.album = album;

        binding.toolbar.setTitle(album.title);
        binding.artistText.setText(album.artistName);
        binding.songCountText.setText(MusicUtil.getSongCountString(this, album.songs.size()));
        binding.durationText.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, album.songs)));
        binding.albumYearText.setText(MusicUtil.getYearString(album.year));

        if (album.songs.size() != 0) adapter.swapDataSet(album.songs);
    }
}
