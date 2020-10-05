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
        OPUS("Opus", "opus|opus"),
        AAC("M4A-AAC", "m4a|aac"),
        OGG("OGG-Vorbis", "ogg|vorbis"),
        OOPUS("OGG-Opus", "ogg|opus"),
        MKA("MKA-Opus", "mka|opus");

        public final String title;
        public final String value;

        Codec(String title, String value) {
            this.value = value;
            this.title = title;
        }
    }
}
