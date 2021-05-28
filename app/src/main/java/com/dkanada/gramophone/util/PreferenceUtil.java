package com.dkanada.gramophone.util;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.dkanada.gramophone.model.Theme;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.SortMethod;
import com.dkanada.gramophone.model.SortOrder;
import com.dkanada.gramophone.model.CategoryInfo;
import com.dkanada.gramophone.model.Codec;
import com.dkanada.gramophone.interfaces.base.PreferenceMigration;
import com.dkanada.gramophone.fragments.player.NowPlayingScreen;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@SuppressLint("ApplySharedPref")
public final class PreferenceUtil {
    public static final String VERSION = "version";

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

    private static final PreferenceMigration Migration1 = new PreferenceMigration(0, 1) {
        @Override
        public void migrate(SharedPreferences preferences) {
            String theme = preferences.getString(GENERAL_THEME, "DARK");
            String imageSize = preferences.getString(IMAGE_CACHE_SIZE, "400");
            String mediaSize = preferences.getString(MEDIA_CACHE_SIZE, "400");

            preferences.edit().putString(GENERAL_THEME, Theme.valueOf(theme.toUpperCase()).toString()).commit();
            preferences.edit().putString(IMAGE_CACHE_SIZE, imageSize.substring(0, imageSize.length() - 6)).commit();
            preferences.edit().putString(MEDIA_CACHE_SIZE, mediaSize.substring(0, imageSize.length() - 6)).commit();
        }
    };

    private static final List<PreferenceMigration> migrations = Collections.singletonList(Migration1);

    private static PreferenceUtil instance;

    private final SharedPreferences mPreferences;
    private final Context mContext;

