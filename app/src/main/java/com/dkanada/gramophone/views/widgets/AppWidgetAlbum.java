package com.dkanada.gramophone.views.widgets;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.dkanada.gramophone.util.ThemeUtil;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.MusicService;
import com.dkanada.gramophone.util.ImageUtil;
import com.dkanada.gramophone.util.Util;

public class AppWidgetAlbum extends BaseAppWidget {
    public static final String NAME = "widget.album";

    private static AppWidgetAlbum mInstance;
    private Target<Bitmap> target;

    public static synchronized AppWidgetAlbum getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetAlbum();
        }

        return mInstance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        Point p = Util.getScreenSize(context);

        imageSize = Math.min(p.x, p.y);
        cardRadius = context.getResources().getDimension(R.dimen.app_widget_card_radius);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    protected void reset(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.app_widget_album);

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        appWidgetView.setImageViewBitmap(R.id.image, createRoundedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art), imageSize, imageSize, cardRadius, cardRadius, cardRadius, cardRadius));
        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_next_white_24dp, ThemeUtil.getPrimaryTextColor(context, false))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_previous_white_24dp, ThemeUtil.getPrimaryTextColor(context, false))));
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_play_arrow_white_24dp, ThemeUtil.getPrimaryTextColor(context, false))));

        linkButtons(context, appWidgetView, R.id.clickable_area);
        pushUpdate(context, appWidgetIds, appWidgetView);
    }

    @Override
    public void updateMeta(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_album);

        final boolean isPlaying = service.isPlaying();
        final Song song = service.queueManager.getCurrentSong();

        if (TextUtils.isEmpty(song.title) && song.getArtistNames().isEmpty()) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.title, song.title);
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song));
        }

        int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, playPauseRes, ThemeUtil.getPrimaryTextColor(service, false))));

        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp, ThemeUtil.getPrimaryTextColor(service, false))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp, ThemeUtil.getPrimaryTextColor(service, false))));

        linkButtons(service, appWidgetView, R.id.clickable_area);

        Point p = Util.getScreenSize(service);
        imageSize = Math.min(p.x, p.y);
        cardRadius = service.getResources().getDimension(R.dimen.app_widget_card_radius);

        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (target != null) {
                    Glide.with(service).clear(target);
                }

                target = CustomGlideRequest.Builder
                        .from(service, song.primary, song.blurHash)
                        .bitmap().build()
                        .into(new CustomTarget<Bitmap>(imageSize, imageSize) {
                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, Transition<? super Bitmap> glideAnimation) {
                                update(resource);
                            }

                            @Override
                            public void onLoadFailed(Drawable drawable) {
                                super.onLoadFailed(drawable);
                                update(null);
                            }

                            @Override
                            public void onLoadCleared(Drawable drawable) {
                                super.onLoadFailed(drawable);
                                update(null);
                            }

                            private void update(@Nullable Bitmap bitmap) {
                                final Drawable image = getAlbumArtDrawable(service.getResources(), bitmap);
                                final Bitmap roundedBitmap = createRoundedBitmap(image, imageSize, imageSize, cardRadius, cardRadius, cardRadius, cardRadius);

                                appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap);
                                pushUpdate(service, appWidgetIds, appWidgetView);
                            }
                        });
            }
        });
    }
}
