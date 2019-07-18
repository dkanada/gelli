package com.kabouzeid.gramophone.glide.audiocover;

import android.media.MediaMetadataRetriever;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import com.bumptech.glide.Priority;
import com.bumptech.glide.load.data.DataFetcher;

/**
 * @author Karim Abou Zeid (kabouzeid)
 */
public class AudioFileCoverFetcher implements DataFetcher<InputStream> {
    private final AudioFileCover model;

    private InputStream stream;

    public AudioFileCoverFetcher(AudioFileCover model) {
        this.model = model;
    }

    @Override
    public String getId() {
        // make sure we never return null here
        return String.valueOf(model.filePath);
    }

    @Override
    public InputStream loadData(final Priority priority) throws Exception {
        final MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(model.filePath);
            byte[] picture = retriever.getEmbeddedPicture();
            if (picture != null) {
                stream = new ByteArrayInputStream(picture);
            } else {
                stream = AudioFileCoverUtils.fallback(model.filePath);
            }
        } finally {
            retriever.release();
        }

        return stream;
    }

    @Override
    public void cleanup() {
        // already cleaned up in loadData
        if (stream != null) {
            try {
                stream.close();
            } catch (IOException e) {
                // can't do much about it
                e.printStackTrace();
            }
        }
    }

    @Override
    public void cancel() {
        // cannot cancel
    }
}
