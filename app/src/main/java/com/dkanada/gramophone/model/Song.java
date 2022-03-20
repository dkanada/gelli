package com.dkanada.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.dto.MediaSourceInfo;
import org.jellyfin.apiclient.model.entities.ImageType;
import org.jellyfin.apiclient.model.entities.MediaStream;

import java.util.UUID;

@Entity(tableName = "songs")
public class Song implements Parcelable {
    @NonNull
    @PrimaryKey
    public String id;
    public String title;
    public int trackNumber;
    public int discNumber;
    public int year;
    public long duration;

    public String albumId;
    public String albumName;

    public String artistId;
    public String artistName;

    public String primary;
    public String blurHash;
    public boolean favorite;

    public String path;
    public long size;

    public String container;
    public String codec;

    public boolean supportsTranscoding;

    public int sampleRate;
    public int bitRate;
    public int bitDepth;
    public int channels;

    @ColumnInfo(defaultValue = "1")
    public boolean cache;

    public Song() {
        this.id = UUID.randomUUID().toString();
    }

    public Song(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.title = itemDto.getName();
        this.trackNumber = itemDto.getIndexNumber() != null ? itemDto.getIndexNumber() : 0;
        this.discNumber = itemDto.getParentIndexNumber() != null ? itemDto.getParentIndexNumber() : 0;
        this.year = itemDto.getProductionYear() != null ? itemDto.getProductionYear() : 0;
        this.duration = itemDto.getRunTimeTicks() != null ? itemDto.getRunTimeTicks() / 10000 : 0;

        this.albumId = itemDto.getAlbumId();
        this.albumName = itemDto.getAlbum();

        if (itemDto.getArtistItems().size() != 0) {
            this.artistId = itemDto.getArtistItems().get(0).getId();
            this.artistName = itemDto.getArtistItems().get(0).getName();
        } else if (itemDto.getAlbumArtists().size() != 0) {
            this.artistId = itemDto.getAlbumArtists().get(0).getId();
            this.artistName = itemDto.getAlbumArtists().get(0).getName();
        }

        this.primary = itemDto.getAlbumPrimaryImageTag() != null ? albumId : null;
        if (itemDto.getImageBlurHashes() != null && itemDto.getImageBlurHashes().get(ImageType.Primary) != null) {
            this.blurHash = (String) itemDto.getImageBlurHashes().get(ImageType.Primary).values().toArray()[0];
        }

        this.favorite = itemDto.getUserData() != null && itemDto.getUserData().getIsFavorite();

        if (itemDto.getMediaSources() != null && itemDto.getMediaSources().get(0) != null) {
            MediaSourceInfo source = itemDto.getMediaSources().get(0);

            this.path = source.getPath();
            this.size = source.getSize() != null ? source.getSize() : 0;

            this.container = source.getContainer();
            this.bitRate = source.getBitrate() != null ? source.getBitrate() : 0;

            this.supportsTranscoding = source.getSupportsTranscoding();

            if (source.getMediaStreams() != null && source.getMediaStreams().size() != 0) {
                MediaStream stream = source.getMediaStreams().get(0);

                this.codec = stream.getCodec();
                this.sampleRate = stream.getSampleRate() != null ? stream.getSampleRate() : 0;
                this.bitDepth = stream.getBitDepth() != null ? stream.getBitDepth() : 0;
                this.channels = stream.getChannels() != null ? stream.getChannels() : 0;
            }
        }
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
        dest.writeString(this.id);
        dest.writeString(this.title);
        dest.writeInt(this.trackNumber);
        dest.writeInt(this.discNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);

        dest.writeString(this.albumId);
        dest.writeString(this.albumName);

        dest.writeString(this.artistId);
        dest.writeString(this.artistName);

        dest.writeString(this.primary);
        dest.writeString(Boolean.toString(favorite));
        dest.writeString(this.blurHash);

        dest.writeString(this.path);
        dest.writeLong(this.size);

        dest.writeString(this.container);
        dest.writeString(this.codec);

        dest.writeInt(this.sampleRate);
        dest.writeInt(this.bitRate);
        dest.writeInt(this.bitDepth);
        dest.writeInt(this.channels);
    }

    protected Song(Parcel in) {
        this.id = in.readString();
        this.title = in.readString();
        this.trackNumber = in.readInt();
        this.discNumber = in.readInt();
        this.year = in.readInt();
        this.duration = in.readLong();

        this.albumId = in.readString();
        this.albumName = in.readString();

        this.artistId = in.readString();
        this.artistName = in.readString();

        this.primary = in.readString();
        this.favorite = Boolean.parseBoolean(in.readString());
        this.blurHash = in.readString();

        this.path = in.readString();
        this.size = in.readLong();

        this.container = in.readString();
        this.codec = in.readString();

        this.sampleRate = in.readInt();
        this.bitRate = in.readInt();
        this.bitDepth = in.readInt();
        this.channels = in.readInt();
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
