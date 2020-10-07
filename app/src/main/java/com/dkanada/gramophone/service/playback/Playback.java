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

    int position();

    int duration();

    void seek(int position);

    void volume(float volume);

    interface PlaybackCallbacks {
        void onTrackStarted();

        void onTrackWentToNext();

        void onTrackEnded();
    }
}
