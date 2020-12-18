package com.dkanada.gramophone.service;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.playback.Playback;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.File;
import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.Dispatcher;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class MultiPlayer implements Playback {
    public static final String TAG = MultiPlayer.class.getSimpleName();

    private final Context context;
    private final OkHttpClient httpClient;

    private SimpleExoPlayer exoPlayer;
    private ConcatenatingMediaSource mediaSource;

    private final SimpleCache simpleCache;
    private final DataSource.Factory dataSource;

    private PlaybackCallbacks callbacks;

    private final ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onIsLoadingChanged(boolean isLoading) {
            Log.i(TAG, String.format("onIsLoadingChanged: %b", isLoading));
        }

        @Override
        public void onPlayWhenReadyChanged(boolean playWhenReady, int reason) {
            Log.i(TAG, String.format("onPlayWhenReadyChanged: %b %d", playWhenReady, reason));
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            Log.i(TAG, String.format("onPlaybackStateChanged: %d", playbackState));
            if (callbacks != null && exoPlayer.isPlaying()) {
                callbacks.onTrackStarted();
            }
        }

        @Override
        public void onMediaItemTransition(MediaItem mediaItem, int reason) {
            Log.i(TAG, String.format("onMediaItemTransition: %s %d", mediaItem, reason));
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.i(TAG, String.format("onPositionDiscontinuity: %d", reason));
            int windowIndex = exoPlayer.getCurrentWindowIndex();

            if (windowIndex == 1) {
                mediaSource.removeMediaSource(0);
                if (exoPlayer.isPlaying()) {
                    // there are still songs left in the queue
                    callbacks.onTrackWentToNext();
                } else {
                    callbacks.onTrackEnded();
                }
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, String.format("onPlayerError: %s", error.getMessage()));
            Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
        }
    };

    public MultiPlayer(Context context) {
        this.context = context;

        Dispatcher dispatcher = new Dispatcher();
        dispatcher.setMaxRequests(1);

        httpClient = new OkHttpClient.Builder().dispatcher(dispatcher).build();

        exoPlayer = new SimpleExoPlayer.Builder(context).build();
        mediaSource = new ConcatenatingMediaSource();

        exoPlayer.addListener(eventListener);
        exoPlayer.prepare(mediaSource);
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);

        long cacheSize = PreferenceUtil.getInstance(context).getMediaCacheSize();
        LeastRecentlyUsedCacheEvictor recentlyUsedCache = new LeastRecentlyUsedCacheEvictor(cacheSize);
        ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(context);

        File cacheDirectory = new File(context.getCacheDir(), "exoplayer");
        simpleCache = new SimpleCache(cacheDirectory, recentlyUsedCache, databaseProvider);
        dataSource = buildDataSourceFactory();
    }

    @Override
    public void setDataSource(Song song) {
        mediaSource = new ConcatenatingMediaSource();

        exoPlayer.addListener(eventListener);
        exoPlayer.prepare(mediaSource);

        appendDataSource(MusicUtil.getSongFileUri(song));
    }

    @Override
    public void queueDataSource(Song song) {
        String path = MusicUtil.getSongFileUri(song);
        while (mediaSource.getSize() > 1) {
            mediaSource.removeMediaSource(1);
        }

        if (mediaSource.getSize() != 2) {
            appendDataSource(path);
        }
    }

    private void appendDataSource(String path) {
        Uri uri = Uri.parse(path);

        httpClient.newCall(new Request.Builder().url(path).head().build()).enqueue(new Callback() {
            @Override
            @EverythingIsNonNull
            public void onFailure(Call call, IOException e) {
                Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
                e.printStackTrace();
            }

            @Override
            @EverythingIsNonNull
            public void onResponse(Call call, Response response) {
                String type = response.header("Content-Type");
                if (type == null) return;

                MediaSource source;
                if (type.equals("application/x-mpegURL")) {
                    source = new HlsMediaSource.Factory(dataSource)
                            .setTag(path)
                            .setAllowChunklessPreparation(true)
                            .createMediaSource(uri);
                } else {
                    source = new ProgressiveMediaSource.Factory(dataSource)
                            .setTag(path)
                            .createMediaSource(uri);
                }

                mediaSource.addMediaSource(source);
            }
        });
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
        return true;
    }

    @Override
    public boolean isPlaying() {
        return exoPlayer.isPlaying() || exoPlayer.getPlayWhenReady();
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
