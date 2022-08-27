package com.dkanada.gramophone.service.playback;

public interface PlaybackListener {
    void onStateChanged(int state);

    void onReadyChanged(boolean ready, int reason);

    void onTrackChanged(int reason);
}
