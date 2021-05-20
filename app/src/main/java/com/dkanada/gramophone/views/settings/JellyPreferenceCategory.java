package com.dkanada.gramophone.views.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.AttributeSet;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceManager;
import androidx.preference.PreferenceViewHolder;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.util.PreferenceUtil;

public class JellyPreferenceCategory extends PreferenceCategory {
    public JellyPreferenceCategory(Context context, AttributeSet attrs) {
        super(context, attrs);

        setLayoutResource(R.layout.preference_category);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        TextView textView = (TextView) holder.findViewById(android.R.id.title);

        int defaultColor = getContext().getResources().getColor(R.color.color_accent);
        int accentColor = preferences.getInt(PreferenceUtil.ACCENT_COLOR, defaultColor);

        textView.setTextColor(accentColor);
    }
}
