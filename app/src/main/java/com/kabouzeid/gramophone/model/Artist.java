package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jellyfin.apiclient.model.dto.BaseItemDto;

import java.util.ArrayList;
import java.util.List;

public class Artist implements Parcelable {
    public static final String UNKNOWN_ARTIST_DISPLAY_NAME = "Unknown Artist";

    public List<Album> albums;
    public List<Song> songs;

    public String id;
    public String name;
    public long duration;

    public Artist(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.name = itemDto.getName();
        this.duration = itemDto.getRunTimeTicks() / 10000;

        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();
    }

    public Artist() {
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public int getSongCount() {
        return songs.size();
    }

    public int getAlbumCount() {
        return albums.size();
    }

    public List<Song> getSongs() {
        return songs;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;
        return id.equals(artist.getId());
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public String toString() {
        return id;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeTypedList(this.albums);
    }

    protected Artist(Parcel in) {
        this.albums = in.createTypedArrayList(Album.CREATOR);
    }

    public static final Parcelable.Creator<Artist> CREATOR = new Parcelable.Creator<Artist>() {
        @Override
        public Artist createFromParcel(Parcel source) {
            return new Artist(source);
        }

        @Override
        public Artist[] newArray(int size) {
            return new Artist[size];
        }
    };
}
