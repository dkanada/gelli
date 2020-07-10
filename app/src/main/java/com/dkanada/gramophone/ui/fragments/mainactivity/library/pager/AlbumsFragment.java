package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import androidx.recyclerview.widget.GridLayoutManager;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.album.AlbumAdapter;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.model.querying.ItemQuery;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class AlbumsFragment extends AbsLibraryPagerRecyclerViewCustomGridSizeFragment<AlbumAdapter, GridLayoutManager> {
    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
    }

    @Override
    protected GridLayoutManager createLayoutManager() {
        return new GridLayoutManager(getActivity(), getGridSize());
    }

    @Override
    protected AlbumAdapter createAdapter() {
        int itemLayoutRes = getItemLayoutRes();
        notifyLayoutResChanged(itemLayoutRes);
        List<Album> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();

        AlbumAdapter adapter = new AlbumAdapter(getLibraryFragment().getMainActivity(), dataSet, itemLayoutRes, loadUsePalette(), getLibraryFragment());
        QueryUtil.getAlbums(new ItemQuery(), new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                dataSet.clear();
                dataSet.addAll((Collection<Album>) media);
                adapter.notifyDataSetChanged();
            }
        });

        return adapter;
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_albums;
    }

    @Override
    protected String loadSortMethod() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumSortMethod();
    }

    @Override
    protected void saveSortMethod(String sortMethod) {
        PreferenceUtil.getInstance(getActivity()).setAlbumSortMethod(sortMethod);
    }

    @Override
    protected void setSortMethod(String sortMethod) {
    }

    @Override
    public boolean loadUsePalette() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumColoredFooters();
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

    @Override
    protected int loadGridSize() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumGridSize(getActivity());
    }

    @Override
    protected void saveGridSize(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setAlbumGridSize(gridSize);
    }

    @Override
    protected int loadGridSizeLand() {
        return PreferenceUtil.getInstance(getActivity()).getAlbumGridSizeLand(getActivity());
    }

    @Override
    protected void saveGridSizeLand(int gridSize) {
        PreferenceUtil.getInstance(getActivity()).setAlbumGridSizeLand(gridSize);
    }

    @Override
    protected void saveUsePalette(boolean usePalette) {
        PreferenceUtil.getInstance(getActivity()).setAlbumColoredFooters(usePalette);
    }

    @Override
    public void onMediaStoreChanged() {
        super.onMediaStoreChanged();
    }
}
