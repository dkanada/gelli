package com.dkanada.gramophone.service;

import android.os.Handler;
import android.os.Message;

import androidx.annotation.NonNull;

public class MultiPlayerHandler extends Handler {

    private final MultiPlayer multiPlayer;

    public MultiPlayerHandler(MultiPlayer multiPlayer) {
        this.multiPlayer = multiPlayer;
    }

    public void handleMessage(@NonNull Message msg) {
        if (msg.what == MultiPlayer.MULTI_PLAYER_START) {
            multiPlayer.start();
        }
    }
}
