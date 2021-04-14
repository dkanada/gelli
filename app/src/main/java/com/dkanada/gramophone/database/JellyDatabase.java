package com.dkanada.gramophone.database;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.model.User;

@androidx.room.Database(
        entities = {
                Song.class,
                QueueSong.class,
                User.class
        },
        version = 4,
        exportSchema = false
)
public abstract class JellyDatabase extends RoomDatabase {
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
}
