package com.dkanada.gramophone.ui.fragments.player.card;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.ColorInt;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageView;

import com.dkanada.gramophone.databinding.FragmentCardPlayerBinding;
import com.h6ah4i.android.widget.advrecyclerview.animator.GeneralItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.animator.RefactoredDefaultItemAnimator;
import com.h6ah4i.android.widget.advrecyclerview.draggable.RecyclerViewDragDropManager;
import com.h6ah4i.android.widget.advrecyclerview.utils.WrapperAdapterUtils;
import com.kabouzeid.appthemehelper.ThemeStore;
import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.ToolbarContentTintHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.base.MediaEntryViewHolder;
import com.dkanada.gramophone.adapter.song.PlayingQueueAdapter;
import com.dkanada.gramophone.dialogs.SongShareDialog;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.helper.menu.SongMenuHelper;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.ui.activities.base.AbsSlidingMusicPanelActivity;
import com.dkanada.gramophone.ui.fragments.player.AbsPlayerFragment;
import com.dkanada.gramophone.ui.fragments.player.PlayerAlbumCoverFragment;
import com.dkanada.gramophone.util.ImageUtil;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.Util;
import com.dkanada.gramophone.util.ViewUtil;
import com.dkanada.gramophone.views.WidthFitSquareLayout;
import com.sothree.slidinguppanel.SlidingUpPanelLayout;

public class CardPlayerFragment extends AbsPlayerFragment implements PlayerAlbumCoverFragment.Callbacks, SlidingUpPanelLayout.PanelSlideListener {
    private FragmentCardPlayerBinding binding;

    private int lastColor;

    private CardPlayerPlaybackControlsFragment playbackControlsFragment;
    private PlayerAlbumCoverFragment playerAlbumCoverFragment;

    private LinearLayoutManager layoutManager;

    private PlayingQueueAdapter playingQueueAdapter;

    private RecyclerView.Adapter wrappedAdapter;
    private RecyclerViewDragDropManager recyclerViewDragDropManager;

    private Impl impl;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentCardPlayerBinding.inflate(inflater);

        if (Util.isLandscape(getResources())) {
            impl = new LandscapeImpl(this, binding);
        } else {
            impl = new PortraitImpl(this, binding);
        }

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        impl.init();

        setUpPlayerToolbar();
        setUpSubFragments();

        setUpRecyclerView();

        binding.playerSlidingLayout.addPanelSlideListener(this);
        binding.playerSlidingLayout.setAntiDragView(view.findViewById(R.id.draggable_area));

