package com.dkanada.gramophone.model;

public class DirectPlayCodec {
    public String codecName;
    public String title;
    public String value;
    public boolean selected;

    public DirectPlayCodec(String codecName, String title, String value, boolean selected) {
        this.codecName = codecName;
        this.title = title;
        this.value = value;
        this.selected = selected;
    }

    public enum Codec {
        FLAC("FLAC","flac|flac"),
        MP3("MP3", "mp3|mp3"),
        AAC("AAC", "m4a|aac"),
        OGG("OGG", "ogg|vorbis"),
        MKA("MKA", "mka|opus");

        public final String title;
        public final String value;

        Codec(String title, String value) {
            this.value = value;
            this.title = title;
        }
    }
}
