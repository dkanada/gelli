package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.util.ViewUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public abstract class AbsLibraryPagerRecyclerViewFragment<A extends RecyclerView.Adapter, L extends RecyclerView.LayoutManager, Q> extends AbsLibraryPagerFragment implements OnOffsetChangedListener {

    private Unbinder unbinder;

    @BindView(R.id.container)
    View container;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(android.R.id.empty)
    TextView empty;

    private A adapter;
    private L layoutManager;
    private Q query;

    public int size;
    public boolean loading;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(getLayoutRes(), container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        getLibraryFragment().addOnAppBarOffsetChangedListener(this);

        initAdapter();
        initLayoutManager();

        initRecyclerView();
    }

    private void initAdapter() {
        adapter = createAdapter();
        query = createQuery();

        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onChanged() {
                super.onChanged();
                checkIsEmpty();
            }
        });

        loadItems();
    }

    private void initLayoutManager() {
        layoutManager = createLayoutManager();
    }

    private void initRecyclerView() {
        if (recyclerView instanceof FastScrollRecyclerView) {
            ViewUtil.setUpFastScrollRecyclerViewColor(getActivity(), ((FastScrollRecyclerView) recyclerView), ThemeStore.accentColor(getActivity()));
        }

        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setAdapter(adapter);
    }

    protected void invalidateAdapter() {
        initAdapter();
        recyclerView.setAdapter(adapter);
    }

    protected void invalidateLayoutManager() {
        initLayoutManager();
        recyclerView.setLayoutManager(layoutManager);
    }

    protected RecyclerView getRecyclerView() {
        return recyclerView;
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

    @LayoutRes
    protected int getLayoutRes() {
        return R.layout.fragment_main_activity_recycler_view;
    }

    @NonNull
    protected abstract A createAdapter();

    @NonNull
    protected abstract L createLayoutManager();

    @NonNull
    protected abstract Q createQuery();

    protected abstract void loadItems();

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(), getLibraryFragment().getTotalAppBarScrollingRange() + i);

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
            loadItems();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getLibraryFragment().removeOnAppBarOffsetChangedListener(this);
        unbinder.unbind();
    }

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setText(getEmptyMessage());
            empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
    }
}
