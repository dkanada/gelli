package com.dkanada.gramophone.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

public class CategoryPreference extends ATEDialogPreference {
    public CategoryPreference(Context context) {
        super(context);
    }

    public CategoryPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public CategoryPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public CategoryPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
