package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.interfaces.DecadesCallback
import com.cappielloantonio.tempo.interfaces.MediaCallback
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.AlbumInfo
import com.cappielloantonio.tempo.subsonic.models.Child
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Calendar
import java.util.Collections
import kotlin.math.min

class AlbumRepository {
    fun getAlbums(
        type: String?,
        size: Int,
        fromYear: Int?,
        toYear: Int?,
    ): MutableLiveData<List<AlbumID3?>?> {
        val listLiveAlbums = MutableLiveData<List<AlbumID3?>?>(ArrayList())

        getSubsonicClientInstance(false)
            .albumSongListClient
            .getAlbumList2(type, size, 0, fromYear, toYear)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            listLiveAlbums.value =
                                response
                                    .body()
                                    ?.subsonicResponse
                                    ?.albumList2
                                    ?.albums
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )

        return listLiveAlbums
    }

    fun getStarredAlbums(
        random: Boolean,
        size: Int,
    ): MutableLiveData<List<AlbumID3>> {
        val starredAlbums = MutableLiveData<List<AlbumID3>>(ArrayList())

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
                            val albums: List<AlbumID3>? =
                                response
                                    .body()
                                    ?.subsonicResponse
                                    ?.starred2
                                    ?.albums

                            if (albums != null) {
                                if (random) {
                                    Collections.shuffle(albums)
                                    starredAlbums.value = albums.subList(0, min(size, albums.size))
                                } else {
                                    starredAlbums.value = albums
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )

        return starredAlbums
    }

    fun setRating(
        id: String?,
        rating: Int,
    ) {
        getSubsonicClientInstance(false)
            .mediaAnnotationClient
            .setRating(id, rating)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) { // TODO
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )
    }

    fun getAlbumTracks(id: String?): MutableLiveData<MutableList<Child?>?> {
        val albumTracks = MutableLiveData<MutableList<Child?>?>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getAlbum(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        val tracks: MutableList<Child?> = ArrayList()
                        if (response.isSuccessful) {
                            response
                                .body()
                                ?.subsonicResponse
                                ?.album
                                ?.songs
                                ?.let {
                                    tracks.addAll(it)
                                }
                        }
                        albumTracks.value = tracks
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )

        return albumTracks
    }

    fun getArtistAlbums(id: String?): MutableLiveData<MutableList<AlbumID3>> {
        val artistsAlbum = MutableLiveData<MutableList<AlbumID3>>(ArrayList())

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
                            response
                                .body()
                                ?.subsonicResponse
                                ?.artist
                                ?.albums
                                ?.let {
                                    val albums = it.toMutableList()
                                    albums.sortWith(Comparator.comparing(AlbumID3::year))
                                    albums.reverse()
                                    artistsAlbum.value = albums
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )

        return artistsAlbum
    }

    fun getAlbum(id: String?): MutableLiveData<AlbumID3?> {
        val album = MutableLiveData<AlbumID3?>()

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
                            response.body()?.subsonicResponse?.album?.let {
                                album.value = it
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )

        return album
    }

    fun getAlbumInfo(id: String?): MutableLiveData<AlbumInfo?> {
        val albumInfo = MutableLiveData<AlbumInfo?>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getAlbumInfo2(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            albumInfo.value = response.body()?.subsonicResponse?.albumInfo
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )

        return albumInfo
    }

    fun getInstantMix(
        album: AlbumID3,
        count: Int,
        callback: MediaCallback,
    ) {
        getSubsonicClientInstance(false)
            .browsingClient
            .getSimilarSongs2(album.id, count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        val songs: MutableList<Child> = ArrayList()

                        if (response.isSuccessful) {
                            response.body()?.subsonicResponse?.similarSongs2?.songs?.let {
                                songs.addAll(it)
                            }
                        }

                        callback.onLoadMedia(songs)
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )
    }

    val decades: MutableLiveData<MutableList<Int?>?>
        get() {
            val decades =
                MutableLiveData<MutableList<Int?>?>()

            getFirstAlbum(
                object : DecadesCallback {
                    override fun onLoadYear(first: Int) {
                        getLastAlbum(
                            object : DecadesCallback {
                                override fun onLoadYear(last: Int) {
                                    if (first != -1 && last != -1) {
                                        val decadeList: MutableList<Int?> = ArrayList()

                                        var startDecade = first - (first % 10)
                                        val lastDecade = last - (last % 10)

                                        while (startDecade <= lastDecade) {
                                            decadeList.add(startDecade)
                                            startDecade = startDecade + 10
                                        }

                                        decades.value = decadeList
                                    }
                                }
                            },
                        )
                    }
                },
            )

            return decades
        }

    private fun getFirstAlbum(callback: DecadesCallback) {
        getSubsonicClientInstance(false)
            .albumSongListClient
            .getAlbumList2("byYear", 1, 0, 1900, Calendar.getInstance().get(Calendar.YEAR))
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            response
                                .body()
                                ?.subsonicResponse
                                ?.albumList2
                                ?.albums
                                ?.let {
                                    if (it.isNotEmpty()) {
                                        callback.onLoadYear(it[0].year)
                                    }
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) { // TODO
                    }
                },
            )
    }

    private fun getLastAlbum(callback: DecadesCallback) {
        getSubsonicClientInstance(false)
            .albumSongListClient
            .getAlbumList2("byYear", 1, 0, Calendar.getInstance().get(Calendar.YEAR), 1900)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            response
                                .body()
                                ?.subsonicResponse
                                ?.albumList2
                                ?.albums
                                ?.let {
                                    if (it.isNotEmpty()) {
                                        callback.onLoadYear(it[0].year)
                                    } else {
                                        callback.onLoadYear(-1)
                                    }
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        callback.onLoadYear(-1)
                    }
                },
            )
    }
}
