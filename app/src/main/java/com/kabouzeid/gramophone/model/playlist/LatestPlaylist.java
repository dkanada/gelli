package com.kabouzeid.gramophone.model.playlist;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.kabouzeid.gramophone.loader.LatestLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.R;

import java.util.List;

public class LatestPlaylist extends AbsSmartPlaylist {

    public LatestPlaylist(@NonNull Context context) {
        super(context.getString(R.string.last_added), R.drawable.ic_library_add_white_24dp);
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull Context context) {
        return LatestLoader.getLatest(context);
    }

    @Override
    public void clear(@NonNull Context context) {
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected LatestPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<LatestPlaylist> CREATOR = new Creator<LatestPlaylist>() {
        public LatestPlaylist createFromParcel(Parcel source) {
            return new LatestPlaylist(source);
        }

        public LatestPlaylist[] newArray(int size) {
            return new LatestPlaylist[size];
        }
    };
}
