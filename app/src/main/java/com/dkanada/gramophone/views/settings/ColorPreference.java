package com.dkanada.gramophone.views.settings;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

import androidx.preference.Preference;
import androidx.preference.PreferenceViewHolder;

import com.dkanada.gramophone.R;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.views.ColorCircleDrawable;
import com.flask.colorpicker.ColorPickerView;
import com.flask.colorpicker.builder.ColorPickerClickListener;
import com.flask.colorpicker.builder.ColorPickerDialogBuilder;
import com.kabouzeid.appthemehelper.ThemeStore;

public class ColorPreference extends Preference implements View.OnClickListener, ColorPickerClickListener {
    private SharedPreferences preferences;
    private ColorCircleDrawable colorView;

    private int defaultValue;

    public ColorPreference(Context context, AttributeSet attrs) {
        super(context, attrs);

        setWidgetLayoutResource(R.layout.preference_color);
    }

    @Override
    protected Object onGetDefaultValue(TypedArray a, int index) {
        defaultValue = a.getInt(index, getContext().getResources().getColor(android.R.color.white));

        return super.onGetDefaultValue(a, index);
    }

    @Override
    public void onBindViewHolder(PreferenceViewHolder holder) {
        super.onBindViewHolder(holder);

        preferences = PreferenceUtil.getInstance(getContext()).getPreferences();
        colorView = holder.itemView.findViewById(R.id.color);

        holder.itemView.setOnClickListener(this);
        colorView.setColor(preferences.getInt(getKey(), defaultValue));
    }

    @Override
    public void onClick(DialogInterface dialog, int color, Integer[] allColors) {
        preferences.edit().putInt(getKey(), color).apply();
        colorView.setColor(color);

        // TODO remove this when the theme helper library is removed
        if (getKey().equals(PreferenceUtil.PRIMARY_COLOR)) {
            ThemeStore.editTheme(getContext()).primaryColor(color).commit();
        } else if (getKey().equals(PreferenceUtil.ACCENT_COLOR)) {
            ThemeStore.editTheme(getContext()).accentColor(color).commit();
        }
    }

    @Override
    public void onClick(View v) {
        ColorPickerDialogBuilder
            .with(getContext())
            .setTitle(getTitle().toString())
            .initialColor(preferences.getInt(getKey(), defaultValue))
            .wheelType(ColorPickerView.WHEEL_TYPE.FLOWER)
            .showColorEdit(true)
            .showAlphaSlider(false)
            .density(8)
            .setPositiveButton(android.R.string.ok, this)
            .build()
            .show();
    }
}
