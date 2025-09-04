package com.cappielloantonio.tempo.repository

import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.LibraryResult
import androidx.media3.session.SessionError
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.database.AppDatabase
import com.cappielloantonio.tempo.database.dao.ChronologyDao
import com.cappielloantonio.tempo.database.dao.SessionMediaItemDao
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.model.Chronology
import com.cappielloantonio.tempo.model.SessionMediaItem
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.cappielloantonio.tempo.subsonic.models.PodcastEpisode
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.getImageSize
import com.google.common.collect.ImmutableList
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.SettableFuture
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.function.Consumer
import java.util.stream.Collectors

@Suppress("D")
@UnstableApi
class AutomotiveRepository {
    private val sessionMediaItemDao: SessionMediaItemDao =
        AppDatabase.Companion.instance.sessionMediaItemDao()
    private val chronologyDao: ChronologyDao =
        AppDatabase.Companion.instance.chronologyDao()

    fun getAlbums(
        prefix: String?,
        type: String?,
        size: Int,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .albumSongListClient
            .getAlbumList2(type, size, 0, null, null)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val albums: MutableList<AlbumID3>? =
                                response
                                    .body()
                                    ?.subsonicResponse
                                    ?.albumList2
                                    ?.albums
                                    ?.toMutableList()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (album in albums!!) {
                                val artworkUri =
                                    CustomGlideRequest
                                        .createUrl(
                                            album.coverArtId,
                                            getImageSize(),
                                        ).toUri()

                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(album.name)
                                        .setAlbumTitle(album.name)
                                        .setArtist(album.artist)
                                        .setGenre(album.genre)
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                                        .setArtworkUri(artworkUri)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(prefix + album.id)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri("")
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            val libraryResult =
                                LibraryResult.ofItemList(
                                    ImmutableList.copyOf<MediaItem>(mediaItems),
                                    null,
                                )

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    val starredSongs: ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>
        get() {
            val listenableFuture =
                SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

            getSubsonicClientInstance(false)
                .albumSongListClient
                .starred2
                .enqueue(
                    object : Callback<ApiResponse?> {
                        override fun onResponse(
                            call: Call<ApiResponse?>,
                            response: Response<ApiResponse?>,
                        ) {
                            if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.starred2 != null &&
                                response
                                    .body()!!
                                    .subsonicResponse.starred2!!
                                    .songs != null
                            ) {
                                val songs =
                                    (
                                            response
                                                .body()
                                                ?.subsonicResponse
                                                ?.starred2
                                                ?.songs
                                                ?: emptyList()
                                            ).toMutableList()

                                setChildrenMetadata(songs)

                                val mediaItems =
                                    MappingUtil.mapMediaItems(songs)

                                val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                    LibraryResult.ofItemList(
                                        ImmutableList.copyOf(
                                            mediaItems,
                                        ),
                                        null,
                                    )

                                listenableFuture.set(libraryResult)
                            } else {
                                listenableFuture.set(
                                    LibraryResult.ofError<ImmutableList<MediaItem>>(
                                        SessionError.ERROR_BAD_VALUE,
                                    ),
                                )
                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse?>,
                            t: Throwable,
                        ) {
                            listenableFuture.setException(t)
                        }
                    },
                )

            return listenableFuture
        }

    fun getRandomSongs(count: Int): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .albumSongListClient
            .getRandomSongs(100, null, null)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val songs: MutableList<Child>? =
                                response
                                    .body()
                                    ?.subsonicResponse
                                    ?.randomSongs
                                    ?.songs
                                    ?.toMutableList()

                            setChildrenMetadata(songs!!)

                            val mediaItems = MappingUtil.mapMediaItems(songs)

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getRecentlyPlayedSongs(
        server: String?,
        count: Int,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        chronologyDao
            .getLastPlayed(server, count)
            .observeForever(
                object : Observer<MutableList<Chronology>> {
                    override fun onChanged(value: MutableList<Chronology>) {
                        if (!value.isEmpty()) {
                            val songs: MutableList<Child> = ArrayList(value)

                            setChildrenMetadata(songs)

                            val mediaItems = MappingUtil.mapMediaItems(songs)

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(
                                    ImmutableList.copyOf(mediaItems),
                                    null,
                                )

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }

                        chronologyDao.getLastPlayed(server, count).removeObserver(this)
                    }
                },
            )

        return listenableFuture
    }

    fun getStarredAlbums(prefix: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .albumSongListClient
            .starred2
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val albums: MutableList<AlbumID3> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.starred2
                                            ?.albums
                                            ?: emptyList()
                                        ).toMutableList()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (album in albums) {
                                val artworkUri =
                                    CustomGlideRequest
                                        .createUrl(
                                            album.coverArtId,
                                            getImageSize(),
                                        ).toUri()

                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(album.name)
                                        .setArtist(album.artist)
                                        .setGenre(album.genre)
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                                        .setArtworkUri(artworkUri)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(prefix + album.id)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri("")
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return listenableFuture
    }

    fun getStarredArtists(prefix: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .albumSongListClient
            .starred2
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.starred2 != null &&
                            response
                                .body()!!
                                .subsonicResponse.starred2!!
                                .artists != null
                        ) {
                            val artists =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.starred2
                                            ?.artists
                                            ?: emptyList()
                                        ).toMutableList()

                            artists.shuffle()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (artist in artists) {
                                val artworkUri =
                                    CustomGlideRequest
                                        .createUrl(
                                            artist.coverArtId,
                                            getImageSize(),
                                        ).toUri()

                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(artist.name)
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                                        .setArtworkUri(artworkUri)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(prefix + artist.id)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri("")
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(
                                    mediaItems,
                                    null,
                                )

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getMusicFolders(prefix: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .musicFolders
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val musicFolders =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.musicFolders
                                            ?.musicFolders
                                            ?: emptyList()
                                        ).toMutableList()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (musicFolder in musicFolders) {
                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(musicFolder.name)
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(prefix + musicFolder.id)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri("")
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(
                                    mediaItems,
                                    null,
                                )

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getIndexes(
        prefix: String?,
        id: String?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getIndexes(id, null)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val mediaItems: MutableList<MediaItem> = ArrayList()
                            response
                                .body()
                                ?.subsonicResponse
                                ?.indexes
                                ?.let {
                                    if (it.indices != null) {
                                        for (index in it.indices) {
                                            if (index.artists != null) {
                                                for (artist in index.artists) {
                                                    val mediaMetadata =
                                                        MediaMetadata
                                                            .Builder()
                                                            .setTitle(artist.name)
                                                            .setIsBrowsable(true)
                                                            .setIsPlayable(false)
                                                            .setMediaType(MediaMetadata.MEDIA_TYPE_ARTIST)
                                                            .build()

                                                    val mediaItem =
                                                        MediaItem
                                                            .Builder()
                                                            .setMediaId(prefix + artist.id)
                                                            .setMediaMetadata(mediaMetadata)
                                                            .setUri("")
                                                            .build()

                                                    mediaItems.add(mediaItem)
                                                }
                                            }
                                        }
                                    }

                                    it.children?.let { children ->
                                        for (song in children) {
                                            val artworkUri =
                                                CustomGlideRequest
                                                    .createUrl(
                                                        song.coverArtId,
                                                        getImageSize(),
                                                    ).toUri()

                                            val mediaMetadata =
                                                MediaMetadata
                                                    .Builder()
                                                    .setTitle(song.title)
                                                    .setAlbumTitle(song.album)
                                                    .setArtist(song.artist)
                                                    .setIsBrowsable(false)
                                                    .setIsPlayable(true)
                                                    .setMediaType(MediaMetadata.MEDIA_TYPE_MUSIC)
                                                    .setArtworkUri(artworkUri)
                                                    .build()

                                            val mediaItem =
                                                MediaItem
                                                    .Builder()
                                                    .setMediaId(prefix + song.id)
                                                    .setMediaMetadata(mediaMetadata)
                                                    .setUri(MusicUtil.getStreamUri(song.id))
                                                    .build()

                                            mediaItems.add(mediaItem)
                                        }
                                        setChildrenMetadata(children.toMutableList())
                                    }
                                }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(
                                    mediaItems,
                                    null,
                                )

                            listenableFuture.set(libraryResult)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getDirectories(
        prefix: String?,
        id: String?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getMusicDirectory(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            response.body()?.subsonicResponse?.directory?.children?.let { children ->
                                val mediaItems: MutableList<MediaItem> = ArrayList()

                                for (child in children) {
                                    val artworkUri =
                                        CustomGlideRequest
                                            .createUrl(
                                                child.coverArtId,
                                                getImageSize(),
                                            ).toUri()

                                    val mediaMetadata =
                                        MediaMetadata
                                            .Builder()
                                            .setTitle(child.title)
                                            .setIsBrowsable(child.isDir)
                                            .setIsPlayable(!child.isDir)
                                            .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                                            .setArtworkUri(artworkUri)
                                            .build()

                                    val mediaItem =
                                        MediaItem
                                            .Builder()
                                            .setMediaId(if (child.isDir) prefix + child.id else child.id)
                                            .setMediaMetadata(mediaMetadata)
                                            .setUri(
                                                if (!child.isDir) {
                                                    MusicUtil.getStreamUri(child.id)
                                                } else {
                                                    "".toUri()
                                                },
                                            ).build()

                                    mediaItems.add(mediaItem)
                                }

                                setChildrenMetadata(
                                    children
                                        .stream()
                                        .filter { child: Child -> !child.isDir }
                                        .collect(
                                            Collectors.toList(),
                                        ),
                                )

                                val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                    LibraryResult.ofItemList(mediaItems, null)

                                listenableFuture.set(libraryResult)
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getPlaylists(prefix: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .playlistClient
            .playlists
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val playlists: MutableList<Playlist> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.playlists
                                            ?.playlists
                                            ?: emptyList()
                                        ).toMutableList()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (playlist in playlists) {
                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(playlist.name)
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(prefix + playlist.id)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri("")
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getNewestPodcastEpisodes(count: Int): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .podcastClient
            .getNewestPodcasts(count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val episodes: MutableList<PodcastEpisode>? =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.newestPodcasts
                                            ?.episodes
                                            ?: emptyList()
                                        ).toMutableList()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (episode in episodes!!) {
                                val artworkUri =
                                    CustomGlideRequest
                                        .createUrl(
                                            episode.coverArtId,
                                            getImageSize(),
                                        ).toUri()

                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(episode.title)
                                        .setIsBrowsable(false)
                                        .setIsPlayable(true)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_PODCAST_EPISODE)
                                        .setArtworkUri(artworkUri)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(episode.id!!)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri(MusicUtil.getStreamUri(episode.streamId!!))
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            setPodcastEpisodesMetadata(episodes)

                            val libraryResult =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    val internetRadioStations: ListenableFuture<LibraryResult<ImmutableList<MediaItem>>>
        get() {
            val listenableFuture =
                SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

            getSubsonicClientInstance(false)
                .internetRadioClient
                .internetRadioStations
                .enqueue(
                    object : Callback<ApiResponse?> {
                        override fun onResponse(
                            call: Call<ApiResponse?>,
                            response: Response<ApiResponse?>,
                        ) {
                            if (response.isSuccessful) {
                                val radioStations: MutableList<InternetRadioStation>? =
                                    (
                                            response
                                                .body()
                                                ?.subsonicResponse
                                                ?.internetRadioStations
                                                ?.internetRadioStations
                                                ?: emptyList()
                                            ).toMutableList()

                                val mediaItems: MutableList<MediaItem> = ArrayList()

                                for (radioStation in radioStations!!) {
                                    val mediaMetadata =
                                        MediaMetadata
                                            .Builder()
                                            .setTitle(radioStation.name)
                                            .setIsBrowsable(false)
                                            .setIsPlayable(true)
                                            .setMediaType(MediaMetadata.MEDIA_TYPE_RADIO_STATION)
                                            .build()

                                    val mediaItem =
                                        MediaItem
                                            .Builder()
                                            .setMediaId(radioStation.id!!)
                                            .setMediaMetadata(mediaMetadata)
                                            .setUri(radioStation.streamUrl)
                                            .build()

                                    mediaItems.add(mediaItem)
                                }

                                setInternetRadioStationsMetadata(radioStations)

                                val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                    LibraryResult.ofItemList(mediaItems, null)

                                listenableFuture.set(libraryResult)
                            } else {
                                listenableFuture.set(
                                    LibraryResult.ofError<ImmutableList<MediaItem>>(
                                        SessionError.ERROR_BAD_VALUE,
                                    ),
                                )
                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse?>,
                            t: Throwable,
                        ) {
                            listenableFuture.setException(t)
                        }
                    },
                )

            return listenableFuture
        }

    fun getAlbumTracks(id: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getAlbum(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val tracks: MutableList<Child> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.album
                                            ?.songs
                                            ?: emptyList()
                                        ).toMutableList()

                            setChildrenMetadata(tracks)

                            val mediaItems = MappingUtil.mapMediaItems(tracks)

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        } else {
                            listenableFuture.set(
                                LibraryResult.ofError<ImmutableList<MediaItem>>(
                                    SessionError.ERROR_BAD_VALUE,
                                ),
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getArtistAlbum(
        prefix: String?,
        id: String?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getArtist(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val albums: MutableList<AlbumID3> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.artist
                                            ?.albums
                                            ?: emptyList()
                                        ).toMutableList()

                            val mediaItems: MutableList<MediaItem> = ArrayList()

                            for (album in albums) {
                                val artworkUri =
                                    CustomGlideRequest
                                        .createUrl(
                                            album.coverArtId,
                                            getImageSize(),
                                        ).toUri()

                                val mediaMetadata =
                                    MediaMetadata
                                        .Builder()
                                        .setTitle(album.name)
                                        .setAlbumTitle(album.name)
                                        .setArtist(album.artist)
                                        .setGenre(album.genre)
                                        .setIsBrowsable(true)
                                        .setIsPlayable(false)
                                        .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                                        .setArtworkUri(artworkUri)
                                        .build()

                                val mediaItem =
                                    MediaItem
                                        .Builder()
                                        .setMediaId(prefix + album.id)
                                        .setMediaMetadata(mediaMetadata)
                                        .setUri("")
                                        .build()

                                mediaItems.add(mediaItem)
                            }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getPlaylistSongs(id: String?): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .playlistClient
            .getPlaylist(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val tracks: MutableList<Child> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.playlist
                                            ?.entries
                                            ?: emptyList()
                                        ).toMutableList()

                            setChildrenMetadata(tracks)

                            val mediaItems = MappingUtil.mapMediaItems(tracks)

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun getMadeForYou(
        id: String?,
        count: Int,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getSimilarSongs2(id, count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.similarSongs2 != null &&
                            response
                                .body()!!
                                .subsonicResponse.similarSongs2!!
                                .songs != null
                        ) {
                            val tracks: MutableList<Child> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.similarSongs2
                                            ?.songs
                                            ?: emptyList()
                                        ).toMutableList()

                            setChildrenMetadata(tracks)

                            val mediaItems = MappingUtil.mapMediaItems(tracks)

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    fun search(
        query: String?,
        albumPrefix: String?,
        artistPrefix: String?,
    ): ListenableFuture<LibraryResult<ImmutableList<MediaItem>>> {
        val listenableFuture = SettableFuture.create<LibraryResult<ImmutableList<MediaItem>>>()

        getSubsonicClientInstance(false)
            .searchingClient
            .search3(query, 20, 20, 20)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val mediaItems: MutableList<MediaItem> = ArrayList()
                            response
                                .body()
                                ?.subsonicResponse
                                ?.searchResult3
                                ?.let { searchResults ->
                                    searchResults.artists?.let { artistID3s ->
                                        for (artist in artistID3s) {
                                            val artworkUri =
                                                CustomGlideRequest
                                                    .createUrl(
                                                        artist.coverArtId,
                                                        getImageSize(),
                                                    ).toUri()

                                            val mediaMetadata =
                                                MediaMetadata
                                                    .Builder()
                                                    .setTitle(artist.name)
                                                    .setIsBrowsable(true)
                                                    .setIsPlayable(false)
                                                    .setMediaType(MediaMetadata.MEDIA_TYPE_PLAYLIST)
                                                    .setArtworkUri(artworkUri)
                                                    .build()

                                            val mediaItem =
                                                MediaItem
                                                    .Builder()
                                                    .setMediaId(artistPrefix + artist.id)
                                                    .setMediaMetadata(mediaMetadata)
                                                    .setUri("")
                                                    .build()

                                            mediaItems.add(mediaItem)
                                        }
                                    }

                                    searchResults.albums?.let { albumID3s ->
                                        for (album in albumID3s) {
                                            val artworkUri =
                                                CustomGlideRequest
                                                    .createUrl(
                                                        album.coverArtId,
                                                        getImageSize(),
                                                    ).toUri()

                                            val mediaMetadata =
                                                MediaMetadata
                                                    .Builder()
                                                    .setTitle(album.name)
                                                    .setAlbumTitle(album.name)
                                                    .setArtist(album.artist)
                                                    .setGenre(album.genre)
                                                    .setIsBrowsable(true)
                                                    .setIsPlayable(false)
                                                    .setMediaType(MediaMetadata.MEDIA_TYPE_ALBUM)
                                                    .setArtworkUri(artworkUri)
                                                    .build()

                                            val mediaItem =
                                                MediaItem
                                                    .Builder()
                                                    .setMediaId(albumPrefix + album.id)
                                                    .setMediaMetadata(mediaMetadata)
                                                    .setUri("")
                                                    .build()

                                            mediaItems.add(mediaItem)
                                        }
                                    }

                                    searchResults.songs?.let { songs ->
                                        val tracks = songs.toMutableList()
                                        setChildrenMetadata(tracks)
                                        mediaItems.addAll(MappingUtil.mapMediaItems(tracks))
                                    }
                                }

                            val libraryResult: LibraryResult<ImmutableList<MediaItem>> =
                                LibraryResult.ofItemList(mediaItems, null)

                            listenableFuture.set(libraryResult)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        listenableFuture.setException(t)
                    }
                },
            )

        return listenableFuture
    }

    @OptIn(markerClass = [UnstableApi::class])
    fun setChildrenMetadata(children: MutableList<Child>) {
        val timestamp = System.currentTimeMillis()
        val sessionMediaItems = ArrayList<SessionMediaItem?>()

        for (child in children) {
            val sessionMediaItem = SessionMediaItem(child)
            sessionMediaItem.timestamp = timestamp
            sessionMediaItems.add(sessionMediaItem)
        }

        val insertAll = InsertAllThreadSafe(sessionMediaItemDao, sessionMediaItems)
        val thread = Thread(insertAll)
        thread.start()
    }

    @OptIn(markerClass = [UnstableApi::class])
    fun setPodcastEpisodesMetadata(podcastEpisodes: MutableList<PodcastEpisode>) {
        val timestamp = System.currentTimeMillis()
        val sessionMediaItems = ArrayList<SessionMediaItem?>()

        for (podcastEpisode in podcastEpisodes) {
            val sessionMediaItem = SessionMediaItem(podcastEpisode)
            sessionMediaItem.timestamp = timestamp
            sessionMediaItems.add(sessionMediaItem)
        }

        val insertAll = InsertAllThreadSafe(sessionMediaItemDao, sessionMediaItems)
        val thread = Thread(insertAll)
        thread.start()
    }

    @OptIn(markerClass = [UnstableApi::class])
    fun setInternetRadioStationsMetadata(internetRadioStations: MutableList<InternetRadioStation>) {
        val timestamp = System.currentTimeMillis()
        val sessionMediaItems = ArrayList<SessionMediaItem?>()

        for (internetRadioStation in internetRadioStations) {
            val sessionMediaItem = SessionMediaItem(internetRadioStation)
            sessionMediaItem.timestamp = timestamp
            sessionMediaItems.add(sessionMediaItem)
        }

        val insertAll = InsertAllThreadSafe(sessionMediaItemDao, sessionMediaItems)
        val thread = Thread(insertAll)
        thread.start()
    }

    fun getSessionMediaItem(id: String?): SessionMediaItem? {
        var sessionMediaItem: SessionMediaItem? = null

        val getMediaItemThreadSafe = GetMediaItemThreadSafe(sessionMediaItemDao, id)
        val thread = Thread(getMediaItemThreadSafe)
        thread.start()

        try {
            thread.join()
            sessionMediaItem = getMediaItemThreadSafe.sessionMediaItem
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return sessionMediaItem
    }

    fun getMetadatas(timestamp: Long): MutableList<MediaItem> {
        var mediaItems = mutableListOf<MediaItem>()

        val getMediaItemsThreadSafe = GetMediaItemsThreadSafe(sessionMediaItemDao, timestamp)
        val thread = Thread(getMediaItemsThreadSafe)
        thread.start()

        try {
            thread.join()
            mediaItems = getMediaItemsThreadSafe.mediaItems
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        return mediaItems
    }

    fun deleteMetadata() {
        val delete = DeleteAllThreadSafe(sessionMediaItemDao)
        val thread = Thread(delete)
        thread.start()
    }

    private class GetMediaItemThreadSafe(
        private val sessionMediaItemDao: SessionMediaItemDao,
        private val id: String?,
    ) : Runnable {
        var sessionMediaItem: SessionMediaItem? = null
            private set

        override fun run() {
            sessionMediaItem = sessionMediaItemDao.get(id)
        }
    }

    @OptIn(markerClass = [UnstableApi::class])
    private class GetMediaItemsThreadSafe(
        private val sessionMediaItemDao: SessionMediaItemDao,
        private val timestamp: Long,
    ) : Runnable {
        val mediaItems: MutableList<MediaItem> = ArrayList()

        override fun run() {
            val sessionMediaItems = sessionMediaItemDao.get(timestamp)
            sessionMediaItems?.forEach(
                Consumer { sessionMediaItem: SessionMediaItem ->
                    sessionMediaItem.getMediaItem()?.let { mediaItem ->
                        mediaItems.add(mediaItem)
                    }
                },
            )
        }
    }

    private class InsertAllThreadSafe(
        private val sessionMediaItemDao: SessionMediaItemDao,
        private val sessionMediaItems: MutableList<SessionMediaItem?>?,
    ) : Runnable {
        override fun run() {
            sessionMediaItemDao.insertAll(sessionMediaItems)
        }
    }

    private class DeleteAllThreadSafe(
        private val sessionMediaItemDao: SessionMediaItemDao,
    ) : Runnable {
        override fun run() {
            sessionMediaItemDao.deleteAll()
        }
    }
}
