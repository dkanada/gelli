package com.dkanada.gramophone.service;

import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.os.Process;
import android.support.v4.media.MediaMetadataCompat;
import android.support.v4.media.session.MediaSessionCompat;
import android.support.v4.media.session.PlaybackStateCompat;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.transition.Transition;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.BuildConfig;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.BlurTransformation;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.helper.ShuffleHelper;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.notifications.PlayingNotification;
import com.dkanada.gramophone.service.notifications.PlayingNotificationMarshmallow;
import com.dkanada.gramophone.service.notifications.PlayingNotificationNougat;
import com.dkanada.gramophone.service.playback.LocalPlayer;
import com.dkanada.gramophone.service.playback.Playback;
import com.dkanada.gramophone.service.receivers.MediaButtonIntentReceiver;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.Util;
import com.dkanada.gramophone.views.widgets.AppWidgetAlbum;
import com.dkanada.gramophone.views.widgets.AppWidgetCard;
import com.dkanada.gramophone.views.widgets.AppWidgetClassic;

import org.jellyfin.apiclient.interaction.EmptyResponse;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.session.PlaybackProgressInfo;
import org.jellyfin.apiclient.model.session.PlaybackStartInfo;
import org.jellyfin.apiclient.model.session.PlaybackStopInfo;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import static com.google.android.exoplayer2.Player.MEDIA_ITEM_TRANSITION_REASON_AUTO;
import static com.google.android.exoplayer2.Player.MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED;
import static com.google.android.exoplayer2.Player.PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM;

public class MusicService extends Service implements SharedPreferences.OnSharedPreferenceChangeListener, Playback.PlaybackCallbacks {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;

    public static final String ACTION_TOGGLE = PACKAGE_NAME + ".toggle";
    public static final String ACTION_PLAY = PACKAGE_NAME + ".play";
    public static final String ACTION_PLAY_PLAYLIST = PACKAGE_NAME + ".play.playlist";
    public static final String ACTION_PAUSE = PACKAGE_NAME + ".pause";
    public static final String ACTION_STOP = PACKAGE_NAME + ".stop";
    public static final String ACTION_SKIP = PACKAGE_NAME + ".skip";
    public static final String ACTION_REWIND = PACKAGE_NAME + ".rewind";
    public static final String ACTION_QUIT = PACKAGE_NAME + ".quit";
    public static final String ACTION_PENDING_QUIT = PACKAGE_NAME + ".quit.pending";

    public static final String INTENT_EXTRA_PLAYLIST = PACKAGE_NAME + ".extra.playlist";
    public static final String INTENT_EXTRA_SHUFFLE = PACKAGE_NAME + ".extra.shuffle";
    public static final String INTENT_EXTRA_WIDGET_UPDATE = PACKAGE_NAME + ".extra.widget.update";
    public static final String INTENT_EXTRA_WIDGET_NAME = PACKAGE_NAME + ".extra.widget.name";

    public static final String STATE_CHANGED = PACKAGE_NAME + ".state.changed";
    public static final String META_CHANGED = PACKAGE_NAME + ".meta.changed";
    public static final String QUEUE_CHANGED = PACKAGE_NAME + ".queue.changed";

    public static final String REPEAT_MODE_CHANGED = PACKAGE_NAME + ".repeat.changed";
    public static final String SHUFFLE_MODE_CHANGED = PACKAGE_NAME + ".shuffle.changed";

    public static final int TRACK_STARTED = 9;
    public static final int TRACK_CHANGED = 1;
    public static final int TRACK_ENDED = 2;

    public static final int RELEASE_WAKELOCK = 0;
    public static final int PLAY_SONG = 3;
    public static final int PREPARE_NEXT = 4;
    public static final int SET_POSITION = 5;

    public static final int SHUFFLE_MODE_NONE = 0;
    public static final int SHUFFLE_MODE_SHUFFLE = 1;

    public static final int REPEAT_MODE_NONE = 0;
    public static final int REPEAT_MODE_ALL = 1;
    public static final int REPEAT_MODE_THIS = 2;

    public static final int SAVE_QUEUE = 0;
    public static final int LOAD_QUEUE = 9;

    private final IBinder musicBinder = new MusicBinder();

