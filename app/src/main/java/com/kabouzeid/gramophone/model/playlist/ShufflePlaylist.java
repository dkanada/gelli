package com.kabouzeid.gramophone.model.playlist;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.SongLoader;
import com.kabouzeid.gramophone.model.Song;

import java.util.List;

public class ShufflePlaylist extends AbsSmartPlaylist {

    public ShufflePlaylist(@NonNull Context context) {
        super(context.getString(R.string.action_shuffle_all), R.drawable.ic_shuffle_white_24dp);
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull Context context) {
        return SongLoader.getAllSongs(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        // Shuffle all is not a real "Smart Playlist"
    }

    @Override
    public int describeContents() {
        return 0;
    }

    protected ShufflePlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<ShufflePlaylist> CREATOR = new Creator<ShufflePlaylist>() {
        public ShufflePlaylist createFromParcel(Parcel source) {
            return new ShufflePlaylist(source);
        }

        public ShufflePlaylist[] newArray(int size) {
            return new ShufflePlaylist[size];
        }
    };
}
