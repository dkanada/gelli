package com.dkanada.gramophone.ui.fragments.mainactivity.library;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dkanada.gramophone.databinding.FragmentLibraryBinding;
import com.google.android.material.appbar.AppBarLayout;
import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.SubMenu;
import android.view.View;
import android.view.ViewGroup;

import com.afollestad.materialcab.MaterialCab;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.common.ATHToolbarActivity;
import com.kabouzeid.appthemehelper.util.TabLayoutUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.MusicLibraryPagerAdapter;
import com.dkanada.gramophone.dialogs.CreatePlaylistDialog;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.helper.sort.SortOrder;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.loader.SongLoader;
import com.dkanada.gramophone.ui.activities.MainActivity;
import com.dkanada.gramophone.ui.activities.SearchActivity;
import com.dkanada.gramophone.ui.fragments.mainactivity.AbsMainActivityFragment;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.pager.AbsLibraryPagerRecyclerViewCustomGridSizeFragment;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.pager.AlbumsFragment;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.pager.ArtistsFragment;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.pager.PlaylistsFragment;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.pager.SongsFragment;
import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.Util;

public class LibraryFragment extends AbsMainActivityFragment implements CabHolder, MainActivity.MainActivityFragmentCallbacks, ViewPager.OnPageChangeListener, SharedPreferences.OnSharedPreferenceChangeListener {

    private FragmentLibraryBinding binding;

    private MusicLibraryPagerAdapter pagerAdapter;
    private MaterialCab cab;

    public static LibraryFragment newInstance() {
        return new LibraryFragment();
    }

