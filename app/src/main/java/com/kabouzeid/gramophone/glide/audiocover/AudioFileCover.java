package com.kabouzeid.gramophone.glide.audiocover;

import com.kabouzeid.gramophone.App;

import org.jellyfin.apiclient.model.dto.ImageOptions;
import org.jellyfin.apiclient.model.entities.ImageType;

public class AudioFileCover {
    public String location;

    public AudioFileCover(String item) {
        ImageOptions options = new ImageOptions();
        options.setImageType(ImageType.Primary);

        try {
            this.location = App.getApiClient().GetImageUrl(item, options);
        } catch (Exception e) {
            e.printStackTrace();
            this.location = "";
        }
    }
}
