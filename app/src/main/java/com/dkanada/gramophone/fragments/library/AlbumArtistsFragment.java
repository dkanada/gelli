package com.dkanada.gramophone.fragments.library;

import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.GridLayoutManager;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.R;
import com.dkanada.gramophone.adapter.artist.ArtistAdapter;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.SortMethod;
import com.dkanada.gramophone.model.SortOrder;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemFields;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.List;

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
