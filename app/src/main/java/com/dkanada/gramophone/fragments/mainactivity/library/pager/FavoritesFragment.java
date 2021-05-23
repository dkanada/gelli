package com.dkanada.gramophone.fragments.mainactivity.library.pager;

import androidx.annotation.NonNull;
import com.dkanada.gramophone.App;
import com.dkanada.gramophone.util.PreferenceUtil;
import com.dkanada.gramophone.util.QueryUtil;
import org.jellyfin.apiclient.model.querying.ItemFields;
import org.jellyfin.apiclient.model.querying.ItemFilter;
import org.jellyfin.apiclient.model.querying.ItemQuery;

public class FavoritesFragment extends SongsFragment {
    @NonNull
    @Override
    protected ItemQuery createQuery() {
        ItemQuery query = super.createQuery();

        // the only difference from the songs fragment is the favorite filter
        query.setFilters(new ItemFilter[]{ItemFilter.IsFavorite});

        return query;
    }
}
