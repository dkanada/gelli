package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.artist.ArtistAdapter;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class ArtistsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<ArtistAdapter, GridLayoutManager> {
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

        ArtistAdapter adapter = new ArtistAdapter(getLibraryFragment().getMainActivity(), dataSet, itemLayoutRes, loadUsePalette(), getLibraryFragment());
        QueryUtil.getArtists(new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                dataSet.addAll((Collection<Artist>) media);
                adapter.notifyDataSetChanged();
            }
        });

        return adapter;
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
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
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
