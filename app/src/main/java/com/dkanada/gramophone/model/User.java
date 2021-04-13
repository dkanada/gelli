package com.dkanada.gramophone.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "users")
public class User {
    @NonNull
    @PrimaryKey
    public String id;

    @ForeignKey(
            entity = Server.class,
            parentColumns = {"id"},
            childColumns = {"serverId"},
            onDelete = ForeignKey.CASCADE
    )
    public String serverId;

    public String name;
    public String token;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(String serverId, String name, String token) {
        this.id = UUID.randomUUID().toString();

        this.serverId = serverId;

        this.name = name;
        this.token = token;
    }
}
