package com.dkanada.gramophone.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.dkanada.gramophone.activities.LoginActivity;
import com.dkanada.gramophone.activities.MainActivity;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.activities.details.AlbumDetailActivity;
import com.dkanada.gramophone.activities.details.ArtistDetailActivity;
import com.dkanada.gramophone.activities.details.GenreDetailActivity;
import com.dkanada.gramophone.activities.details.PlaylistDetailActivity;

public class NavigationUtil {
    public static void openUrl(@NonNull final Context context, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public static void openSettings(@NonNull final Context context) {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));

        context.startActivity(intent);
    }

    public static void goToLogin(@NonNull final Context context) {
        final Intent intent = new Intent(context, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void goToMain(@NonNull final Context context) {
        final Intent intent = new Intent(context, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void goToArtist(@NonNull final Activity activity, final Artist artist, @Nullable Pair... sharedElements) {
        final Intent intent = new Intent(activity, ArtistDetailActivity.class);
        intent.putExtra(ArtistDetailActivity.EXTRA_ARTIST, artist);

        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void goToAlbum(@NonNull final Activity activity, final Album album, @Nullable Pair... sharedElements) {
        final Intent intent = new Intent(activity, AlbumDetailActivity.class);
        intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM, album);

        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void goToGenre(@NonNull final Activity activity, final Genre genre, @Nullable Pair... sharedElements) {
        final Intent intent = new Intent(activity, GenreDetailActivity.class);
        intent.putExtra(GenreDetailActivity.EXTRA_GENRE, genre);

        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void goToPlaylist(@NonNull final Activity activity, final Playlist playlist, @Nullable Pair... sharedElements) {
        final Intent intent = new Intent(activity, PlaylistDetailActivity.class);
        intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST, playlist);

        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void startActivitySharedElements(@NonNull final Activity activity, Intent intent, @Nullable Pair... sharedElements) {
        if (sharedElements != null && sharedElements.length > 0) {
            // noinspection unchecked
            activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }
}
