package com.dkanada.gramophone.model;

import androidx.annotation.StringRes;

import com.dkanada.gramophone.R;

public enum Category {
    SONGS(R.string.songs),
    ALBUMS(R.string.albums),
    ARTISTS(R.string.artists),
    ALBUMARTISTS(R.string.albumartists),
    GENRES(R.string.genres),
    PLAYLISTS(R.string.playlists),
    FAVORITES(R.string.favorites);

    @StringRes
    public final int title;

    public boolean select;

    Category(int title) {
        this.title = title;
    }
}