    private PreferenceUtil(final Context context) {
        mPreferences = PreferenceManager.getDefaultSharedPreferences(context);
        mContext = context;

        for (PreferenceMigration migration : migrations) {
            if (mPreferences.getInt(VERSION, 0) == migration.startVersion) {
                migration.migrate(mPreferences);
                mPreferences.edit().putInt(VERSION, migration.endVersion).commit();
            }
        }
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

    public int getShuffle() {
        return mPreferences.getInt(SHUFFLE, 0);
    }

    public void setShuffle(int shuffle) {
        mPreferences.edit().putInt(SHUFFLE, shuffle).apply();
    }

    public int getRepeat() {
        return mPreferences.getInt(REPEAT, 0);
    }

    public void setRepeat(int repeat) {
        mPreferences.edit().putInt(REPEAT, repeat).apply();
    }

    public int getPosition() {
        return mPreferences.getInt(POSITION, -1);
    }

    public void setPosition(int position) {
        mPreferences.edit().putInt(POSITION, position).apply();
    }

    public int getProgress() {
        return mPreferences.getInt(PROGRESS, -1);
    }

    public void setProgress(int progress) {
        mPreferences.edit().putInt(PROGRESS, progress).apply();
    }

    public Theme getTheme() {
        return Theme.valueOf(mPreferences.getString(GENERAL_THEME, Theme.DARK.toString()));
    }

    public void setPrimaryColor(int color) {
        mPreferences.edit().putInt(PRIMARY_COLOR, color).apply();
    }

    public void setAccentColor(int color) {
        mPreferences.edit().putInt(ACCENT_COLOR, color).apply();
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
        mPreferences.edit().putInt(TAB, value).apply();
    }

    public final NowPlayingScreen getNowPlayingScreen() {
        int id = mPreferences.getInt(NOW_PLAYING_SCREEN, 0);
        for (NowPlayingScreen nowPlayingScreen : NowPlayingScreen.values()) {
            if (nowPlayingScreen.id == id) return nowPlayingScreen;
        }

        return NowPlayingScreen.CARD;
    }

    public void setNowPlayingScreen(NowPlayingScreen nowPlayingScreen) {
        mPreferences.edit().putInt(NOW_PLAYING_SCREEN, nowPlayingScreen.id).apply();
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

    public final SortOrder getAlbumSortOrder() {
        return SortOrder.valueOf(mPreferences.getString(ALBUM_SORT_ORDER, SortOrder.DESCENDING.toString()));
    }

    public void setAlbumSortOrder(SortOrder sortOrder) {
        mPreferences.edit().putString(ALBUM_SORT_ORDER, sortOrder.toString()).apply();
    }

    public final SortOrder getSongSortOrder() {
        return SortOrder.valueOf(mPreferences.getString(SONG_SORT_ORDER, SortOrder.DESCENDING.toString()));
    }

    public void setSongSortOrder(SortOrder sortOrder) {
        mPreferences.edit().putString(SONG_SORT_ORDER, sortOrder.toString()).apply();
    }

    public final SortMethod getAlbumSortMethod() {
        return SortMethod.valueOf(mPreferences.getString(ALBUM_SORT_METHOD, SortMethod.RANDOM.toString()));
    }

    public void setAlbumSortMethod(SortMethod sortMethod) {
        mPreferences.edit().putString(ALBUM_SORT_METHOD, sortMethod.toString()).apply();
    }

    public final SortMethod getSongSortMethod() {
        return SortMethod.valueOf(mPreferences.getString(SONG_SORT_METHOD, SortMethod.RANDOM.toString()));
    }

    public void setSongSortMethod(SortMethod sortMethod) {
        mPreferences.edit().putString(SONG_SORT_METHOD, sortMethod.toString()).apply();
    }

    public int getLastSleepTimerValue() {
        return mPreferences.getInt(SLEEP_TIMER_LAST_VALUE, 30);
    }

    public void setLastSleepTimerValue(final int value) {
        mPreferences.edit().putInt(SLEEP_TIMER_LAST_VALUE, value).apply();
    }

    public long getNextSleepTimerElapsedRealTime() {
        return mPreferences.getLong(SLEEP_TIMER_ELAPSED_REALTIME, -1);
    }

    public void setNextSleepTimerElapsedRealtime(final long value) {
        mPreferences.edit().putLong(SLEEP_TIMER_ELAPSED_REALTIME, value).apply();
    }

    public boolean getSleepTimerFinishMusic() {
        return mPreferences.getBoolean(SLEEP_TIMER_FINISH_SONG, false);
    }

    public void setSleepTimerFinishMusic(final boolean value) {
        mPreferences.edit().putBoolean(SLEEP_TIMER_FINISH_SONG, value).apply();
    }

    public final int getAlbumGridSize(Context context) {
        return mPreferences.getInt(ALBUM_GRID_SIZE, context.getResources().getInteger(R.integer.default_grid_columns));
    }

    public void setAlbumGridSize(final int gridSize) {
        mPreferences.edit().putInt(ALBUM_GRID_SIZE, gridSize).apply();
    }

    public final int getSongGridSize(Context context) {
        return mPreferences.getInt(SONG_GRID_SIZE, context.getResources().getInteger(R.integer.default_list_columns));
    }

    public void setSongGridSize(final int gridSize) {
        mPreferences.edit().putInt(SONG_GRID_SIZE, gridSize).apply();
    }

    public final int getArtistGridSize(Context context) {
        return mPreferences.getInt(ARTIST_GRID_SIZE, context.getResources().getInteger(R.integer.default_list_columns));
    }

    public void setArtistGridSize(final int gridSize) {
        mPreferences.edit().putInt(ARTIST_GRID_SIZE, gridSize).apply();
    }

    public final int getAlbumGridSizeLand(Context context) {
        return mPreferences.getInt(ALBUM_GRID_SIZE_LAND, context.getResources().getInteger(R.integer.default_grid_columns_land));
    }

    public void setAlbumGridSizeLand(final int gridSize) {
        mPreferences.edit().putInt(ALBUM_GRID_SIZE_LAND, gridSize).apply();
    }

    public final int getSongGridSizeLand(Context context) {
        return mPreferences.getInt(SONG_GRID_SIZE_LAND, context.getResources().getInteger(R.integer.default_list_columns_land));
    }

    public void setSongGridSizeLand(final int gridSize) {
        mPreferences.edit().putInt(SONG_GRID_SIZE_LAND, gridSize).apply();
    }

    public final int getArtistGridSizeLand(Context context) {
        return mPreferences.getInt(ARTIST_GRID_SIZE_LAND, context.getResources().getInteger(R.integer.default_list_columns_land));
    }

    public void setArtistGridSizeLand(final int gridSize) {
        mPreferences.edit().putInt(ARTIST_GRID_SIZE_LAND, gridSize).apply();
    }

    public final boolean getAlbumColoredFooters() {
        return mPreferences.getBoolean(ALBUM_COLORED_FOOTERS, true);
    }

    public void setAlbumColoredFooters(final boolean value) {
        mPreferences.edit().putBoolean(ALBUM_COLORED_FOOTERS, value).apply();
    }

    public final boolean getSongColoredFooters() {
        return mPreferences.getBoolean(SONG_COLORED_FOOTERS, true);
    }

    public void setSongColoredFooters(final boolean value) {
        mPreferences.edit().putBoolean(SONG_COLORED_FOOTERS, value).apply();
    }

    public final boolean getArtistColoredFooters() {
        return mPreferences.getBoolean(ARTIST_COLORED_FOOTERS, true);
    }

    public void setArtistColoredFooters(final boolean value) {
        mPreferences.edit().putBoolean(ARTIST_COLORED_FOOTERS, value).apply();
    }

    public final boolean getAlbumArtistColoredFooters() {
        return mPreferences.getBoolean(ALBUM_ARTIST_COLORED_FOOTERS, true);
    }

    public void setAlbumArtistColoredFooters(final boolean value) {
        mPreferences.edit().putBoolean(ALBUM_ARTIST_COLORED_FOOTERS, value).apply();
    }

    public final String getLocationDownload() {
        return mPreferences.getString(LOCATION_DOWNLOAD, mContext.getCacheDir().toString());
    }

    public final String getLocationCache() {
        return mPreferences.getString(LOCATION_CACHE, mContext.getCacheDir().toString());
    }

    public final long getImageCacheSize() {
        return Long.parseLong(mPreferences.getString(IMAGE_CACHE_SIZE, "400")) * 100000;
    }

    public final long getMediaCacheSize() {
        return Long.parseLong(mPreferences.getString(MEDIA_CACHE_SIZE, "400")) * 100000;
    }

    public List<CategoryInfo> getCategories() {
        String data = mPreferences.getString(CATEGORIES, null);
        if (data != null) {
            return new Gson().fromJson(data, new TypeToken<List<CategoryInfo>>(){}.getType());
        }

        return getDefaultCategories();
    }

    public void setCategories(List<CategoryInfo> categories) {
        Gson gson = new Gson();
        Type type = new TypeToken<List<CategoryInfo>>(){}.getType();

        mPreferences.edit().putString(CATEGORIES, gson.toJson(categories, type)).apply();
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

    public List<Codec> getDirectPlayCodecs() {
        Set<String> defaultValues = Arrays.stream(Codec.values()).map(Enum::toString).collect(Collectors.toSet());
        Set<String> values = mPreferences.getStringSet(DIRECT_PLAY_CODECS, defaultValues);

        return values.stream().map(Codec::valueOf).collect(Collectors.toList());
    }

    public void setDirectPlayCodecs(List<Codec> codecs) {
        Set<String> values = codecs.stream().map(Enum::toString).collect(Collectors.toSet());

        mPreferences.edit().putStringSet(DIRECT_PLAY_CODECS, values).apply();
    }

    public String getServer() {
        return mPreferences.getString(SERVER, "https://jellyfin.org");
    }

    public void setServer(String server) {
        mPreferences.edit().putString(SERVER, server).apply();
    }

    public String getUser() {
        return mPreferences.getString(USER, null);
    }

    public void setUser(String user) {
        mPreferences.edit().putString(USER, user).apply();
    }
}
