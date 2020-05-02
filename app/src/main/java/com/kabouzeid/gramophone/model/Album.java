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
    public int year;

    public String artistId;
    public String artistName;

    public Album(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.title = itemDto.getName();
        this.year = itemDto.getProductionYear() != null ? itemDto.getProductionYear() : 0;

        this.artistId = itemDto.getAlbumArtists().get(0).getId();
        this.artistName = itemDto.getAlbumArtists().get(0).getName();

        this.songs = new ArrayList<>();
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
        dest.writeString(id);
        dest.writeString(title);
        dest.writeInt(year);

        dest.writeString(artistId);
        dest.writeString(artistName);
    }

    protected Album(Parcel in) {
        this.songs = new ArrayList<>();

        this.id = in.readString();
        this.title = in.readString();
        this.year = in.readInt();

        this.artistId = in.readString();
        this.artistName = in.readString();
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
