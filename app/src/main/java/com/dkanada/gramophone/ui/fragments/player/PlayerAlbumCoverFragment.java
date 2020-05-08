package com.dkanada.gramophone.ui.fragments.player;

import android.animation.Animator;
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
import android.widget.ImageView;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.AlbumCoverPagerAdapter;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.misc.SimpleAnimatorListener;
import com.dkanada.gramophone.ui.fragments.AbsMusicServiceFragment;
import com.dkanada.gramophone.util.ViewUtil;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.Unbinder;

public class PlayerAlbumCoverFragment extends AbsMusicServiceFragment implements ViewPager.OnPageChangeListener {

    public static final int VISIBILITY_ANIM_DURATION = 300;

    private Unbinder unbinder;

    @BindView(R.id.player_album_cover_viewpager)
    ViewPager viewPager;
    @BindView(R.id.player_favorite_icon)
    ImageView favoriteIcon;

    private Callbacks callbacks;
    private int currentPosition;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_player_album_cover, container, false);
        unbinder = ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        viewPager.addOnPageChangeListener(this);
        viewPager.setOnTouchListener(new View.OnTouchListener() {
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
        viewPager.removeOnPageChangeListener(this);
        unbinder.unbind();
    }

    @Override
    public void onServiceConnected() {
        updatePlayingQueue();
    }

    @Override
    public void onPlayingMetaChanged() {
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onQueueChanged() {
        updatePlayingQueue();
    }

    private void updatePlayingQueue() {
        viewPager.setAdapter(new AlbumCoverPagerAdapter(getFragmentManager(), MusicPlayerRemote.getPlayingQueue()));
        viewPager.setCurrentItem(MusicPlayerRemote.getPosition());
        onPageSelected(MusicPlayerRemote.getPosition());
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
    }

    @Override
    public void onPageSelected(int position) {
        currentPosition = position;
        ((AlbumCoverPagerAdapter) viewPager.getAdapter()).receiveColor(colorReceiver, position);
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
        favoriteIcon.clearAnimation();

        favoriteIcon.setAlpha(0f);
        favoriteIcon.setScaleX(0f);
        favoriteIcon.setScaleY(0f);
        favoriteIcon.setVisibility(View.VISIBLE);
        favoriteIcon.setPivotX(favoriteIcon.getWidth() / 2);
        favoriteIcon.setPivotY(favoriteIcon.getHeight() / 2);

        favoriteIcon.animate()
                .setDuration(ViewUtil.PHONOGRAPH_ANIM_TIME / 2)
                .setInterpolator(new DecelerateInterpolator())
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setListener(new SimpleAnimatorListener() {
                    @Override
                    public void onAnimationCancel(Animator animation) {
                        favoriteIcon.setVisibility(View.INVISIBLE);
                    }
                })
                .withEndAction(() -> favoriteIcon.animate()
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