    public boolean pendingQuit = false;

    private final AppWidgetAlbum appWidgetAlbum = AppWidgetAlbum.getInstance();
    private final AppWidgetCard appWidgetCard = AppWidgetCard.getInstance();
    private final AppWidgetClassic appWidgetClassic = AppWidgetClassic.getInstance();

    private Playback playback;

    private List<Song> playingQueue = new ArrayList<>();
    private List<Song> originalPlayingQueue = new ArrayList<>();

    private int position = -1;
    private int nextPosition = -1;

    private int shuffleMode;
    private int repeatMode;

    private boolean notHandledMetaChangedForCurrentTrack;
    private boolean queuesRestored;

    private PlayingNotification playingNotification;
    private MediaSessionCompat mediaSession;
    private PowerManager.WakeLock wakeLock;

    private PlaybackHandler playerHandler;
    private Handler uiThreadHandler;
    private ThrottledSeekHandler throttledSeekHandler;
    private QueueHandler queueHandler;
    private ProgressHandler progressHandler;

    private HandlerThread playerHandlerThread;
    private HandlerThread progressHandlerThread;
    private HandlerThread queueHandlerThread;

    private final BroadcastReceiver becomingNoisyReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, @NonNull Intent intent) {
            if (intent.getAction().equals(AudioManager.ACTION_AUDIO_BECOMING_NOISY)) {
                pause();
            }
        }
    };

    private final BroadcastReceiver widgetIntentReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(final Context context, final Intent intent) {
            final String command = intent.getStringExtra(INTENT_EXTRA_WIDGET_NAME);
            final int[] ids = intent.getIntArrayExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS);

            switch (command) {
                case AppWidgetClassic.NAME:
                    appWidgetClassic.notifyChange(MusicService.this, META_CHANGED, ids);
                    break;
                case AppWidgetAlbum.NAME:
                    appWidgetAlbum.notifyChange(MusicService.this, META_CHANGED, ids);
                    break;
                case AppWidgetCard.NAME:
                    appWidgetCard.notifyChange(MusicService.this, META_CHANGED, ids);
                    break;
            }
        }
    };

    private static final long MEDIA_SESSION_ACTIONS = PlaybackStateCompat.ACTION_PLAY
            | PlaybackStateCompat.ACTION_PAUSE
            | PlaybackStateCompat.ACTION_PLAY_PAUSE
            | PlaybackStateCompat.ACTION_SKIP_TO_NEXT
            | PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS
            | PlaybackStateCompat.ACTION_STOP
            | PlaybackStateCompat.ACTION_SEEK_TO;

    @Override
    public void onCreate() {
        super.onCreate();

        final PowerManager powerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, getClass().getName());
        wakeLock.setReferenceCounted(false);

        playback = new LocalPlayer(this);
        playback.setCallbacks(this);

        playerHandlerThread = new HandlerThread(PlaybackHandler.class.getName());
        playerHandlerThread.start();
        playerHandler = new PlaybackHandler(this, playerHandlerThread.getLooper());

        progressHandlerThread = new HandlerThread(ProgressHandler.class.getName());
        progressHandlerThread.start();
        progressHandler = new ProgressHandler(this, progressHandlerThread.getLooper());

        queueHandlerThread = new HandlerThread(QueueHandler.class.getName(), Process.THREAD_PRIORITY_BACKGROUND);
        queueHandlerThread.start();
        queueHandler = new QueueHandler(this, queueHandlerThread.getLooper());

        throttledSeekHandler = new ThrottledSeekHandler(playerHandler);
        uiThreadHandler = new Handler();

        registerReceiver(widgetIntentReceiver, new IntentFilter(INTENT_EXTRA_WIDGET_UPDATE));
        registerReceiver(becomingNoisyReceiver, new IntentFilter(AudioManager.ACTION_AUDIO_BECOMING_NOISY));

        PreferenceUtil.getInstance(this).registerOnSharedPreferenceChangedListener(this);

        initNotification();
        initMediaSession();
        restoreState();

        mediaSession.setActive(true);
    }

    private void initMediaSession() {
        ComponentName mediaButtonReceiverComponentName = new ComponentName(getApplicationContext(), MediaButtonIntentReceiver.class);

        Intent mediaButtonIntent = new Intent(Intent.ACTION_MEDIA_BUTTON);
        mediaButtonIntent.setComponent(mediaButtonReceiverComponentName);

        PendingIntent mediaButtonReceiverPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, mediaButtonIntent, 0);

        mediaSession = new MediaSessionCompat(this, getResources().getString(R.string.app_name), mediaButtonReceiverComponentName, mediaButtonReceiverPendingIntent);
        mediaSession.setCallback(new MediaSessionCompat.Callback() {
            @Override
            public void onPlay() {
                play();
            }

            @Override
            public void onPause() {
                pause();
            }

            @Override
            public void onSkipToNext() {
                playNextSong(true);
            }

            @Override
            public void onSkipToPrevious() {
                back(true);
            }

            @Override
            public void onStop() {
                quit();
            }

            @Override
            public void onSeekTo(long pos) {
                seek((int) pos);
            }

            @Override
            public boolean onMediaButtonEvent(Intent mediaButtonEvent) {
                return MediaButtonIntentReceiver.handleIntent(MusicService.this, mediaButtonEvent);
            }
        });

        mediaSession.setMediaButtonReceiver(mediaButtonReceiverPendingIntent);
    }

    @Override
    public int onStartCommand(@Nullable Intent intent, int flags, int startId) {
        if (intent != null) {
            if (intent.getAction() != null) {
                String action = intent.getAction();
                switch (action) {
                    case ACTION_TOGGLE:
                        if (isPlaying()) {
                            pause();
                        } else {
                            play();
                        }
                        break;
                    case ACTION_PAUSE:
                        pause();
                        break;
                    case ACTION_PLAY:
                        play();
                        break;
                    case ACTION_PLAY_PLAYLIST:
                        Playlist playlist = intent.getParcelableExtra(INTENT_EXTRA_PLAYLIST);
                        int shuffleMode = intent.getIntExtra(INTENT_EXTRA_SHUFFLE, getShuffleMode());
                        if (playlist != null) {
                            List<Song> playlistSongs = new ArrayList<>();
                            if (!playlistSongs.isEmpty()) {
                                if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                                    int startPosition = new Random().nextInt(playlistSongs.size());
                                    openQueue(playlistSongs, startPosition, true);
                                    setShuffleMode(shuffleMode);
                                } else {
                                    openQueue(playlistSongs, 0, true);
                                }
                            } else {
                                Toast.makeText(getApplicationContext(), R.string.playlist_is_empty, Toast.LENGTH_LONG).show();
                            }
                        } else {
                            Toast.makeText(getApplicationContext(), R.string.playlist_is_empty, Toast.LENGTH_LONG).show();
                        }
                        break;
                    case ACTION_REWIND:
                        back(true);
                        break;
                    case ACTION_SKIP:
                        playNextSong(true);
                        break;
                    case ACTION_STOP:
                    case ACTION_QUIT:
                        pendingQuit = false;
                        quit();
                        break;
                    case ACTION_PENDING_QUIT:
                        pendingQuit = true;
                        break;
                }
            }
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(widgetIntentReceiver);
        unregisterReceiver(becomingNoisyReceiver);

        progressHandler.sendEmptyMessage(TRACK_ENDED);
        mediaSession.setActive(false);
        quit();
        releaseResources();
        PreferenceUtil.getInstance(this).unregisterOnSharedPreferenceChangedListener(this);
        wakeLock.release();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return musicBinder;
    }

    private static final class QueueHandler extends Handler {
        @NonNull
        private final WeakReference<MusicService> mService;

        public QueueHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull Message msg) {
            final MusicService service = mService.get();
            switch (msg.what) {
                case LOAD_QUEUE:
                    service.restoreQueuesAndPositionIfNecessary();
                    break;
                case SAVE_QUEUE:
                    service.saveQueue();
                    break;
            }
        }
    }

    private void saveQueue() {
        // copy queues by value to avoid concurrent modification exceptions from database
        App.getDatabase().songDao().deleteSongs();
        App.getDatabase().songDao().insertSongs(new ArrayList<>(playingQueue));

        App.getDatabase().queueSongDao().deleteQueueSongs();
        App.getDatabase().queueSongDao().setQueue(new ArrayList<>(playingQueue), 0);
        App.getDatabase().queueSongDao().setQueue(new ArrayList<>(originalPlayingQueue), 1);
    }

    public void saveState() {
        queueHandler.removeMessages(SAVE_QUEUE);
        queueHandler.sendEmptyMessage(SAVE_QUEUE);

        PreferenceUtil.getInstance(this).setPosition(getPosition());
        PreferenceUtil.getInstance(this).setProgress(getSongProgressMillis());
    }

    private void restoreState() {
        shuffleMode = PreferenceUtil.getInstance(this).getShuffle();
        repeatMode = PreferenceUtil.getInstance(this).getRepeat();

        notifyChange(SHUFFLE_MODE_CHANGED);
        notifyChange(REPEAT_MODE_CHANGED);

        queueHandler.removeMessages(LOAD_QUEUE);
        queueHandler.sendEmptyMessage(LOAD_QUEUE);
    }

    private synchronized void restoreQueuesAndPositionIfNecessary() {
        if (!queuesRestored && playingQueue.isEmpty()) {
            List<Song> restoredQueue = App.getDatabase().queueSongDao().getQueue(0);
            List<Song> restoredOriginalQueue = App.getDatabase().queueSongDao().getQueue(1);

            int restoredPosition = PreferenceUtil.getInstance(this).getPosition();
            int restoredProgress = PreferenceUtil.getInstance(this).getProgress();

            if (restoredQueue.size() > 0 && restoredQueue.size() == restoredOriginalQueue.size() && restoredPosition != -1) {
                this.originalPlayingQueue = restoredOriginalQueue;
                this.playingQueue = restoredQueue;

                position = restoredPosition;
                openCurrent();

                if (restoredProgress > 0) seek(restoredProgress);

                notHandledMetaChangedForCurrentTrack = true;
                handleChangeInternal(META_CHANGED);
                handleChangeInternal(QUEUE_CHANGED);
            }
        }

        queuesRestored = true;
    }

    private void quit() {
        pause();
        playingNotification.stop();

        stopSelf();
    }

    private void releaseResources() {
        playerHandler.removeCallbacksAndMessages(null);
        playerHandlerThread.quitSafely();

        progressHandler.removeCallbacksAndMessages(null);
        progressHandlerThread.quitSafely();

        queueHandler.removeCallbacksAndMessages(null);
        queueHandlerThread.quitSafely();

        playback.stop();
        mediaSession.release();
    }

    public boolean isPlaying() {
        return playback != null && playback.isPlaying();
    }

    public boolean isLoading() {
        return playback != null && playback.isLoading();
    }

    public int getPosition() {
        return position;
    }

    public void playNextSong(boolean force) {
        playSongAt(getNextPosition(force));
    }

    private void openTrackAndPrepareNextAt(int position) {
        synchronized (this) {
            this.position = position;

            openCurrent();
            playback.start();

            notifyChange(META_CHANGED);
            notHandledMetaChangedForCurrentTrack = false;
        }
    }

    private void openCurrent() {
        synchronized (this) {
            if (getCurrentSong() == null) return;

            playback.setDataSource(getCurrentSong());
        }
    }

    private void prepareNext() {
        playerHandler.removeMessages(PREPARE_NEXT);
        playerHandler.obtainMessage(PREPARE_NEXT).sendToTarget();
    }

    private void prepareNextImpl() {
        synchronized (this) {
            if (getCurrentSong() == null) return;

            nextPosition = getNextPosition(false);
            playback.queueDataSource(getSongAt(nextPosition));
        }
    }

    public void initNotification() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N && !PreferenceUtil.getInstance(this).getClassicNotification()) {
            playingNotification = new PlayingNotificationNougat();
        } else {
            playingNotification = new PlayingNotificationMarshmallow();
        }

        playingNotification.init(this);
    }

    public void updateNotification() {
        if (playingNotification != null && getCurrentSong() != null) {
            playingNotification.update();
        }
    }

    private void updateMediaSessionState() {
        mediaSession.setPlaybackState(
            new PlaybackStateCompat.Builder()
                .setActions(MEDIA_SESSION_ACTIONS)
                .setState(isPlaying() ? PlaybackStateCompat.STATE_PLAYING : PlaybackStateCompat.STATE_PAUSED, getSongProgressMillis(), 1)
                .build());
    }

    @SuppressLint("CheckResult")
    private void updateMediaSessionMetadata() {
        final Song song = getCurrentSong();

        if (song == null) {
            mediaSession.setMetadata(null);
            return;
        }

        final MediaMetadataCompat.Builder metaData = new MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, song.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM_ARTIST, song.artistName)
            .putString(MediaMetadataCompat.METADATA_KEY_ALBUM, song.albumName)
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, song.title)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, song.duration)
            .putLong(MediaMetadataCompat.METADATA_KEY_TRACK_NUMBER, getPosition() + 1)
            .putLong(MediaMetadataCompat.METADATA_KEY_YEAR, song.year)
            .putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            metaData.putLong(MediaMetadataCompat.METADATA_KEY_NUM_TRACKS, getPlayingQueue().size());
        }

        if (PreferenceUtil.getInstance(this).getShowAlbumCover()) {
            final Point screenSize = Util.getScreenSize(MusicService.this);
            final RequestBuilder<Bitmap> request = CustomGlideRequest.Builder
                .from(MusicService.this, song.primary, song.blurHash)
                .bitmap().build();

            if (PreferenceUtil.getInstance(this).getBlurAlbumCover()) {
                request.transform(new BlurTransformation.Builder(MusicService.this).build());
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    request.into(new CustomTarget<Bitmap>(screenSize.x, screenSize.y) {
                        @Override
                        public void onLoadFailed(Drawable drawable) {
                            super.onLoadFailed(drawable);
                            mediaSession.setMetadata(metaData.build());
                        }

                        @Override
                        public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                            Bitmap.Config config = resource.getConfig();
                            Bitmap copy = resource.copy(config, false);

                            metaData.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, copy);
                            mediaSession.setMetadata(metaData.build());
                        }

                        @Override
                        public void onLoadCleared(Drawable drawable) {
                            super.onLoadFailed(drawable);
                            mediaSession.setMetadata(metaData.build());
                        }
                    });
                }
            });
        } else {
            mediaSession.setMetadata(metaData.build());
        }
    }

    public void runOnUiThread(Runnable runnable) {
        uiThreadHandler.post(runnable);
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

    public int getNextPosition(boolean force) {
        int position = getPosition() + 1;
        switch (getRepeatMode()) {
            case REPEAT_MODE_ALL:
                if (isLastTrack()) {
                    position = 0;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (isLastTrack()) {
                        position = 0;
                    }
                } else {
                    position -= 1;
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (isLastTrack()) {
                    position -= 1;
                }
                break;
        }

        return position;
    }

    private boolean isLastTrack() {
        return getPosition() == getPlayingQueue().size() - 1;
    }

    public List<Song> getPlayingQueue() {
        return playingQueue;
    }

    public int getRepeatMode() {
        return repeatMode;
    }

    public void setRepeatMode(final int repeatMode) {
        switch (repeatMode) {
            case REPEAT_MODE_NONE:
            case REPEAT_MODE_ALL:
            case REPEAT_MODE_THIS:
                this.repeatMode = repeatMode;
                PreferenceUtil.getInstance(this).setRepeat(repeatMode);
                prepareNext();
                notifyChange(REPEAT_MODE_CHANGED);
                break;
        }
    }

    public void openQueue(@Nullable final List<Song> playingQueue, final int startPosition, final boolean startPlaying) {
        if (playingQueue != null && !playingQueue.isEmpty() && startPosition >= 0 && startPosition < playingQueue.size()) {
            // it is important to copy the playing queue here first as we might add or remove songs later
            originalPlayingQueue = new ArrayList<>(playingQueue);
            this.playingQueue = new ArrayList<>(originalPlayingQueue);

            int position = startPosition;
            if (shuffleMode == SHUFFLE_MODE_SHUFFLE) {
                ShuffleHelper.makeShuffleList(this.playingQueue, startPosition);
                position = 0;
            }

            if (startPlaying) {
                playSongAt(position);
            } else {
                setPosition(position);
            }

            notifyChange(QUEUE_CHANGED);
        }
    }

    public void addSong(int position, Song song) {
        playingQueue.add(position, song);
        originalPlayingQueue.add(position, song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSong(Song song) {
        playingQueue.add(song);
        originalPlayingQueue.add(song);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongs(int position, List<Song> songs) {
        playingQueue.addAll(position, songs);
        originalPlayingQueue.addAll(position, songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void addSongs(List<Song> songs) {
        playingQueue.addAll(songs);
        originalPlayingQueue.addAll(songs);
        notifyChange(QUEUE_CHANGED);
    }

    public void removeSong(int position) {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            playingQueue.remove(position);
            originalPlayingQueue.remove(position);
        } else {
            originalPlayingQueue.remove(playingQueue.remove(position));
        }

        reposition(position);
        notifyChange(QUEUE_CHANGED);
    }

    private void reposition(int deletedPosition) {
        int currentPosition = getPosition();
        if (deletedPosition < currentPosition) {
            position = currentPosition - 1;
        } else if (deletedPosition == currentPosition) {
            if (playingQueue.size() > deletedPosition) {
                setPosition(position);
            } else {
                setPosition(position - 1);
            }
        }
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

        notifyChange(QUEUE_CHANGED);
    }

    public void clearQueue() {
        playingQueue.clear();
        originalPlayingQueue.clear();

        setPosition(-1);
        notifyChange(QUEUE_CHANGED);
    }

    public void playSongAt(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(PLAY_SONG);
        playerHandler.obtainMessage(PLAY_SONG, position, 0).sendToTarget();
    }

    public void setPosition(final int position) {
        // handle this on the handlers thread to avoid blocking the ui thread
        playerHandler.removeMessages(SET_POSITION);
        playerHandler.obtainMessage(SET_POSITION, position, 0).sendToTarget();
    }

    private void playSongAtImpl(int position) {
        openTrackAndPrepareNextAt(position);
    }

    public void pause() {
        if (playback.isPlaying()) {
            playback.pause();
            notifyChange(STATE_CHANGED);
        }
    }

    public void play() {
        synchronized (this) {
            if (!playback.isPlaying()) {
                if (!playback.isReady()) {
                    playSongAt(getPosition());
                } else {
                    playback.start();
                    if (notHandledMetaChangedForCurrentTrack) {
                        handleChangeInternal(META_CHANGED);
                        notHandledMetaChangedForCurrentTrack = false;
                    }

                    notifyChange(STATE_CHANGED);
                }
            }
        }
    }

    public void playPreviousSong(boolean force) {
        playSongAt(getPreviousPosition(force));
    }

    public void back(boolean force) {
        if (getSongProgressMillis() > 5000) {
            seek(0);
        } else {
            playPreviousSong(force);
        }
    }

    public int getPreviousPosition(boolean force) {
        int newPosition = getPosition() - 1;
        switch (repeatMode) {
            case REPEAT_MODE_ALL:
                if (newPosition < 0) {
                    newPosition = getPlayingQueue().size() - 1;
                }
                break;
            case REPEAT_MODE_THIS:
                if (force) {
                    if (newPosition < 0) {
                        newPosition = getPlayingQueue().size() - 1;
                    }
                } else {
                    newPosition = getPosition();
                }
                break;
            default:
            case REPEAT_MODE_NONE:
                if (newPosition < 0) {
                    newPosition = 0;
                }
                break;
        }

        return newPosition;
    }

    public int getSongProgressMillis() {
        return playback.getProgress();
    }

    public int getSongDurationMillis() {
        return playback.getDuration();
    }

    public long getQueueDurationMillis(int position) {
        long duration = 0;
        for (int i = position + 1; i < playingQueue.size(); i++) {
            duration += playingQueue.get(i).duration;
        }

        return duration;
    }

    public int seek(int millis) {
        synchronized (this) {
            playback.setProgress(millis);
            throttledSeekHandler.notifySeek();
            return millis;
        }
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

    public void toggleShuffle() {
        if (getShuffleMode() == SHUFFLE_MODE_NONE) {
            setShuffleMode(SHUFFLE_MODE_SHUFFLE);
        } else {
            setShuffleMode(SHUFFLE_MODE_NONE);
        }
    }

    public int getShuffleMode() {
        return shuffleMode;
    }

    public void setShuffleMode(final int shuffleMode) {
        PreferenceUtil.getInstance(this).setShuffle(shuffleMode);

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

        notifyChange(SHUFFLE_MODE_CHANGED);
        notifyChange(QUEUE_CHANGED);
    }

    private void notifyChange(@NonNull final String what) {
        handleChangeInternal(what);
        sendChangeInternal(what);
    }

    private void sendChangeInternal(final String what) {
        sendBroadcast(new Intent(what));

        appWidgetAlbum.notifyChange(this, what, null);
        appWidgetClassic.notifyChange(this, what, null);
        appWidgetCard.notifyChange(this, what, null);
    }

    private void handleChangeInternal(@NonNull final String what) {
        switch (what) {
            case STATE_CHANGED:
                if (!isPlaying()) {
                    PreferenceUtil.getInstance(this).setProgress(getSongProgressMillis());
                }

                updateNotification();
                updateMediaSessionState();
                break;
            case META_CHANGED:
                updateNotification();
                updateMediaSessionMetadata();
                updateMediaSessionState();
                PreferenceUtil.getInstance(this).setPosition(getPosition());
                PreferenceUtil.getInstance(this).setProgress(getSongProgressMillis());
                break;
            case QUEUE_CHANGED:
                // because playing queue size might have changed
                updateMediaSessionMetadata();
                saveState();
                if (playingQueue.size() > 0) {
                    prepareNext();
                } else {
                    playback.pause();
                    playingNotification.stop();
                }
                break;
        }
    }

    public MediaSessionCompat getMediaSession() {
        return mediaSession;
    }

    public void releaseWakeLock() {
        if (wakeLock.isHeld()) {
            wakeLock.release();
        }
    }

    public void acquireWakeLock(long milli) {
        wakeLock.acquire(milli);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        switch (key) {
            case PreferenceUtil.SHOW_ALBUM_COVER:
            case PreferenceUtil.BLUR_ALBUM_COVER:
                updateMediaSessionMetadata();
                break;
            case PreferenceUtil.COLORED_NOTIFICATION:
                updateNotification();
                break;
            case PreferenceUtil.CLASSIC_NOTIFICATION:
                initNotification();
                updateNotification();
                break;
        }
    }

    @Override
    public void onStateChanged(int state) {
        notifyChange(STATE_CHANGED);
    }

    @Override
    public void onReadyChanged(boolean ready, int reason) {
        notifyChange(STATE_CHANGED);

        if (ready) {
            progressHandler.sendEmptyMessage(TRACK_STARTED);
            prepareNext();
        } else if (reason == PLAY_WHEN_READY_CHANGE_REASON_END_OF_MEDIA_ITEM) {
            progressHandler.sendEmptyMessage(TRACK_ENDED);
        }
    }

    @Override
    public void onTrackChanged(int reason) {
        acquireWakeLock(30000);

        if (reason == MEDIA_ITEM_TRANSITION_REASON_AUTO) {
            playerHandler.sendEmptyMessage(TRACK_CHANGED);
            progressHandler.sendEmptyMessage(TRACK_CHANGED);
        } else if (reason == MEDIA_ITEM_TRANSITION_REASON_PLAYLIST_CHANGED) {
            progressHandler.sendEmptyMessage(TRACK_CHANGED);
            prepareNext();
        }
    }

    private static final class PlaybackHandler extends Handler {
        private final WeakReference<MusicService> mService;

        public PlaybackHandler(final MusicService service, @NonNull final Looper looper) {
            super(looper);
            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull final Message msg) {
            final MusicService service = mService.get();
            if (service == null) {
                return;
            }

            switch (msg.what) {
                case TRACK_CHANGED:
                    if (service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.pause();
                        service.seek(0);
                        service.notifyChange(STATE_CHANGED);
                    } else {
                        service.position = service.nextPosition;
                        service.prepareNextImpl();
                        service.notifyChange(META_CHANGED);
                        service.notifyChange(QUEUE_CHANGED);
                    }
                    break;

                case TRACK_ENDED:
                    // if there is a timer finished, don't continue
                    if (service.pendingQuit || service.getRepeatMode() == REPEAT_MODE_NONE && service.isLastTrack()) {
                        service.notifyChange(STATE_CHANGED);
                        service.seek(0);
                        if (service.pendingQuit) {
                            service.pendingQuit = false;
                            service.quit();
                            break;
                        }
                    } else {
                        service.playNextSong(false);
                    }

                    sendEmptyMessage(RELEASE_WAKELOCK);
                    break;

                case RELEASE_WAKELOCK:
                    service.releaseWakeLock();
                    break;

                case PLAY_SONG:
                    service.playSongAtImpl(msg.arg1);
                    service.notifyChange(STATE_CHANGED);
                    break;

                case SET_POSITION:
                    service.openTrackAndPrepareNextAt(msg.arg1);
                    service.notifyChange(STATE_CHANGED);
                    break;

                case PREPARE_NEXT:
                    service.prepareNextImpl();
                    break;
            }
        }
    }

    public class MusicBinder extends Binder {
        @NonNull
        public MusicService getService() {
            return MusicService.this;
        }
    }

    private class ThrottledSeekHandler implements Runnable {
        // milliseconds to throttle before calling run to aggregate events
        private static final long THROTTLE = 500;
        private final Handler mHandler;

        public ThrottledSeekHandler(Handler handler) {
            mHandler = handler;
        }

        public void notifySeek() {
            mHandler.removeCallbacks(this);
            mHandler.postDelayed(this, THROTTLE);
        }

        @Override
        public void run() {
            notifyChange(STATE_CHANGED);
        }
    }

    private static final class ProgressHandler extends Handler {
        private final WeakReference<MusicService> mService;

        private ScheduledExecutorService executorService;
        private Future<?> task;

        public ProgressHandler(MusicService service, Looper looper) {
            super(looper);

            mService = new WeakReference<>(service);
        }

        @Override
        public void handleMessage(@NonNull final Message msg) {
            Song song = mService.get().getCurrentSong();
            if (song == null) return;

            switch (msg.what) {
                case TRACK_STARTED:
                    onStart();
                case TRACK_CHANGED:
                    onNext();
                    break;
                case TRACK_ENDED:
                    onStop();
            }
        }

        public void onStart() {
            if (executorService != null) executorService.shutdownNow();

            executorService = Executors.newScheduledThreadPool(1);
            task = executorService.scheduleAtFixedRate(this::onProgress, 10, 10, TimeUnit.SECONDS);
        }

        public void onNext() {
            PlaybackStartInfo startInfo = new PlaybackStartInfo();

            startInfo.setItemId(mService.get().getCurrentSong().id);
            startInfo.setVolumeLevel(mService.get().playback.getVolume());
            startInfo.setCanSeek(true);
            startInfo.setIsPaused(false);

            App.getApiClient().ensureWebSocket();
            App.getApiClient().ReportPlaybackStartAsync(startInfo, new EmptyResponse());
        }

        public void onProgress() {
            PlaybackProgressInfo progressInfo = new PlaybackProgressInfo();
            Song current = mService.get().getCurrentSong();
            String user = App.getApiClient().getCurrentUserId();
            Date time = new Date(System.currentTimeMillis());

            if (current == null) {
                return;
            }

            // TODO these cause a wrong thread error
            long progress = mService.get().getSongProgressMillis();
            double duration = mService.get().getSongDurationMillis();
            if (progress / duration > 0.9) {
                App.getApiClient().MarkPlayedAsync(current.id, user, time, new Response<>());
            }

            progressInfo.setItemId(current.id);
            progressInfo.setPositionTicks(progress * 10000);
            progressInfo.setVolumeLevel(mService.get().playback.getVolume());
            progressInfo.setIsPaused(!mService.get().playback.isPlaying());
            progressInfo.setCanSeek(true);

            App.getApiClient().ReportPlaybackProgressAsync(progressInfo, new EmptyResponse());
        }

        public void onStop() {
            PlaybackStopInfo info = new PlaybackStopInfo();
            long progress = mService.get().getSongProgressMillis();

            info.setItemId(mService.get().getCurrentSong().id);
            info.setPositionTicks(progress * 10000);

            if (task != null) task.cancel(true);
            if (executorService != null) executorService.shutdownNow();
        }
    }
}
