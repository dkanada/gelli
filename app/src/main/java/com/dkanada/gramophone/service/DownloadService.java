package com.dkanada.gramophone.service;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.BuildConfig;
import com.dkanada.gramophone.database.Cache;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class DownloadService extends Service {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String EXTRA_SONG = PACKAGE_NAME + ".extra.song";

    private Executor executor;
    private Handler handler;

    @Override
    public void onCreate() {
        super.onCreate();

        Looper looper = Looper.myLooper();
        if (looper == null) {
            looper = Looper.getMainLooper();
        }

        executor = Executors.newFixedThreadPool(4);
        handler = new Handler(looper);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return super.onStartCommand(null, flags, startId);
        Song song = intent.getParcelableExtra(EXTRA_SONG);

        executor.execute(() -> {
            try {
                URL url = new URL(MusicUtil.getDownloadUri(song));
                URLConnection connection = url.openConnection();
                File audio = new File(MusicUtil.getFileUri(song));

                audio.getParentFile().mkdirs();
                audio.createNewFile();

                InputStream input = connection.getInputStream();
                OutputStream output = new FileOutputStream(audio);

                connection.connect();

                byte[] data = new byte[262144];
                int count;

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                input.close();
                output.close();

                App.getDatabase().cacheDao().insertCache(new Cache(song));
            } catch (Exception e) {
                e.printStackTrace();
            }
        });

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
