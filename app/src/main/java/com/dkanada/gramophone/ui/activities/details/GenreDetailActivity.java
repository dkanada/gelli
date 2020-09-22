package com.dkanada.gramophone.ui.activities.details;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.afollestad.materialcab.MaterialCab;
import com.dkanada.gramophone.databinding.ActivityGenreDetailBinding;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.song.SongAdapter;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.util.QueryUtil;
import com.dkanada.gramophone.util.ViewUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.ArrayList;
import java.util.List;

public class GenreDetailActivity extends AbsSlidingMusicPanelActivity implements CabHolder {
    public static final String EXTRA_GENRE = "extra_genre";

    private ActivityGenreDetailBinding binding;

    private Genre genre;

    private MaterialCab cab;
    private SongAdapter adapter;

    private RecyclerView.Adapter wrappedAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setDrawUnderStatusbar();
        setStatusbarColorAuto();

        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        genre = getIntent().getExtras().getParcelable(EXTRA_GENRE);

        setUpRecyclerView();
        setUpToolBar();

        ItemQuery query = new ItemQuery();
        query.setGenreIds(new String[]{genre.id});
        QueryUtil.getSongs(query, new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                adapter.getDataSet().addAll((List<Song>) media);
                adapter.notifyDataSetChanged();
            }
        });
    }

    @Override
    protected View createContentView() {
        binding = ActivityGenreDetailBinding.inflate(getLayoutInflater());

        return wrapSlidingMusicPanel(binding.getRoot());
    }

    private void setUpRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(this, ((FastScrollRecyclerView) binding.recyclerView), ThemeStore.accentColor(this));
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new SongAdapter(this, new ArrayList<>(), R.layout.item_list, false, this);
        binding.recyclerView.setAdapter(adapter);

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void setUpToolBar() {
        binding.toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(binding.toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setTitle(genre.name);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_genre_detail, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_genre:
                MusicPlayerRemote.openAndShuffleQueue(adapter.getDataSet(), true);
                return true;
            case android.R.id.home:
                onBackPressed();
                return true;
        }

        return super.onOptionsItemSelected(item);
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
        if (cab != null && cab.isActive()) cab.finish();
        else {
            binding.recyclerView.stopScroll();
            super.onBackPressed();
        }
    }

    private void checkIsEmpty() {
        binding.empty.setVisibility(adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }

    @Override
    protected void onDestroy() {
        binding.recyclerView.setAdapter(null);

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }

        adapter = null;
        super.onDestroy();
    }
}
