package com.dkanada.gramophone.util;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.provider.Settings;

import androidx.core.app.ActivityOptionsCompat;
import androidx.core.util.Pair;

import com.dkanada.gramophone.activities.LoginActivity;
import com.dkanada.gramophone.activities.MainActivity;
import com.dkanada.gramophone.activities.SelectActivity;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.activities.details.AlbumDetailActivity;
import com.dkanada.gramophone.activities.details.ArtistDetailActivity;
import com.dkanada.gramophone.activities.details.GenreDetailActivity;
import com.dkanada.gramophone.activities.details.PlaylistDetailActivity;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.service.DownloadService;

import java.util.ArrayList;
import java.util.List;

public class NavigationUtil {
    public static void openUrl(Context context, String url) {
        final Intent intent = new Intent(Intent.ACTION_VIEW);

        intent.setData(Uri.parse(url));
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        context.startActivity(intent);
    }

    public static void openSettings(Context context) {
        Intent intent = new Intent();

        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        intent.setData(Uri.fromParts("package", context.getPackageName(), null));

        context.startActivity(intent);
    }

    // TODO investigate why the normal recreate method wont work
    // initial recreate is fine but subsequent runs will incorrectly apply the light theme on main
    public static void recreateMain(Activity activity) {
        NavigationUtil.startMain(activity);

        activity.overridePendingTransition(0, android.R.anim.fade_out);
        activity.finish();
    }

    public static void startLogin(Context context) {
        final Intent intent = new Intent(context, LoginActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startSelect(Context context) {
        final Intent intent = new Intent(context, SelectActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        context.startActivity(intent);
    }

    public static void startMain(Context context) {
        final Intent intent = new Intent(context, MainActivity.class);

        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void startArtist(Activity activity, Artist artist, Pair sharedElements) {
        final Intent intent = new Intent(activity, ArtistDetailActivity.class);

        intent.putExtra(ArtistDetailActivity.EXTRA_ARTIST, artist);
        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void startAlbum(Activity activity, Album album, Pair sharedElements) {
        final Intent intent = new Intent(activity, AlbumDetailActivity.class);

        intent.putExtra(AlbumDetailActivity.EXTRA_ALBUM, album);
        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void startGenre(Activity activity, Genre genre, Pair sharedElements) {
        final Intent intent = new Intent(activity, GenreDetailActivity.class);

        intent.putExtra(GenreDetailActivity.EXTRA_GENRE, genre);
        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void startPlaylist(Activity activity, Playlist playlist, Pair sharedElements) {
        final Intent intent = new Intent(activity, PlaylistDetailActivity.class);

        intent.putExtra(PlaylistDetailActivity.EXTRA_PLAYLIST, playlist);
        startActivitySharedElements(activity, intent, sharedElements);
    }

    public static void startActivitySharedElements(Activity activity, Intent intent, Pair sharedElements) {
        if (sharedElements != null) {
            activity.startActivity(intent, ActivityOptionsCompat.makeSceneTransitionAnimation(activity, sharedElements).toBundle());
        } else {
            activity.startActivity(intent);
        }
    }

    public static void startDownload(Activity activity, List<Song> songs) {
        Intent intent = new Intent(activity, DownloadService.class);

        intent.putExtra(DownloadService.EXTRA_SONGS, new ArrayList<>(songs));
        activity.startService(intent);
    }
}
