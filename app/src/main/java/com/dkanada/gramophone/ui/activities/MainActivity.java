package com.dkanada.gramophone.ui.activities;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.navigation.NavigationView;
import androidx.fragment.app.Fragment;
import androidx.drawerlayout.widget.DrawerLayout;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.NavigationViewUtil;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.ui.fragments.mainactivity.library.LibraryFragment;
import com.dkanada.gramophone.util.MusicUtil;

import com.dkanada.gramophone.util.QueryUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

import org.jellyfin.apiclient.interaction.EmptyResponse;
import org.jellyfin.apiclient.interaction.VolleyHttpClient;
import org.jellyfin.apiclient.interaction.http.IAsyncHttpClient;
import org.jellyfin.apiclient.logging.AndroidLogger;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.logging.ILogger;
import org.jellyfin.apiclient.model.serialization.GsonJsonSerializer;
import org.jellyfin.apiclient.model.serialization.IJsonSerializer;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AbsSlidingMusicPanelActivity {
    public static final String TAG = MainActivity.class.getSimpleName();

    @BindView(R.id.navigation_view)
    NavigationView navigationView;
    @BindView(R.id.drawer_layout)
    DrawerLayout drawerLayout;

    @Nullable
    MainActivityFragmentCallbacks currentFragment;

    @Nullable
    private View navigationDrawerHeader;

    @Nullable
    private List<BaseItemDto> libraries;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setDrawUnderStatusbar();
        ButterKnife.bind(this);

        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            navigationView.setFitsSystemWindows(false);
        }

        Menu menu = navigationView.getMenu();
        QueryUtil.getLibraries(new MediaCallback() {
            @Override
            public void onLoadMedia(List<?> media) {
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
            }
        });
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
        @SuppressLint("InflateParams")
        View contentView = getLayoutInflater().inflate(R.layout.activity_main_drawer_layout, null);
        ViewGroup drawerContent = contentView.findViewById(R.id.drawer_content_container);
        drawerContent.addView(wrapSlidingMusicPanel(R.layout.activity_main_content));
        return contentView;
    }

    private void setUpNavigationView() {
        int accentColor = ThemeStore.accentColor(this);
        NavigationViewUtil.setItemIconColors(navigationView, ATHUtil.resolveColor(this, R.attr.iconColor, ThemeStore.textColorSecondary(this)), accentColor);
        NavigationViewUtil.setItemTextColors(navigationView, ThemeStore.textColorPrimary(this), accentColor);

        navigationView.setNavigationItemSelectedListener(menuItem -> {
            drawerLayout.closeDrawers();
            switch (menuItem.getItemId()) {
                case R.id.nav_settings:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, SettingsActivity.class)), 200);
                    break;
                case R.id.nav_about:
                    new Handler().postDelayed(() -> startActivity(new Intent(MainActivity.this, AboutActivity.class)), 200);
                    break;
                case R.id.nav_logout:
                    IJsonSerializer jsonSerializer = new GsonJsonSerializer();
                    ILogger logger = new AndroidLogger(TAG);
                    IAsyncHttpClient httpClient = new VolleyHttpClient(logger, this);

                    App.getConnectionManager(this, jsonSerializer, logger, httpClient).Logout(new EmptyResponse());
                    Intent intent = new Intent(this, LoginActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    this.startActivity(intent);
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
            if (menuItem.getItemId() == R.id.nav_settings || menuItem.getItemId() == R.id.nav_about) return true;
            for (int i = 0; i < navigationView.getMenu().size(); i++) {
                if (navigationView.getMenu().getItem(i) == menuItem) {
                    navigationView.getMenu().getItem(i).setChecked(true);
                } else {
                    navigationView.getMenu().getItem(i).setChecked(false);
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
            if (navigationDrawerHeader == null) {
                navigationDrawerHeader = navigationView.inflateHeaderView(R.layout.navigation_drawer_header);
                navigationDrawerHeader.setOnClickListener(v -> {
                    drawerLayout.closeDrawers();
                    if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                        expandPanel();
                    }
                });
            }

            ((TextView) navigationDrawerHeader.findViewById(R.id.title)).setText(song.title);
            ((TextView) navigationDrawerHeader.findViewById(R.id.text)).setText(MusicUtil.getSongInfoString(song));

            CustomGlideRequest.Builder
                    .from(Glide.with(this), song.primary)
                    .build().into(((ImageView) navigationDrawerHeader.findViewById(R.id.image)));
        } else {
            if (navigationDrawerHeader != null) {
                navigationView.removeHeaderView(navigationDrawerHeader);
                navigationDrawerHeader = null;
            }
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
            if (drawerLayout.isDrawerOpen(navigationView)) {
                drawerLayout.closeDrawer(navigationView);
            } else {
                drawerLayout.openDrawer(navigationView);
            }

            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean handleBackPress() {
        if (drawerLayout.isDrawerOpen(navigationView)) {
            drawerLayout.closeDrawers();
            return true;
        }

        return super.handleBackPress() || (currentFragment != null && currentFragment.handleBackPress());
    }

    @Override
    public void onPanelExpanded(View view) {
        super.onPanelExpanded(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
    }

    @Override
    public void onPanelCollapsed(View view) {
        super.onPanelCollapsed(view);
        drawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_UNLOCKED);
    }

    public interface MainActivityFragmentCallbacks {
        boolean handleBackPress();
    }
}
