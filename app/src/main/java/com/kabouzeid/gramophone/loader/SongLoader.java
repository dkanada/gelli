package com.kabouzeid.gramophone.loader;

import android.content.Context;
import android.database.Cursor;
import android.provider.BaseColumns;
import android.provider.MediaStore;
import android.provider.MediaStore.Audio.AudioColumns;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.util.PreferenceUtil;

import java.util.ArrayList;
import java.util.List;

public class SongLoader {
    public static final String QUEUE_PRIMARY = "image";
    public static final String QUEUE_FAVORITE = "favorite";

    protected static final String BASE_SELECTION = AudioColumns.IS_MUSIC + "=1" + " AND " + AudioColumns.TITLE + " != ''";
    protected static final String[] BASE_PROJECTION = new String[]{
            BaseColumns._ID,
            AudioColumns.TITLE,
            AudioColumns.TRACK,
            AudioColumns.YEAR,
            AudioColumns.DURATION,
            AudioColumns.ALBUM_ID,
            AudioColumns.ALBUM,
            AudioColumns.ARTIST_ID,
            AudioColumns.ARTIST,
            QUEUE_PRIMARY,
            QUEUE_FAVORITE,
    };

    @NonNull
    public static List<Song> getAllSongs(@NonNull Context context) {
        Cursor cursor = makeSongCursor(context, null, null);
        return getSongs(cursor);
    }

    @NonNull
    public static List<Song> getSongs(@Nullable final Cursor cursor) {
        List<Song> songs = new ArrayList<>();
        if (cursor != null && cursor.moveToFirst()) {
            do {
                songs.add(getSongFromCursorImpl(cursor));
            } while (cursor.moveToNext());
        }

        if (cursor != null) cursor.close();
        return songs;
    }

    @NonNull
    public static Song getSong(@Nullable Cursor cursor) {
        Song song;
        if (cursor != null && cursor.moveToFirst()) {
            song = getSongFromCursorImpl(cursor);
        } else {
            song = Song.EMPTY_SONG;
        }

        if (cursor != null) {
            cursor.close();
        }

        return song;
    }

    @NonNull
    private static Song getSongFromCursorImpl(@NonNull Cursor cursor) {
        final String id = cursor.getString(0);
        final String title = cursor.getString(1);
        final int trackNumber = cursor.getInt(2);
        final int year = cursor.getInt(3);
        final long duration = cursor.getLong(4);

        final String albumId = cursor.getString(5);
        final String albumName = cursor.getString(6);

        final String artistId = cursor.getString(7);
        final String artistName = cursor.getString(8);

        final String primary = cursor.getString(9);
        final boolean favorite = Boolean.valueOf(cursor.getString(10));

        return new Song(id, title, trackNumber, year, duration, albumId, albumName, artistId, artistName, primary, favorite);
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable final String selection, final String[] selectionValues) {
        return makeSongCursor(context, selection, selectionValues, PreferenceUtil.getInstance(context).getSongSortOrder());
    }

    @Nullable
    public static Cursor makeSongCursor(@NonNull final Context context, @Nullable String selection, String[] selectionValues, final String sortOrder) {
        if (selection != null && !selection.trim().equals("")) {
            selection = BASE_SELECTION + " AND " + selection;
        } else {
            selection = BASE_SELECTION;
        }

        try {
            return context.getContentResolver().query(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                    BASE_PROJECTION, selection, selectionValues, sortOrder);
        } catch (SecurityException e) {
            return null;
        }
    }
}
