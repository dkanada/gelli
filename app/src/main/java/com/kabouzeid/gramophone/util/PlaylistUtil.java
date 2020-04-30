package com.kabouzeid.gramophone.util;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.interfaces.MediaCallback;
import com.kabouzeid.gramophone.model.Playlist;
import com.kabouzeid.gramophone.model.PlaylistSong;
import com.kabouzeid.gramophone.model.Song;

import org.jellyfin.apiclient.interaction.EmptyResponse;
import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.playlists.PlaylistCreationRequest;
import org.jellyfin.apiclient.model.querying.ItemQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.List;

public class PlaylistUtil {
    public static void getPlaylist(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"Audio"});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(100);
        query.setRecursive(true);
        if (QueryUtil.currentLibrary != null && query.getParentId() == null) query.setParentId(QueryUtil.currentLibrary.getId());
        App.getApiClient().GetItemsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<PlaylistSong> songs = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    songs.add(new PlaylistSong(itemDto, query.getParentId()));
                }

                callback.onLoadMedia(songs);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void createPlaylist(final String name, final List<Song> songs) {
        ArrayList<String> ids = new ArrayList<>();
        for (Song song : songs) {
            ids.add(song.id);
        }

        PlaylistCreationRequest request = new PlaylistCreationRequest();
        request.setUserId(App.getApiClient().getCurrentUserId());
        request.setName(name);
        if (ids.size() != 0) request.setItemIdList(ids);
        App.getApiClient().CreatePlaylist(request, new Response<>());
    }

    public static void deletePlaylist(final List<Playlist> playlists) {
        for (Playlist playlist : playlists) {
            App.getApiClient().DeleteItem(playlist.id, new EmptyResponse());
        }
    }

    public static void addItems(final List<Song> songs, final String playlist) {
        String[] ids = new String[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            ids[i] = songs.get(i).id;
        }

        String user = App.getApiClient().getCurrentUserId();
        App.getApiClient().AddToPlaylist(playlist, ids, user, new EmptyResponse());
    }

    public static void deleteItems(final List<Song> songs, final String playlist) {
        String[] ids = new String[songs.size()];
        for (int i = 0; i < songs.size(); i++) {
            ids[i] = songs.get(i).id;
        }

        App.getApiClient().RemoveFromPlaylist(playlist, ids, new EmptyResponse());
    }

    public static void moveItem(final String playlist, final Song song, int to) {
    }

    public static void renamePlaylist(final String playlist, final String name) {
        String user = App.getApiClient().getCurrentUserId();
        App.getApiClient().GetItemAsync(playlist, user, new Response<BaseItemDto>() {
            @Override
            public void onResponse(BaseItemDto itemDto) {
                itemDto.setName(name);
                renamePlaylistInner(itemDto);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void renamePlaylistInner(final BaseItemDto itemDto) {
        // TODO find a method to upload metadata changes
        // at some point this could become metadata utilities
    }
}