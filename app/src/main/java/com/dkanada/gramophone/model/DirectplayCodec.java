package com.dkanada.gramophone.model;

public class DirectplayCodec {
    public String codecName;
    public String title;
    public String value;
    public boolean selected;

    public DirectplayCodec(String codecName, String title, String value, boolean selected) {
        this.codecName = codecName;
        this.title = title;
        this.value = value;
        this.selected = selected;
    }

    public enum Codec {
        // These are all non-translatable so just keep them here.
        FLAC("FLAC","flac|flac"),
        MP3("MP3", "mp3|mp3"),
        AAC("AAC (.m4a)", "m4a|aac"),
        OPUS("OPUS (.mka)", "mka|opus"),
        VORBIS("VORBIS (.ogg)", "ogg|vorbis");

        public final String title;
        public final String value;

        Codec(String title, String value) {
            this.value = value;
            this.title = title;
        }
    }
}
