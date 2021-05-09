package com.dkanada.gramophone.database;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Transaction;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.model.Song;

import java.util.ArrayList;
import java.util.List;

@Dao
public abstract class QueueSongDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    public abstract void insertQueueSongs(List<QueueSong> queueSongs);

    @Query("DELETE FROM queueSongs")
    public abstract void deleteQueueSongs();

    @Query("SELECT * from queueSongs WHERE queue = :queue ORDER BY `index`")
    public abstract List<QueueSong> getQueueSongs(int queue);

    @Transaction
    public List<Song> getQueue(int queue) {
        List<QueueSong> queueSongs = getQueueSongs(queue);
        List<Song> songs = new ArrayList<>();

        for (QueueSong queueSong : queueSongs) {
            Song song = App.getDatabase().songDao().getSong(queueSong.songId);
            if (song != null) songs.add(song);
        }

        return songs;
    }

    @Transaction
    public void setQueue(List<Song> songs, int queue) {
        List<QueueSong> queueSongs = new ArrayList<>();
        for (int i = 0; i < songs.size(); i++) {
            queueSongs.add(new QueueSong(songs.get(i).id, i, queue));
        }

        insertQueueSongs(queueSongs);
    }
}
