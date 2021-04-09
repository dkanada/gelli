package com.dkanada.gramophone.database;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.dkanada.gramophone.model.Server;

@Dao
public interface ServerDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertServer(Server server);

    @Delete
    void deleteServer(Server server);

    @Query("SELECT * FROM servers WHERE id = :id")
    Server getServer(String id);
}
