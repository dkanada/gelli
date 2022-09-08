package com.dkanada.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;

import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.dto.GenreDto;
import org.jellyfin.apiclient.model.entities.ImageType;

import java.util.ArrayList;
import java.util.List;

public class Artist implements Parcelable {
    public List<Genre> genres;
    public List<Album> albums;
    public List<Song> songs;

    public String id;
    public String name;

    public String primary;
    public String blurHash;

    public Artist(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.name = itemDto.getName();

        this.primary = itemDto.getImageTags().containsKey(ImageType.Primary) ? id : null;
        if (itemDto.getImageBlurHashes() != null && itemDto.getImageBlurHashes().get(ImageType.Primary) != null) {
            this.blurHash = (String) itemDto.getImageBlurHashes().get(ImageType.Primary).values().toArray()[0];
        }

        this.genres = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();

        if (itemDto.getGenreItems() != null) {
            for (GenreDto genre : itemDto.getGenreItems()) {
                genres.add(new Genre(genre));
            }
        }
    }

    public Artist(Album album) {
        this.id = album.artistId;
        this.name = album.artistName;
        this.primary = this.id;
    }

    public Artist(Song song) {
        this.id = song.albumArtistId != null ? song.albumArtistId : song.artistId.size() != 0 ? song.artistId.get(0) : null ;
        this.name = song.albumArtistName != null ? song.albumArtistName : song.artistName.size() != 0 ? song.artistName.get(0) : null ;
        this.primary = this.id;
    }

    public Artist() {
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Artist artist = (Artist) o;
        return id.equals(artist.id);
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
        dest.writeString(name);

        dest.writeString(primary);
        dest.writeString(blurHash);
    }

    protected Artist(Parcel in) {
        this.genres = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();

        this.id = in.readString();
        this.name = in.readString();

        this.primary = in.readString();
        this.blurHash = in.readString();
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
