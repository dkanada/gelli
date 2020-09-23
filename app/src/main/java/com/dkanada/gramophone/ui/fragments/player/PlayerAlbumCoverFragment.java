package com.dkanada.gramophone.ui.fragments.player;

import android.animation.Animator;
import android.annotation.SuppressLint;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.viewpager.widget.ViewPager;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.DecelerateInterpolator;

import com.dkanada.gramophone.adapter.AlbumCoverPagerAdapter;
import com.dkanada.gramophone.databinding.FragmentPlayerAlbumCoverBinding;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.misc.SimpleAnimatorListener;
import com.dkanada.gramophone.ui.fragments.AbsMusicServiceFragment;
import com.dkanada.gramophone.util.ViewUtil;

public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements ViewPager.OnPageChangeListener {

    public static final int VISIBILITY_ANIM_DURATION = 300;

    private FragmentPlayerAlbumCoverBinding binding;

    private Callbacks callbacks;
    private int currentPosition;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentPlayerAlbumCoverBinding.inflate(inflater);

        return binding.getRoot();
    }

    @Override
    @SuppressLint("ClickableViewAccessibility")
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.playerAlbumCoverViewPager.addOnPageChangeListener(this);
        binding.playerAlbumCoverViewPager.setOnTouchListener(new View.OnTouchListener() {
            GestureDetector gestureDetector = new GestureDetector(getActivity(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    if (callbacks != null) {
                        callbacks.onToolbarToggled();
                        return true;
                    }
                    return super.onSingleTapConfirmed(e);
                }
            });

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                return gestureDetector.onTouchEvent(event);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();

        binding.playerAlbumCoverViewPager.removeOnPageChangeListener(this);
    }

    @Override
    public void onServiceConnected() {
        updatePlayingQueue();
    }

    @Override
    public void onPlayMetadataChanged() {
        binding.playerAlbumCoverViewPager.setCurrentItem(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onQueueChanged() {
        updatePlayingQueue();
    }

    private void updatePlayingQueue() {
        binding.playerAlbumCoverViewPager.setAdapter(new AlbumCoverPagerAdapter(getFragmentManager(), MusicPlayerRemote.getPlayingQueue()));
        binding.playerAlbumCoverViewPager.setCurrentItem(MusicPlayerRemote.getPosition());
        onPageSelected(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        ((AlbumCoverPagerAdapter) binding.playerAlbumCoverViewPager.getAdapter()).receiveColor(colorReceiver, position);
        if (position != MusicPlayerRemote.getPosition()) {
            MusicPlayerRemote.playSongAt(position);
        }
    }

    private AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver colorReceiver = new AlbumCoverPagerAdapter.AlbumCoverFragment.ColorReceiver() {
        @Override
        public void onColorReady(int color, int requestCode) {
            if (currentPosition == requestCode) {
                notifyColorChange(color);
            }
        }
    };

    @Override
    public void onPageScrollStateChanged(int state) {
    }

    public void showHeartAnimation() {
        binding.playerFavoriteIcon.clearAnimation();

        binding.playerFavoriteIcon.setAlpha(0f);
        binding.playerFavoriteIcon.setScaleX(0f);
        binding.playerFavoriteIcon.setScaleY(0f);
        binding.playerFavoriteIcon.setVisibility(View.VISIBLE);
        binding.playerFavoriteIcon.setPivotX(binding.playerFavoriteIcon.getWidth() / 2);
        binding.playerFavoriteIcon.setPivotY(binding.playerFavoriteIcon.getHeight() / 2);

        binding.playerFavoriteIcon.animate()
                .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        binding.playerFavoriteIcon.setVisibility(View.INVISIBLE);
                    }
                })
                .withEndAction(() -> binding.playerFavoriteIcon.animate()
                        .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                        .setInterpolator(new AccelerateInterpolator())
                        .scaleX(0f)
                        .scaleY(0f)
                        .alpha(0f)
                        .start())
                .start();
    }

    private void notifyColorChange(int color) {
        if (callbacks != null) callbacks.onColorChanged(color);
    }

    public void setCallbacks(Callbacks listener) {
        callbacks = listener;
    }

    public interface Callbacks {
        void onColorChanged(int color);

        void onFavoriteToggled();

        void onToolbarToggled();
    }
}