package com.dkanada.gramophone.activities;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.base.AbsMusicContentActivity;
import com.dkanada.gramophone.adapter.SearchAdapter;
import com.dkanada.gramophone.databinding.ActivitySearchBinding;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;
import com.dkanada.gramophone.util.Util;

import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@SuppressLint("ClickableViewAccessibility")
public class SearchActivity extends AbsMusicContentActivity implements SearchView.OnQueryTextListener {
    private final String QUERY = "query";

    private ActivitySearchBinding binding;

    private SearchView searchView;

    private Handler handler;

    private SearchAdapter adapter;
    private String query;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setUpToolBar();

        handler = new Handler();
        adapter = new SearchAdapter(this, Collections.emptyList());

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                binding.empty.setVisibility(adapter.getItemCount() < 1 ? View.VISIBLE : View.GONE);
            }
        });

        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerView.setAdapter(adapter);
        binding.recyclerView.setOnTouchListener((v, event) -> {
            hideSoftKeyboard();
            return false;
        });
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(QUERY, query);
    }

    @Override
    protected void onRestoreInstanceState(@NonNull Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        search(savedInstanceState.getString(QUERY, ""));
    }

    @Override
    protected View createContentView() {
        binding = ActivitySearchBinding.inflate(getLayoutInflater());

        return wrapSlidingMusicPanel(binding.getRoot());
    }

    @Override
    public void onStateOnline() {
    }

    private void setUpToolBar() {
        binding.toolbar.setBackgroundColor(PreferenceUtil.getInstance(this).getPrimaryColor());
        setSupportActionBar(binding.toolbar);
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
        searchView.setOnQueryTextListener(SearchActivity.this);

        return super.onCreateOptionsMenu(menu);
    }

    private void search(@NonNull String query) {
        this.query = query;

        ItemQuery itemQuery = new ItemQuery();
        itemQuery.setSearchTerm(query);

        MediaCallback<Object> callback = media -> {
            List<Artist> artists = new ArrayList<>();
            List<Album> albums = new ArrayList<>();
            List<Song> songs = new ArrayList<>();

            for (Object result : media) {
                if (result instanceof Artist) {
                    artists.add((Artist) result);
                } else if (result instanceof Album) {
                    albums.add((Album) result);
                } else if (result instanceof Song) {
                    songs.add((Song) result);
                }
            }

            Collections.sort(artists, (one, two) -> one.name.compareTo(two.name));
            Collections.sort(albums, (one, two) -> one.title.compareTo(two.title));
            Collections.sort(songs, (one, two) -> one.title.compareTo(two.title));

            List<Object> sortedData = new ArrayList<>();
            sortedData.addAll(artists);
            sortedData.addAll(albums);
            sortedData.addAll(songs);

            adapter.swapDataSet(sortedData);
        };

        QueryUtil.getItems(itemQuery, callback);
    }

    @Override
    public boolean onQueryTextSubmit(String query) {
        hideSoftKeyboard();
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText) {
        handler.removeCallbacksAndMessages(null);
        handler.postDelayed(() -> search(newText), 1000);

        return false;
    }

    private void hideSoftKeyboard() {
        Util.hideSoftKeyboard(SearchActivity.this);
        if (searchView != null) {
            searchView.clearFocus();
        }
    }
}
