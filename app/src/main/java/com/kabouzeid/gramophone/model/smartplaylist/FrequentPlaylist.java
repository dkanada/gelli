package com.kabouzeid.gramophone.model.smartplaylist;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.loader.FrequentLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.SongPlayCountStore;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class FrequentPlaylist extends AbsSmartPlaylist {

    public FrequentPlaylist(@NonNull Context context) {
        super(context.getString(R.string.my_top_tracks), R.drawable.ic_trending_up_white_24dp);
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull Context context) {
        return FrequentLoader.getFrequent(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        SongPlayCountStore.getInstance(context).clear();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected FrequentPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<FrequentPlaylist> CREATOR = new Creator<FrequentPlaylist>() {
        public FrequentPlaylist createFromParcel(Parcel source) {
            return new FrequentPlaylist(source);
        }

        public FrequentPlaylist[] newArray(int size) {
            return new FrequentPlaylist[size];
        }
    };
}
