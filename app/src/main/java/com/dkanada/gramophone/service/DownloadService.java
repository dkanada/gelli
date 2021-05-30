package com.dkanada.gramophone.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.BuildConfig;
import com.dkanada.gramophone.database.Cache;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.notifications.DownloadNotification;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class DownloadService extends Service {
    public static final String PACKAGE_NAME = BuildConfig.APPLICATION_ID;
    public static final String EXTRA_SONGS = PACKAGE_NAME + ".extra.songs";

    private Executor executor;
    private DownloadNotification notification;

    @Override
    public void onCreate() {
        super.onCreate();

        executor = Executors.newFixedThreadPool(4);
        notification = new DownloadNotification(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            return super.onStartCommand(null, flags, startId);
        }

        List<Song> songs = intent.getParcelableArrayListExtra(EXTRA_SONGS);
        for (Song song : songs) {
            download(song);
            notification.start(song);
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @SuppressWarnings("ConstantConditions")
    public void download(Song song) {
        executor.execute(() -> {
            try {
                URL url = new URL(MusicUtil.getDownloadUri(song));
                URLConnection connection = url.openConnection();

                String cache = PreferenceUtil.getInstance(App.getInstance()).getLocationCache();
                File download = new File(cache, "download/" + song.id);
                File audio = new File(MusicUtil.getFileUri(song));

                download.getParentFile().mkdirs();
                download.createNewFile();
                audio.getParentFile().mkdirs();
                audio.createNewFile();

                InputStream input = connection.getInputStream();
                OutputStream output = new FileOutputStream(download);

                connection.connect();

                byte[] data = new byte[1048576];
                int count;

                notification.update(0, connection.getContentLength());
                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                    notification.update(count, 0);
                }

                input.close();
                output.close();

                input = new FileInputStream(download);
                output = new FileOutputStream(audio);

                while ((count = input.read(data)) != -1) {
                    output.write(data, 0, count);
                }

                input.close();
                output.close();

                download.delete();
                App.getDatabase().cacheDao().insertCache(new Cache(song));
                notification.stop(song);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }
}
