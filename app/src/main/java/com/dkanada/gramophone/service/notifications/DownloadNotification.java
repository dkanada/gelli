package com.dkanada.gramophone.service.notifications;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.MainActivity;
import com.dkanada.gramophone.model.Song;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static android.content.Context.NOTIFICATION_SERVICE;

public class DownloadNotification {
    private static final String CHANNEL_ID = DownloadNotification.class.getSimpleName();
    private static final int NOTIFICATION_ID = 2;

    private final Context context;
    private final NotificationManager notificationManager;

    private final List<Song> songs;

    private int current;
    private int maximum;

    public DownloadNotification(Context context) {
        this.notificationManager = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        this.context = context;

        this.songs = new ArrayList<>();
    }

    public synchronized void start(Song song, int maximum) {
        this.songs.add(song);

        this.maximum += maximum;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            createNotificationChannel();
        }
    }

    public synchronized void update(int current) {
        this.current += current;

        Intent action = new Intent(context, MainActivity.class).setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent clickIntent = PendingIntent.getActivity(context, 0, action, 0);

        NotificationCompat.InboxStyle style = new NotificationCompat.InboxStyle();
        for (Song item : songs.stream().limit(5).collect(Collectors.toList())) {
            style.addLine(item.title);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentIntent(clickIntent)
            .setContentTitle(String.format(context.getString(R.string.downloading_x_songs), songs.size()))
            .setProgress(maximum, this.current, false)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setStyle(style)
            .setShowWhen(false);

        notificationManager.notify(NOTIFICATION_ID, builder.build());
    }

    public synchronized void stop(Song song) {
        if (song != null) {
            songs.remove(song);
        }

        if (songs.size() != 0) {
            return;
        }

        current = 0;
        maximum = 0;

        notificationManager.cancel(NOTIFICATION_ID);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.deleteNotificationChannel(CHANNEL_ID);
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private void createNotificationChannel() {
        NotificationChannel notificationChannel = notificationManager.getNotificationChannel(CHANNEL_ID);

        if (notificationChannel == null) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, context.getString(R.string.action_download), NotificationManager.IMPORTANCE_LOW);

            notificationChannel.setDescription(context.getString(R.string.playing_notification_description));
            notificationChannel.enableLights(false);
            notificationChannel.enableVibration(false);

            notificationManager.createNotificationChannel(notificationChannel);
        }
    }
}
