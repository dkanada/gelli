package com.dkanada.gramophone.service;

import android.content.Context;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.helper.ShuffleHelper;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class QueueManager {
    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_THIS = 1;
    public static final int REPEAT_MODE_ALL = 2;

    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;

    private final Context context;
    private final QueueCallbacks callbacks;

    List<Song> playingQueue = new ArrayList<>();
    List<Song> originalPlayingQueue = new ArrayList<>();

    private int position = 0;
    private int restoredProgress = 0;
    private boolean resetCurrentSong = true;

    private int shuffleMode;
    private int repeatMode;

    public QueueManager(Context context, QueueCallbacks callbacks) {
        this.context = context;
        this.callbacks = callbacks;
    }

    public void setPlayingQueueAndPosition(List<Song> queue, List<Song> originalPlayingQueue, int position) {
        this.position = position;
        this.playingQueue = new ArrayList<>(queue);
        this.originalPlayingQueue = new ArrayList<>(originalPlayingQueue);

        callbacks.onQueueChanged();
    }

    public int getPosition() {
        return position;
    }

    public Song getCurrentSong() {
        return getSongAt(getPosition());
    }

    public Song getSongAt(int position) {
        if (position >= 0 && position < getPlayingQueue().size()) {
            return getPlayingQueue().get(position);
        }

        return null;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public void setNextPosition() {
        switch (getRepeatMode()) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_THIS:
                position = Math.min(position + 1, playingQueue.size() - 1);
                break;
            case REPEAT_MODE_ALL:
                position = (position + 1) % playingQueue.size();
                break;
        }
    }

    public void setPreviousPosition() {
        switch (getRepeatMode()) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_THIS:
                position = Math.max(position - 1, 0);
                break;
            case REPEAT_MODE_ALL:
                position = (position - 1 + playingQueue.size()) % playingQueue.size();
                break;
        }
    }

    public boolean isLastTrack() {
        return getPosition() == getPlayingQueue().size() - 1;
    }

    public List<Song> getPlayingQueue() {
        return playingQueue;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    public void toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE);
        } else {
            setShuffleMode(SHUFFLE_MODE_NONE);
        }
    }

    public void setRepeatMode(final int repeatMode) {
        switch (repeatMode) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_ALL:
            case REPEAT_MODE_THIS:
                this.repeatMode = repeatMode;
                PreferenceUtil.getInstance(context).setRepeat(repeatMode);
                callbacks.onRepeatModeChanged();
                break;
        }
    }

    public void setShuffleMode(final int shuffleMode) {
        PreferenceUtil.getInstance(context).setShuffle(shuffleMode);

        switch (shuffleMode) {
            case SHUFFLE_MODE_SHUFFLE:
                this.shuffleMode = shuffleMode;
                ShuffleHelper.makeShuffleList(this.getPlayingQueue(), getPosition());
                position = 0;
                break;
            case SHUFFLE_MODE_NONE:
                this.shuffleMode = shuffleMode;
                String currentSongId = getCurrentSong().id;
                playingQueue = new ArrayList<>(originalPlayingQueue);
                int newPosition = 0;
                for (Song song : getPlayingQueue()) {
                    if (song.id == currentSongId) {
                        newPosition = getPlayingQueue().indexOf(song);
                    }
                }

                position = newPosition;
                break;
        }

        resetCurrentSong = false;
        callbacks.onShuffleModeChanged();
        callbacks.onQueueChanged();
    }

    public void addSong(int position, Song song) {
        playingQueue.add(position, song);
        originalPlayingQueue.add(position, song);

        resetCurrentSong = false;
        callbacks.onQueueChanged();
    }

    public void addSong(Song song) {
        playingQueue.add(song);
        originalPlayingQueue.add(song);

        resetCurrentSong = false;
        callbacks.onQueueChanged();
    }

    public void addSongs(int position, List<Song> songs) {
        playingQueue.addAll(position, songs);
        originalPlayingQueue.addAll(position, songs);

        resetCurrentSong = false;
        callbacks.onQueueChanged();
    }

    public void addSongs(List<Song> songs) {
        playingQueue.addAll(songs);
        originalPlayingQueue.addAll(songs);

        resetCurrentSong = false;
        callbacks.onQueueChanged();
    }

    public void removeSong(int position) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.remove(position);
            originalPlayingQueue.remove(position);
        } else {
            originalPlayingQueue.remove(playingQueue.remove(position));
        }

        int currentPosition = getPosition();
        if (position != currentPosition) {
            resetCurrentSong = false;
        }

        if (position < currentPosition) {
            this.position = currentPosition - 1;
        }

        callbacks.onQueueChanged();
    }

    public void moveSong(int from, int to) {
        if (from == to) return;

        final int currentPosition = getPosition();
        Song songToMove = playingQueue.remove(from);
        playingQueue.add(to, songToMove);
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            Song tmpSong = originalPlayingQueue.remove(from);
            originalPlayingQueue.add(to, tmpSong);
        }

        if (from > currentPosition && to <= currentPosition) {
            position = currentPosition + 1;
        } else if (from < currentPosition && to >= currentPosition) {
            position = currentPosition - 1;
        } else if (from == currentPosition) {
            position = to;
        }

        resetCurrentSong = false;
        callbacks.onQueueChanged();
    }

    public void clearQueue() {
        playingQueue.clear();
        originalPlayingQueue.clear();

        position = 0;
        callbacks.onQueueChanged();
    }

    public long getQueueDurationMillis(int position) {
        long duration = 0;
        for (int i = position + 1; i < playingQueue.size(); i++) {
            duration += playingQueue.get(i).duration;
        }

        return duration;
    }

    public void cycleRepeatMode() {
        switch (getRepeatMode()) {
            case REPEAT_MODE_NONE:
                setRepeatMode(REPEAT_MODE_ALL);
                break;
            case REPEAT_MODE_ALL:
                setRepeatMode(REPEAT_MODE_THIS);
                break;
            default:
                setRepeatMode(REPEAT_MODE_NONE);
                break;
        }
    }

    public void restoreQueue() {
        position = PreferenceUtil.getInstance(context).getPosition();
        restoredProgress = PreferenceUtil.getInstance(context).getProgress();

        playingQueue = new ArrayList<>(App.getDatabase().queueSongDao().getQueue(0));
        originalPlayingQueue = new ArrayList<>(App.getDatabase().queueSongDao().getQueue(1));

        shuffleMode = PreferenceUtil.getInstance(context).getShuffle();
        repeatMode = PreferenceUtil.getInstance(context).getRepeat();

        callbacks.onQueueChanged();
        callbacks.onRepeatModeChanged();
        callbacks.onShuffleModeChanged();
    }

    public void saveQueue() {
        PreferenceUtil.getInstance(context).setPosition(position);

        // copy queues by value to avoid concurrent modification exceptions from database
        App.getDatabase().songDao().deleteSongs();
        App.getDatabase().songDao().insertSongs(new ArrayList<>(playingQueue));

        App.getDatabase().queueSongDao().deleteQueueSongs();
        App.getDatabase().queueSongDao().setQueue(new ArrayList<>(playingQueue), 0);
        App.getDatabase().queueSongDao().setQueue(new ArrayList<>(originalPlayingQueue), 1);
    }

    public int getRestoredProgress() {
        int progress = restoredProgress;
        restoredProgress = 0;

        return progress;
    }

    public boolean isResetCurrentSong() {
        boolean reset = resetCurrentSong;
        resetCurrentSong = true;

        return reset;
    }

    interface QueueCallbacks {
        void onQueueChanged();
        void onRepeatModeChanged();
        void onShuffleModeChanged();
    }
}
