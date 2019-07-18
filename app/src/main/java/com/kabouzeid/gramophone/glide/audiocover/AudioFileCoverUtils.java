package com.kabouzeid.gramophone.glide.audiocover;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.jaudiotagger.audio.mp3.MP3File;
import org.jaudiotagger.tag.images.Artwork;

public class AudioFileCoverUtils {
    public static final String[] FALLBACKS = {"cover.jpg", "album.jpg", "folder.jpg", "cover.png", "album.png", "folder.png"};

    public static InputStream fallback(String path) throws FileNotFoundException {
        // use embedded high resolution album art
        try {
            MP3File mp3File = new MP3File(path);
            if (mp3File.hasID3v2Tag()) {
                Artwork art = mp3File.getTag().getFirstArtwork();
                if (art != null) {
                    byte[] imageData = art.getBinaryData();
                    return new ByteArrayInputStream(imageData);
                }
            }
        } catch (Exception e) {
            // log exceptions and continue to the other fallback method
            e.printStackTrace();
        }

        // look for album art in external files
        final File parent = new File(path).getParentFile();
        for (String fallback : FALLBACKS) {
            File cover = new File(parent, fallback);
            if (cover.exists()) {
                return new FileInputStream(cover);
            }
        }
        return null;
    }
}
