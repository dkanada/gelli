package com.dkanada.gramophone.util;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.helper.sort.SortOrder;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Song;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ItemFields;
import org.jellyfin.apiclient.model.querying.ItemQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.List;

public class ShortcutUtil {
    public static void getFrequent(MediaCallback<Song> callback) {
        ItemQuery query = new ItemQuery();

        QueryUtil.applySortMethod(query, SortMethod.COUNT);
        QueryUtil.applySortOrder(query, SortOrder.DESCENDING);

        getSongs(query, callback);
    }

    public static void getLatest(MediaCallback<Song> callback) {
        ItemQuery query = new ItemQuery();

        QueryUtil.applySortMethod(query, SortMethod.ADDED);
        QueryUtil.applySortOrder(query, SortOrder.DESCENDING);

        getSongs(query, callback);
    }

    public static void getShuffle(MediaCallback<Song> callback) {
        ItemQuery query = new ItemQuery();

        QueryUtil.applySortMethod(query, SortMethod.RANDOM);
        QueryUtil.applySortOrder(query, SortOrder.DESCENDING);

        getSongs(query, callback);
    }

    public static void getSongs(ItemQuery query, MediaCallback<Song> callback) {
        query.setIncludeItemTypes(new String[]{"Audio"});
        query.setFields(new ItemFields[]{ItemFields.MediaSources});

        query.setLimit(200);
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setRecursive(true);

        App.getApiClient().GetItemsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<Song> songs = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    songs.add(new Song(itemDto));
                }

                callback.onLoadMedia(songs);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
