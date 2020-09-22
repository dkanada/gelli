package com.dkanada.gramophone.glide.palette;

import android.graphics.Bitmap;

import com.bumptech.glide.request.transition.BitmapContainerTransitionFactory;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;

public class BitmapPaletteCrossFadeFactory extends BitmapContainerTransitionFactory<BitmapPaletteWrapper> {
    public BitmapPaletteCrossFadeFactory() {
        super(new DrawableCrossFadeFactory.Builder().build());
    }

    @Override
    protected Bitmap getBitmap(BitmapPaletteWrapper current) {
        return current.getBitmap();
    }
}
