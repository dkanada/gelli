package com.dkanada.gramophone.fragments.library;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.model.Artist;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

public class AlbumArtistsFragment extends AbsArtistsFragment {

    @Override
    protected void loadItems(int index) {
        ArtistsQuery query = getQuery();
        query.setStartIndex(index);

        App.getApiClient().GetAlbumArtistsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                if (index == 0) getAdapter().getDataSet().clear();
                for (BaseItemDto itemDto : result.getItems()) {
                    getAdapter().getDataSet().add(new Artist(itemDto));
                }

                size = result.getTotalRecordCount();
                getAdapter().notifyDataSetChanged();
                loading = false;
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
