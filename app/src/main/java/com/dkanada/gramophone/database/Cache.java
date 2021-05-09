package com.dkanada.gramophone.database;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import com.dkanada.gramophone.model.Song;

import java.util.UUID;

@Entity(tableName = "cache")
public class Cache {
    @NonNull
    @PrimaryKey
    public String id;

    @ColumnInfo(defaultValue = "1")
    public Boolean cache;

    public Cache() {
        this.id = UUID.randomUUID().toString();
    }

    public Cache(Song song) {
        this.id = song.id;
        this.cache = true;
    }
}
