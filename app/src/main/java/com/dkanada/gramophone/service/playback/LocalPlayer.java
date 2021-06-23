package com.dkanada.gramophone.service.playback;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.audio.AudioAttributes;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.EventListener;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.DefaultMediaSourceFactory;
import com.google.android.exoplayer2.source.MediaSourceFactory;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;
import com.google.android.exoplayer2.util.EventLogger;

import java.io.File;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class LocalPlayer implements Playback {
    public static final String TAG = LocalPlayer.class.getSimpleName();

    private final Context context;
    private final SimpleExoPlayer exoPlayer;
    private final SimpleCache simpleCache;

    private PlaybackCallbacks callbacks;

    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    @SuppressWarnings("FieldCanBeLocal")
    private final EventListener eventListener = new EventListener() {
        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            Log.i(TAG, String.format("onPlayWhenReadyChanged: %b %d", playWhenReady, reason));
            if (callbacks != null) callbacks.onReadyChanged(playWhenReady, reason);
        }

        @Override
        public void onPlaybackStateChanged(int state) {
            Log.i(TAG, String.format("onPlaybackStateChanged: %d", state));
            if (callbacks != null) callbacks.onStateChanged(state);
        }

        @Override
        public void onPlaybackSuppressionReasonChanged(@Player.PlaybackSuppressionReason int playbackSuppressionReason) {
            Log.i(TAG, String.format("onPlaybackSuppressionReasonChanged: %d", playbackSuppressionReason));
            if (callbacks != null) callbacks.onStateChanged(Player.STATE_READY);
        }

        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason) {
            Log.i(TAG, String.format("onMediaItemTransition: %s %d", mediaItem, reason));
            if (callbacks != null) callbacks.onTrackChanged(reason);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.i(TAG, String.format("onPositionDiscontinuity: %d", reason));
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, String.format("onPlayerError: %s", error.getMessage()));

            exoPlayer.clearMediaItems();
            exoPlayer.prepare();

            Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        }
    };

    public LocalPlayer(Context context) {
        this.context = context;

        MediaSourceFactory mediaSourceFactory = new DefaultMediaSourceFactory(buildDataSourceFactory());
        AudioAttributes audioAttributes = new AudioAttributes.Builder()
            .setUsage(C.USAGE_MEDIA)
            .setContentType(C.CONTENT_TYPE_MUSIC)
            .build();

        exoPlayer = new SimpleExoPlayer.Builder(context)
            .setMediaSourceFactory(mediaSourceFactory)
            .setAudioAttributes(audioAttributes, true)
            .build();

        exoPlayer.addListener(eventListener);
        exoPlayer.prepare();

        long cacheSize = PreferenceUtil.getInstance(context).getMediaCacheSize();
        LeastRecentlyUsedCacheEvictor recentlyUsedCache = new LeastRecentlyUsedCacheEvictor(cacheSize);
        ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(context);

        File cacheDirectory = new File(PreferenceUtil.getInstance(context).getLocationCache(), "exoplayer");
        simpleCache = new SimpleCache(cacheDirectory, recentlyUsedCache, databaseProvider);
    }

    @Override
    public void setQueue(List<Song> queue, int position, int progress, boolean resetCurrentSong) {
        executorService.submit(() -> {
            List<MediaItem> mediaItems = createMediaItems(queue);

            // TODO: Call this on main thread
            if (resetCurrentSong) {
                exoPlayer.setMediaItems(mediaItems, position, progress);
                return;
            }

            int currentPosition = exoPlayer.getCurrentWindowIndex();
            exoPlayer.removeMediaItems(0, currentPosition);

            if (exoPlayer.getMediaItemCount() > 1) {
                exoPlayer.removeMediaItems(1, exoPlayer.getMediaItemCount());
            }

            if (position + 1 < mediaItems.size()) {
                exoPlayer.addMediaItems(1, mediaItems.subList(position + 1, mediaItems.size()));
            }

            exoPlayer.addMediaItems(0, mediaItems.subList(0, position));
        });
    }

    private List<MediaItem> createMediaItems(List<Song> queue) {
        return queue.stream().map(song -> {
            File audio = new File(MusicUtil.getFileUri(song));
            Uri uri = Uri.fromFile(audio);

            if (!audio.exists()) {
                uri = Uri.parse(MusicUtil.getTranscodeUri(song));
            }

            MediaItem mediaItem = MediaItem.fromUri(uri);
            return mediaItem.buildUpon().setMediaId(song.id).build();
        }).collect(Collectors.toList());
    }

    @Override
    public void playSongAt(int position) {
        if (exoPlayer.getMediaItemCount() > 0) {
            exoPlayer.seekTo(Math.max(0, position) % exoPlayer.getMediaItemCount(), 0);
        }
    }

    private DataSource.Factory buildDataSourceFactory() {
        return () -> new CacheDataSource(
                simpleCache,
                new DefaultDataSourceFactory(context, context.getPackageName(), null).createDataSource(),
                new FileDataSource(),
                new CacheDataSink(simpleCache, 10 * 1024 * 1024),
                CacheDataSource.FLAG_BLOCK_ON_CACHE,
                null
        );
    }

    @Override
    public void setCallbacks(Playback.PlaybackCallbacks callbacks) {
        this.callbacks = callbacks;
    }

    @Override
    public boolean isReady() {
        return exoPlayer.getPlayWhenReady();
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.getPlayWhenReady() && exoPlayer.getPlaybackSuppressionReason() == Player.PLAYBACK_SUPPRESSION_REASON_NONE;
    }

    @Override
    @SuppressWarnings("ConstantConditions")
    public boolean isLoading() {
        MediaItem current = exoPlayer.getCurrentMediaItem();
        if (current != null && current.playbackProperties.uri.toString().contains("file://")) {
            return false;
        }

        return exoPlayer.getPlaybackState() == Player.STATE_BUFFERING;
    }

    @Override
    public void start() {
        exoPlayer.setPlayWhenReady(true);
    }

    @Override
    public void pause() {
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        simpleCache.release();
        exoPlayer.release();
    }

    @Override
    public void previous() {
        exoPlayer.previous();
    }

    @Override
    public void next() {
        exoPlayer.next();
    }

    @Override
    public void setRepeatMode(@Player.RepeatMode int repeatMode) {
        exoPlayer.setRepeatMode(repeatMode);
    }

    @Override
    public int getProgress() {
        return (int) exoPlayer.getCurrentPosition();
    }

    @Override
    public int getDuration() {
        return (int) exoPlayer.getDuration();
    }

    @Override
    public void setProgress(int progress) {
        exoPlayer.seekTo(progress);
    }

    @Override
    public void setVolume(int volume) {
        exoPlayer.setVolume(volume / 100f);
    }

    @Override
    public int getVolume() {
        return (int) (exoPlayer.getVolume() * 100);
    }
}
