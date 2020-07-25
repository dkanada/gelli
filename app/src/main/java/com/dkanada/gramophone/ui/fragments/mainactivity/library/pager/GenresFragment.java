package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.GenreAdapter;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.util.QueryUtil;

import java.util.ArrayList;
import java.util.List;

public class GenresFragment extends AbsLibraryPagerRecyclerViewFragment<GenreAdapter, LinearLayoutManager> {
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

        GenreAdapter adapter = new GenreAdapter(getLibraryFragment().getMainActivity(), dataSet, R.layout.item_list_single_row);
        QueryUtil.getGenres(new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
                dataSet.addAll((List<Genre>) media);
                adapter.notifyDataSetChanged();
            }
        });

        return adapter;
    }

    @Override
    protected int getEmptyMessage() {
        return R.string.no_genres;
    }
}
