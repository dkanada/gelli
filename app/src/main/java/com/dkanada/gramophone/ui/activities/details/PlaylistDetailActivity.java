package com.dkanada.gramophone.ui.activities.details;

import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.afollestad.materialcab.MaterialCab;
import com.dkanada.gramophone.databinding.ActivityPlaylistDetailBinding;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.song.OrderablePlaylistSongAdapter;
import com.dkanada.gramophone.adapter.song.SongAdapter;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.helper.menu.PlaylistMenuHelper;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.model.PlaylistSong;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.util.PlaylistUtil;
import com.dkanada.gramophone.util.ViewUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jellyfin.apiclient.model.playlists.PlaylistItemQuery;

import java.util.ArrayList;
import java.util.List;

public class PlaylistDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder {
    public static String EXTRA_PLAYLIST = "extra_playlist";

    private ActivityPlaylistDetailBinding binding;

    private Playlist playlist;

    private MaterialCab cab;
    private SongAdapter adapter;

    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDrawUnderStatusbar();
        setStatusbarColorAuto();

        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        playlist = getIntent().getExtras().getParcelable(EXTRA_PLAYLIST);

        setUpRecyclerView();
        setUpToolbar();

        PlaylistItemQuery query = new PlaylistItemQuery();
        query.setId(playlist.id);
        PlaylistUtil.getPlaylist(query, new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                adapter.getDataSet().addAll((List<PlaylistSong>) media);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected View createContentView() {
        binding = ActivityPlaylistDetailBinding.inflate(getLayoutInflater());

        return wrapSlidingMusicPanel(binding.getRoot());
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(this, ((FastScrollRecyclerView) binding.recyclerView), ThemeStore.accentColor(this));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();
        adapter = new OrderablePlaylistSongAdapter(this, new ArrayList<>(), R.layout.item_list, false, this, (fromPosition, toPosition) -> {
            PlaylistUtil.moveItem(playlist.id, (PlaylistSong) adapter.getDataSet().get(fromPosition), toPosition);
            Song song = adapter.getDataSet().remove(fromPosition);
            adapter.getDataSet().add(toPosition, song);
            adapter.notifyItemMoved(fromPosition, toPosition);
        });

        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(adapter);

        binding.recyclerView.setAdapter(wrappedAdapter);
        binding.recyclerView.setItemAnimator(animator);

        recyclerViewDragDropManager.attachRecyclerView(binding.recyclerView);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpToolbar() {
        binding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(binding.toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setToolbarTitle(playlist.name);
    }

    private void setToolbarTitle(String title) {
        // noinspection ConstantConditions
        getSupportActionBar().setTitle(title);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_playlist_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_playlist:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return PlaylistMenuHelper.handleMenuClick(this, playlist, item);
    }

    @NonNull
    @Override
    public MaterialCab openCab(final int menu, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(this, R.id.cab_stub)
                .setMenu(menu)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(ThemeUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(this)))
                .start(callback);

        return cab;
    }

    @Override
    public void onBackPressed() {
        if (cab != null && cab.isActive()) {
            cab.finish();
        } else {
            binding.recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    private void checkIsEmpty() {
        binding.empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }

        super.onPause();
    }

    @Override
    protected void onDestroy() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        binding.recyclerView.setItemAnimator(null);
        binding.recyclerView.setAdapter(null);

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }

        adapter = null;
        super.onDestroy();
    }
}
