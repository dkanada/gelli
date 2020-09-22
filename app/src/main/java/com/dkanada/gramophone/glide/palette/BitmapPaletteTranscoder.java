package com.dkanada.gramophone.glide.palette;

import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.load.Options;
import com.bumptech.glide.load.engine.Resource;
import com.bumptech.glide.load.engine.bitmap_recycle.BitmapPool;
import com.bumptech.glide.load.resource.transcode.ResourceTranscoder;
import com.dkanada.gramophone.util.ThemeUtil;

public class BitmapPaletteTranscoder implements ResourceTranscoder<Bitmap, BitmapPaletteWrapper> {
    private final BitmapPool bitmapPool;

    public BitmapPaletteTranscoder(BitmapPool bitmapPool) {
        this.bitmapPool = bitmapPool;
    }

    @Override
    public Resource<BitmapPaletteWrapper> transcode(@NonNull Resource<Bitmap> resource, @NonNull Options options) {
        BitmapPaletteWrapper bitmapPaletteWrapper = new BitmapPaletteWrapper(resource.get(), ThemeUtil.generatePalette(resource.get()));
        return new BitmapPaletteResource(bitmapPaletteWrapper, bitmapPool);
    }
}