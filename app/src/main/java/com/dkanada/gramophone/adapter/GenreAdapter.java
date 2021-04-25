package com.dkanada.gramophone.adapter;

import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.dkanada.gramophone.adapter.base.MediaEntryViewHolder;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.util.MusicUtil;
import com.dkanada.gramophone.util.NavigationUtil;
import com.simplecityapps.recyclerview_fastscroll.views.FastScrollRecyclerView;

import java.util.List;

public class GenreAdapter extends RecyclerView.Adapter<GenreAdapter.ViewHolder> implements FastScrollRecyclerView.SectionedAdapter {
    private final AppCompatActivity activity;
    private List<Genre> dataSet;

    private int itemLayoutRes;

    public GenreAdapter(@NonNull AppCompatActivity activity, List<Genre> dataSet, @LayoutRes int itemLayoutRes) {
        this.activity = activity;
        this.dataSet = dataSet;
        this.itemLayoutRes = itemLayoutRes;
    }

    public List<Genre> getDataSet() {
        return dataSet;
    }

    public void swapDataSet(List<Genre> dataSet) {
        this.dataSet = dataSet;
        notifyDataSetChanged();
    }

    @Override
    public long getItemId(int position) {
        return dataSet.get(position).hashCode();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(itemLayoutRes, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Genre genre = dataSet.get(position);

        if (holder.getBindingAdapterPosition() == getItemCount() - 1) {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.GONE);
            }
        } else {
            if (holder.shortSeparator != null) {
                holder.shortSeparator.setVisibility(View.VISIBLE);
            }
        }

        if (holder.menu != null) {
            holder.menu.setVisibility(View.GONE);
        }

        if (holder.title != null) {
            holder.title.setText(genre.name);
        }

        loadImage(genre, holder);
    }

    protected void loadImage(Genre genre, final GenreAdapter.ViewHolder holder) {
        if (holder.image == null) return;

        CustomGlideRequest.Builder
                .from(activity, genre.primary, genre.blurHash)
                .palette().build()
                .into(new CustomPaletteTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                    }

                    @Override
                    public void onColorReady(int color) {
                    }
                });
    }

    @Override
    public int getItemCount() {
        return dataSet.size();
    }

    @NonNull
    @Override
    public String getSectionName(int position) {
        final Genre genre = dataSet.get(position);
        return genre.id.hashCode() == -1 ? "" : MusicUtil.getSectionName(dataSet.get(position).name);
    }

    public class ViewHolder extends MediaEntryViewHolder {
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(View view) {
            Genre genre = dataSet.get(getBindingAdapterPosition());
            NavigationUtil.goToGenre(activity, genre);
        }
    }
}
