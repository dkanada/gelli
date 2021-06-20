package com.dkanada.gramophone.helper;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.MusicService;
import com.dkanada.gramophone.service.QueueManager;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.WeakHashMap;

public class MusicPlayerRemote {
    public static MusicService musicService;

    private static final WeakHashMap<Context, ServiceBinder> mConnectionMap = new WeakHashMap<>();

    public static ServiceToken bindToService(@NonNull final Context context, final ServiceConnection callback) {
        Activity realActivity = ((Activity) context).getParent();
        if (realActivity == null) {
            realActivity = (Activity) context;
        }

        final ContextWrapper contextWrapper = new ContextWrapper(realActivity);
        contextWrapper.startService(new Intent(contextWrapper, MusicService.class));

        final ServiceBinder binder = new ServiceBinder(callback);

        if (contextWrapper.bindService(new Intent().setClass(contextWrapper, MusicService.class), binder, Context.BIND_AUTO_CREATE)) {
            mConnectionMap.put(contextWrapper, binder);
            return new ServiceToken(contextWrapper);
        }

        return null;
    }

    public static void unbindFromService(@Nullable final ServiceToken token) {
        if (token == null) {
            return;
        }

        final ContextWrapper mContextWrapper = token.mWrappedContext;
        final ServiceBinder mBinder = mConnectionMap.remove(mContextWrapper);
        if (mBinder == null) {
            return;
        }

        mContextWrapper.unbindService(mBinder);
        if (mConnectionMap.isEmpty()) {
            musicService = null;
        }
    }

    public static final class ServiceBinder implements ServiceConnection {
        private final ServiceConnection mCallback;

        public ServiceBinder(final ServiceConnection callback) {
            mCallback = callback;
        }

        @Override
        public void onServiceConnected(final ComponentName className, final IBinder service) {
            MusicService.MusicBinder binder = (MusicService.MusicBinder) service;
            musicService = binder.getService();
            if (mCallback != null) {
                mCallback.onServiceConnected(className, service);
            }
        }

        @Override
        public void onServiceDisconnected(final ComponentName className) {
            if (mCallback != null) {
                mCallback.onServiceDisconnected(className);
            }

            musicService = null;
        }
    }

    public static final class ServiceToken {
        public ContextWrapper mWrappedContext;

        public ServiceToken(final ContextWrapper context) {
            mWrappedContext = context;
        }
    }

    public static void playSongAt(final int position) {
        if (musicService != null) {
            musicService.playSongAt(position);
        }
    }

    public static void pauseSong() {
        if (musicService != null) {
            musicService.pause();
        }
    }

    public static void playNextSong() {
        if (musicService != null) {
            musicService.playNextSong();
        }
    }

    public static void playPreviousSong() {
        if (musicService != null) {
            musicService.playPreviousSong();
        }
    }

    public static void back() {
        if (musicService != null) {
            musicService.back();
        }
    }

    public static boolean isPlaying() {
        return musicService != null && musicService.isPlaying();
    }

    public static boolean isLoading() {
        return musicService != null && musicService.isLoading();
    }

    public static void resumePlaying() {
        if (musicService != null) {
            musicService.play();
        }
    }

    public static void openQueue(final List<Song> queue, final int startPosition, final boolean startPlaying) {
        if (!tryToHandleOpenPlayingQueue(queue, startPosition) && musicService != null) {
            if (!PreferenceUtil.getInstance(musicService).getRememberShuffle()) {
                setShuffleMode(QueueManager.SHUFFLE_MODE_NONE);
            }
            musicService.openQueue(queue, startPosition, startPlaying);
        }
    }

    public static void openAndShuffleQueue(final List<Song> queue, boolean startPlaying) {
        int startPosition = 0;
        if (!queue.isEmpty()) {
            startPosition = new Random().nextInt(queue.size());
        }

        if (!tryToHandleOpenPlayingQueue(queue, startPosition) && musicService != null) {
            setShuffleMode(QueueManager.SHUFFLE_MODE_SHUFFLE);
            openQueue(queue, startPosition, startPlaying);
        }
    }

    private static boolean tryToHandleOpenPlayingQueue(final List<Song> queue, final int startPosition) {
        if (getPlayingQueue() == queue) {
            playSongAt(startPosition);

            return true;
        }

        return false;
    }

    public static Song getCurrentSong() {
        if (musicService != null && musicService.queueManager != null) {
            return musicService.queueManager.getCurrentSong();
        }

        return null;
    }

