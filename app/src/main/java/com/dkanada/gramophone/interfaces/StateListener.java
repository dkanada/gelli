package com.dkanada.gramophone.interfaces;

public interface StateListener {
    void onStatePolling();

    void onStateOnline();

    void onStateOffline();
}
