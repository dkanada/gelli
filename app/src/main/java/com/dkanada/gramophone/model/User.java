package com.dkanada.gramophone.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jellyfin.apiclient.model.users.AuthenticationResult;

import java.util.UUID;

@Entity(tableName = "users")
public class User {
    @NonNull
    @PrimaryKey
    public String id;
    public String name;

    public String server;
    public String token;

    public User() {
        this.id = UUID.randomUUID().toString();
    }

    public User(AuthenticationResult result, String server) {
        this.id = result.getUser().getId();
        this.name = result.getUser().getName();

        this.server = server;
        this.token = result.getAccessToken();
    }
}