        view.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                view.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                impl.setUpPanelAndAlbumCoverHeight();
            }
        });

        // for some reason the xml attribute doesn't get applied here.
        binding.playingQueueCard.setCardBackgroundColor(ATHUtil.resolveColor(getActivity(), R.attr.cardBackgroundColor));
    }

    @Override
    public void onDestroyView() {
        binding.playerSlidingLayout.removePanelSlideListener(this);

        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.release();
            recyclerViewDragDropManager = null;
        }

        binding.playerRecyclerView.setItemAnimator(null);
        binding.playerRecyclerView.setAdapter(null);

        if (wrappedAdapter != null) {
            WrapperAdapterUtils.releaseAll(wrappedAdapter);
            wrappedAdapter = null;
        }

        playingQueueAdapter = null;
        layoutManager = null;

        super.onDestroyView();
    }

    @Override
    public void onPause() {
        if (recyclerViewDragDropManager != null) {
            recyclerViewDragDropManager.cancelDrag();
        }

        super.onPause();
    }

    @Override
    public void onResume() {
        super.onResume();
        checkToggleToolbar(binding.toolbarContainer);
    }

    @Override
    public void onServiceConnected() {
        updateQueue();
        updateCurrentSong();
        updateIsFavorite();
    }

    @Override
    public void onPlayMetadataChanged() {
        updateCurrentSong();
        updateIsFavorite();
        updateQueuePosition();
    }

    @Override
    public void onQueueChanged() {
        updateQueue();
    }

    private void updateQueue() {
        playingQueueAdapter.swapDataSet(MusicPlayerRemote.getPlayingQueue(), MusicPlayerRemote.getPosition());
        binding.playerQueueSubHeader.setText(getUpNextAndQueueTime());
        if (binding.playerSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            resetToCurrentPosition();
        }
    }

    private void updateQueuePosition() {
        playingQueueAdapter.setCurrent(MusicPlayerRemote.getPosition());
        binding.playerQueueSubHeader.setText(getUpNextAndQueueTime());
        if (binding.playerSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
            resetToCurrentPosition();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private void updateCurrentSong() {
        impl.updateCurrentSong(MusicPlayerRemote.getCurrentSong());
    }

    private void setUpSubFragments() {
        playbackControlsFragment = (CardPlayerPlaybackControlsFragment) getChildFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        playerAlbumCoverFragment = (PlayerAlbumCoverFragment) getChildFragmentManager().findFragmentById(R.id.player_album_cover_fragment);

        playerAlbumCoverFragment.setCallbacks(this);
    }

    private void setUpPlayerToolbar() {
        binding.playerToolbar.inflateMenu(R.menu.menu_player);
        binding.playerToolbar.setNavigationIcon(R.drawable.ic_close_white_24dp);
        binding.playerToolbar.setNavigationOnClickListener(v -> getActivity().onBackPressed());
        binding.playerToolbar.setOnMenuItemClickListener(this);
    }

    private void setUpRecyclerView() {
        recyclerViewDragDropManager = new RecyclerViewDragDropManager();
        final GeneralItemAnimator animator = new RefactoredDefaultItemAnimator();

        playingQueueAdapter = new PlayingQueueAdapter(
                ((AppCompatActivity) getActivity()),
                MusicPlayerRemote.getPlayingQueue(),
                MusicPlayerRemote.getPosition(),
                R.layout.item_list,
                false,
                null);
        wrappedAdapter = recyclerViewDragDropManager.createWrappedAdapter(playingQueueAdapter);

        layoutManager = new LinearLayoutManager(getActivity());

        binding.playerRecyclerView.setLayoutManager(layoutManager);
        binding.playerRecyclerView.setAdapter(wrappedAdapter);
        binding.playerRecyclerView.setItemAnimator(animator);

        recyclerViewDragDropManager.attachRecyclerView(binding.playerRecyclerView);

        layoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
    }

    private void updateIsFavorite() {
        boolean favorite = MusicPlayerRemote.getCurrentSong().favorite;
        int res = favorite ? R.drawable.ic_favorite_white_24dp : R.drawable.ic_favorite_border_white_24dp;
        int color = ToolbarContentTintHelper.toolbarContentColor(getActivity(), Color.TRANSPARENT);
        Drawable drawable = ImageUtil.getTintedVectorDrawable(getActivity(), res, color);
        binding.playerToolbar.getMenu().findItem(R.id.action_toggle_favorite)
                .setIcon(drawable)
                .setTitle(favorite ? getString(R.string.action_remove_from_favorites) : getString(R.string.action_add_to_favorites));
    }

    @Override
    @ColorInt
    public int getPaletteColor() {
        return lastColor;
    }

    private void animateColorChange(final int newColor) {
        impl.animateColorChange(newColor);
        lastColor = newColor;
    }

    @Override
    protected void toggleFavorite(Song song) {
        super.toggleFavorite(song);
        if (song.id == MusicPlayerRemote.getCurrentSong().id) {
            if (song.favorite) {
                playerAlbumCoverFragment.showHeartAnimation();
            }

            updateIsFavorite();
        }
    }

    @Override
    public void onShow() {
        playbackControlsFragment.show();
    }

    @Override
    public void onHide() {
        playbackControlsFragment.hide();
        onBackPressed();
    }

    @Override
    public boolean onBackPressed() {
        boolean wasExpanded = binding.playerSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED;
        binding.playerSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);

        return wasExpanded;
    }

    @Override
    public void onColorChanged(int color) {
        animateColorChange(color);
        playbackControlsFragment.setDark(ColorUtil.isColorLight(color));
        getCallbacks().onPaletteColorChanged();
    }

    @Override
    public void onFavoriteToggled() {
        toggleFavorite(MusicPlayerRemote.getCurrentSong());
    }

    @Override
    public void onToolbarToggled() {
        toggleToolbar(binding.toolbarContainer);
    }

    @Override
    public void onPanelSlide(View view, float slide) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            float density = getResources().getDisplayMetrics().density;
            binding.playingQueueCard.setCardElevation((6 * slide + 2) * density);
            playbackControlsFragment.binding.playerPlayPauseFab.setElevation((2 * Math.max(0, (1 - (slide * 16))) + 2) * density);
        }
    }

    @Override
    public void onPanelStateChanged(View panel, SlidingUpPanelLayout.PanelState previousState, SlidingUpPanelLayout.PanelState newState) {
        switch (newState) {
            case COLLAPSED:
                onPanelCollapsed(panel);
                break;
            case ANCHORED:
                binding.playerSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED); // this fixes a bug where the panel would get stuck for some reason
                break;
        }
    }

    public void onPanelCollapsed(View panel) {
        resetToCurrentPosition();
    }

    private void resetToCurrentPosition() {
        binding.playerRecyclerView.stopScroll();
        layoutManager.scrollToPositionWithOffset(MusicPlayerRemote.getPosition() + 1, 0);
    }

    interface Impl {
        void init();

        void updateCurrentSong(Song song);

        void animateColorChange(final int newColor);

        void setUpPanelAndAlbumCoverHeight();
    }

    private static abstract class BaseImpl implements Impl {
        protected CardPlayerFragment fragment;
        protected FragmentCardPlayerBinding binding;

        public BaseImpl(CardPlayerFragment fragment, FragmentCardPlayerBinding binding) {
            this.fragment = fragment;
            this.binding = binding;
        }

        public AnimatorSet createDefaultColorChangeAnimatorSet(int newColor) {
            Animator backgroundAnimator;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                //noinspection ConstantConditions
                int x = (int) (fragment.playbackControlsFragment.binding.playerPlayPauseFab.getX() + fragment.playbackControlsFragment.binding.playerPlayPauseFab.getWidth() / 2 + fragment.playbackControlsFragment.getView().getX());
                int y = (int) (fragment.playbackControlsFragment.binding.playerPlayPauseFab.getY() + fragment.playbackControlsFragment.binding.playerPlayPauseFab.getHeight() / 2 + fragment.playbackControlsFragment.getView().getY() + fragment.playbackControlsFragment.binding.playerProgressSlider.getHeight());
                float startRadius = Math.max(fragment.playbackControlsFragment.binding.playerPlayPauseFab.getWidth() / 2, fragment.playbackControlsFragment.binding.playerPlayPauseFab.getHeight() / 2);
                float endRadius = Math.max(binding.colorBackground.getWidth(), binding.colorBackground.getHeight());
                binding.colorBackground.setBackgroundColor(newColor);
                backgroundAnimator = ViewAnimationUtils.createCircularReveal(binding.colorBackground, x, y, startRadius, endRadius);
            } else {
                backgroundAnimator = ViewUtil.createBackgroundColorTransition(binding.colorBackground, fragment.lastColor, newColor);
            }

            AnimatorSet animatorSet = new AnimatorSet();

            animatorSet.play(backgroundAnimator);

            if (!ATHUtil.isWindowBackgroundDark(fragment.getActivity())) {
                int adjustedLastColor = ColorUtil.isColorLight(fragment.lastColor) ? ColorUtil.darkenColor(fragment.lastColor) : fragment.lastColor;
                int adjustedNewColor = ColorUtil.isColorLight(newColor) ? ColorUtil.darkenColor(newColor) : newColor;
                Animator subHeaderAnimator = ViewUtil.createTextColorTransition(binding.playerQueueSubHeader, adjustedLastColor, adjustedNewColor);
                animatorSet.play(subHeaderAnimator);
            }

            animatorSet.setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME);
            return animatorSet;
        }

        @Override
        public void animateColorChange(int newColor) {
            if (ATHUtil.isWindowBackgroundDark(fragment.getActivity())) {
                binding.playerQueueSubHeader.setTextColor(ThemeStore.textColorSecondary(fragment.getActivity()));
            }
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class PortraitImpl extends BaseImpl {
        MediaEntryViewHolder currentSongViewHolder;
        Song currentSong = Song.EMPTY_SONG;

        public PortraitImpl(CardPlayerFragment fragment, FragmentCardPlayerBinding binding) {
            super(fragment, binding);
        }

        @Override
        public void init() {
            currentSongViewHolder = new MediaEntryViewHolder(fragment.getView().findViewById(R.id.current_song));

            currentSongViewHolder.separator.setVisibility(View.VISIBLE);
            currentSongViewHolder.shortSeparator.setVisibility(View.GONE);
            currentSongViewHolder.image.setScaleType(ImageView.ScaleType.CENTER);
            currentSongViewHolder.image.setColorFilter(ATHUtil.resolveColor(fragment.getActivity(), R.attr.iconColor, ThemeStore.textColorSecondary(fragment.getActivity())), PorterDuff.Mode.SRC_IN);
            currentSongViewHolder.image.setImageResource(R.drawable.ic_volume_up_white_24dp);
            currentSongViewHolder.itemView.setOnClickListener(v -> {
                // toggle the panel
                if (binding.playerSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.COLLAPSED) {
                    binding.playerSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.EXPANDED);
                } else if (binding.playerSlidingLayout.getPanelState() == SlidingUpPanelLayout.PanelState.EXPANDED) {
                    binding.playerSlidingLayout.setPanelState(SlidingUpPanelLayout.PanelState.COLLAPSED);
                }
            });

            currentSongViewHolder.menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu((AppCompatActivity) fragment.getActivity()) {
                @Override
                public Song getSong() {
                    return currentSong;
                }

                public int getMenuRes() {
                    return R.menu.menu_item_playing_queue_song;
                }

                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.action_remove_from_queue:
                            MusicPlayerRemote.removeFromQueue(MusicPlayerRemote.getPosition());
                            return true;
                        case R.id.action_share:
                            SongShareDialog.create(getSong()).show(fragment.getFragmentManager(), "SONG_SHARE_DIALOG");
                            return true;
                    }

                    return super.onMenuItemClick(item);
                }
            });
        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            WidthFitSquareLayout albumCoverContainer = fragment.getView().findViewById(R.id.album_cover_container);

            final int availablePanelHeight = binding.playerSlidingLayout.getHeight() - fragment.getView().findViewById(R.id.player_content).getHeight() + (int) ViewUtil.convertDpToPixel(8, fragment.getResources());
            final int minPanelHeight = (int) ViewUtil.convertDpToPixel(72 + 24, fragment.getResources());
            if (availablePanelHeight < minPanelHeight) {
                albumCoverContainer.getLayoutParams().height = albumCoverContainer.getHeight() - (minPanelHeight - availablePanelHeight);
                albumCoverContainer.forceSquare(false);
            }

            binding.playerSlidingLayout.setPanelHeight(Math.max(minPanelHeight, availablePanelHeight));
            ((AbsSlidingMusicPanelActivity) fragment.getActivity()).setAntiDragView(binding.playerSlidingLayout.findViewById(R.id.player_panel));
        }

        @Override
        public void updateCurrentSong(Song song) {
            currentSong = song;
            currentSongViewHolder.title.setText(song.title);
            currentSongViewHolder.text.setText(MusicUtil.getSongInfoString(song));
        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);

            binding.playerSlidingLayout.setBackgroundColor(fragment.lastColor);
            createDefaultColorChangeAnimatorSet(newColor).start();
        }
    }

    @SuppressWarnings("ConstantConditions")
    private static class LandscapeImpl extends BaseImpl {
        public LandscapeImpl(CardPlayerFragment fragment, FragmentCardPlayerBinding binding) {
            super(fragment, binding);
        }

        @Override
        public void init() {
        }

        @Override
        public void setUpPanelAndAlbumCoverHeight() {
            int panelHeight = binding.playerSlidingLayout.getHeight() - fragment.playbackControlsFragment.getView().getHeight();
            binding.playerSlidingLayout.setPanelHeight(panelHeight);

            ((AbsSlidingMusicPanelActivity) fragment.getActivity()).setAntiDragView(binding.playerSlidingLayout.findViewById(R.id.player_panel));
        }

        @Override
        public void updateCurrentSong(Song song) {
            binding.playerToolbar.setTitle(song.title);
            binding.playerToolbar.setSubtitle(MusicUtil.getSongInfoString(song));
        }

        @Override
        public void animateColorChange(int newColor) {
            super.animateColorChange(newColor);

            binding.playerSlidingLayout.setBackgroundColor(fragment.lastColor);

            AnimatorSet animatorSet = createDefaultColorChangeAnimatorSet(newColor);
            animatorSet.play(ViewUtil.createBackgroundColorTransition(binding.playerToolbar, fragment.lastColor, newColor))
                    .with(ViewUtil.createBackgroundColorTransition(fragment.getView().findViewById(R.id.status_bar), ColorUtil.darkenColor(fragment.lastColor), ColorUtil.darkenColor(newColor)));

            animatorSet.start();
        }
    }
}
