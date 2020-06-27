package com.dkanada.gramophone.ui.activities;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.SearchView;
import androidx.appcompat.widget.Toolbar;

import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.SearchAdapter;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsMusicServiceActivity;
import com.dkanada.gramophone.util.QueryUtil;
import com.dkanada.gramophone.util.Util;

import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class SearchActivity extends AbsMusicServiceActivity implements SearchView.OnQueryTextListener {

    public static final String QUERY = "query";

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    @BindView(R.id.toolbar)
    Toolbar toolbar;

    @BindView(android.R.id.empty)
    TextView empty;

    SearchView searchView;

    private Handler handler;

    private SearchAdapter adapter;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_search);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        setStatusbarColorAuto();
        setNavigationbarColorAuto();
        setTaskDescriptionColorAuto();

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        adapter = new SearchAdapter(this, Collections.emptyList());
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }
        });

        recyclerView.setAdapter(adapter);
        recyclerView.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });

        setUpToolBar();

        handler = new Handler();
        if (savedInstanceState != null) {
            query = savedInstanceState.getString(QUERY);
            search(query);
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString(QUERY, query);
    }

    private void setUpToolBar() {
        toolbar.setBackgroundColor(ThemeStore.primaryColor(this));
        setSupportActionBar(toolbar);
        // noinspection ConstantConditions
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_search, menu);

        final MenuItem searchItem = menu.findItem(R.id.search);
        searchView = (SearchView) searchItem.getActionView();
        searchView.setQueryHint(getString(R.string.action_search));
        searchView.setMaxWidth(Integer.MAX_VALUE);

        searchItem.expandActionView();
        searchItem.setOnActionExpandListener(new MenuItem.OnActionExpandListener() {
            @Override
            public boolean onMenuItemActionExpand(MenuItem item) {
                return true;
            }

            @Override
            public boolean onMenuItemActionCollapse(MenuItem item) {
                onBackPressed();
                return false;
            }
        });

        searchView.setQuery(query, false);
        searchView.post(() -> searchView.setOnQueryTextListener(SearchActivity.this));

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
        }

        return super.onOptionsItemSelected(item);
    }

    private void search(@NonNull String query) {
        this.query = query;
        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setSearchTerm(query);

        ArtistsQuery artistsQuery = new ArtistsQuery();
        artistsQuery.setSearchTerm(query);

        MediaCallback callback = new MediaCallback() {
            private final List<Object> data = new ArrayList<>();

            @Override
            public void onLoadMedia(List<?> media) {
                data.addAll(media);

                Collections.sort(data, (one, two) -> {
                    if (one.getClass() == Album.class || one.getClass() == Artist.class) {
                        if (two.getClass() == Song.class) return -1;
                    }

                    if (two.getClass() == Album.class || two.getClass() == Artist.class) {
                        if (one.getClass() == Song.class) return 1;
                    }

                    return 0;
                });

                adapter.swapDataSet(data);
            }
        };

        QueryUtil.getArtists(artistsQuery, callback);
        QueryUtil.getItems(itemQuery, callback);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideSoftKeyboard();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                search(newText);
            }
        }, 1000);

        return false;
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }
}
