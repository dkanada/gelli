package com.kabouzeid.gramophone.model;

import android.os.Parcel;

import org.jellyfin.apiclient.model.dto.BaseItemDto;

public class PlaylistSong extends Song {
    public final String playlistId;

    public PlaylistSong(BaseItemDto itemDto, String playlistId) {
        super(itemDto);
        this.playlistId = playlistId;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);

        dest.writeString(this.playlistId);
    }

    protected PlaylistSong(Parcel in) {
        super(in);

        this.playlistId = in.readString();
    }

    public static final Creator<PlaylistSong> CREATOR = new Creator<PlaylistSong>() {
        public PlaylistSong createFromParcel(Parcel source) {
            return new PlaylistSong(source);
        }

        public PlaylistSong[] newArray(int size) {
            return new PlaylistSong[size];
        }
    };
}
