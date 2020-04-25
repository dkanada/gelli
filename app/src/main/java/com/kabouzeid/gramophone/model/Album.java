package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;
import androidx.annotation.NonNull;

import org.jellyfin.apiclient.model.dto.BaseItemDto;

import java.util.ArrayList;
import java.util.List;

public class Album implements Parcelable {
    public List<Song> songs;

    public String id;
    public String title;
    public String artistId;
    public String artistName;
    public int year;

    public Album(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.title = itemDto.getName();
        this.artistId = itemDto.getAlbumArtists().get(0).getId();
        this.artistName = itemDto.getAlbumArtists().get(0).getName();

        if (itemDto.getProductionYear() != null) {
            this.year = itemDto.getProductionYear();
        }

        this.songs = new ArrayList<>();
        songs.add(Song.EMPTY_SONG);
    }

    public Album() {
        this.songs = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtistId() {
        return artistId;
    }

    public String getArtistName() {
        return artistName;
    }

    public int getYear() {
        return year;
    }

    public int getSongCount() {
        return songs.size();
    }

    @NonNull
    public Song safeGetFirstSong() {
        return songs.isEmpty() ? Song.EMPTY_SONG : songs.get(0);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Album album = (Album) o;
        return id.equals(album.id);
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
        dest.writeTypedList(songs);
    }

    protected Album(Parcel in) {
        this.songs = in.createTypedArrayList(Song.CREATOR);
    }

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
}
