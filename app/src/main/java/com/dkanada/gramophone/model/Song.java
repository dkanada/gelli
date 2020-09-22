package com.dkanada.gramophone.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.dto.MediaSourceInfo;
import org.jellyfin.apiclient.model.entities.ImageType;
import org.jellyfin.apiclient.model.entities.MediaStream;

public class Song implements Parcelable {
    public static final Song EMPTY_SONG = new Song(null, "", -1, -1, -1, -1, null, "", null, "", null, false);

    public final String id;
    public final String title;
    public final int trackNumber;
    public final int discNumber;
    public final int year;
    public final long duration;

    public final String albumId;
    public final String albumName;

    public String artistId;
    public String artistName;

    public String primary;
    public String blurHash;
    public boolean favorite;

    public String path;
    public long size;

    public String container;
    public String codec;

    public int sampleRate;
    public int bitRate;
    public int bitDepth;
    public int channels;

    public Song(BaseItemDto itemDto) {
        this.id = itemDto.getId();
        this.title = itemDto.getName();
        this.trackNumber = itemDto.getIndexNumber() != null ? itemDto.getIndexNumber() : 0;
        this.discNumber = itemDto.getParentIndexNumber() != null ? itemDto.getParentIndexNumber() : 0;
        this.year = itemDto.getProductionYear() != null ? itemDto.getProductionYear() : 0;
        this.duration = itemDto.getRunTimeTicks() != null ? itemDto.getRunTimeTicks() / 10000 : 0;

        this.albumId = itemDto.getAlbumId();
        this.albumName = itemDto.getAlbum();

        if (itemDto.getAlbumArtists().size() != 0) {
            this.artistId = itemDto.getAlbumArtists().get(0).getId();
            this.artistName = itemDto.getAlbumArtists().get(0).getName();
        } else if (itemDto.getArtistItems().size() != 0) {
            this.artistId = itemDto.getArtistItems().get(0).getId();
            this.artistName = itemDto.getArtistItems().get(0).getName();
        }

        this.primary = itemDto.getAlbumPrimaryImageTag() != null ? albumId : null;
        this.favorite = itemDto.getUserData() != null && itemDto.getUserData().getIsFavorite();

        if (itemDto.getImageBlurHashes().get(ImageType.Primary) != null) {
            this.blurHash = (String) itemDto.getImageBlurHashes().get(ImageType.Primary).values().toArray()[0];
        }

        if (itemDto.getMediaSources() != null && itemDto.getMediaSources().get(0) != null) {
            MediaSourceInfo source = itemDto.getMediaSources().get(0);

            this.path = source.getPath();
            this.size = source.getSize();

            this.container = source.getContainer();
            this.bitRate = source.getBitrate();

            if (source.getMediaStreams() != null && source.getMediaStreams().get(0) != null) {
                MediaStream stream = source.getMediaStreams().get(0);

                this.codec = stream.getCodec();
                this.sampleRate = stream.getSampleRate() != null ? stream.getSampleRate() : 0;
                this.bitDepth = stream.getBitDepth() != null ? stream.getBitDepth() : 0;
                this.channels = stream.getChannels() != null ? stream.getChannels() : 0;
            }
        }
    }

    public Song(String id, String title, int trackNumber, int discNumber, int year, long duration, String albumId, String albumName, String artistId, String artistName, String primary, boolean favorite) {
        this.id = id;
        this.title = title;
        this.trackNumber = trackNumber;
        this.discNumber = discNumber;
        this.year = year;
        this.duration = duration;

        this.albumId = albumId;
        this.albumName = albumName;

        this.artistId = artistId;
        this.artistName = artistName;

        this.primary = primary;
        this.favorite = favorite;
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
        dest.writeInt(this.discNumber);
        dest.writeInt(this.year);
        dest.writeLong(this.duration);

        dest.writeString(this.albumId);
        dest.writeString(this.albumName);

        dest.writeString(this.artistId);
        dest.writeString(this.artistName);

        dest.writeString(this.primary);
        dest.writeString(Boolean.toString(favorite));

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
