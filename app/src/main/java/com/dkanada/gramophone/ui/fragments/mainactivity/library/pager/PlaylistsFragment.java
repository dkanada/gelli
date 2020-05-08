package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.PlaylistAdapter;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.util.QueryUtil;

import java.util.ArrayList;
import java.util.List;

public class PlaylistsFragment extends AbsLibraryPagerRecyclerViewFragment<PlaylistAdapter, LinearLayoutManager> {
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
    protected PlaylistAdapter createAdapter() {
        List<Playlist> dataSet = getAdapter() == null ? new ArrayList<>() : getAdapter().getDataSet();

        PlaylistAdapter adapter = new PlaylistAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_single_row, getLibraryFragment());
        QueryUtil.getPlaylists(new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                dataSet.addAll((List<Playlist>) media);
                adapter.notifyDataSetChanged();
            }
        });

        return adapter;
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_playlists;
    }

    @Override
    public void onMediaStoreChanged() {
    }
}
