package com.dkanada.gramophone.util;

import android.content.Context;
import android.content.Intent;
import android.text.TextUtils;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.DirectPlayCodec;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.model.Song;

import org.jellyfin.apiclient.interaction.ApiClient;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.UserItemDataDto;

import java.util.List;
import java.util.Locale;

public class MusicUtil {
    public static String getSongFileUri(Song song) {
        PreferenceUtil preferenceUtil = PreferenceUtil.getInstance(App.getInstance());

        StringBuilder builder = new StringBuilder(256);
        ApiClient apiClient = App.getApiClient();

        builder.append(apiClient.getApiUrl());
        builder.append("/Audio/");
        builder.append(song.id);
        builder.append("/universal");
        builder.append("?UserId=").append(apiClient.getCurrentUserId());
        builder.append("&DeviceId=").append(apiClient.getDeviceId());

        // web client maximum is 12444445 and 320kbps is 320000
        builder.append("&MaxStreamingBitrate=").append(preferenceUtil.getMaximumBitrate());

        boolean containerAdded = false;
        for (DirectPlayCodec directPlayCodec : preferenceUtil.getDirectPlayCodecs()) {
            if (directPlayCodec.selected) {
                if (!containerAdded) {
                    builder.append("&Container=");
                    containerAdded = true;
                }

                builder.append(directPlayCodec.codec.value).append(',');
            }
        }

        if (containerAdded) {
            // remove last comma
            builder.deleteCharAt(builder.length() - 1);
        }

        builder.append("&TranscodingContainer=ts");
        builder.append("&TranscodingProtocol=hls");

        // preferred codec when transcoding
        builder.append("&AudioCodec=").append(preferenceUtil.getTranscodeCodec());
        builder.append("&api_key=").append(apiClient.getAccessToken());

        Log.i(MusicUtil.class.getName(), "playing audio: " + builder);
        return builder.toString();
    }

    @NonNull
    public static Intent createShareSongFileIntent(@NonNull final Song song, Context context) {
        try {
            return new Intent();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Toast.makeText(context, R.string.error_share_file, Toast.LENGTH_SHORT).show();
            return new Intent();
        }
    }

    @NonNull
    public static String getArtistInfoString(@NonNull final Context context, @NonNull final Artist artist) {
        return artist.genres.size() != 0 ? artist.genres.get(0).name : "";
    }

    @NonNull
    public static String getAlbumInfoString(@NonNull final Context context, @NonNull final Album album) {
        return album.artistName;
    }

    @NonNull
    public static String getSongInfoString(@NonNull final Song song) {
        return song.albumName;
    }

    @NonNull
    public static String getGenreInfoString(@NonNull final Context context, @NonNull final Genre genre) {
        int songCount = genre.songCount;
        return MusicUtil.getSongCountString(context, songCount);
    }

    @NonNull
    public static String getPlaylistInfoString(@NonNull final Context context, @NonNull List<Song> songs) {
        final long duration = getTotalDuration(context, songs);

        return MusicUtil.buildInfoString(
            MusicUtil.getSongCountString(context, songs.size()),
            MusicUtil.getReadableDurationString(duration)
        );
    }

    @NonNull
    public static String getSongCountString(@NonNull final Context context, int songCount) {
        final String songString = songCount == 1 ? context.getResources().getString(R.string.song) : context.getResources().getString(R.string.songs);
        return songCount + " " + songString;
    }

    @NonNull
    public static String getAlbumCountString(@NonNull final Context context, int albumCount) {
        final String albumString = albumCount == 1 ? context.getResources().getString(R.string.album) : context.getResources().getString(R.string.albums);
        return albumCount + " " + albumString;
    }

    @NonNull
    public static String getYearString(int year) {
        return year > 0 ? String.valueOf(year) : "-";
    }

    public static long getTotalDuration(@NonNull final Context context, @NonNull List<Song> songs) {
        long duration = 0;
        for (int i = 0; i < songs.size(); i++) {
            duration += songs.get(i).duration;
        }

        return duration;
    }

    public static String getReadableDurationString(long songDurationMillis) {
        long minutes = (songDurationMillis / 1000) / 60;
        long seconds = (songDurationMillis / 1000) % 60;

        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds);
        } else {
            long hours = minutes / 60;
            minutes = minutes % 60;
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds);
        }
    }

    @NonNull
    public static String buildInfoString(@Nullable final String one, @Nullable final String two) {
        // skip empty strings
        if (TextUtils.isEmpty(one)) {
            return TextUtils.isEmpty(two) ? "" : two;
        }
        if (TextUtils.isEmpty(two)) {
            return TextUtils.isEmpty(one) ? "" : one;
        }

        return one + "  •  " + two;
    }

    public static void toggleFavorite(@NonNull final Context context, @NonNull final Song song) {
        song.favorite = !song.favorite;

        String user = App.getApiClient().getCurrentUserId();
        App.getApiClient().UpdateFavoriteStatusAsync(song.id, user, song.favorite, new Response<UserItemDataDto>() {
                @Override
                public void onResponse(UserItemDataDto data) {
                    song.favorite = data.getIsFavorite();
                }

                @Override
                public void onError(Exception exception) {
                    exception.printStackTrace();
                }
            }
        );
    }

    @NonNull
    public static String getSectionName(@Nullable String musicMediaTitle) {
        if (TextUtils.isEmpty(musicMediaTitle)) return "";

        musicMediaTitle = musicMediaTitle.trim().toLowerCase();
        if (musicMediaTitle.startsWith("the ")) {
            musicMediaTitle = musicMediaTitle.substring(4);
        } else if (musicMediaTitle.startsWith("a ")) {
            musicMediaTitle = musicMediaTitle.substring(2);
        }

        if (musicMediaTitle.isEmpty()) return "";
        return String.valueOf(musicMediaTitle.charAt(0)).toUpperCase();
    }
}
