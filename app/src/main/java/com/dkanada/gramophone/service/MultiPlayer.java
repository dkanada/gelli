package com.dkanada.gramophone.service;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.playback.Playback;
import com.dkanada.gramophone.util.MusicUtil;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.database.ExoDatabaseProvider;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ProgressiveMediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.FileDataSource;
import com.google.android.exoplayer2.upstream.cache.CacheDataSink;
import com.google.android.exoplayer2.upstream.cache.CacheDataSource;
import com.google.android.exoplayer2.upstream.cache.LeastRecentlyUsedCacheEvictor;
import com.google.android.exoplayer2.upstream.cache.SimpleCache;

import java.io.IOException;
import java.io.File;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.internal.annotations.EverythingIsNonNull;

public class MultiPlayer implements Playback {
    public static final String TAG = MultiPlayer.class.getSimpleName();

    private Context context;

    private OkHttpClient httpClient;
    private SimpleExoPlayer exoPlayer;
    private ConcatenatingMediaSource mediaSource;

    private SimpleCache simpleCache;
    private DataSource.Factory dataSource;

    private PlaybackCallbacks callbacks;

    private boolean isReady = false;
    private boolean isPlaying = false;
    private boolean isNew = false;

    private ExoPlayer.EventListener eventListener = new ExoPlayer.EventListener() {
        @Override
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.i(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.i(TAG, "onLoadingChanged: " + isLoading);
        }

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.i(TAG, "onPlayerStateChanged playWhenReady: " + playWhenReady);
            Log.i(TAG, "onPlayerStateChanged playbackState: " + playbackState);
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.i(TAG, "onPositionDiscontinuity: " + reason);
            int windowIndex = exoPlayer.getCurrentWindowIndex();

            if (windowIndex == 1) {
                mediaSource.removeMediaSource(0);
                if (mediaSource.getSize() != 0) {
                    // there are still songs left in the queue
                    callbacks.onTrackWentToNext();
                } else {
                    callbacks.onTrackEnded();
                }
            }
        }

        @Override
        public void onPlayerError(ExoPlaybackException error) {
            Log.i(TAG, "onPlayerError: " + error.getMessage());
            if (context != null) {
                Toast.makeText(context, context.getResources().getString(R.string.unplayable_file), Toast.LENGTH_SHORT).show();
            }

            stop();
        }
    };

    public MultiPlayer(final Context context) {
        this.context = context;

        httpClient = new OkHttpClient();
        exoPlayer = new SimpleExoPlayer.Builder(context).build();
        mediaSource = new ConcatenatingMediaSource();

        if (simpleCache != null) simpleCache.release();
        LeastRecentlyUsedCacheEvictor recentlyUsedCache = new LeastRecentlyUsedCacheEvictor(Long.MAX_VALUE);
        ExoDatabaseProvider databaseProvider = new ExoDatabaseProvider(context);

        simpleCache = new SimpleCache(new File(Environment.getExternalStorageDirectory() + "/Gelli/cache"), recentlyUsedCache, databaseProvider);
        dataSource = buildDataSourceFactory();
    }

    @Override
    public void setDataSource(Song song) {
        isReady = false;
        if (context == null) {
            return;
        }

        isNew = true;
        mediaSource = new ConcatenatingMediaSource();

        exoPlayer.addListener(eventListener);
        exoPlayer.prepare(mediaSource);

        // queue and other information is currently handled outside exoplayer
        exoPlayer.setRepeatMode(Player.REPEAT_MODE_OFF);

        appendDataSource(MusicUtil.getSongFileUri(song), 0);
        isReady = true;
    }

    @Override
    public void queueDataSource(Song song) {
        if (context == null) {
            return;
        }

        String path = MusicUtil.getSongFileUri(song);
        if (mediaSource.getSize() == 2 && mediaSource.getMediaSource(1).getTag() != path) {
            mediaSource.removeMediaSource(1);
        }

        if (mediaSource.getSize() != 2) {
            appendDataSource(path, 1);
        }
    }

    private void appendDataSource(String path, int position) {
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
                MediaSource source;
                if (response.header("Content-Type").equals("application/x-mpegURL")) {
                    source = new HlsMediaSource.Factory(dataSource)
                            .setTag(path)
                            .setAllowChunklessPreparation(true)
                            .createMediaSource(uri);
                } else {
                    source = new ProgressiveMediaSource.Factory(dataSource)
                            .setTag(path)
                            .createMediaSource(uri);
                }

                if (mediaSource.getSize() < position) {
                    mediaSource.addMediaSource(mediaSource.getSize(), source);
                } else {
                    mediaSource.addMediaSource(position, source);
                }

                if (position == 0) start();
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
    public boolean isInitialized() {
        return isReady;
    }

    @Override
    public void start() {
        isPlaying = true;
        exoPlayer.setPlayWhenReady(true);

        if (isNew) {
            callbacks.onTrackStarted();
            isNew = false;
        }
    }

    @Override
    public void pause() {
        isPlaying = false;
        exoPlayer.setPlayWhenReady(false);
    }

    @Override
    public void stop() {
        exoPlayer.release();
        isReady = false;
    }

    @Override
    public boolean isPlaying() {
        return isReady && isPlaying;
    }

    @Override
    public int position() {
        if (!isReady) return -1;
        return (int) exoPlayer.getCurrentPosition();
    }

    @Override
    public int duration() {
        if (!isReady) return -1;
        return (int) exoPlayer.getDuration();
    }

    @Override
    public void seek(int position) {
        exoPlayer.seekTo(position);
    }

    @Override
    public void volume(float volume) {
        exoPlayer.setVolume(volume);
    }
}
