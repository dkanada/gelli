package com.kabouzeid.gramophone.util;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Environment;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.widget.Toast;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.helper.M3UWriter;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static android.provider.MediaStore.Audio.Playlists.EXTERNAL_CONTENT_URI;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class PlaylistsUtil {
    public static boolean doesPlaylistExist(@NonNull final Context context, final String name) {
        return false;
    }

    public static int createPlaylist(@NonNull final Context context, @Nullable final String name) {
        return -1;
    }

    public static void deletePlaylists(@NonNull final Context context, @NonNull final List<Playlist> playlists) {
    }

    public static void addToPlaylist(@NonNull final Context context, final Song song, final int playlistId, final boolean showToastOnFinish) {
    }

    public static void addToPlaylist(@NonNull final Context context, @NonNull final List<Song> songs, final int playlistId, final boolean showToastOnFinish) {
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final Song song, int playlistId) {
    }

    public static void removeFromPlaylist(@NonNull final Context context, @NonNull final List<PlaylistSong> songs) {
    }

    public static boolean moveItem(@NonNull final Context context, int playlistId, int from, int to) {
        return true;
    }

    public static void renamePlaylist(@NonNull final Context context, final long id, final String newName) {
    }

    public static String getNameForPlaylist(@NonNull final Context context, final long id) {
        return "";
    }
}