package com.dkanada.gramophone.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dkanada.gramophone.model.Song;

import java.util.List;

@Dao
public interface SongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertSongs(List<Song> songs);

    @Query("DELETE FROM songs")
    void deleteSongs();

    @Query("SELECT * FROM songs WHERE id = :id")
    Song getSong(String id);
}
