package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.artist.ArtistAdapter;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.helper.sort.SortOrder;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemFields;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.List;

public class ArtistsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<ArtistAdapter, GridLayoutManager, ArtistsQuery> {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @NonNull
    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @NonNull
    @Override
    protected ArtistAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);

        List<Artist> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();
        return new ArtistAdapter(getLibraryFragment().getMainActivity(), dataSet, itemLayoutRes, loadUsePalette(), getLibraryFragment());
    }

    @NonNull
    @Override
    protected ArtistsQuery createQuery() {
        ArtistsQuery query = new ArtistsQuery();

        query.setFields(new ItemFields[]{ItemFields.Genres});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setRecursive(true);
        query.setLimit(PreferenceUtil.getInstance(App.getInstance()).getPageSize());
        query.setStartIndex(getAdapter().getItemCount());
        query.setParentId(QueryUtil.currentLibrary.getId());

        return query;
    }

    @Override
    protected void loadItems(int index) {
        ArtistsQuery query = getQuery();
        query.setStartIndex(index);

        App.getApiClient().GetAlbumArtistsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                if (index == 0) getAdapter().getDataSet().clear();
                for (BaseItemDto itemDto : result.getItems()) {
                    getAdapter().getDataSet().add(new Artist(itemDto));
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
        return R.string.no_artists;
    }

    @LayoutRes
    protected int getItemLayoutRes() {
        if (getGridSize() > getMaxGridSizeForList()) {
            return R.layout.item_grid;
        }

        return R.layout.item_list_single_row;
    }

    @Override
    protected String loadSortMethod() {
        return SortMethod.NAME;
    }

    @Override
    protected void saveSortMethod(String sortMethod) {
        // not supported through API
    }

    @Override
    protected void setSortMethod(String sortMethod) {
    }

    @Override
    protected String loadSortOrder() {
        return SortOrder.ASCENDING;
    }

    @Override
    protected void saveSortOrder(String sortOrder) {
        // not supported through API
    }

    @Override
    protected void setSortOrder(String sortOrder) {
    }

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance(getActivity()).getArtistGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setArtistGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance(getActivity()).getArtistGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setArtistGridSizeLand(gridSize);
    }

    @Override
    protected void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance(getActivity()).setArtistColoredFooters(usePalette);
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).getArtistColoredFooters();
    }

    @Override
    protected void setUsePalette(boolean usePalette) {
        getAdapter().usePalette(usePalette);
    }

    @Override
    protected void setGridSize(int gridSize) {
        getLayoutManager().setSpanCount(gridSize);
        getAdapter().notifyDataSetChanged();
    }
}
