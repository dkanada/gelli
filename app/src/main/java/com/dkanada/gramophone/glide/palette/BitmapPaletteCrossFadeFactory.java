package com.dkanada.gramophone.glide.palette;

import android.graphics.Bitmap;

import com.bumptech.glide.request.transition.BitmapContainerTransitionFactory;
import com.bumptech.glide.request.transition.DrawableCrossFadeFactory;
import com.dkanada.gramophone.glide.CustomGlideRequest;

public class BitmapPaletteCrossFadeFactory extends BitmapContainerTransitionFactory<BitmapPaletteWrapper> {
    public BitmapPaletteCrossFadeFactory() {
        super(new DrawableCrossFadeFactory.Builder(CustomGlideRequest.DEFAULT_DURATION).build());
    }

    @Override
    protected Bitmap getBitmap(BitmapPaletteWrapper current) {
        return current.getBitmap();
    }
}
