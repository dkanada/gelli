package com.kabouzeid.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;

import androidx.annotation.NonNull;

import com.bumptech.glide.BitmapRequestBuilder;
import com.bumptech.glide.DrawableRequestBuilder;
import com.bumptech.glide.DrawableTypeRequest;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.R;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteTranscoder;
import com.kabouzeid.gramophone.glide.palette.BitmapPaletteWrapper;

import org.jellyfin.apiclient.model.dto.ImageOptions;
import org.jellyfin.apiclient.model.entities.ImageType;

public class CustomGlideRequest {
    public static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.ALL;

    public static final int DEFAULT_IMAGE = R.drawable.default_album_art;
    public static final int DEFAULT_ANIMATION = android.R.anim.fade_in;

    public static class Builder {
        final RequestManager requestManager;
        final String item;

        public static Builder from(@NonNull RequestManager requestManager, String item) {
            return new Builder(requestManager, item);
        }

        private Builder(@NonNull RequestManager requestManager, String item) {
            this.requestManager = requestManager;
            this.item = item;
        }

        public PaletteBuilder generatePalette(Context context) {
            return new PaletteBuilder(this, context);
        }

        public BitmapBuilder asBitmap() {
            return new BitmapBuilder(this);
        }

        public DrawableRequestBuilder<GlideDrawable> build() {
            // noinspection unchecked
            return createBaseRequest(requestManager, item)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .placeholder(DEFAULT_IMAGE)
                    .animate(DEFAULT_ANIMATION)
                    .signature(createSignature(item));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public BitmapRequestBuilder<?, Bitmap> build() {
            // noinspection unchecked
            return createBaseRequest(builder.requestManager, builder.item)
                    .asBitmap()
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .placeholder(DEFAULT_IMAGE)
                    .animate(DEFAULT_ANIMATION)
                    .signature(createSignature(builder.item));
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public BitmapRequestBuilder<?, BitmapPaletteWrapper> build() {
            // noinspection unchecked
            return createBaseRequest(builder.requestManager, builder.item)
                    .asBitmap()
                    .transcode(new BitmapPaletteTranscoder(context), BitmapPaletteWrapper.class)
                    .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                    .placeholder(DEFAULT_IMAGE)
                    .animate(DEFAULT_ANIMATION)
                    .signature(createSignature(builder.item));
        }
    }

    public static DrawableTypeRequest createBaseRequest(RequestManager requestManager, String item) {
        if (item == null) {
            return requestManager.load(R.drawable.default_album_art);
        }

        ImageOptions options = new ImageOptions();
        options.setImageType(ImageType.Primary);
        options.setMaxHeight(800);

        String url = App.getApiClient().GetImageUrl(item, options);
        return requestManager.load(url);
    }

    public static Key createSignature(String item) {
        return new MediaStoreSignature("image/jpeg", item != null ? item.hashCode() : 0, 0);
    }
}
