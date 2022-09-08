package com.dkanada.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.entities.ImageType;

import java.util.ArrayList;
import java.util.List;

public class Album implements Parcelable {
    public List<Song> songs;

    public String id;
    public String title;
    public int year;

    public String artistId;
    public String artistName;

    public String primary;
    public String blurHash;

    public Album(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.title = itemDto.getName();
        this.year = itemDto.getProductionYear() != null ? itemDto.getProductionYear() : 0;

        if (itemDto.getAlbumArtists().size() != 0) {
            this.artistId = itemDto.getAlbumArtists().get(0).getId();
            this.artistName = itemDto.getAlbumArtists().get(0).getName();
        } else if (itemDto.getArtistItems().size() != 0) {
            this.artistId = itemDto.getArtistItems().get(0).getId();
            this.artistName = itemDto.getArtistItems().get(0).getName();
        }

        this.primary = itemDto.getImageTags().containsKey(ImageType.Primary) ? id : null;
        if (itemDto.getImageBlurHashes() != null && itemDto.getImageBlurHashes().get(ImageType.Primary) != null) {
            this.blurHash = (String) itemDto.getImageBlurHashes().get(ImageType.Primary).values().toArray()[0];
        }

        this.songs = new ArrayList<>();
    }

    public Album(Song song) {
        this.id = song.albumId;
        this.title = song.albumName;
        this.year = song.year;

        this.artistId = song.albumArtistId;
        this.artistName = song.albumArtistName;

        this.primary = song.primary;
        this.blurHash = song.blurHash;
    }

    public Album() {
        this.songs = new ArrayList<>();
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

    @NonNull
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

        dest.writeString(primary);
        dest.writeString(blurHash);
    }

    protected Album(Parcel in) {
        this.songs = new ArrayList<>();

        this.id = in.readString();
        this.title = in.readString();
        this.year = in.readInt();

        this.artistId = in.readString();
        this.artistName = in.readString();

        this.primary = in.readString();
        this.blurHash = in.readString();
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
