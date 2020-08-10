package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.GenreAdapter;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ItemsByNameQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.List;

public class GenresFragment extends AbsLibraryPagerRecyclerViewFragment<GenreAdapter, LinearLayoutManager, ItemsByNameQuery> {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    protected LinearLayoutManager createLayoutManager() {
        return new LinearLayoutManager(getActivity());
    }

    @NonNull
    @Override
    protected GenreAdapter createAdapter() {
        List<Genre> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new GenreAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_single_row);
    }

    @NonNull
    @Override
    protected ItemsByNameQuery createQuery() {
        ItemsByNameQuery query = new ItemsByNameQuery();

        query.setParentId(QueryUtil.currentLibrary.getId());
        query.setLimit(PreferenceUtil.getInstance(App.getInstance()).getMaximumListSize());
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setStartIndex(getAdapter().getItemCount());
        query.setRecursive(true);

        return query;
    }

    @Override
    protected void loadItems() {
        App.getApiClient().GetGenresAsync(getQuery(), new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                for (BaseItemDto itemDto : result.getItems()) {
                    getAdapter().getDataSet().add(new Genre(itemDto));
                }

                size = result.getTotalRecordCount();
                getAdapter().notifyDataSetChanged();
                loading = false;
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_genres;
    }
}
