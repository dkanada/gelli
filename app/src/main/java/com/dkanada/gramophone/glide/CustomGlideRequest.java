package com.dkanada.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;

import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.Key;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.transition.ViewAnimationFactory;
import com.bumptech.glide.signature.MediaStoreSignature;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;

import org.jellyfin.apiclient.model.dto.ImageOptions;
import org.jellyfin.apiclient.model.entities.ImageType;

import static com.bumptech.glide.GenericTransitionOptions.with;

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

        public RequestBuilder<Drawable> build() {
            Object uri = item != null ? createUrl(item) : R.drawable.default_album_art;

            return requestManager.load(uri)
                    .apply(createRequestOptions(item));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            Object uri = builder.item != null ? createUrl(builder.item) : R.drawable.default_album_art;

            return builder.requestManager.asBitmap().load(uri)
                    .apply(createRequestOptions(builder.item))
                    .transition(new BitmapTransitionOptions().crossFade(DEFAULT_ANIMATION));
        }
    }

    public static class PaletteBuilder {
        final Context context;
        private final Builder builder;

        public PaletteBuilder(Builder builder, Context context) {
            this.builder = builder;
            this.context = context;
        }

        public RequestBuilder<BitmapPaletteWrapper> build() {
            Object uri = builder.item != null ? createUrl(builder.item) : R.drawable.default_album_art;

            return builder.requestManager.as(BitmapPaletteWrapper.class).load(uri)
                    .apply(createRequestOptions(builder.item))
                    .transition(with(new ViewAnimationFactory<>(DEFAULT_ANIMATION)));
        }
    }

    public static RequestOptions createRequestOptions(String item) {
        return new RequestOptions()
                .centerCrop()
                .error(DEFAULT_IMAGE)
                .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                .signature(createSignature(item));
    }

    public static String createUrl(String item) {
        ImageOptions options = new ImageOptions();
        options.setImageType(ImageType.Primary);
        options.setMaxHeight(800);

        return App.getApiClient().GetImageUrl(item, options);
    }

    public static Key createSignature(String item) {
        return new MediaStoreSignature("image/jpeg", item != null ? item.hashCode() : 0, 0);
    }
}
