package com.dkanada.gramophone.adapter;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.activities.SplashActivity;
import com.dkanada.gramophone.model.User;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.views.IconImageView;

import java.util.List;

public class SelectAdapter extends RecyclerView.Adapter<SelectAdapter.ViewHolder> {
    private final AppCompatActivity activity;
    private final List<User> users;

    public SelectAdapter(@NonNull AppCompatActivity activity, List<User> users) {
        this.activity = activity;
        this.users = users;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(activity).inflate(R.layout.card_server, parent, false);

        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final User user = users.get(position);

        holder.name.setText(user.name);
        holder.url.setText(user.server);
    }

    @Override
    public int getItemCount() {
        return users.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView url;

        IconImageView delete;
        IconImageView select;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            name = itemView.findViewById(R.id.name);
            url = itemView.findViewById(R.id.url);
            delete = itemView.findViewById(R.id.delete);
            select = itemView.findViewById(R.id.select);

            delete.setOnClickListener(this::onDelete);
            select.setOnClickListener(this::onSelect);
        }

        public void onSelect(View v) {
            final User user = users.get(getBindingAdapterPosition());

            PreferenceUtil.getInstance(activity).setServer(user.server);
            PreferenceUtil.getInstance(activity).setUser(user.id);

            activity.startActivity(new Intent(activity, SplashActivity.class));
        }

        public void onDelete(View v) {
            final User user = users.get(getBindingAdapterPosition());

            App.getDatabase().userDao().deleteUser(user);
            users.remove(user);
            notifyDataSetChanged();
        }
    }
}