    public LibraryFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    public void onDestroyView() {
        PreferenceUtil.getInstance(getActivity()).unregisterOnSharedPreferenceChangedListener(this);
        super.onDestroyView();
        binding.pager.removeOnPageChangeListener(this);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        PreferenceUtil.getInstance(getActivity()).registerOnSharedPreferenceChangedListener(this);
        getMainActivity().setStatusbarColorAuto();
        getMainActivity().setNavigationbarColorAuto();
        getMainActivity().setTaskDescriptionColorAuto();

        setUpToolbar();
        setUpViewPager();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences preferences, String key) {
        if (PreferenceUtil.CATEGORIES.equals(key)) {
            Fragment current = getCurrentFragment();
            pagerAdapter.setCategoryInfos(PreferenceUtil.getInstance(getActivity()).getCategories());
            binding.pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);
            int position = pagerAdapter.getItemPosition(current);
            if (position < 0) position = 0;
            binding.pager.setCurrentItem(position);
            PreferenceUtil.getInstance(getContext()).setLastTab(position);

            updateTabVisibility();
        }
    }

    private void setUpToolbar() {
        int primaryColor = ThemeStore.primaryColor(getActivity());
        binding.appbar.setBackgroundColor(primaryColor);
        binding.toolbar.setBackgroundColor(primaryColor);
        binding.toolbar.setNavigationIcon(R.drawable.ic_menu_white_24dp);
        getActivity().setTitle(R.string.app_name);
        getMainActivity().setSupportActionBar(binding.toolbar);
    }

    private void setUpViewPager() {
        pagerAdapter = new MusicLibraryPagerAdapter(getActivity(), getChildFragmentManager());
        binding.pager.setAdapter(pagerAdapter);
        binding.pager.setOffscreenPageLimit(pagerAdapter.getCount() - 1);

        binding.tabs.setupWithViewPager(binding.pager);

        int primaryColor = ThemeStore.primaryColor(getActivity());
        int normalColor = ToolbarContentTintHelper.toolbarSubtitleColor(getActivity(), primaryColor);
        int selectedColor = ToolbarContentTintHelper.toolbarTitleColor(getActivity(), primaryColor);
        TabLayoutUtil.setTabIconColors(binding.tabs, normalColor, selectedColor);
        binding.tabs.setTabTextColors(normalColor, selectedColor);
        binding.tabs.setSelectedTabIndicatorColor(ThemeStore.accentColor(getActivity()));

        updateTabVisibility();

        if (PreferenceUtil.getInstance(getContext()).getRememberLastTab()) {
            binding.pager.setCurrentItem(PreferenceUtil.getInstance(getContext()).getLastTab());
        }

        binding.pager.addOnPageChangeListener(this);
    }

    private void updateTabVisibility() {
        // hide the tab bar when only a single tab is visible
        binding.tabs.setVisibility(pagerAdapter.getCount() == 1 ? View.GONE : View.VISIBLE);
    }

    public Fragment getCurrentFragment() {
        return pagerAdapter.getFragment(binding.pager.getCurrentItem());
    }

    private boolean isPlaylistPage() {
        return getCurrentFragment() instanceof PlaylistsFragment;
    }

    @NonNull
    @Override
    public MaterialCab openCab(final int menuRes, final MaterialCab.Callback callback) {
        if (cab != null && cab.isActive()) cab.finish();
        cab = new MaterialCab(getMainActivity(), R.id.cab_stub)
                .setMenu(menuRes)
                .setCloseDrawableRes(R.drawable.ic_close_white_24dp)
                .setBackgroundColor(ThemeUtil.shiftBackgroundColorForLightText(ThemeStore.primaryColor(getActivity())))
                .start(callback);

        return cab;
    }

    public void addOnAppBarOffsetChangedListener(AppBarLayout.OnOffsetChangedListener onOffsetChangedListener) {
        binding.appbar.addOnOffsetChangedListener(onOffsetChangedListener);
    }

    public void removeOnAppBarOffsetChangedListener(AppBarLayout.OnOffsetChangedListener onOffsetChangedListener) {
        binding.appbar.removeOnOffsetChangedListener(onOffsetChangedListener);
    }

    public int getTotalAppBarScrollingRange() {
        return binding.appbar.getTotalScrollRange();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.menu_main, menu);
        if (isPlaylistPage()) {
            menu.add(0, R.id.action_new_playlist, 0, R.string.action_new_playlist);
        }

        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsLibraryPagerRecyclerViewCustomGridSizeFragment && currentFragment.isAdded()) {
            AbsLibraryPagerRecyclerViewCustomGridSizeFragment absLibraryRecyclerViewCustomGridSizeFragment = (AbsLibraryPagerRecyclerViewCustomGridSizeFragment) currentFragment;

            MenuItem gridSizeItem = menu.findItem(R.id.action_grid_size);
            if (Util.isLandscape(getResources())) {
                gridSizeItem.setTitle(R.string.action_grid_size_land);
            }

            setUpGridSizeMenu(absLibraryRecyclerViewCustomGridSizeFragment, gridSizeItem.getSubMenu());

            menu.findItem(R.id.action_colored_footers).setChecked(absLibraryRecyclerViewCustomGridSizeFragment.usePalette());
            menu.findItem(R.id.action_colored_footers).setEnabled(absLibraryRecyclerViewCustomGridSizeFragment.canUsePalette());

            // TODO the API doesn't support artist sorting
            if (currentFragment instanceof ArtistsFragment) {
                menu.removeItem(R.id.action_sort_method);
                menu.removeItem(R.id.action_sort_order);
            } else {
                setUpSortMethodMenu(absLibraryRecyclerViewCustomGridSizeFragment, menu.findItem(R.id.action_sort_method).getSubMenu());
                setUpSortOrderMenu(absLibraryRecyclerViewCustomGridSizeFragment, menu.findItem(R.id.action_sort_order).getSubMenu());
            }
        } else {
            menu.removeItem(R.id.action_grid_size);
            menu.removeItem(R.id.action_colored_footers);
            menu.removeItem(R.id.action_sort_method);
            menu.removeItem(R.id.action_sort_order);
        }

        Activity activity = getActivity();
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnCreateOptionsMenu(getActivity(), binding.toolbar, menu, ATHToolbarActivity.getToolbarBackgroundColor(binding.toolbar));
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        Activity activity = getActivity();
        if (activity == null) return;
        ToolbarContentTintHelper.handleOnPrepareOptionsMenu(activity, binding.toolbar);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment instanceof AbsLibraryPagerRecyclerViewCustomGridSizeFragment) {
            AbsLibraryPagerRecyclerViewCustomGridSizeFragment absLibraryRecyclerViewCustomGridSizeFragment = (AbsLibraryPagerRecyclerViewCustomGridSizeFragment) currentFragment;
            if (item.getItemId() == R.id.action_colored_footers) {
                item.setChecked(!item.isChecked());
                absLibraryRecyclerViewCustomGridSizeFragment.setAndSaveUsePalette(item.isChecked());
                return true;
            }

            if (handleGridSizeMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true;
            }

            if (handleSortMethodMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true;
            }

            if (handleSortOrderMenuItem(absLibraryRecyclerViewCustomGridSizeFragment, item)) {
                return true;
            }
        }

        int id = item.getItemId();
        switch (id) {
            case R.id.action_shuffle_all:
                MusicPlayerRemote.openAndShuffleQueue(SongLoader.getAllSongs(getActivity()), true);
                return true;
            case R.id.action_new_playlist:
                CreatePlaylistDialog.create().show(getChildFragmentManager(), "CREATE_PLAYLIST");
                return true;
            case R.id.action_search:
                startActivity(new Intent(getActivity(), SearchActivity.class));
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private void setUpGridSizeMenu(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull SubMenu gridSizeMenu) {
        switch (fragment.getGridSize()) {
            case 1:
                gridSizeMenu.findItem(R.id.action_grid_size_1).setChecked(true);
                break;
            case 2:
                gridSizeMenu.findItem(R.id.action_grid_size_2).setChecked(true);
                break;
            case 3:
                gridSizeMenu.findItem(R.id.action_grid_size_3).setChecked(true);
                break;
            case 4:
                gridSizeMenu.findItem(R.id.action_grid_size_4).setChecked(true);
                break;
            case 5:
                gridSizeMenu.findItem(R.id.action_grid_size_5).setChecked(true);
                break;
            case 6:
                gridSizeMenu.findItem(R.id.action_grid_size_6).setChecked(true);
                break;
            case 7:
                gridSizeMenu.findItem(R.id.action_grid_size_7).setChecked(true);
                break;
            case 8:
                gridSizeMenu.findItem(R.id.action_grid_size_8).setChecked(true);
                break;
        }

        int maxGridSize = fragment.getMaxGridSize();
        if (maxGridSize < 8) {
            gridSizeMenu.findItem(R.id.action_grid_size_8).setVisible(false);
        }
        if (maxGridSize < 7) {
            gridSizeMenu.findItem(R.id.action_grid_size_7).setVisible(false);
        }
        if (maxGridSize < 6) {
            gridSizeMenu.findItem(R.id.action_grid_size_6).setVisible(false);
        }
        if (maxGridSize < 5) {
            gridSizeMenu.findItem(R.id.action_grid_size_5).setVisible(false);
        }
        if (maxGridSize < 4) {
            gridSizeMenu.findItem(R.id.action_grid_size_4).setVisible(false);
        }
        if (maxGridSize < 3) {
            gridSizeMenu.findItem(R.id.action_grid_size_3).setVisible(false);
        }
    }

    private boolean handleGridSizeMenuItem(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull MenuItem item) {
        int gridSize = 0;
        switch (item.getItemId()) {
            case R.id.action_grid_size_1:
                gridSize = 1;
                break;
            case R.id.action_grid_size_2:
                gridSize = 2;
                break;
            case R.id.action_grid_size_3:
                gridSize = 3;
                break;
            case R.id.action_grid_size_4:
                gridSize = 4;
                break;
            case R.id.action_grid_size_5:
                gridSize = 5;
                break;
            case R.id.action_grid_size_6:
                gridSize = 6;
                break;
            case R.id.action_grid_size_7:
                gridSize = 7;
                break;
            case R.id.action_grid_size_8:
                gridSize = 8;
                break;
        }

        if (gridSize > 0) {
            item.setChecked(true);
            fragment.setAndSaveGridSize(gridSize);
            binding.toolbar.getMenu().findItem(R.id.action_colored_footers).setEnabled(fragment.canUsePalette());
            return true;
        }

        return false;
    }

    private void setUpSortMethodMenu(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull SubMenu sortMethodMenu) {
        String currentSortMethod = fragment.getSortMethod();
        sortMethodMenu.clear();

        if (fragment instanceof AlbumsFragment) {
            sortMethodMenu.add(0, R.id.action_sort_method_name, 0, R.string.sort_method_name)
                    .setChecked(currentSortMethod.equals(SortMethod.NAME));
            sortMethodMenu.add(0, R.id.action_sort_method_artist, 1, R.string.sort_method_artist)
                    .setChecked(currentSortMethod.equals(SortMethod.ARTIST));
            sortMethodMenu.add(0, R.id.action_sort_method_year, 2, R.string.sort_method_year)
                    .setChecked(currentSortMethod.equals(SortMethod.YEAR));
            sortMethodMenu.add(0, R.id.action_sort_method_added, 3, R.string.sort_method_added)
                    .setChecked(currentSortMethod.equals(SortMethod.ADDED));
            sortMethodMenu.add(0, R.id.action_sort_method_random, 4, R.string.sort_method_random)
                    .setChecked(currentSortMethod.equals(SortMethod.RANDOM));
        } else if (fragment instanceof SongsFragment) {
            sortMethodMenu.add(0, R.id.action_sort_method_name, 0, R.string.sort_method_name)
                    .setChecked(currentSortMethod.equals(SortMethod.NAME));
            sortMethodMenu.add(0, R.id.action_sort_method_album, 1, R.string.sort_method_album)
                    .setChecked(currentSortMethod.equals(SortMethod.ALBUM));
            sortMethodMenu.add(0, R.id.action_sort_method_artist, 2, R.string.sort_method_artist)
                    .setChecked(currentSortMethod.equals(SortMethod.ARTIST));
            sortMethodMenu.add(0, R.id.action_sort_method_year, 3, R.string.sort_method_year)
                    .setChecked(currentSortMethod.equals(SortMethod.YEAR));
            sortMethodMenu.add(0, R.id.action_sort_method_added, 4, R.string.sort_method_added)
                    .setChecked(currentSortMethod.equals(SortMethod.ADDED));
            sortMethodMenu.add(0, R.id.action_sort_method_random, 5, R.string.sort_method_random)
                    .setChecked(currentSortMethod.equals(SortMethod.RANDOM));
        }

        sortMethodMenu.setGroupCheckable(0, true, true);
    }

    private void setUpSortOrderMenu(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull SubMenu sortOrderMenu) {
        String currentSortOrder = fragment.getSortOrder();
        sortOrderMenu.clear();

        sortOrderMenu.add(0, R.id.action_sort_order_ascending, 0, R.string.sort_order_ascending)
                .setChecked(currentSortOrder.equals(SortOrder.ASCENDING));
        sortOrderMenu.add(0, R.id.action_sort_order_descending, 1, R.string.sort_order_descending)
                .setChecked(currentSortOrder.equals(SortOrder.DESCENDING));

        sortOrderMenu.setGroupCheckable(0, true, true);
    }

    private boolean handleSortMethodMenuItem(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull MenuItem item) {
        String sortMethod = null;
        switch (item.getItemId()) {
            case R.id.action_sort_method_name:
                sortMethod = SortMethod.NAME;
                break;
            case R.id.action_sort_method_album:
                sortMethod = SortMethod.ALBUM;
                break;
            case R.id.action_sort_method_artist:
                sortMethod = SortMethod.ARTIST;
                break;
            case R.id.action_sort_method_year:
                sortMethod = SortMethod.YEAR;
                break;
            case R.id.action_sort_method_added:
                sortMethod = SortMethod.ADDED;
                break;
            case R.id.action_sort_method_random:
                sortMethod = SortMethod.RANDOM;
                break;
        }

        if (sortMethod != null) {
            item.setChecked(true);
            fragment.setAndSaveSortMethod(sortMethod);
            return true;
        }

        return false;
    }

    private boolean handleSortOrderMenuItem(@NonNull AbsLibraryPagerRecyclerViewCustomGridSizeFragment fragment, @NonNull MenuItem item) {
        String sortOrder = null;
        switch (item.getItemId()) {
            case R.id.action_sort_order_ascending:
                sortOrder = SortOrder.ASCENDING;
                break;
            case R.id.action_sort_order_descending:
                sortOrder = SortOrder.DESCENDING;
                break;
        }

        if (sortOrder != null) {
            item.setChecked(true);
            fragment.setAndSaveSortOrder(sortOrder);
            return true;
        }

        return false;
    }

    @Override
    public boolean handleBackPress() {
        if (cab != null && cab.isActive()) {
            cab.finish();
            return true;
        }

        return false;
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        PreferenceUtil.getInstance(getActivity()).setLastTab(position);
    }

    @Override
    public void onPageScrollStateChanged(int state) {
    }
}
