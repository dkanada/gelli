package com.dkanada.gramophone.fragments.library;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.databinding.FragmentMainActivityRecyclerViewBinding;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.util.ViewUtil;

public abstract class AbsLibraryPagerRecyclerViewFragment<A extends RecyclerView.Adapter, L extends RecyclerView.LayoutManager, Q> extends AbsLibraryPagerFragment implements OnOffsetChangedListener {
    private FragmentMainActivityRecyclerViewBinding binding;

    private A adapter;
    private L layoutManager;
    private Q query;

    public int size;
    public boolean loading;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        binding = FragmentMainActivityRecyclerViewBinding.inflate(getLayoutInflater(), container, false);

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLibraryFragment().addOnAppBarOffsetChangedListener(this);

        initAdapter();
        initLayoutManager();
        initQuery();

        initRecyclerView();
        loadItems(0);
    }

    private void initAdapter() {
        adapter = createAdapter();
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });
    }

    private void initLayoutManager() {
        layoutManager = createLayoutManager();
    }

    private void initQuery() {
        query = createQuery();
    }

    private void initRecyclerView() {
        ViewUtil.setUpFastScrollRecyclerViewColor(getActivity(), binding.recyclerView, PreferenceUtil.getInstance(requireActivity()).getAccentColor());

        binding.recyclerView.setLayoutManager(layoutManager);
        binding.recyclerView.setAdapter(adapter);
    }

    protected void invalidateAdapter() {
        initAdapter();
        initQuery();

        binding.recyclerView.setAdapter(adapter);
        loadItems(0);
    }

    protected void invalidateLayoutManager() {
        initLayoutManager();
        binding.recyclerView.setLayoutManager(layoutManager);
    }

    protected RecyclerView getRecyclerView() {
        return binding.recyclerView;
    }

    protected A getAdapter() {
        return adapter;
    }

    protected L getLayoutManager() {
        return layoutManager;
    }

    protected Q getQuery() {
        return query;
    }

    @StringRes
    protected int getEmptyMessage() {
        return R.string.empty;
    }

    @NonNull
    protected abstract A createAdapter();

    @NonNull
    protected abstract L createLayoutManager();

    @NonNull
    protected abstract Q createQuery();

    protected abstract void loadItems(int index);

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        binding.container.setPadding(binding.container.getPaddingLeft(), binding.container.getPaddingTop(), binding.container.getPaddingRight(), getLibraryFragment().getTotalAppBarScrollingRange() + i);

        int last = 0;
        if (!loading && getLayoutManager() instanceof GridLayoutManager) {
            GridLayoutManager layoutManager = (GridLayoutManager) getLayoutManager();
            last = layoutManager.findLastVisibleItemPosition();
        } else if (!loading && getLayoutManager() instanceof LinearLayoutManager) {
            LinearLayoutManager layoutManager = (LinearLayoutManager) getLayoutManager();
            last = layoutManager.findLastVisibleItemPosition();
        }

        int page = PreferenceUtil.getInstance(App.getInstance()).getPageSize();
        int total = getAdapter().getItemCount();
        if (last > total - page / 2 && total < size) {
            query = createQuery();
            loading = true;

            loadItems(getAdapter().getItemCount());
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getLibraryFragment().removeOnAppBarOffsetChangedListener(this);
    }

    private void checkIsEmpty() {
        binding.empty.setText(getEmptyMessage());
        binding.empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
    }
}
