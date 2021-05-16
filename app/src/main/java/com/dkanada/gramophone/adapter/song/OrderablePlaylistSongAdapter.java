package com.dkanada.gramophone.adapter.song;

import android.view.View;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.dkanada.gramophone.interfaces.OrderableListener;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemAdapter;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemState;
import com.h6ah4i.android.widget.advrecyclerview.draggable.DraggableItemViewHolder;
import com.h6ah4i.android.widget.advrecyclerview.draggable.ItemDraggableRange;
import com.h6ah4i.android.widget.advrecyclerview.draggable.annotation.DraggableItemStateFlags;
import com.dkanada.gramophone.interfaces.CabHolder;
import com.dkanada.gramophone.model.Song;
import com.dkanada.gramophone.util.ViewUtil;

import java.util.List;

public class OrderablePlaylistSongAdapter extends PlaylistSongAdapter implements DraggableItemAdapter<OrderablePlaylistSongAdapter.ViewHolder> {
    private OrderableListener onMoveItemListener;

    public OrderablePlaylistSongAdapter(@NonNull AppCompatActivity activity, @NonNull List<Song> dataSet, @LayoutRes int itemLayoutRes, boolean usePalette, @Nullable CabHolder cabHolder, @Nullable OrderableListener onMoveItemListener) {
        super(activity, dataSet, itemLayoutRes, usePalette, cabHolder);

        this.onMoveItemListener = onMoveItemListener;
    }

    @Override
    protected SongAdapter.ViewHolder createViewHolder(View view) {
        return new OrderablePlaylistSongAdapter.ViewHolder(view);
    }

    @Override
    public long getItemId(int position) {
        position--;

        if (position < 0) return -2;
        return dataSet.get(position).id.hashCode();
    }

    @Override
    public boolean onCheckCanStartDrag(@NonNull ViewHolder holder, int position, int x, int y) {
        return onMoveItemListener != null && position > 0 &&
                (ViewUtil.hitTest(holder.dragView, x, y) || ViewUtil.hitTest(holder.image, x, y));
    }

    @Override
    public ItemDraggableRange onGetItemDraggableRange(@NonNull ViewHolder holder, int position) {
        return new ItemDraggableRange(1, dataSet.size());
    }

    @Override
    public void onMoveItem(int fromPosition, int toPosition) {
        if (onMoveItemListener != null && fromPosition != toPosition) {
            onMoveItemListener.onMoveItem(fromPosition - 1, toPosition - 1);
        }
    }

    @Override
    public boolean onCheckCanDrop(int draggingPosition, int dropPosition) {
        return dropPosition > 0;
    }

    @Override
    public void onItemDragStarted(int position) {
        notifyDataSetChanged();
    }

    @Override
    public void onItemDragFinished(int fromPosition, int toPosition, boolean result) {
        notifyDataSetChanged();
    }

    public class ViewHolder extends PlaylistSongAdapter.ViewHolder implements DraggableItemViewHolder {
        @DraggableItemStateFlags
        private int mDragStateFlags;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            if (dragView != null) {
                if (onMoveItemListener != null) {
                    dragView.setVisibility(View.VISIBLE);
                } else {
                    dragView.setVisibility(View.GONE);
                }
            }
        }

        @Override
        public void setDragStateFlags(@DraggableItemStateFlags int flags) {
            mDragStateFlags = flags;
        }

        @Override
        @DraggableItemStateFlags
        public int getDragStateFlags() {
            return mDragStateFlags;
        }

        @NonNull
        @Override
        public DraggableItemState getDragState() {
            return new DraggableItemState();
        }
    }
}
