package com.dkanada.gramophone.preferences;

import android.content.Context;
import android.util.AttributeSet;

import com.kabouzeid.appthemehelper.common.prefs.supportv7.ATEDialogPreference;

public class DirectPlayPreference extends ATEDialogPreference {
    public DirectPlayPreference(Context context) {
        super(context);
    }

    public DirectPlayPreference(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DirectPlayPreference(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public DirectPlayPreference(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }
}
