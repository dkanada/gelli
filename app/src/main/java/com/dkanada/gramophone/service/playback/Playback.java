package com.dkanada.gramophone.service.playback;

import com.dkanada.gramophone.model.Song;

public interface Playback {
    void setDataSource(Song song);

    void queueDataSource(Song song);

    void setCallbacks(PlaybackCallbacks callbacks);

    void start();

    void pause();

    void stop();

    boolean isInitialized();

    boolean isPlaying();

    int getProgress();

    int getDuration();

    void setProgress(int position);

    void setVolume(int volume);

    int getVolume();

    interface PlaybackCallbacks {
        void onTrackStarted();

        void onTrackWentToNext();

        void onTrackEnded();
    }
}
