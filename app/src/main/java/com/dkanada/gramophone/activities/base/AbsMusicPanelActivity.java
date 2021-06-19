package com.dkanada.gramophone.activities.base;

import android.animation.ValueAnimator;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.PathInterpolator;

import androidx.annotation.FloatRange;
import androidx.annotation.RequiresApi;
import androidx.core.graphics.ColorUtils;
import androidx.fragment.app.Fragment;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.databinding.SlidingMusicPanelLayoutBinding;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.fragments.player.AbsPlayerFragment;
import com.dkanada.gramophone.fragments.player.MiniPlayerFragment;
import com.dkanada.gramophone.fragments.player.NowPlayingScreen;
import com.dkanada.gramophone.fragments.player.card.CardPlayerFragment;
import com.dkanada.gramophone.fragments.player.flat.FlatPlayerFragment;
import com.dkanada.gramophone.util.NavigationUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.ViewUtil;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public abstract class AbsMusicPanelActivity extends AbsMusicServiceActivity implements SlidingUpPanelLayout.PanelSlideListener, CardPlayerFragment.Callbacks {
    private SlidingMusicPanelLayoutBinding binding;

    private int navigationBarColor;
    private int taskDescriptionColor;
    private boolean lightStatusBar;

    private NowPlayingScreen currentNowPlayingScreen;
    private AbsPlayerFragment playerFragment;
    private MiniPlayerFragment miniPlayerFragment;
    private ValueAnimator navigationBarColorAnimator;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(createContentView());

        currentNowPlayingScreen = PreferenceUtil.getInstance(this).getNowPlayingScreen();

        // must implement AbsPlayerFragment
        Fragment fragment;
        switch (currentNowPlayingScreen) {
            case FLAT:
                fragment = new FlatPlayerFragment();
                break;
            case CARD:
            default:
                fragment = new CardPlayerFragment();
                break;
        }

        getSupportFragmentManager().beginTransaction().replace(R.id.player_fragment_container, fragment).commit();
        getSupportFragmentManager().executePendingTransactions();

        playerFragment = (AbsPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.player_fragment_container);
        miniPlayerFragment = (MiniPlayerFragment) getSupportFragmentManager().findFragmentById(R.id.mini_player_fragment);

        // noinspection ConstantConditions
        miniPlayerFragment.getView().setOnClickListener(v -> expandPanel());

        binding.slidingLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                binding.slidingLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                switch (getPanelState()) {
                    case EXPANDED:
                        onPanelSlide(binding.slidingLayout, 1);
                        onPanelExpanded(binding.slidingLayout);
                        break;
                    case COLLAPSED:
                        onPanelCollapsed(binding.slidingLayout);
                        break;
                    default:
                        playerFragment.onHide();
                        break;
                }
            }
        });

        binding.slidingLayout.addPanelSlideListener(this);

        // TODO remove this once createContentView works with inheritance
        // any AbsMusicPanelActivity child without setColor has status bar issues
        // setDrawUnderStatusBar only works after content view has been set
        setColor(PreferenceUtil.getInstance(this).getPrimaryColor());
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (currentNowPlayingScreen != PreferenceUtil.getInstance(this).getNowPlayingScreen()) {
            NavigationUtil.recreateMain(this);
        }
    }

    public void setAntiDragView(View antiDragView) {
        binding.slidingLayout.setAntiDragView(antiDragView);
    }

    protected abstract View createContentView();

    @Override
    public void onServiceConnected() {
        super.onServiceConnected();

        // don't call hideBottomBar(true) here as it causes a bug with the SlidingUpPanelLayout
        if (!MusicPlayerRemote.getPlayingQueue().isEmpty()) {
            binding.slidingLayout.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    binding.slidingLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    hideBottomBar(false);
                }
            });
        }
    }

    @Override
    public void onQueueChanged() {
        super.onQueueChanged();
        hideBottomBar(MusicPlayerRemote.getPlayingQueue().isEmpty());
    }

    @Override
    public void onPanelSlide(View panel, @FloatRange(from = 0, to = 1) float slideOffset) {
        setMiniPlayerAlphaProgress(slideOffset);
        if (navigationBarColorAnimator != null) navigationBarColorAnimator.cancel();
        super.setNavigationBarColor(ColorUtils.blendARGB(navigationBarColor, playerFragment.getPaletteColor(), slideOffset));
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case EXPANDED:
                onPanelExpanded(panel);
                break;
            case ANCHORED:
                // this fixes a bug where the panel would get stuck for some reason
                collapsePanel();
                break;
        }
    }

    public void onPanelCollapsed(View panel) {
        super.setLightStatusBar(lightStatusBar);
        super.setTaskDescriptionColor(taskDescriptionColor);
        super.setNavigationBarColor(navigationBarColor);

        playerFragment.setMenuVisibility(false);
        playerFragment.setUserVisibleHint(false);
        playerFragment.onHide();
    }

    public void onPanelExpanded(View panel) {
        int playerFragmentColor = playerFragment.getPaletteColor();

        super.setLightStatusBar(false);
        super.setTaskDescriptionColor(playerFragmentColor);
        super.setNavigationBarColor(playerFragmentColor);

        playerFragment.setMenuVisibility(true);
        playerFragment.setUserVisibleHint(true);
        playerFragment.onShow();
    }

    private void setMiniPlayerAlphaProgress(@FloatRange(from = 0, to = 1) float progress) {
        if (miniPlayerFragment.getView() == null) return;
        float alpha = 1 - progress;
        miniPlayerFragment.getView().setAlpha(alpha);
        // necessary to make the views below clickable
        miniPlayerFragment.getView().setVisibility(alpha == 0 ? View.GONE : View.VISIBLE);
    }


    public SlidingUpPanelLayout.PanelState getPanelState() {
        return binding == null ? null : binding.slidingLayout.getPanelState();
    }

    public void collapsePanel() {
        binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
    }

    public void expandPanel() {
        binding.slidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
    }

    public void hideBottomBar(final boolean hide) {
        if (hide) {
            binding.slidingLayout.setPanelHeight(0);
            collapsePanel();
        } else {
            binding.slidingLayout.setPanelHeight(getResources().getDimensionPixelSize(R.dimen.mini_player_height));
        }
    }

    protected View wrapSlidingMusicPanel(View view) {
        binding = SlidingMusicPanelLayoutBinding.inflate(getLayoutInflater());

        ViewGroup contentContainer = binding.contentContainer;
        contentContainer.addView(view);

        return binding.getRoot();
    }

    @Override
    public void onBackPressed() {
        if (!handleBackPress()) super.onBackPressed();
    }

    public boolean handleBackPress() {
        if (binding.slidingLayout.getPanelHeight() != 0 && playerFragment.onBackPressed())
            return true;
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            collapsePanel();
            return true;
        }

        return false;
    }

    @Override
    public void onPaletteColorChanged() {
        if (getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
            int playerFragmentColor = playerFragment.getPaletteColor();

            super.setTaskDescriptionColor(playerFragmentColor);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                animateNavigationBarColor(playerFragmentColor);
            }
        }
    }

    @Override
    public void setLightStatusBar(boolean enabled) {
        lightStatusBar = enabled;

        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setLightStatusBar(enabled);
        }
    }

    @Override
    public void setNavigationBarColor(int color) {
        navigationBarColor = color;

        if (navigationBarColorAnimator != null) {
            navigationBarColorAnimator.cancel();
        }

        if (getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setNavigationBarColor(color);
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void animateNavigationBarColor(int color) {
        if (navigationBarColorAnimator != null) {
            navigationBarColorAnimator.cancel();
        }

        navigationBarColorAnimator = ValueAnimator
            .ofArgb(getWindow().getNavigationBarColor(), color)
            .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME);

        navigationBarColorAnimator.setInterpolator(new PathInterpolator(0.4f, 0f, 1f, 1f));
        navigationBarColorAnimator.addUpdateListener(animation -> super.setNavigationBarColor((int) animation.getAnimatedValue()));
        navigationBarColorAnimator.start();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (navigationBarColorAnimator != null) {
            navigationBarColorAnimator.cancel();
        }
    }

    @Override
    public void setTaskDescriptionColor(int color) {
        taskDescriptionColor = color;

        if (getPanelState() == null || getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            super.setTaskDescriptionColor(color);
        }
    }
}
