package com.dkanada.gramophone.util;

import com.dkanada.gramophone.App;
import com.dkanada.gramophone.helper.sort.SortMethod;
import com.dkanada.gramophone.interfaces.MediaCallback;
import com.dkanada.gramophone.model.Album;
import com.dkanada.gramophone.model.Artist;
import com.dkanada.gramophone.model.Genre;
import com.dkanada.gramophone.model.Playlist;
import com.dkanada.gramophone.model.Song;

import org.jellyfin.apiclient.interaction.Response;
import org.jellyfin.apiclient.model.dto.BaseItemDto;
import org.jellyfin.apiclient.model.dto.BaseItemType;
import org.jellyfin.apiclient.model.entities.SortOrder;
import org.jellyfin.apiclient.model.querying.ArtistsQuery;
import org.jellyfin.apiclient.model.querying.ItemFields;
import org.jellyfin.apiclient.model.querying.ItemQuery;
import org.jellyfin.apiclient.model.querying.ItemsByNameQuery;
import org.jellyfin.apiclient.model.querying.ItemsResult;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QueryUtil {
    public static BaseItemDto currentLibrary;

    // TODO return BaseItemDto everywhere
    // will simplify the code for the getPlaylists method
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

    public static void getPlaylists(MediaCallback callback) {
        ItemQuery query = new ItemQuery();
        query.setIncludeItemTypes(new String[]{"Playlist"});
        applyProperties(query);
        App.getApiClient().GetItemsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<Playlist> playlists = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    playlists.add(new Playlist(itemDto));
                }

                callback.onLoadMedia(playlists);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getGenres(MediaCallback callback) {
        ItemsByNameQuery query = new ItemsByNameQuery();
        applyProperties(query);
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

    public static void getItems(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"MusicArtist", "MusicAlbum", "Audio"});
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setLimit(40);
        query.setRecursive(true);
        App.getApiClient().GetItemsAsync(query, new Response<ItemsResult>() {
            @Override
            public void onResponse(ItemsResult result) {
                List<Object> items = new ArrayList<>();
                for (BaseItemDto itemDto : result.getItems()) {
                    if (itemDto.getBaseItemType() == BaseItemType.MusicArtist) {
                        items.add(new Artist(itemDto));
                    } else if (itemDto.getBaseItemType() == BaseItemType.MusicAlbum) {
                        items.add(new Album(itemDto));
                    } else {
                        items.add(new Song(itemDto));
                    }
                }

                callback.onLoadMedia(items);
            }

            @Override
            public void onError(Exception exception) {
                exception.printStackTrace();
            }
        });
    }

    public static void getAlbums(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"MusicAlbum"});
        applyProperties(query);
        applySortMethod(query, PreferenceUtil.getInstance(App.getInstance()).getAlbumSortMethod());
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

    public static void getArtists(ArtistsQuery query, MediaCallback callback) {
        query.setFields(new ItemFields[]{ItemFields.Genres});
        applyProperties(query);
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

    public static void getSongs(ItemQuery query, MediaCallback callback) {
        query.setIncludeItemTypes(new String[]{"Audio"});
        applyProperties(query);
        applySortMethod(query, PreferenceUtil.getInstance(App.getInstance()).getSongSortMethod());
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

    private static void applyProperties(ItemQuery query) {
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setRecursive(true);
        if (query.getParentId() == null && query.getArtistIds().length == 0) {
            query.setLimit(PreferenceUtil.getInstance(App.getInstance()).getMaximumListSize());
        }

        if (currentLibrary == null || query.getParentId() != null) return;
        if (query.getArtistIds().length == 0) query.setParentId(currentLibrary.getId());
    }

    private static void applyProperties(ItemsByNameQuery query) {
        query.setUserId(App.getApiClient().getCurrentUserId());
        query.setRecursive(true);
        if (query.getParentId() == null) {
            query.setLimit(PreferenceUtil.getInstance(App.getInstance()).getMaximumListSize());
        }

        if (currentLibrary == null || query.getParentId() != null) return;
        query.setParentId(currentLibrary.getId());
    }

    private static void applySortMethod(ItemQuery query, String method) {
        // album activity will always sort by track number
        if (query.getSortBy().length != 0) return;

        switch (method) {
            case SortMethod.NAME:
                query.setSortBy(new String[]{"SortName"});
                break;
            case SortMethod.ALBUM:
                query.setSortBy(new String[]{"Album"});
                break;
            case SortMethod.ARTIST:
                query.setSortBy(new String[]{"AlbumArtist"});
                break;
            case SortMethod.YEAR:
                query.setSortBy(new String[]{"ProductionYear"});
                break;
            case SortMethod.ADDED:
                query.setSortBy(new String[]{"DateCreated"});
                break;
            case SortMethod.RANDOM:
                query.setSortBy(new String[]{"Random"});
                break;
        }
    }
}
