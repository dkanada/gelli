package com.dkanada.gramophone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.DirectPlayCodec;

import java.util.List;

public class DirectPlayCodecAdapter extends RecyclerView.Adapter<DirectPlayCodecAdapter.ViewHolder> {
    private final List<DirectPlayCodec> directPlayCodecs;

    public DirectPlayCodecAdapter(List<DirectPlayCodec> directPlayCodecs) {
        this.directPlayCodecs = directPlayCodecs;
    }

    @NonNull
    @Override
    public DirectPlayCodecAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_direct_play_codecs_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectPlayCodecAdapter.ViewHolder holder, int position) {
        DirectPlayCodec directPlayCodec = directPlayCodecs.get(position);

        holder.checkbox.setChecked(directPlayCodec.selected);
        holder.container.setText(directPlayCodec.codec.container);
        holder.codec.setText(directPlayCodec.codec.codec);

        holder.itemView.setOnClickListener(v -> {
            directPlayCodec.selected = !directPlayCodec.selected;
            holder.checkbox.setChecked(directPlayCodec.selected);
        });
    }

    @Override
    public int getItemCount() {
        return directPlayCodecs.size();
    }

    public List<DirectPlayCodec> getDirectPlayCodecs() {
        return directPlayCodecs;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkbox;
        public TextView container;
        public TextView codec;

        public ViewHolder(View view) {
            super(view);

            checkbox = view.findViewById(R.id.checkbox);
            container = view.findViewById(R.id.container);
            codec = view.findViewById(R.id.codec);
        }
    }
}
