package com.dkanada.gramophone.helper;

import java.util.Locale;

public class StopWatch {
    private long startTime;
    private long previousElapsedTime;
    private boolean isRunning;

    public void start() {
        synchronized (this) {
            startTime = System.currentTimeMillis();
            isRunning = true;
        }
    }

    public void pause() {
        synchronized (this) {
            previousElapsedTime += System.currentTimeMillis() - startTime;
            isRunning = false;
        }
    }

    public void reset() {
        synchronized (this) {
            startTime = 0;
            previousElapsedTime = 0;
            isRunning = false;
        }
    }

    public final long getElapsedTime() {
        synchronized (this) {
            long currentElapsedTime = 0;
            if (isRunning) {
                currentElapsedTime = System.currentTimeMillis() - startTime;
            }

            return previousElapsedTime + currentElapsedTime;
        }
    }

    @Override
    public String toString() {
        return String.format(Locale.getDefault(), "%d millis", getElapsedTime());
    }
}
