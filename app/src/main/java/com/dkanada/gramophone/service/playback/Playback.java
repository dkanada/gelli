package com.dkanada.gramophone.service.playback;

import com.dkanada.gramophone.model.Song;
import com.google.android.exoplayer2.Player;

import java.util.List;

public interface Playback {
    void setQueue(List<Song> queue, int position, int progress, boolean resetCurrentSong);

    void playSongAt(int position);

    void setCallbacks(PlaybackCallbacks callbacks);

    boolean isReady();

    boolean isPlaying();

    boolean isLoading();

    void start();

    void pause();

    void stop();

    void previous();

    void next();

    void setRepeatMode(@Player.RepeatMode int repeatMode);

    int getProgress();

    int getDuration();

    void setProgress(int progress);

    void setVolume(int volume);

    int getVolume();

    interface PlaybackCallbacks {
        void onStateChanged(int state);

        void onReadyChanged(boolean ready, int reason);

        void onTrackChanged(int reason);
    }
}
