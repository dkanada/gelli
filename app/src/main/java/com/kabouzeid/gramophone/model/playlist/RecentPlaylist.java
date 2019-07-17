package com.kabouzeid.gramophone.model.playlist;

import android.content.Context;
import android.os.Parcel;
import androidx.annotation.NonNull;

import com.kabouzeid.gramophone.loader.RecentLoader;
import com.kabouzeid.gramophone.model.Song;
import com.kabouzeid.gramophone.provider.HistoryStore;
import com.kabouzeid.gramophone.R;

import java.util.List;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class RecentPlaylist extends AbsSmartPlaylist {

    public RecentPlaylist(@NonNull Context context) {
        super(context.getString(R.string.history), R.drawable.ic_access_time_white_24dp);
    }

    @NonNull
    @Override
    public List<Song> getSongs(@NonNull Context context) {
        return RecentLoader.getRecent(context);
    }

    @Override
    public void clear(@NonNull Context context) {
        HistoryStore.getInstance(context).clear();
    }


    @Override
    public int describeContents() {
        return 0;
    }

    protected RecentPlaylist(Parcel in) {
        super(in);
    }

    public static final Creator<RecentPlaylist> CREATOR = new Creator<RecentPlaylist>() {
        public RecentPlaylist createFromParcel(Parcel source) {
            return new RecentPlaylist(source);
        }

        public RecentPlaylist[] newArray(int size) {
            return new RecentPlaylist[size];
        }
    };
}
