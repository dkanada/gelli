package com.dkanada.gramophone.interfaces.base;

import android.content.SharedPreferences;

public abstract class PreferenceMigration {
    public final int startVersion;
    public final int endVersion;

    public PreferenceMigration(int startVersion, int endVersion) {
        this.startVersion = startVersion;
        this.endVersion = endVersion;
    }

    public abstract void migrate(SharedPreferences preferences);
}
