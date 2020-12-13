package com.dkanada.gramophone.activities;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.dkanada.gramophone.databinding.ActivityMainContentBinding;
import com.dkanada.gramophone.databinding.ActivityMainDrawerLayoutBinding;
import com.dkanada.gramophone.databinding.NavigationDrawerHeaderBinding;
import com.dkanada.gramophone.dialogs.ConfirmLogoutDialog;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.fragments.mainactivity.library.LibraryFragment;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.QueryUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jellyfin.apiclient.model.dto.BaseItemDto;

import java.util.List;

public class MainActivity extends AbsSlidingMusicPanelActivity {
    private ActivityMainDrawerLayoutBinding binding;
    private ActivityMainContentBinding contentBinding;
    private NavigationDrawerHeaderBinding navigationBinding;
    private boolean onLogout;

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    @Nullable
    private List<BaseItemDto> libraries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            binding.navigationView.setFitsSystemWindows(false);
        }

        Menu menu = binding.navigationView.getMenu();
        QueryUtil.getLibraries(media -> {
            libraries = (List<BaseItemDto>) media;
            menu.clear();

            for (BaseItemDto itemDto : libraries) {
                if (menu.size() == 0) {
                    QueryUtil.currentLibrary = itemDto;
                }

                if (itemDto.getCollectionType() == null || !itemDto.getCollectionType().equals("music")) continue;
                menu.add(R.id.navigation_drawer_menu_category_sections, itemDto.getId().hashCode(), menu.size(), itemDto.getName());
                menu.getItem(menu.size() - 1).setIcon(R.drawable.ic_album_white_24dp);
            }

            menu.add(R.id.navigation_drawer_menu_category_other, R.id.nav_settings, menu.size(), R.string.action_settings);
            menu.getItem(menu.size() - 1).setIcon(R.drawable.ic_settings_white_24dp);
            menu.add(R.id.navigation_drawer_menu_category_other, R.id.nav_about, menu.size(), R.string.action_about);
            menu.getItem(menu.size() - 1).setIcon(R.drawable.ic_info_outline_white_24dp);
            menu.add(R.id.navigation_drawer_menu_category_other, R.id.nav_logout, menu.size(), R.string.logout);
            menu.getItem(menu.size() - 1).setIcon(R.drawable.ic_exit_to_app_white_48dp);

            setUpDrawerLayout();

            menu.getItem(0).setChecked(true);
            if (savedInstanceState == null) {
                setCurrentFragment(LibraryFragment.newInstance());
            } else {
                restoreCurrentFragment();
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();

        // only override when logout selected
        if (onLogout) {
            overridePendingTransition(0, R.anim.fade_slow);
            onLogout = false;
        }
    }

    private void setCurrentFragment(Fragment fragment) {
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, fragment, null).commit();
        currentFragment = (MainActivityFragmentCallbacks) fragment;
    }

    private void restoreCurrentFragment() {
        currentFragment = (MainActivityFragmentCallbacks) getSupportFragmentManager().findFragmentById(R.id.fragment_container);
    }

    @Override
    protected View createContentView() {
        binding = ActivityMainDrawerLayoutBinding.inflate(getLayoutInflater());
        contentBinding = ActivityMainContentBinding.inflate(getLayoutInflater());

        ViewGroup drawerContent = binding.drawerContentContainer;
        drawerContent.addView(wrapSlidingMusicPanel(contentBinding.getRoot()));

        return binding.getRoot();
    }

    private void setUpNavigationView() {
        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(binding.navigationView, ATHUtil.resolveColor(this, R.attr.iconColor, ThemeStore.textColorSecondary(this)), accentColor);
        NavigationViewUtil.setItemTextColors(binding.navigationView, ThemeStore.textColorPrimary(this), accentColor);

        binding.navigationView.setNavigationItemSelectedListener(menuItem -> {
            binding.drawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_settings:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 200);
                    break;
                case R.id.nav_about:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, AboutActivity.class)), 200);
                    break;
                case R.id.nav_logout:
                    onLogout = true;
                    ConfirmLogoutDialog.create().show(getSupportFragmentManager(), "CONFIRM_LOGOUT_DIALOG");
                    break;
            }

            // only run the following code when a new library has been selected
            if (menuItem.getItemId() == QueryUtil.currentLibrary.getId().hashCode()) return true;
            for (BaseItemDto itemDto : libraries) {
                if (menuItem.getItemId() == itemDto.getId().hashCode()) {
                    QueryUtil.currentLibrary = itemDto;
                    setCurrentFragment(LibraryFragment.newInstance());
                    break;
                }
            }

            // setCheckable must be applied to the items on creation
            // it also applies a tacky background color for the checked item
            // this is a hack to check the current item without that
            if (menuItem.getItemId() == R.id.nav_settings
                    || menuItem.getItemId() == R.id.nav_about
                    || menuItem.getItemId() == R.id.nav_logout) return true;

            for (int i = 0; i < binding.navigationView.getMenu().size(); i++) {
                if (binding.navigationView.getMenu().getItem(i) == menuItem) {
                    binding.navigationView.getMenu().getItem(i).setChecked(true);
                } else {
                    binding.navigationView.getMenu().getItem(i).setChecked(false);
                }
            }

            return true;
        });
    }

    private void setUpDrawerLayout() {
        setUpNavigationView();
    }

    private void updateNavigationDrawerHeader() {
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            Song song = MusicPlayerRemote.getCurrentSong();
            if (navigationBinding == null) {
                navigationBinding = NavigationDrawerHeaderBinding.inflate(getLayoutInflater());

                binding.navigationView.addHeaderView(navigationBinding.getRoot());
                navigationBinding.getRoot().setOnClickListener(v -> {
                    binding.drawerLayout.closeDrawers();
                    if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        expandPanel();
                    }
                });
            }

            navigationBinding.title.setText(song.title);
            navigationBinding.text.setText(MusicUtil.getSongInfoString(song));

            CustomGlideRequest.Builder
                    .from(this, song.primary, song.blurHash)
                    .build().centerInside().into(navigationBinding.image);
        } else if (binding.navigationView.getHeaderCount() != 0) {
            binding.navigationView.removeHeaderView(navigationBinding.getRoot());
            navigationBinding = null;
        }
    }

    @Override
    public void onPlayMetadataChanged() {
        super.onPlayMetadataChanged();
        updateNavigationDrawerHeader();
    }

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();
        updateNavigationDrawerHeader();
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            if (binding.drawerLayout.isDrawerOpen(binding.navigationView)) {
                binding.drawerLayout.closeDrawer(binding.navigationView);
            } else {
                binding.drawerLayout.openDrawer(binding.navigationView);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        if (binding.drawerLayout.isDrawerOpen(binding.navigationView)) {
            binding.drawerLayout.closeDrawers();
            return true;
        }

        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        binding.drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }
}
