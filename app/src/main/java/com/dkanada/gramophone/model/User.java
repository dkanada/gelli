package com.dkanada.gramophone.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.PrimaryKey;

import org.jellyfin.apiclient.model.users.AuthenticationResult;

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

    public User(AuthenticationResult result) {
        this.id = result.getUser().getId();

        this.serverId = result.getServerId();

        this.name = result.getUser().getName();
        this.token = result.getAccessToken();
    }
}
