package com.dkanada.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.os.Environment;

import androidx.annotation.NonNull;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.Registry;
import com.bumptech.glide.load.DecodeFormat;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.bumptech.glide.request.RequestOptions;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.glide.palette.BitmapPaletteTranscoder;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.io.File;

public class CustomGlideModule implements GlideModule {
    @Override
    public void applyOptions(@NonNull Context context, GlideBuilder builder) {
        File file = PreferenceUtil.getInstance(context).getExternalDirectory()
                ? new File(Environment.getExternalStorageDirectory() + "/Gelli/images")
                : new File(App.getInstance().getApplicationInfo().dataDir + "/glide");

        int size = PreferenceUtil.getInstance(context).getCacheSize();
        builder.setDiskCache(new DiskLruCacheFactory(() -> file, size));
        builder.setDefaultRequestOptions(new RequestOptions().format(DecodeFormat.PREFER_RGB_565));
    }

    @Override
    public void registerComponents(@NonNull Context context, @NonNull Glide glide, @NonNull Registry registry) {
        registry.register(Bitmap.class, BitmapPaletteWrapper.class, new BitmapPaletteTranscoder());
    }
}
