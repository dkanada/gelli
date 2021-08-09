package com.dkanada.gramophone.views.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.util.Log;
import android.widget.RemoteViews;

import androidx.core.content.res.ResourcesCompat;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.MainActivity;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.MusicService;
import com.dkanada.gramophone.util.MusicUtil;

import java.util.Arrays;

public abstract class BaseAppWidget extends AppWidgetProvider {
    public static final String NAME = "widget.base";

    public int imageSize = 0;
    public float cardRadius = 0f;

    @Override
    public void onUpdate(final Context context, final AppWidgetManager appWidgetManager, final int[] appWidgetIds) {
        Log.i(NAME, String.format("onUpdate: %s", Arrays.toString(appWidgetIds)));
        reset(context, appWidgetIds);

        final Intent updateIntent = new Intent(MusicService.INTENT_EXTRA_WIDGET_UPDATE);

        updateIntent.putExtra(MusicService.INTENT_EXTRA_WIDGET_NAME, NAME);
        updateIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, appWidgetIds);
        updateIntent.addFlags(Intent.FLAG_RECEIVER_REGISTERED_ONLY);

        context.sendBroadcast(updateIntent);
    }

    abstract protected void reset(final Context context, final int[] appWidgetIds);

    abstract protected void updateMeta(final MusicService service, final int[] appWidgetIds);

    public void notifyChange(final MusicService service, final String what, int[] appWidgetIds) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(service);
        final ComponentName componentName = new ComponentName(service, getClass());

        // will only find widgets for the current class to avoid updating other styles
        if (appWidgetIds == null) {
            appWidgetIds = appWidgetManager.getAppWidgetIds(componentName);
        }

        Song song = service.getCurrentSong();
        if (song != null && (what.equals(MusicService.STATE_CHANGED) || what.equals(MusicService.META_CHANGED))) {
            updateMeta(service, appWidgetIds);
        }
    }

    protected void pushUpdate(final Context context, final int[] appWidgetIds, final RemoteViews views) {
        final AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(context);

        if (appWidgetIds != null) {
            appWidgetManager.updateAppWidget(appWidgetIds, views);
        } else {
            appWidgetManager.updateAppWidget(new ComponentName(context, getClass()), views);
        }
    }

    protected void linkButtons(Context context, RemoteViews views, Integer... clickableViews) {
        ComponentName serviceName = new ComponentName(context, MusicService.class);

        Intent action = new Intent(context, MainActivity.class);
        PendingIntent open = PendingIntent.getActivity(context, 0, action, 0);

        for (int id : clickableViews) {
            views.setOnClickPendingIntent(id, open);
        }

        PendingIntent previous = buildPendingIntent(context, MusicService.ACTION_REWIND, serviceName);
        views.setOnClickPendingIntent(R.id.button_prev, previous);

        PendingIntent toggle = buildPendingIntent(context, MusicService.ACTION_TOGGLE, serviceName);
        views.setOnClickPendingIntent(R.id.button_toggle_play_pause, toggle);

        PendingIntent next = buildPendingIntent(context, MusicService.ACTION_SKIP, serviceName);
        views.setOnClickPendingIntent(R.id.button_next, next);
    }

    protected PendingIntent buildPendingIntent(Context context, final String action, final ComponentName serviceName) {
        Intent intent = new Intent(action);

        intent.setComponent(serviceName);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return PendingIntent.getForegroundService(context, 0, intent, 0);
        } else {
            return PendingIntent.getService(context, 0, intent, 0);
        }
    }

    protected Bitmap createRoundedBitmap(Bitmap bitmap, int width, int height, float tl, float tr, float bl, float br) {
        Bitmap rounded = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(rounded);
        Paint paint = new Paint();

        paint.setShader(new BitmapShader(bitmap, BitmapShader.TileMode.CLAMP, BitmapShader.TileMode.CLAMP));
        paint.setAntiAlias(true);

        canvas.drawPath(composeRoundedRectPath(new RectF(0, 0, width, height), tl, tr, bl, br), paint);

        return rounded;
    }

    protected Bitmap createRoundedBitmap(Drawable drawable, int width, int height, float tl, float tr, float bl, float br) {
        Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);

        drawable.setBounds(0, 0, width, height);
        drawable.draw(canvas);

        return createRoundedBitmap(bitmap, width, height, tl, tr, bl, br);
    }

    protected Path composeRoundedRectPath(RectF rect, float tl, float tr, float bl, float br) {
        Path path = new Path();
        tl = tl < 0 ? 0 : tl;
        tr = tr < 0 ? 0 : tr;
        bl = bl < 0 ? 0 : bl;
        br = br < 0 ? 0 : br;

        path.moveTo(rect.left + tl, rect.top);
        path.lineTo(rect.right - tr, rect.top);
        path.quadTo(rect.right, rect.top, rect.right, rect.top + tr);
        path.lineTo(rect.right, rect.bottom - br);
        path.quadTo(rect.right, rect.bottom, rect.right - br, rect.bottom);
        path.lineTo(rect.left + bl, rect.bottom);
        path.quadTo(rect.left, rect.bottom, rect.left, rect.bottom - bl);
        path.lineTo(rect.left, rect.top + tl);
        path.quadTo(rect.left, rect.top, rect.left + tl, rect.top);
        path.close();

        return path;
    }

    protected Drawable getAlbumArtDrawable(final Resources resources, final Bitmap bitmap) {
        if (bitmap == null) {
            return ResourcesCompat.getDrawable(resources, R.drawable.default_album_art, null);
        } else {
            return new BitmapDrawable(resources, bitmap);
        }
    }

    protected String getSongArtistAndAlbum(final Song song) {
        return MusicUtil.getSongInfoString(song);
    }
}
