package com.dkanada.gramophone.adapter.album;

import android.graphics.drawable.Drawable;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import android.view.View;
import android.view.ViewGroup;

import com.bumptech.glide.Glide;
import com.kabouzeid.appthemehelper.util.ColorUtil;
import com.kabouzeid.appthemehelper.util.MaterialValueHelper;
import com.dkanada.gramophone.glide.CustomGlideRequest;
import com.dkanada.gramophone.glide.CustomPaletteTarget;
import com.dkanada.gramophone.helper.HorizontalAdapterHelper;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.util.MusicUtil;

import java.util.List;

public class HorizontalAlbumAdapter extends AlbumAdapter {
    public HorizontalAlbumAdapter(@NonNull AppCompatActivity activity, List<Album> dataSet, boolean usePalette, @Nullable CabHolder cabHolder) {
        super(activity, dataSet, HorizontalAdapterHelper.LAYOUT_RES, usePalette, cabHolder);
    }

    @Override
    protected ViewHolder createViewHolder(View view, int viewType) {
        ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) view.getLayoutParams();
        HorizontalAdapterHelper.applyMarginToLayoutParams(activity, params, viewType);
        return new ViewHolder(view);
    }

    @Override
    protected void setColors(int color, ViewHolder holder) {
        if (holder.itemView != null) {
            CardView card=(CardView)holder.itemView;
            card.setCardBackgroundColor(color);
            if (holder.title != null) {
                holder.title.setTextColor(MaterialValueHelper.getPrimaryTextColor(activity, ColorUtil.isColorLight(color)));
            }

            if (holder.text != null) {
                holder.text.setTextColor(MaterialValueHelper.getSecondaryTextColor(activity, ColorUtil.isColorLight(color)));
            }
        }
    }

    @Override
    protected void loadAlbumCover(Album album, final ViewHolder holder) {
        if (holder.image == null) return;

        CustomGlideRequest.Builder
                .from(Glide.with(activity), album.primary)
                .generatePalette(activity).build()
                .into(new CustomPaletteTarget(holder.image) {
                    @Override
                    public void onLoadCleared(Drawable placeholder) {
                        super.onLoadCleared(placeholder);
                        setColors(getAlbumArtistFooterColor(), holder);
                    }

                    @Override
                    public void onColorReady(int color) {
                        if (usePalette) {
                            setColors(color, holder);
                        } else {
                            setColors(getAlbumArtistFooterColor(), holder);
                        }
                    }
                });
    }

    @Override
    protected String getAlbumText(Album album) {
        return MusicUtil.getYearString(album.year);
    }

    @Override
    public int getItemViewType(int position) {
        return HorizontalAdapterHelper.getItemViewtype(position, getItemCount());
    }
}
