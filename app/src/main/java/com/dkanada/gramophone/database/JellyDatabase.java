package com.dkanada.gramophone.database;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.model.User;

@androidx.room.Database(
        entities = {
                Cache.class,
                Song.class,
                QueueSong.class,
                User.class
        },
        version = 7,
        exportSchema = false
)
public abstract class JellyDatabase extends RoomDatabase {
    public abstract CacheDao cacheDao();
    public abstract SongDao songDao();
    public abstract QueueSongDao queueSongDao();
    public abstract UserDao userDao();

    public static final Migration Migration2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE servers (id TEXT NOT NULL PRIMARY KEY, name TEXT,"
                    + "url TEXT, user TEXT, token TEXT)");
        }
    };

    public static final Migration Migration3 = new Migration(2, 3) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE servers");

            database.execSQL("CREATE TABLE servers (id TEXT NOT NULL PRIMARY KEY,"
                    + "name TEXT, url TEXT)");
            database.execSQL("CREATE TABLE users (id TEXT NOT NULL PRIMARY KEY,"
                    + "serverId TEXT, name TEXT, token TEXT)");
        }
    };

    public static final Migration Migration4 = new Migration(3, 4) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE servers");
            database.execSQL("DROP TABLE users");

            database.execSQL("CREATE TABLE users (id TEXT NOT NULL PRIMARY KEY,"
                    + "name TEXT, server TEXT, token TEXT)");
        }
    };

    public static final Migration Migration5 = new Migration(4, 5) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("ALTER TABLE songs ADD COLUMN cache INTEGER NOT NULL DEFAULT 1");

            database.execSQL("CREATE TABLE cache (id TEXT NOT NULL PRIMARY KEY,"
                    + "cache INTEGER NOT NULL DEFAULT 1)");
        }
    };

    public static final Migration Migration6 = new Migration(5, 6) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DROP TABLE queueSongs");

            database.execSQL("CREATE TABLE queueSongs ('index' INTEGER NOT NULL,"
                + "queue INTEGER NOT NULL, songId TEXT,"
                + "PRIMARY KEY ('index', queue),"
                + "FOREIGN KEY (songId) REFERENCES songs(id) ON DELETE CASCADE)");
        }
    };

    public static final Migration Migration7 = new Migration(6, 7) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("DELETE FROM queueSongs");
            database.execSQL("DROP TABLE songs");

            database.execSQL("CREATE TABLE songs (id TEXT NOT NULL PRIMARY KEY,"
                    + "title TEXT,"
                    + "trackNumber INTEGER NOT NULL,"
                    + "discNumber INTEGER NOT NULL,"
                    + "year INTEGER NOT NULL,"
                    + "duration INTEGER NOT NULL,"
                    + "albumId TEXT,"
                    + "albumName TEXT,"
                    + "artistId TEXT,"
                    + "artistName TEXT,"
                    + "'primary' TEXT,"
                    + "blurHash TEXT,"
                    + "favorite INTEGER NOT NULL,"
                    + "path TEXT,"
                    + "size INTEGER NOT NULL,"
                    + "container TEXT,"
                    + "codec TEXT,"
                    + "supportsTranscoding INTEGER NOT NULL,"
                    + "sampleRate INTEGER NOT NULL,"
                    + "bitRate INTEGER NOT NULL,"
                    + "bitDepth INTEGER NOT NULL,"
                    + "channels INTEGER NOT NULL,"
                    + "cache INTEGER NOT NULL DEFAULT 1)");
        }
    };
}
