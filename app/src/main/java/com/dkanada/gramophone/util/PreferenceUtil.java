package com.dkanada.gramophone.util;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import androidx.annotation.StyleRes;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;
import com.google.gson.reflect.TypeToken;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.helper.sort.SortOrder;
import com.dkanada.gramophone.model.CategoryInfo;
import com.dkanada.gramophone.model.DirectPlayCodec;
import com.dkanada.gramophone.fragments.player.NowPlayingScreen;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class PreferenceUtil {
    public static final String SERVER = "server";
    public static final String USER = "user";

    public static final String SHUFFLE = "shuffle";
    public static final String REPEAT = "repeat";
    public static final String POSITION = "position";
    public static final String PROGRESS = "progress";
    public static final String TAB = "tab";

    public static final String SLEEP_TIMER_LAST_VALUE = "sleep_timer_last_value";
    public static final String SLEEP_TIMER_ELAPSED_REALTIME = "sleep_timer_elapsed_real_time";
    public static final String SLEEP_TIMER_FINISH_SONG = "sleep_timer_finish_music";

    public static final String ALBUM_SORT_METHOD = "album_sort_method";
    public static final String SONG_SORT_METHOD = "song_sort_method";

    public static final String ALBUM_SORT_ORDER = "album_sort_order";
    public static final String SONG_SORT_ORDER = "song_sort_order";

    public static final String ALBUM_GRID_SIZE = "album_grid_size";
    public static final String ALBUM_GRID_SIZE_LAND = "album_grid_size_land";

    public static final String SONG_GRID_SIZE = "song_grid_size";
    public static final String SONG_GRID_SIZE_LAND = "song_grid_size_land";

    public static final String ARTIST_GRID_SIZE = "artist_grid_size";
    public static final String ARTIST_GRID_SIZE_LAND = "artist_grid_size_land";

    public static final String ALBUM_COLORED_FOOTERS = "album_colored_footers";
    public static final String SONG_COLORED_FOOTERS = "song_colored_footers";
    public static final String ARTIST_COLORED_FOOTERS = "artist_colored_footers";
    public static final String ALBUM_ARTIST_COLORED_FOOTERS = "album_artist_colored_footers";

    public static final String CATEGORIES = "categories";
    public static final String PAGE_SIZE = "page_size";
    public static final String REMEMBER_LAST_TAB = "remember_last_tab";

    public static final String GENERAL_THEME = "general_theme";
    public static final String PRIMARY_COLOR = "primary_color";
    public static final String ACCENT_COLOR = "accent_color";
    public static final String COLORED_SHORTCUTS = "colored_shortcuts";

    public static final String CLASSIC_NOTIFICATION = "classic_notification";
    public static final String COLORED_NOTIFICATION = "colored_notification";

    public static final String NOW_PLAYING_SCREEN = "now_playing_screen";

    public static final String SHOW_ALBUM_COVER = "show_album_cover";
    public static final String BLUR_ALBUM_COVER = "blur_album_cover";

    public static final String TRANSCODE_CODEC = "transcode_codec";
    public static final String DIRECT_PLAY_CODECS = "direct_play_codecs";
    public static final String MAXIMUM_BITRATE = "maximum_bitrate";
    public static final String AUDIO_DUCKING = "audio_ducking";
    public static final String REMEMBER_SHUFFLE = "remember_shuffle";
    public static final String REMEMBER_QUEUE = "remember_queue";

    public static final String LOCATION_DOWNLOAD = "location_download";
    public static final String LOCATION_CACHE = "location_cache";
    public static final String IMAGE_CACHE_SIZE = "image_cache_size";
    public static final String MEDIA_CACHE_SIZE = "media_cache_size";

    private static PreferenceUtil instance;

    private final SharedPreferences mPreferences;
    private final Context mContext;

    private PreferenceUtil(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;
    }

    public static PreferenceUtil getInstance(final Context context) {
        if (instance == null) {
            instance = new PreferenceUtil(context.getApplicationContext());
        }

        return instance;
    }

    public void registerOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.registerOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    public void unregisterOnSharedPreferenceChangedListener(SharedPreferences.OnSharedPreferenceChangeListener sharedPreferenceChangeListener) {
        mPreferences.unregisterOnSharedPreferenceChangeListener(sharedPreferenceChangeListener);
    }

    @StyleRes
    public int getTheme() {
        return getThemeResource(mPreferences.getString(GENERAL_THEME, "dark"));
    }

    @StyleRes
    public static int getThemeResource(String themePrefValue) {
        switch (themePrefValue) {
            case "light":
                return R.style.Theme_Phonograph_Light;
            case "black":
                return R.style.Theme_Phonograph_Black;
            case "dark":
            default:
                return R.style.Theme_Phonograph;
        }
    }

    public final int getPageSize() {
        return Integer.parseInt(mPreferences.getString(PAGE_SIZE, "100"));
    }

    public final boolean getRememberLastTab() {
        return mPreferences.getBoolean(REMEMBER_LAST_TAB, true);
    }

    public final int getLastTab() {
        return mPreferences.getInt(TAB, 0);
    }

    public void setLastTab(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(TAB, value);
        editor.apply();
    }

    public final NowPlayingScreen getNowPlayingScreen() {
        int id = mPreferences.getInt(NOW_PLAYING_SCREEN, 0);
        for (NowPlayingScreen nowPlayingScreen : NowPlayingScreen.values()) {
            if (nowPlayingScreen.id == id) return nowPlayingScreen;
        }

        return NowPlayingScreen.CARD;
    }

    public void setNowPlayingScreen(NowPlayingScreen nowPlayingScreen) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(NOW_PLAYING_SCREEN, nowPlayingScreen.id);
        editor.apply();
    }

    public final boolean getColoredNotification() {
        return mPreferences.getBoolean(COLORED_NOTIFICATION, true);
    }

    public final boolean getClassicNotification() {
        return mPreferences.getBoolean(CLASSIC_NOTIFICATION, Build.VERSION.SDK_INT <= Build.VERSION_CODES.O);
    }

    public final boolean getColoredShortcuts() {
        return mPreferences.getBoolean(COLORED_SHORTCUTS, true);
    }

    public final String getTranscodeCodec() {
        return mPreferences.getString(TRANSCODE_CODEC, "aac");
    }

    public final String getMaximumBitrate() {
        return mPreferences.getString(MAXIMUM_BITRATE, "10000000");
    }

    public final boolean getAudioDucking() {
        return mPreferences.getBoolean(AUDIO_DUCKING, true);
    }

    public final boolean getRememberShuffle() {
        return mPreferences.getBoolean(REMEMBER_SHUFFLE, true);
    }

    public final boolean getRememberQueue() {
        return mPreferences.getBoolean(REMEMBER_QUEUE, true);
    }

    public final boolean getShowAlbumCover() {
        return mPreferences.getBoolean(SHOW_ALBUM_COVER, true);
    }

    public final boolean getBlurAlbumCover() {
        return mPreferences.getBoolean(BLUR_ALBUM_COVER, true);
    }

    public final String getAlbumSortOrder() {
        return mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.DESCENDING);
    }

    public void setAlbumSortOrder(final String sortOrder) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(ALBUM_SORT_ORDER, sortOrder);
        editor.apply();
    }

    public final String getSongSortOrder() {
        return mPreferences.getString(SONG_SORT_ORDER, SortOrder.DESCENDING);
    }

    public void setSongSortOrder(final String sortOrder) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SONG_SORT_ORDER, sortOrder);
        editor.apply();
    }

    public final String getAlbumSortMethod() {
        return mPreferences.getString(ALBUM_SORT_METHOD, SortMethod.RANDOM);
    }

    public void setAlbumSortMethod(final String sortMethod) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(ALBUM_SORT_METHOD, sortMethod);
        editor.apply();
    }

    public final String getSongSortMethod() {
        return mPreferences.getString(SONG_SORT_METHOD, SortMethod.RANDOM);
    }

    public void setSongSortMethod(final String sortMethod) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SONG_SORT_METHOD, sortMethod);
        editor.apply();
    }

    public int getLastSleepTimerValue() {
        return mPreferences.getInt(SLEEP_TIMER_LAST_VALUE, 30);
    }

    public void setLastSleepTimerValue(final int value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SLEEP_TIMER_LAST_VALUE, value);
        editor.apply();
    }

    public long getNextSleepTimerElapsedRealTime() {
        return mPreferences.getLong(SLEEP_TIMER_ELAPSED_REALTIME, -1);
    }

    public void setNextSleepTimerElapsedRealtime(final long value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putLong(SLEEP_TIMER_ELAPSED_REALTIME, value);
        editor.apply();
    }

    public boolean getSleepTimerFinishMusic() {
        return mPreferences.getBoolean(SLEEP_TIMER_FINISH_SONG, false);
    }

    public void setSleepTimerFinishMusic(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SLEEP_TIMER_FINISH_SONG, value);
        editor.apply();
    }

    public final int getAlbumGridSize(Context context) {
        return mPreferences.getInt(ALBUM_GRID_SIZE, context.getResources().getInteger(R.integer.default_grid_columns));
    }

    public void setAlbumGridSize(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUM_GRID_SIZE, gridSize);
        editor.apply();
    }

    public final int getSongGridSize(Context context) {
        return mPreferences.getInt(SONG_GRID_SIZE, context.getResources().getInteger(R.integer.default_list_columns));
    }

    public void setSongGridSize(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SONG_GRID_SIZE, gridSize);
        editor.apply();
    }

    public final int getArtistGridSize(Context context) {
        return mPreferences.getInt(ARTIST_GRID_SIZE, context.getResources().getInteger(R.integer.default_list_columns));
    }

    public void setArtistGridSize(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ARTIST_GRID_SIZE, gridSize);
        editor.apply();
    }

    public final int getAlbumGridSizeLand(Context context) {
        return mPreferences.getInt(ALBUM_GRID_SIZE_LAND, context.getResources().getInteger(R.integer.default_grid_columns_land));
    }

    public void setAlbumGridSizeLand(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ALBUM_GRID_SIZE_LAND, gridSize);
        editor.apply();
    }

    public final int getSongGridSizeLand(Context context) {
        return mPreferences.getInt(SONG_GRID_SIZE_LAND, context.getResources().getInteger(R.integer.default_list_columns_land));
    }

    public void setSongGridSizeLand(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(SONG_GRID_SIZE_LAND, gridSize);
        editor.apply();
    }

    public final int getArtistGridSizeLand(Context context) {
        return mPreferences.getInt(ARTIST_GRID_SIZE_LAND, context.getResources().getInteger(R.integer.default_list_columns_land));
    }

    public void setArtistGridSizeLand(final int gridSize) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putInt(ARTIST_GRID_SIZE_LAND, gridSize);
        editor.apply();
    }

    public final boolean getAlbumColoredFooters() {
        return mPreferences.getBoolean(ALBUM_COLORED_FOOTERS, true);
    }

    public void setAlbumColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ALBUM_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final boolean getSongColoredFooters() {
        return mPreferences.getBoolean(SONG_COLORED_FOOTERS, true);
    }

    public void setSongColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(SONG_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final boolean getArtistColoredFooters() {
        return mPreferences.getBoolean(ARTIST_COLORED_FOOTERS, true);
    }

    public void setArtistColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ARTIST_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final boolean getAlbumArtistColoredFooters() {
        return mPreferences.getBoolean(ALBUM_ARTIST_COLORED_FOOTERS, true);
    }

    public void setAlbumArtistColoredFooters(final boolean value) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putBoolean(ALBUM_ARTIST_COLORED_FOOTERS, value);
        editor.apply();
    }

    public final String getLocationDownload() {
        return mPreferences.getString(LOCATION_DOWNLOAD, mContext.getCacheDir().toString());
    }

    public final String getLocationCache() {
        return mPreferences.getString(LOCATION_CACHE, mContext.getCacheDir().toString());
    }

    public final long getImageCacheSize() {
        return Integer.parseInt(mPreferences.getString(IMAGE_CACHE_SIZE, "400000000"));
    }

    public final long getMediaCacheSize() {
        return Integer.parseInt(mPreferences.getString(MEDIA_CACHE_SIZE, "400000000"));
    }

    public List<CategoryInfo> getCategories() {
        String data = mPreferences.getString(CATEGORIES, null);
        if (data != null) {
            Gson gson = new Gson();
            Type collectionType = new TypeToken<List<CategoryInfo>>() {
            }.getType();

            try {
                return gson.fromJson(data, collectionType);
            } catch (JsonSyntaxException e) {
                e.printStackTrace();
            }
        }

        return getDefaultCategories();
    }

    public void setCategories(List<CategoryInfo> categories) {
        Gson gson = new Gson();
        Type collectionType = new TypeToken<List<CategoryInfo>>() {}.getType();

        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(CATEGORIES, gson.toJson(categories, collectionType));
        editor.apply();
    }

    public List<CategoryInfo> getDefaultCategories() {
        List<CategoryInfo> defaultCategories = new ArrayList<>(5);
        defaultCategories.add(new CategoryInfo(CategoryInfo.Category.SONGS, true));
        defaultCategories.add(new CategoryInfo(CategoryInfo.Category.ALBUMS, true));
        defaultCategories.add(new CategoryInfo(CategoryInfo.Category.ARTISTS, true));
        defaultCategories.add(new CategoryInfo(CategoryInfo.Category.GENRES, true));
        defaultCategories.add(new CategoryInfo(CategoryInfo.Category.PLAYLISTS, true));
        defaultCategories.add(new CategoryInfo(CategoryInfo.Category.FAVORITES, true));
        return defaultCategories;
    }

    public List<DirectPlayCodec> getDirectPlayCodecs() {
        DirectPlayCodec.Codec[] codecs = DirectPlayCodec.Codec.values();

        Set<String> selectedCodecNames = new HashSet<>();
        for (DirectPlayCodec.Codec codec : codecs) {
            // this will be the default value
            selectedCodecNames.add(codec.name());
        }

        selectedCodecNames = mPreferences.getStringSet(DIRECT_PLAY_CODECS, selectedCodecNames);

        ArrayList<DirectPlayCodec> directPlayCodecs = new ArrayList<>();
        for (DirectPlayCodec.Codec codec : codecs) {
            String name = codec.name();
            boolean selected = selectedCodecNames.contains(name);
            directPlayCodecs.add(new DirectPlayCodec(codec, selected));
        }

        return directPlayCodecs;
    }

    public void setDirectPlayCodecs(List<DirectPlayCodec> directPlayCodecs) {
        Set<String> codecNames = new HashSet<>();
        for (DirectPlayCodec directPlayCodec : directPlayCodecs) {
            if (directPlayCodec.selected) {
                codecNames.add(directPlayCodec.codec.toString());
            }
        }

        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putStringSet(DIRECT_PLAY_CODECS, codecNames);
        editor.apply();
    }

    public String getServer() {
        return mPreferences.getString(SERVER, "https://jellyfin.org");
    }

    public void setServer(String server) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(SERVER, server);
        editor.apply();
    }

    public String getUser() {
        return mPreferences.getString(USER, null);
    }

    public void setUser(String user) {
        final SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(USER, user);
        editor.apply();
    }
}
