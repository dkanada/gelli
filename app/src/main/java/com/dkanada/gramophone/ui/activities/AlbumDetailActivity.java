package com.dkanada.gramophone.ui.activities;

import android.graphics.PorterDuff;
import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.materialcab.MaterialCab;
import com.afollestad.materialdialogs.util.DialogUtils;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.material.appbar.AppBarLayout;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.song.AlbumSongAdapter;
import com.dkanada.gramophone.dialogs.AddToPlaylistDialog;
import com.dkanada.gramophone.dialogs.SleepTimerDialog;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.interfaces.PaletteColorHolder;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AlbumDetailActivity extends AbsSlidingMusicPanelActivity implements PaletteColorHolder, CabHolder, AppBarLayout.OnOffsetChangedListener {
    public static final String EXTRA_ALBUM = "extra_album";

    private Album album;

    @BindView(R.id.app_bar_layout)
    AppBarLayout appBarLayout;
    @BindView(R.id.list)
    RecyclerView recyclerView;
    @BindView(R.id.image)
    ImageView albumArtImageView;
    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.header)
    View headerView;

    @BindView(R.id.artist_icon)
    ImageView artistIconImageView;
    @BindView(R.id.duration_icon)
    ImageView durationIconImageView;
    @BindView(R.id.song_count_icon)
    ImageView songCountIconImageView;
    @BindView(R.id.album_year_icon)
    ImageView albumYearIconImageView;
    @BindView(R.id.artist_text)
    TextView artistTextView;
    @BindView(R.id.duration_text)
    TextView durationTextView;
    @BindView(R.id.song_count_text)
    TextView songCountTextView;
    @BindView(R.id.album_year_text)
    TextView albumYearTextView;

    private AlbumSongAdapter adapter;

    private MaterialCab cab;
    private int headerViewHeight;
    private int toolbarColor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ButterKnife.bind(this);

        setDrawUnderStatusbar();
        setUpObservableListViewParams();
        setUpToolbar();
        setUpViews();

        if (Build.VERSION.SDK_INT > 21) postponeEnterTransition();
        Album album = getIntent().getExtras().getParcelable(EXTRA_ALBUM);
        loadAlbumCover(album.primary);
        setAlbum(album);

        ItemQuery query = new ItemQuery();
        query.setParentId(album.id);
        query.setSortBy(new String[]{"ParentIndexNumber", "IndexNumber"});

        QueryUtil.getSongs(query, new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                album.songs = (List<Song>) media;
                setAlbum(album);
            }
        });
    }

    @Override
    public void onOffsetChanged (AppBarLayout appBarLayout, int verticalOffset) {
        float headerAlpha = Math.max(0, Math.min(1, 1 + (2 * (float) verticalOffset / headerViewHeight)));
        headerView.setAlpha(headerAlpha);
    }

    @Override
    protected View createContentView() {
        return wrapSlidingMusicPanel(R.layout.activity_album_detail);
    }

    private void setUpObservableListViewParams() {
        headerViewHeight = getResources().getDimensionPixelSize(R.dimen.detail_header_height);
    }

    private void setUpViews() {
        setUpRecyclerView();
        setUpSongsAdapter();
        artistTextView.setOnClickListener(v -> {
            if (album != null) {
                NavigationUtil.goToArtist(AlbumDetailActivity.this, new Artist(album));
            }
        });

        setColors(DialogUtils.resolveColor(this, R.attr.defaultFooterColor));
    }

    private void loadAlbumCover(String primary) {
        CustomGlideRequest.Builder
                .from(Glide.with(this), primary)
                .generatePalette(this).build()
                .listener(new RequestListener<Object, BitmapPaletteWrapper>() {
                    @Override
                    public boolean onException(Exception e, Object model, Target<BitmapPaletteWrapper> target, boolean isFirstResource) {
                        if (Build.VERSION.SDK_INT > 21) startPostponedEnterTransition();
                        return false;
                    }

                    @Override
                    public boolean onResourceReady(BitmapPaletteWrapper resource, Object model, Target<BitmapPaletteWrapper> target, boolean dataSource, boolean isFirstResource) {
                        if (Build.VERSION.SDK_INT > 21) startPostponedEnterTransition();
                        return false;
                    }
                })
                .dontAnimate()
                .into(new CustomPaletteTarget(albumArtImageView) {
                    @Override
                    public void onColorReady(int color) {
                        setColors(color);
                    }
                });
    }

    private void setColors(int color) {
        toolbarColor = color;
        appBarLayout.setBackgroundColor(color);

        setNavigationbarColor(color);
        setTaskDescriptionColor(color);

        toolbar.setBackgroundColor(color);
        // needed to auto readjust the toolbar content color
        setSupportActionBar(toolbar);
        setStatusbarColor(color);

        int secondaryTextColor = MaterialValueHelper.getSecondaryTextColor(this, ColorUtil.isColorLight(color));
        artistIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        durationIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        songCountIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);
        albumYearIconImageView.setColorFilter(secondaryTextColor, PorterDuff.Mode.SRC_IN);

        artistTextView.setTextColor(MaterialValueHelper.getPrimaryTextColor(this, ColorUtil.isColorLight(color)));
        durationTextView.setTextColor(secondaryTextColor);
        songCountTextView.setTextColor(secondaryTextColor);
        albumYearTextView.setTextColor(secondaryTextColor);
    }

    @Override
    public int getPaletteColor() {
        return toolbarColor;
    }

    private void setUpRecyclerView() {
        appBarLayout.addOnOffsetChangedListener(this);
    }

    private void setUpToolbar() {
        setSupportActionBar(toolbar);
        //noinspection ConstantConditions
        getSupportActionBar().setTitle(null);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setUpSongsAdapter() {
        adapter = new AlbumSongAdapter(this, getAlbum().songs, R.layout.item_list, false, this);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(adapter);
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
        getMenuInflater().inflate(R.menu.menu_album_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        final List<Song> songs = adapter.getDataSet();
        switch (id) {
            case R.id.action_sleep_timer:
                new SleepTimerDialog().show(getSupportFragmentManager(), "SET_SLEEP_TIMER");
                return true;
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
            case android.R.id.home:
                super.onBackPressed();
                return true;
            case R.id.action_go_to_artist:
                NavigationUtil.goToArtist(this, new Artist(album));
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
            recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    @Override
    public void setStatusbarColor(int color) {
        super.setStatusbarColor(color);
        setLightStatusbar(false);
    }

    private void setAlbum(Album album) {
        this.album = album;

        getSupportActionBar().setTitle(album.title);
        artistTextView.setText(album.artistName);
        songCountTextView.setText(MusicUtil.getSongCountString(this, album.songs.size()));
        durationTextView.setText(MusicUtil.getReadableDurationString(MusicUtil.getTotalDuration(this, album.songs)));
        albumYearTextView.setText(MusicUtil.getYearString(album.year));

        if (album.songs.size() != 0) adapter.swapDataSet(album.songs);
    }

    private Album getAlbum() {
        if (album == null) album = new Album();
        return album;
    }
}
