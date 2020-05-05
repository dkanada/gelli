package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

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

    public Artist(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.name = itemDto.getName();

        this.primary = itemDto.getImageTags().containsKey(ImageType.Primary) ? id : null;

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
    }

    public Artist(Song song) {
        this.id = song.artistId;
        this.name = song.artistName;
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
        dest.writeString(id);
        dest.writeString(name);

        dest.writeString(primary);
    }

    protected Artist(Parcel in) {
        this.genres = new ArrayList<>();
        this.albums = new ArrayList<>();
        this.songs = new ArrayList<>();

        this.id = in.readString();
        this.name = in.readString();

        this.primary = in.readString();
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
