package com.dkanada.gramophone.views.settings;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.codekidlabs.storagechooser.StorageChooser;
import com.dkanada.gramophone.util.PreferenceUtil;

public class FilePreference extends Preference implements View.OnClickListener, StorageChooser.OnSelectListener {
    private final SharedPreferences preferences;
    private final String defaultLocation;

    public FilePreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        preferences = PreferenceUtil.getInstance(getContext()).getPreferences();
        defaultLocation = getContext().getCacheDir().toString();

        setSummary(preferences.getString(getKey(), defaultLocation));
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        holder.itemView.setOnClickListener(this);
    }

    @Override
    public void onSelect(String path) {
        preferences.edit().putString(getKey(), path).apply();
        setSummary(path);
    }

    @Override
    public void onClick(View v) {
        Activity activity = (Activity) getContext();
        FragmentManager fragmentManager = activity.getFragmentManager();

        StorageChooser chooser = new StorageChooser.Builder()
            .withActivity(activity)
            .withFragmentManager(fragmentManager)
            .withPredefinedPath(preferences.getString(getKey(), defaultLocation))
            .allowCustomPath(true)
            .allowAddFolder(true)
            .setType(StorageChooser.DIRECTORY_CHOOSER)
            .build();

        chooser.setOnSelectListener(this);
        chooser.show();
    }
}