    public static int getPosition() {
        if (musicService != null && musicService.queueManager != null) {
            return musicService.queueManager.getPosition();
        }

        return -1;
    }

    public static List<Song> getPlayingQueue() {
        if (musicService != null && musicService.queueManager != null) {
            return musicService.queueManager.getPlayingQueue();
        }

        return new ArrayList<>();
    }

    public static int getSongProgressMillis() {
        if (musicService != null) {
            return musicService.getSongProgressMillis();
        }

        return -1;
    }

    public static int getSongDurationMillis() {
        if (musicService != null) {
            return musicService.getSongDurationMillis();
        }

        return -1;
    }

    public static long getQueueDurationMillis(int position) {
        if (musicService != null && musicService.queueManager != null) {
            return musicService.queueManager.getQueueDurationMillis(position);
        }

        return -1;
    }

    public static int seekTo(int millis) {
        if (musicService != null) {
            return musicService.seek(millis);
        }

        return -1;
    }

    public static int getRepeatMode() {
        if (musicService != null && musicService.queueManager != null) {
            return musicService.queueManager.getRepeatMode();
        }

        return QueueManager.REPEAT_MODE_NONE;
    }

    public static int getShuffleMode() {
        if (musicService != null && musicService.queueManager != null) {
            return musicService.queueManager.getShuffleMode();
        }

        return QueueManager.SHUFFLE_MODE_NONE;
    }

    public static boolean cycleRepeatMode() {
        if (musicService != null && musicService.queueManager != null) {
            musicService.queueManager.cycleRepeatMode();
            return true;
        }

        return false;
    }

    public static boolean toggleShuffleMode() {
        if (musicService != null && musicService.queueManager != null) {
            musicService.queueManager.toggleShuffle();
            return true;
        }

        return false;
    }

    public static boolean setShuffleMode(final int shuffleMode) {
        if (musicService != null && musicService.queueManager != null) {
            musicService.queueManager.setShuffleMode(shuffleMode);
            return true;
        }

        return false;
    }

    public static boolean playNext(Song song) {
        if (musicService != null && musicService.queueManager != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.queueManager.addSong(getPosition() + 1, song);
            } else {
                List<Song> queue = new ArrayList<>();
                queue.add(song);
                openQueue(queue, 0, false);
            }

            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_queue), Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public static boolean playNext(@NonNull List<Song> songs) {
        if (musicService != null && musicService.queueManager != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.queueManager.addSongs(getPosition() + 1, songs);
            } else {
                openQueue(songs, 0, false);
            }

            final String toast = songs.size() == 1 ? musicService.getResources().getString(R.string.added_title_to_queue) : musicService.getResources().getString(R.string.added_x_titles_to_queue, songs.size());
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public static boolean enqueue(Song song) {
        if (musicService != null && musicService.queueManager != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.queueManager.addSong(song);
            } else {
                List<Song> queue = new ArrayList<>();
                queue.add(song);
                openQueue(queue, 0, false);
            }

            Toast.makeText(musicService, musicService.getResources().getString(R.string.added_title_to_queue), Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public static boolean enqueue(@NonNull List<Song> songs) {
        if (musicService != null && musicService.queueManager != null) {
            if (getPlayingQueue().size() > 0) {
                musicService.queueManager.addSongs(songs);
            } else {
                openQueue(songs, 0, false);
            }

            final String toast = songs.size() == 1 ? musicService.getResources().getString(R.string.added_title_to_queue) : musicService.getResources().getString(R.string.added_x_titles_to_queue, songs.size());
            Toast.makeText(musicService, toast, Toast.LENGTH_SHORT).show();
            return true;
        }

        return false;
    }

    public static boolean removeFromQueue(int position) {
        if (musicService != null && musicService.queueManager != null && position >= 0 && position < getPlayingQueue().size()) {
            musicService.queueManager.removeSong(position);
            return true;
        }

        return false;
    }

    public static boolean moveSong(int from, int to) {
        if (musicService != null && musicService.queueManager != null && from >= 0 && to >= 0 && from < getPlayingQueue().size() && to < getPlayingQueue().size()) {
            musicService.queueManager.moveSong(from, to);
            return true;
        }

        return false;
    }

    public static boolean clearQueue() {
        if (musicService != null && musicService.queueManager != null) {
            musicService.queueManager.clearQueue();
            return true;
        }

        return false;
    }
}
