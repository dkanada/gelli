package com.dkanada.gramophone.adapter;

import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.util.Pair;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.kabouzeid.appthemehelper.util.ATHUtil;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.base.MediaEntryViewHolder;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.helper.MusicPlayerRemote;
import com.dkanada.gramophone.helper.menu.SongMenuHelper;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.NavigationUtil;

import java.util.ArrayList;
import java.util.List;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.ViewHolder> {
    private static final int HEADER = 0;
    private static final int ALBUM = 1;
    private static final int ARTIST = 2;
    private static final int SONG = 3;

    private final AppCompatActivity activity;
    private List<Object> dataSet;

    public SearchAdapter(@NonNull AppCompatActivity activity, @NonNull List<Object> dataSet) {
        this.activity = activity;
        this.dataSet = dataSet;
    }

    public void swapDataSet(@NonNull List<Object> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (dataSet.get(position) instanceof Album) return ALBUM;
        if (dataSet.get(position) instanceof Artist) return ARTIST;
        if (dataSet.get(position) instanceof Song) return SONG;
        return HEADER;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == HEADER)
            return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.sub_header, parent, false), viewType);
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_list, parent, false), viewType);
    }

    @SuppressWarnings("ConstantConditions")
    @Override
    public void onBindViewHolder(@NonNull final ViewHolder holder, int position) {
        switch (getItemViewType(position)) {
            case ALBUM:
                final Album album = (Album) dataSet.get(position);
                holder.title.setText(album.title);
                holder.text.setText(MusicUtil.getAlbumInfoString(activity, album));
                CustomGlideRequest.Builder
                        .from(activity, album.primary, album.blurHash)
                        .build().into(holder.image);
                break;
            case ARTIST:
                final Artist artist = (Artist) dataSet.get(position);
                holder.title.setText(artist.name);
                holder.text.setText(MusicUtil.getArtistInfoString(activity, artist));
                CustomGlideRequest.Builder
                        .from(activity, artist.primary, artist.blurHash)
                        .build().into(holder.image);
                break;
            case SONG:
                final Song song = (Song) dataSet.get(position);
                holder.title.setText(song.title);
                holder.text.setText(MusicUtil.getSongInfoString(song));
                break;
            default:
                holder.title.setText(dataSet.get(position).toString());
                break;
        }
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView, int itemViewType) {
            super(itemView);

            itemView.setOnLongClickListener(null);

            if (itemViewType != HEADER) {
                itemView.setBackgroundColor(ATHUtil.resolveColor(activity, R.attr.cardBackgroundColor));
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    itemView.setElevation(activity.getResources().getDimensionPixelSize(R.dimen.card_elevation));
                }

                if (shortSeparator != null) {
                    shortSeparator.setVisibility(View.GONE);
                }
            }

            if (menu != null) {
                if (itemViewType == SONG) {
                    menu.setVisibility(View.VISIBLE);
                    menu.setOnClickListener(new SongMenuHelper.OnClickSongMenu(activity) {
                        @Override
                        public Song getSong() {
                            return (Song) dataSet.get(getBindingAdapterPosition());
                        }
                    });
                } else {
                    menu.setVisibility(View.GONE);
                }
            }

            switch (itemViewType) {
                case ALBUM:
                    setImageTransitionName(activity.getString(R.string.transition_album_image));
                    break;
                case ARTIST:
                    setImageTransitionName(activity.getString(R.string.transition_artist_image));
                    View description = itemView.findViewById(R.id.text);
                    if (description != null) description.setVisibility(View.GONE);
                    break;
                default:
                    View container = itemView.findViewById(R.id.image_container);
                    if (container != null) container.setVisibility(View.GONE);
                    break;
            }
        }

        @Override
        public void onClick(View view) {
            Object item = dataSet.get(getBindingAdapterPosition());
            switch (getItemViewType()) {
                case ALBUM:
                    NavigationUtil.startAlbum(activity,
                            (Album) item,
                            Pair.create(image, activity.getResources().getString(R.string.transition_album_image)));
                    break;
                case ARTIST:
                    NavigationUtil.startArtist(activity,
                            (Artist) item,
                            Pair.create(image, activity.getResources().getString(R.string.transition_artist_image)));
                    break;
                case SONG:
                    List<Song> playList = new ArrayList<>();
                    playList.add((Song) item);
                    MusicPlayerRemote.openQueue(playList, 0, true);
                    break;
            }
        }
    }
}
