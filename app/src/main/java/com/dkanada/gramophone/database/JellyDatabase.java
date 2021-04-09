package com.dkanada.gramophone.database;

import androidx.annotation.NonNull;
import androidx.room.RoomDatabase;
import androidx.room.migration.Migration;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.dkanada.gramophone.model.Server;
import com.dkanada.gramophone.model.Song;

@androidx.room.Database(
        entities = {
                Song.class,
                QueueSong.class,
                Server.class
        },
        version = 2,
        exportSchema = false
)
public abstract class JellyDatabase extends RoomDatabase {
    public abstract QueueSongDao queueSongDao();
    public abstract SongDao songDao();
    public abstract ServerDao serverDao();

    public static final Migration Migration2 = new Migration(1, 2) {
        @Override
        public void migrate(@NonNull SupportSQLiteDatabase database) {
            database.execSQL("CREATE TABLE servers (id TEXT NOT NULL PRIMARY KEY, name TEXT,"
                    + "url TEXT, user TEXT, token TEXT)");
        }
    };
}
