package com.dkanada.gramophone.model;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jellyfin.apiclient.model.system.SystemInfo;

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

    public Server(SystemInfo systemInfo) {
        this.id = systemInfo.getId();

        this.name = systemInfo.getServerName();
        this.url = systemInfo.getWanAddress();
    }
}
