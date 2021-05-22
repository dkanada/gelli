package com.dkanada.gramophone.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.model.Codec;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DirectPlayCodecAdapter extends RecyclerView.Adapter<DirectPlayCodecAdapter.ViewHolder> {
    private final List<Codec> codecs;

    public DirectPlayCodecAdapter(List<Codec> codecs) {
        this.codecs = Arrays.stream(Codec.values())
            .peek(codec -> codec.select = codecs.contains(codec))
            .collect(Collectors.toList());
    }

    @NonNull
    @Override
    public DirectPlayCodecAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.preference_dialog_direct_play_codecs_item, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DirectPlayCodecAdapter.ViewHolder holder, int position) {
        Codec codec = codecs.get(position);

        holder.checkbox.setChecked(codec.select);
        holder.container.setText(codec.container);
        holder.codec.setText(codec.codec);

        holder.itemView.setOnClickListener(v -> {
            codec.select = !codec.select;
            holder.checkbox.setChecked(codec.select);
        });
    }

    @Override
    public int getItemCount() {
        return codecs.size();
    }

    public List<Codec> getCodecs() {
        return codecs.stream().filter(codec -> codec.select).collect(Collectors.toList());
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
