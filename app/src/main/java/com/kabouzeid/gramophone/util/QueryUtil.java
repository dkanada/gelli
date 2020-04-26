package com.kabouzeid.gramophone.util;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.interfaces.MediaCallback;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Song;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.List;

public class QueryUtil {
    public static void getAlbums(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"MusicAlbum"});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(100);
        query.setRecursive(true);
        App.getApiClient().GetItemsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<Album> albums = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    albums.add(new Album(itemDto));
                }

                callback.onLoadMedia(albums);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getAlbum(String id, MediaCallback callback) {
        App.getApiClient().GetItemAsync(id, App.getApiClient().getCurrentUserId(), new Response<BaseItemDto>() {
            @Override
            public void onResponse(BaseItemDto itemDto) {
                List<Album> albums = new ArrayList<>();
                albums.add(new Album(itemDto));
                callback.onLoadMedia(albums);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getSongs(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"Audio"});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(100);
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

    public static void getArtists(MediaCallback callback) {
        ArtistsQuery query = new ArtistsQuery();
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(100);
        query.setRecursive(true);
        App.getApiClient().GetAlbumArtistsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<Artist> artists = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    artists.add(new Artist(itemDto));
                }

                callback.onLoadMedia(artists);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getArtist(String id, MediaCallback callback) {
        App.getApiClient().GetItemAsync(id, App.getApiClient().getCurrentUserId(), new Response<BaseItemDto>() {
            @Override
            public void onResponse(BaseItemDto itemDto) {
                List<Artist> artists = new ArrayList<>();
                artists.add(new Artist(itemDto));
                callback.onLoadMedia(artists);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }
}
