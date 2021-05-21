package com.dkanada.gramophone.views.widgets;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.palette.graphics.Palette;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.request.transition.Transition;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.MusicService;
import com.dkanada.gramophone.activities.MainActivity;
import com.dkanada.gramophone.util.ImageUtil;

public class AppWidgetClassic extends BaseAppWidget {
    public static final String NAME = "widget.classic";

    private static AppWidgetClassic mInstance;
    private Target<BitmapPaletteWrapper> target;

    public static synchronized AppWidgetClassic getInstance() {
        if (mInstance == null) {
            mInstance = new AppWidgetClassic();
        }

        return mInstance;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        imageSize = context.getResources().getDimensionPixelSize(R.dimen.app_widget_classic_image_size);
        cardRadius = context.getResources().getDimension(R.dimen.app_widget_card_radius);

        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @Override
    protected void reset(final Context context, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(context.getPackageName(), R.layout.app_widget_classic);

        appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        appWidgetView.setImageViewBitmap(R.id.image, createRoundedBitmap(BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art), imageSize, imageSize, cardRadius, 0, cardRadius, 0));
        appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_next_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true))));
        appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_skip_previous_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true))));
        appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(context, R.drawable.ic_play_arrow_white_24dp, MaterialValueHelper.getSecondaryTextColor(context, true))));

        linkButtons(context, appWidgetView, R.id.image, R.id.media_titles);
        pushUpdate(context, appWidgetIds, appWidgetView);
    }

    @Override
    public void updateMeta(final MusicService service, final int[] appWidgetIds) {
        final RemoteViews appWidgetView = new RemoteViews(service.getPackageName(), R.layout.app_widget_classic);

        final boolean isPlaying = service.isPlaying();
        final Song song = service.getCurrentSong();

        if (TextUtils.isEmpty(song.title) && TextUtils.isEmpty(song.artistName)) {
            appWidgetView.setViewVisibility(R.id.media_titles, View.INVISIBLE);
        } else {
            appWidgetView.setViewVisibility(R.id.media_titles, View.VISIBLE);
            appWidgetView.setTextViewText(R.id.title, song.title);
            appWidgetView.setTextViewText(R.id.text, getSongArtistAndAlbum(song));
        }

        linkButtons(service, appWidgetView, R.id.image, R.id.media_titles);

        imageSize = service.getResources().getDimensionPixelSize(R.dimen.app_widget_classic_image_size);
        cardRadius = service.getResources().getDimension(R.dimen.app_widget_card_radius);

        service.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (target != null) {
                    Glide.with(service).clear(target);
                }

                target = CustomGlideRequest.Builder
                        .from(service, song.primary, song.blurHash)
                        .palette().build()
                        .into(new CustomTarget<BitmapPaletteWrapper>(imageSize, imageSize) {
                            @Override
                            public void onResourceReady(@NonNull BitmapPaletteWrapper resource, Transition<? super BitmapPaletteWrapper> glideAnimation) {
                                Palette palette = resource.getPalette();
                                update(resource.getBitmap(), palette.getVibrantColor(palette.getMutedColor(MaterialValueHelper.getSecondaryTextColor(service, true))));
                            }

                            @Override
                            public void onLoadFailed(Drawable drawable) {
                                super.onLoadFailed(drawable);
                                update(null, MaterialValueHelper.getSecondaryTextColor(service, true));
                            }

                            @Override
                            public void onLoadCleared(Drawable drawable) {
                                super.onLoadFailed(drawable);
                                update(null, MaterialValueHelper.getSecondaryTextColor(service, true));
                            }

                            private void update(@Nullable Bitmap bitmap, int color) {
                                int playPauseRes = isPlaying ? R.drawable.ic_pause_white_24dp : R.drawable.ic_play_arrow_white_24dp;
                                appWidgetView.setImageViewBitmap(R.id.button_toggle_play_pause, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, playPauseRes, color)));

                                appWidgetView.setImageViewBitmap(R.id.button_next, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_next_white_24dp, color)));
                                appWidgetView.setImageViewBitmap(R.id.button_prev, ImageUtil.createBitmap(ImageUtil.getTintedVectorDrawable(service, R.drawable.ic_skip_previous_white_24dp, color)));

                                final Drawable image = getAlbumArtDrawable(service.getResources(), bitmap);
                                final Bitmap roundedBitmap = createRoundedBitmap(image, imageSize, imageSize, cardRadius, 0, cardRadius, 0);
                                appWidgetView.setImageViewBitmap(R.id.image, roundedBitmap);

                                pushUpdate(service, appWidgetIds, appWidgetView);
                            }
                        });
            }
        });
    }
}
