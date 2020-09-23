package com.dkanada.gramophone.ui.fragments.player;

import androidx.annotation.DrawableRes;
import androidx.annotation.StringRes;

import com.dkanada.gramophone.R;

public enum NowPlayingScreen {
    CARD(R.string.card, R.drawable.np_card, 0),
    FLAT(R.string.flat, R.drawable.np_flat, 1);

    @StringRes
    public final int titleRes;

    @DrawableRes
    public final int drawableRes;

    public final int id;

    NowPlayingScreen(@StringRes int titleRes, @DrawableRes int drawableRes, int id) {
        this.titleRes = titleRes;
        this.drawableRes = drawableRes;
        this.id = id;
    }
}
