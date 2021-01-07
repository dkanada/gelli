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
        ItemQuery query = new ItemQuery();

        query.setIncludeItemTypes(new String[]{"Audio"});
        query.setFields(new ItemFields[]{ItemFields.MediaSources});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setRecursive(true);
        query.setLimit(PreferenceUtil.getInstance(App.getInstance()).getPageSize());
        query.setStartIndex(getAdapter().getItemCount());
        query.setParentId(QueryUtil.currentLibrary.getId());
        query.setFilters(new ItemFilter[]{ItemFilter.IsFavorite});

        QueryUtil.applySortMethod(query, PreferenceUtil.getInstance(App.getInstance()).getSongSortMethod());
        QueryUtil.applySortOrder(query, PreferenceUtil.getInstance(App.getInstance()).getSongSortOrder());
        return query;
    }
}
