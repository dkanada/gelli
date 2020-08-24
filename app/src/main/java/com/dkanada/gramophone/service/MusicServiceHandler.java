package com.dkanada.gramophone.service;

import android.media.AudioManager;
import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

import com.dkanada.gramophone.util.PreferenceUtil;

final class MusicServiceHandler extends Handler {
    @NonNull
    private final MusicService musicService;
    private float currentDuckVolume = 1.0f;

    public MusicServiceHandler(MusicService musicService) {
        this.musicService = musicService;
    }

    @Override
    public void handleMessage(@NonNull final Message msg) {

        switch (msg.what) {
            case MusicService.DUCK:
                if (PreferenceUtil.getInstance(musicService).getAudioDucking()) {
                    currentDuckVolume -= .05f;
                    if (currentDuckVolume > .2f) {
                        sendEmptyMessageDelayed(MusicService.DUCK, 10);
                    } else {
                        currentDuckVolume = .2f;
                    }
                } else {
                    currentDuckVolume = 1f;
                }
                musicService.setVolume(currentDuckVolume);
                break;

            case MusicService.UNDUCK:
                if (PreferenceUtil.getInstance(musicService).getAudioDucking()) {
                    currentDuckVolume += .03f;
                    if (currentDuckVolume < 1f) {
                        sendEmptyMessageDelayed(MusicService.UNDUCK, 10);
                    } else {
                        currentDuckVolume = 1f;
                    }
                } else {
                    currentDuckVolume = 1f;
                }
                musicService.setVolume(currentDuckVolume);
                break;

            case MusicService.TRACK_WENT_TO_NEXT:
                if (musicService.getRepeatMode() == MusicService.REPEAT_MODE_NONE && musicService.isLastTrack()) {
                    musicService.pause();
                    musicService.seek(0);
                } else {
                    musicService.setPosition(musicService.getNextPosition());
                    musicService.prepareNextImpl();
                    musicService.notifyChange(MusicService.META_CHANGED);
                }
                break;

            case MusicService.TRACK_ENDED:
                // if there is a timer finished, don't continue
                if (musicService.pendingQuit ||
                        musicService.getRepeatMode() == MusicService.REPEAT_MODE_NONE && musicService.isLastTrack()) {
                    musicService.notifyChange(MusicService.PLAY_STATE_CHANGED);
                    musicService.seek(0);
                    if (musicService.pendingQuit) {
                        musicService.pendingQuit = false;
                        musicService.quit();
                        break;
                    }
                } else {
                    musicService.playNextSong(false);
                }

                sendEmptyMessage(MusicService.RELEASE_WAKELOCK);
                break;

            case MusicService.RELEASE_WAKELOCK:
                musicService.releaseWakeLock();
                break;

            case MusicService.PLAY_SONG:
                musicService.playSongAtImpl(msg.arg1);
                break;

            case MusicService.SET_POSITION:
                musicService.openTrackAndPrepareNextAt(msg.arg1);
                musicService.notifyChange(MusicService.PLAY_STATE_CHANGED);
                break;

            case MusicService.PREPARE_NEXT:
                musicService.prepareNextImpl();
                break;

            case MusicService.RESTORE_QUEUES:
                musicService.restoreQueuesAndPositionIfNecessary();
                break;

            case MusicService.FOCUS_CHANGE:
                switch (msg.arg1) {
                    case AudioManager.AUDIOFOCUS_GAIN:
                        if (!musicService.isPlaying() && musicService.getPausedByTransientLossOfFocus()) {
                            musicService.play();
                            musicService.setPausedByTransientLossOfFocus(false);
                        }
                        removeMessages(MusicService.DUCK);
                        sendEmptyMessage(MusicService.UNDUCK);
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS:
                        // Lost focus for an unbounded amount of time: stop playback and release media playback
                        musicService.pause();
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                        // Lost focus for a short time, but we have to stop
                        // playback. We don't release the media playback because playback
                        // is likely to resume
                        boolean wasPlaying = musicService.isPlaying();
                        musicService.pause();
                        musicService.setPausedByTransientLossOfFocus(wasPlaying);
                        break;

                    case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                        // Lost focus for a short time, but it's ok to keep playing
                        // at an attenuated level
                        removeMessages(MusicService.UNDUCK);
                        sendEmptyMessage(MusicService.DUCK);
                        break;
                }
                break;
        }
    }
}
