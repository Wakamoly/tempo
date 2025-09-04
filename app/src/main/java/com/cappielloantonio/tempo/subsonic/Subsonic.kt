package com.cappielloantonio.tempo.subsonic

import com.cappielloantonio.tempo.subsonic.api.albumsonglist.AlbumSongListClient
import com.cappielloantonio.tempo.subsonic.api.bookmarks.BookmarksClient
import com.cappielloantonio.tempo.subsonic.api.browsing.BrowsingClient
import com.cappielloantonio.tempo.subsonic.api.internetradio.InternetRadioClient
import com.cappielloantonio.tempo.subsonic.api.mediaannotation.MediaAnnotationClient
import com.cappielloantonio.tempo.subsonic.api.medialibraryscanning.MediaLibraryScanningClient
import com.cappielloantonio.tempo.subsonic.api.mediaretrieval.MediaRetrievalClient
import com.cappielloantonio.tempo.subsonic.api.open.OpenClient
import com.cappielloantonio.tempo.subsonic.api.playlist.PlaylistClient
import com.cappielloantonio.tempo.subsonic.api.podcast.PodcastClient
import com.cappielloantonio.tempo.subsonic.api.searching.SearchingClient
import com.cappielloantonio.tempo.subsonic.api.sharing.SharingClient
import com.cappielloantonio.tempo.subsonic.api.system.SystemClient
import com.cappielloantonio.tempo.subsonic.base.Version

class Subsonic(private val preferences: SubsonicPreferences) {
    val apiVersion: Version? = API_MAX_VERSION

    private var _systemClient: SystemClient? = null
        get() {
            if (field == null) {
                field = SystemClient(this)
            }
            return field
        }
    private var _browsingClient: BrowsingClient? = null
        get() {
            if (field == null) {
                field = BrowsingClient(this)
            }
            return field
        }
    private var _mediaRetrievalClient: MediaRetrievalClient? = null
        get() {
            if (field == null) {
                field = MediaRetrievalClient(this)
            }
            return field
        }
    private var _playlistClient: PlaylistClient? = null
        get() {
            if (field == null) {
                field = PlaylistClient(this)
            }
            return field
        }
    private var _searchingClient: SearchingClient? = null
        get() {
            if (field == null) {
                field = SearchingClient(this)
            }
            return field
        }
    private var _albumSongListClient: AlbumSongListClient? = null
        get() {
            if (field == null) {
                field = AlbumSongListClient(this)
            }
            return field
        }
    private var _mediaAnnotationClient: MediaAnnotationClient? = null
        get() {
            if (field == null) {
                field = MediaAnnotationClient(this)
            }
            return field
        }
    private var _podcastClient: PodcastClient? = null
        get() {
            if (field == null) {
                field = PodcastClient(this)
            }
            return field
        }
    private var _mediaLibraryScanningClient: MediaLibraryScanningClient? = null
        get() {
            if (field == null) {
                field = MediaLibraryScanningClient(this)
            }
            return field
        }
    private var _bookmarksClient: BookmarksClient? = null
        get() {
            if (field == null) {
                field = BookmarksClient(this)
            }
            return field
        }
    private var _internetRadioClient: InternetRadioClient? = null
        get() {
            if (field == null) {
                field = InternetRadioClient(this)
            }
            return field
        }
    private var _sharingClient: SharingClient? = null
        get() {
            if (field == null) {
                field = SharingClient(this)
            }
            return field
        }
    private var _openClient: OpenClient? = null
        get() {
            if (field == null) {
                field = OpenClient(this)
            }
            return field
        }

    val systemClient: SystemClient
        get() = requireNotNull(_systemClient)
    val browsingClient: BrowsingClient
        get() = requireNotNull(_browsingClient)
    val mediaRetrievalClient: MediaRetrievalClient
        get() = requireNotNull(_mediaRetrievalClient)
    val playlistClient: PlaylistClient
        get() = requireNotNull(_playlistClient)
    val searchingClient: SearchingClient
        get() = requireNotNull(_searchingClient)
    val albumSongListClient: AlbumSongListClient
        get() = requireNotNull(_albumSongListClient)
    val mediaAnnotationClient: MediaAnnotationClient
        get() = requireNotNull(_mediaAnnotationClient)
    val podcastClient: PodcastClient
        get() = requireNotNull(_podcastClient)
    val mediaLibraryScanningClient: MediaLibraryScanningClient
        get() = requireNotNull(_mediaLibraryScanningClient)
    val bookmarksClient: BookmarksClient
        get() = requireNotNull(_bookmarksClient)
    val internetRadioClient: InternetRadioClient
        get() = requireNotNull(_internetRadioClient)
    val sharingClient: SharingClient
        get() = requireNotNull(_sharingClient)
    val openClient: OpenClient
        get() = requireNotNull(_openClient)

    val url: String
        get() = (preferences.serverUrl + "/rest/").replace("//rest", "/rest")

    val params: MutableMap<String, String>
        get() {
            val params: MutableMap<String, String> =
                HashMap()
            params.put("u", preferences.username ?: "Unknown")

            preferences.authentication?.password?.let {
                params.put("p", it)
            }
            preferences.authentication?.token?.let {
                params.put("t", it)
            }
            preferences.authentication?.salt?.let {
                params.put("s", it)
            }

            params.put("v", apiVersion?.versionString ?: "Unknown")
            params.put("c", preferences.clientName ?: "Unknown")
            params.put("f", "json")

            return params
        }

    companion object {
        private val API_MAX_VERSION: Version = Version.Companion.of("1.15.0")
    }
}
