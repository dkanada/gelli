package com.dkanada.gramophone.database;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.dkanada.gramophone.model.Song;

@Entity(
        tableName = "queueSongs",
        primaryKeys = {
                "index",
                "queue"
        },
        foreignKeys = {
            @ForeignKey(
                entity = Song.class,
                parentColumns = {"id"},
                childColumns = {"songId"},
                onDelete = ForeignKey.CASCADE
            )
        }
)
public class QueueSong {
    public int index;

    public int queue;

    public String songId;

    public QueueSong(String songId, int index, int queue) {
        this.songId = songId;

        this.index = index;
        this.queue = queue;
    }
}
