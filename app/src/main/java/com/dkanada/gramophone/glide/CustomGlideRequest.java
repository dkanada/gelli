package com.dkanada.gramophone.glide;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;

import androidx.core.content.res.ResourcesCompat;

import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestBuilder;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.resource.bitmap.BitmapTransitionOptions;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.signature.ObjectKey;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.glide.palette.BitmapPaletteCrossFadeFactory;
import com.dkanada.gramophone.glide.palette.BitmapPaletteWrapper;
import com.wolt.blurhashkt.BlurHashDecoder;

import org.jellyfin.apiclient.model.dto.ImageOptions;
import org.jellyfin.apiclient.model.entities.ImageType;

import static com.bumptech.glide.GenericTransitionOptions.with;

public class CustomGlideRequest {
    public static final DiskCacheStrategy DEFAULT_DISK_CACHE_STRATEGY = DiskCacheStrategy.ALL;
    public static final int DEFAULT_IMAGE = R.drawable.default_album_art;
    public static final int DEFAULT_DURATION = 200;

    public static class Builder {
        private final RequestManager requestManager;
        private final Object item;
        private final Context context;

        private Builder(Context context, String item, String placeholder) {
            this.requestManager = Glide.with(context);
            this.item = item != null ? createUrl(item) : DEFAULT_IMAGE;
            this.context = context;

            if (placeholder != null) {
                Bitmap bitmap = BlurHashDecoder.INSTANCE.decode(placeholder, 40, 40, 1, true);
                BitmapDrawable drawable = new BitmapDrawable(context.getResources(), bitmap);
                requestManager.applyDefaultRequestOptions(createRequestOptions(item, drawable));
            } else {
                Drawable drawable = ResourcesCompat.getDrawable(context.getResources(), DEFAULT_IMAGE, null);
                requestManager.applyDefaultRequestOptions(createRequestOptions(item, drawable));
            }
        }

        public static Builder from(Context context, String item, String placeholder) {
            return new Builder(context, item, placeholder);
        }

        public PaletteBuilder palette() {
            return new PaletteBuilder(this, this.context);
        }

        public BitmapBuilder bitmap() {
            return new BitmapBuilder(this);
        }

        public RequestBuilder<Drawable> build() {
            return requestManager.load(item)
                    .transition(DrawableTransitionOptions.withCrossFade(DEFAULT_DURATION));
        }
    }

    public static class BitmapBuilder {
        private final Builder builder;

        public BitmapBuilder(Builder builder) {
            this.builder = builder;
        }

        public RequestBuilder<Bitmap> build() {
            return builder.requestManager.asBitmap().load(builder.item)
                    .transition(BitmapTransitionOptions.withCrossFade(DEFAULT_DURATION));
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
            return builder.requestManager.as(BitmapPaletteWrapper.class).load(builder.item)
                    .transition(with(new BitmapPaletteCrossFadeFactory()));
        }
    }

    public static RequestOptions createRequestOptions(String item, Drawable placeholder) {
        return new RequestOptions()
                .placeholder(placeholder)
                .error(DEFAULT_IMAGE)
                .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
                .signature(new ObjectKey(item != null ? item : 0));
    }

    public static String createUrl(String item) {
        ImageOptions options = new ImageOptions();
        options.setImageType(ImageType.Primary);
        options.setMaxHeight(800);

        return App.getApiClient().GetImageUrl(item, options);
    }
}
