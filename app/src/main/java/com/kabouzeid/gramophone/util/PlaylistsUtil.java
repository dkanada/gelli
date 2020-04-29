package com.kabouzeid.gramophone.util;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.Song;

import java.util.List;

public class PlaylistsUtil {
    public static boolean doesPlaylistExist(@NonNull final Context context, final String name) {
        return false;
    }

    public static String createPlaylist(@NonNull final Context context, @Nullable final String name) {
        return "";
    }

    public static void deletePlaylist(@NonNull final Context context, @NonNull final List<Playlist> playlists) {
    }

    public static void addToPlaylist(@NonNull final Context context, final Song song, final String playlistId, final boolean showToastOnFinish) {
    }

    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<Song> songs, final String playlistId, final boolean showToastOnFinish) {
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, String playlistId) {
    }

    public static boolean moveItem(@NonNull final Context context, String playlistId, int from, int to) {
        return true;
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        return "";
    }
}