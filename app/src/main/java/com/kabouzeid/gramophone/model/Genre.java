package com.kabouzeid.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.dto.GenreDto;

public class Genre implements Parcelable {
    public final String id;
    public final String name;
    public final int songCount;

    public Genre(GenreDto genreDto) {
        this.id = genreDto.getId();
        this.name = genreDto.getName();
        this.songCount = 0;
    }

    public Genre(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.name = itemDto.getName();
        this.songCount = itemDto.getSongCount() != null ? itemDto.getSongCount() : 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Genre genre = (Genre) o;
        return id.equals(genre.id);
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
        dest.writeString(this.name);
        dest.writeInt(this.songCount);
    }

    protected Genre(Parcel in) {
        this.id = in.readString();
        this.name = in.readString();
        this.songCount = in.readInt();
    }

    public static final Creator<Genre> CREATOR = new Creator<Genre>() {
        public Genre createFromParcel(Parcel source) {
            return new Genre(source);
        }

        public Genre[] newArray(int size) {
            return new Genre[size];
        }
    };
}
