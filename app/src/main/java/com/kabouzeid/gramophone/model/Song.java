package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jellyfin.apiclient.model.dto.BaseItemDto;

public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(null, "", -1, -1, -1, null, "", null, "");

    public final String id;
    public final String title;
    public final int trackNumber;
    public final int year;
    public final long duration;

    public final String albumId;
    public final String albumName;

    public final String artistId;
    public final String artistName;

    public boolean favorite;

    public Song(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.title = itemDto.getName();
        this.trackNumber = itemDto.getIndexNumber() != null ? itemDto.getIndexNumber() : 0;
        this.year = itemDto.getProductionYear() != null ? itemDto.getProductionYear() : 0;
        this.duration = itemDto.getRunTimeTicks() / 10000;

        this.albumId = itemDto.getAlbumId();
        this.albumName = itemDto.getAlbum();

        this.artistId = itemDto.getAlbumArtists().get(0).getId();
        this.artistName = itemDto.getAlbumArtists().get(0).getName();

        this.favorite = itemDto.getUserData() != null && itemDto.getUserData().getIsFavorite();
    }

    public Song(String id, String title, int trackNumber, int year, long duration, String albumId, String albumName, String artistId, String artistName) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.year = year;
        this.duration = duration;

        this.albumId = albumId;
        this.albumName = albumName;

        this.artistId = artistId;
        this.artistName = artistName;

        this.favorite = false;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Song song = (Song) o;
        return id.equals(song.id);
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
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);

        dest.writeString(this.albumId);
        dest.writeString(this.albumName);

        dest.writeString(this.artistId);
        dest.writeString(this.artistName);
    }

    protected Song(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        this.year = in.readInt();
        this.duration = in.readLong();

        this.albumId = in.readString();
        this.albumName = in.readString();

        this.artistId = in.readString();
        this.artistName = in.readString();

        this.favorite = false;
    }

    public static final Creator<Song> CREATOR = new Creator<Song>() {
        public Song createFromParcel(Parcel source) {
            return new Song(source);
        }

        public Song[] newArray(int size) {
            return new Song[size];
        }
    };
}
