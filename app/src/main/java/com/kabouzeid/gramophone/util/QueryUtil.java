package com.kabouzeid.gramophone.util;

import com.kabouzeid.gramophone.App;
import com.kabouzeid.gramophone.interfaces.MediaCallback;
import com.kabouzeid.gramophone.model.Album;
import com.kabouzeid.gramophone.model.Artist;
import com.kabouzeid.gramophone.model.Genre;
import com.kabouzeid.gramophone.model.Song;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemQuery;
import org.jellyfin.apiclient.model.querying.ItemsByNameQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryUtil {
    public static BaseItemDto currentLibrary;

    public static void getLibraries(MediaCallback callback) {
        String id = App.getApiClient().getCurrentUserId();
        App.getApiClient().GetUserViews(id, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<BaseItemDto> libraries = new ArrayList<>();
                libraries.addAll(Arrays.asList(result.getItems()));

                callback.onLoadMedia(libraries);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getGenres(MediaCallback callback) {
        ItemsByNameQuery query = new ItemsByNameQuery();
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(100);
        query.setRecursive(true);
        if (currentLibrary != null && query.getParentId() == null) query.setParentId(currentLibrary.getId());
        App.getApiClient().GetGenresAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<Genre> genres = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    genres.add(new Genre(itemDto));
                }

                callback.onLoadMedia(genres);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getAlbums(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"MusicAlbum"});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(100);
        query.setRecursive(true);
        if (currentLibrary != null && query.getParentId() == null) query.setParentId(currentLibrary.getId());
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
        if (currentLibrary != null && query.getParentId() == null) query.setParentId(currentLibrary.getId());
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
        if (currentLibrary != null && query.getParentId() == null) query.setParentId(currentLibrary.getId());
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
