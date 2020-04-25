package com.kabouzeid.gramophone.glide.audiocover;

import java.io.IOException;
import java.io.InputStream;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;
import com.bumptech.glide.load.data.HttpUrlFetcher;
import com.bumptech.glide.load.model.GlideUrl;

public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private final AudioFileCover model;

    private InputStream stream;

    public AudioFileCoverFetcher(AudioFileCover model) {
        this.model = model;
    }

    @Override
    public String getId() {
        // make sure we never return null here
        return String.valueOf(model.location);
    }

    @Override
    public InputStream loadData(final Priority priority) throws Exception {
        final GlideUrl url = new GlideUrl(String.valueOf(model.location));
        final HttpUrlFetcher retriever = new HttpUrlFetcher(url);

        stream = retriever.loadData(Priority.NORMAL);
        return stream;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
