package com.dkanada.gramophone.service.playback;

import com.dkanada.gramophone.model.Song;

public interface Playback {
    void setDataSource(Song song);

    void queueDataSource(Song song);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isReady();

    boolean isPlaying();

    boolean isBuffering();

    void start();

    void pause();

    void stop();

    int getProgress();

    int getDuration();

    void setProgress(int progress);

    void setVolume(int volume);

    int getVolume();

    interface PlaybackCallbacks {
        void onPlayerStateChanged(boolean playWhenReady, int playbackState);

        void onTrackWentToNext();

        void onTrackEnded();
    }
}
