package com.dkanada.gramophone.service.playback;

import androidx.annotation.Nullable;

public interface Playback {
    void setDataSource(String path);

    void setNextDataSource(@Nullable String path);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isInitialized();

    boolean start();

    void stop();

    boolean pause();

    boolean isPlaying();

    int duration();

    int position();

    int seek(int whereto);

    boolean setVolume(float vol);

    interface PlaybackCallbacks {
        void onTrackStarted();

        void onTrackWentToNext();

        void onTrackEnded();
    }
}
