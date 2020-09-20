package com.dkanada.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.glide.palette.BitmapPaletteTranscoder;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.io.File;

public class CustomGlideModule implements GlideModule {
    @Override
    public void applyOptions(@NonNull Context context, GlideBuilder builder) {
        builder.setDiskCache(new DiskLruCacheFactory(new DiskLruCacheFactory.CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                String folder = "/Gelli/images";
                return PreferenceUtil.getInstance(App.getInstance()).getExternalDirectory()
                    ? new File(Environment.getExternalStorageDirectory() + folder)
                    : new File(App.getInstance().getApplicationInfo().dataDir + folder);
            }
        }, PreferenceUtil.getInstance(App.getInstance()).getCacheSize()));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.register(Bitmap.class, BitmapPaletteWrapper.class, new BitmapPaletteTranscoder(glide.getBitmapPool()));
    }
}
