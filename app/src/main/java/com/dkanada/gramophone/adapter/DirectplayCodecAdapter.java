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
import com.dkanada.gramophone.model.DirectplayCodec;

import java.util.List;

public class DirectplayCodecAdapter extends RecyclerView.Adapter<DirectplayCodecAdapter.ViewHolder> {
    private List<DirectplayCodec> directplayCodecs;

    public DirectplayCodecAdapter(List<DirectplayCodec> directplayCodecs) {
        this.directplayCodecs = directplayCodecs;
    }

    @Override
    @NonNull
    public DirectplayCodecAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_directplay_codecs_listitem, parent, false);
        return new ViewHolder(view);
    }

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onBindViewHolder(@NonNull DirectplayCodecAdapter.ViewHolder holder, int position) {
        DirectplayCodec directplayCodec = directplayCodecs.get(position);

        holder.checkBox.setChecked(directplayCodec.selected);
        holder.title.setText(directplayCodec.title);

        holder.itemView.setOnClickListener(v -> {
            directplayCodec.selected = !directplayCodec.selected;
            holder.checkBox.setChecked(directplayCodec.selected);
        });
    }

    @Override
    public int getItemCount() {
        return directplayCodecs.size();
    }

    public List<DirectplayCodec> getDirectplayCodecs() {
        return directplayCodecs;
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

