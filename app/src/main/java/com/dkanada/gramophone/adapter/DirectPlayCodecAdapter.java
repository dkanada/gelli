package com.dkanada.gramophone.adapter;

import android.annotation.SuppressLint;
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
    private List<DirectPlayCodec> directPlayCodecs;

    public DirectPlayCodecAdapter(List<DirectPlayCodec> directPlayCodecs) {
        this.directPlayCodecs = directPlayCodecs;
    }

    @Override
    @NonNull
    public DirectPlayCodecAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_direct_play_codecs_listitem, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull DirectPlayCodecAdapter.ViewHolder holder, int position) {
        DirectPlayCodec directPlayCodec = directPlayCodecs.get(position);

        holder.checkBox.setChecked(directPlayCodec.selected);
        holder.title.setText(directPlayCodec.title);

        holder.itemView.setOnClickListener(v -> {
            directPlayCodec.selected = !directPlayCodec.selected;
            holder.checkBox.setChecked(directPlayCodec.selected);
        });
    }

    @Override
    public int getItemCount() {
        return directPlayCodecs.size();
    }

    public List<DirectPlayCodec> getDirectPlayCodecs() {
        return directPlayCodecs;
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        public CheckBox checkBox;
        public TextView title;

        public ViewHolder(View view) {
            super(view);
            checkBox = view.findViewById(R.id.checkbox);
            title = view.findViewById(R.id.title);
        }
    }
}

