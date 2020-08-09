package com.dkanada.gramophone.ui.fragments.mainactivity.library.pager;

import android.os.Bundle;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.StringRes;
import com.google.android.material.appbar.AppBarLayout;
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener;
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

public abstract class AbsLibraryPagerRecyclerViewFragment<A extends RecyclerView.Adapter, L extends RecyclerView.LayoutManager> extends AbsLibraryPagerFragment implements OnOffsetChangedListener {

    private Unbinder unbinder;

    @BindView(R.id.container)
    View container;
    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;
    @BindView(android.R.id.empty)
    TextView empty;

    private A adapter;
    private L layoutManager;

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

        initLayoutManager();
        initAdapter();
        initRecyclerView();
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

    private void checkIsEmpty() {
        if (empty != null) {
            empty.setText(getEmptyMessage());
            empty.setVisibility(adapter == null || adapter.getItemCount() == 0 ? View.VISIBLE : View.GONE);
        }
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

    @Override
    public void onOffsetChanged(AppBarLayout appBarLayout, int i) {
        container.setPadding(container.getPaddingLeft(), container.getPaddingTop(), container.getPaddingRight(), getLibraryFragment().getTotalAppBarScrollingRange() + i);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        getLibraryFragment().removeOnAppBarOffsetChangedListener(this);
        unbinder.unbind();
    }
}
