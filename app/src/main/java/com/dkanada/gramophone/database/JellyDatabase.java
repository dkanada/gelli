package com.dkanada.gramophone.database;

import androidx.room.RoomDatabase;

import com.dkanada.gramophone.model.Song;

@androidx.room.Database(
        entities = {
                Song.class,
                QueueSong.class
        },
        version = 1,
        exportSchema = false
)
public abstract class JellyDatabase extends RoomDatabase {
    public abstract QueueSongDao queueSongDao();
    public abstract SongDao songDao();
}
