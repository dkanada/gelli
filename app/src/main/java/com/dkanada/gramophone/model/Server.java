package com.dkanada.gramophone.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import java.util.UUID;

@Entity(tableName = "servers")
public class Server {
    @NonNull
    @PrimaryKey
    public String id;

    public String name;
    public String url;

    public Server() {
        this.id = UUID.randomUUID().toString();
    }

    public Server(String name, String url) {
        this.id = UUID.randomUUID().toString();

        this.name = name;
        this.url = url;
    }
}
