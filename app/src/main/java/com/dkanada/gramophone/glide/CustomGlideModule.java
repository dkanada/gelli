package com.dkanada.gramophone.glide;

import android.content.Context;
import android.os.Environment;

import com.bumptech.glide.Glide;
import com.bumptech.glide.GlideBuilder;
import com.bumptech.glide.load.engine.cache.DiskLruCacheFactory;
import com.bumptech.glide.module.GlideModule;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.util.PreferenceUtil;

import java.io.File;

public class CustomGlideModule implements GlideModule {
    @Override
    public void applyOptions(Context context, GlideBuilder builder) {
        builder.setDiskCache(new DiskLruCacheFactory(new DiskLruCacheFactory.CacheDirectoryGetter() {
            @Override
            public File getCacheDirectory() {
                String folder = "/Gelli/images";
                return PreferenceUtil.getInstance(App.getInstance()).getImagesExternalDirectory()
                    ? new File(Environment.getExternalStorageDirectory() + folder)
                    : new File(App.getInstance().getApplicationInfo().dataDir + folder);
            }
        }, PreferenceUtil.getInstance(App.getInstance()).getImagesCacheSize()));
    }

    @Override
    public void registerComponents(Context context, Glide glide) {
    }
}
