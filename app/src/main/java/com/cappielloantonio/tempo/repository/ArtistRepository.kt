package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.ArtistInfo2
import com.cappielloantonio.tempo.subsonic.models.Child
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.min

@Suppress("TooManyFunctions")
class ArtistRepository {
    fun getStarredArtists(
        random: Boolean,
        size: Int,
    ): MutableLiveData<MutableList<ArtistID3>> {
        val starredArtists = MutableLiveData<MutableList<ArtistID3>>(ArrayList())

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
                            val artists: MutableList<ArtistID3>? =
                                response
                                    .body()
                                    ?.subsonicResponse
                                    ?.starred2
                                    ?.artists
                                    ?.toMutableList()

                            if (artists != null) {
                                if (!random) {
                                    getArtistInfo(artists, starredArtists)
                                } else {
                                    artists.shuffle()
                                    getArtistInfo(
                                        artists.subList(0, min(size, artists.size)),
                                        starredArtists,
                                    )
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return starredArtists
    }

    @Suppress("D")
    fun getArtists(
        random: Boolean,
        size: Int,
    ): MutableLiveData<MutableList<ArtistID3>> {
        val listLiveArtists = MutableLiveData<MutableList<ArtistID3>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .artists
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val artists: MutableList<ArtistID3> = ArrayList()
                            response.body()?.subsonicResponse?.artists?.let { artistID3s ->
                                artistID3s.indices?.let { indexID3s ->
                                    for (indexID3 in indexID3s) {
                                        indexID3.artists?.run {
                                            artists.addAll(this)
                                        }
                                    }
                                }

                                if (random) {
                                    artists.shuffle()
                                    val artistSubListSize =
                                        if (artists.size / size > 0) size else artists.size
                                    val artistSubList = artists.subList(0, artistSubListSize)
                                    getArtistInfo(artistSubList, listLiveArtists)
                                } else {
                                    listLiveArtists.value = artists
                                }
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return listLiveArtists
    }

    /*
     * Metodo che mi restituisce le informazioni essenzionali dell'artista (cover, numero di album...)
     */
    fun getArtistInfo(
        artists: MutableList<ArtistID3>,
        list: MutableLiveData<MutableList<ArtistID3>>,
    ) {
        var liveArtists = list.getValue()
        if (liveArtists == null) liveArtists = ArrayList()
        list.value = liveArtists

        for (artist in artists) {
            getSubsonicClientInstance(false)
                .browsingClient
                .getArtist(artist.id)
                .enqueue(
                    object : Callback<ApiResponse?> {
                        override fun onResponse(
                            call: Call<ApiResponse?>,
                            response: Response<ApiResponse?>,
                        ) {
                            if (response.isSuccessful) {
                                response.body()?.subsonicResponse?.artist?.let {
                                    addToMutableLiveData(list, it)
                                }
                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse?>,
                            t: Throwable,
                        ) {
                            // TODO
                        }
                    },
                )
        }
    }

    fun getArtistInfo(id: String?): MutableLiveData<ArtistID3?> {
        val artist = MutableLiveData<ArtistID3?>()

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
                                ?.let {
                                    artist.value = it
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return artist
    }

    fun getArtistFullInfo(id: String?): MutableLiveData<ArtistInfo2?> {
        val artistFullInfo = MutableLiveData<ArtistInfo2?>(null)

        getSubsonicClientInstance(false)
            .browsingClient
            .getArtistInfo2(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.artistInfo2 != null) {
                            artistFullInfo.value = response.body()!!.subsonicResponse.artistInfo2
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return artistFullInfo
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
                    ) {
                        // TODO
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )
    }

    fun getArtist(id: String?): MutableLiveData<ArtistID3?> {
        val artist = MutableLiveData<ArtistID3?>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getArtist(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.artist != null) {
                            artist.value = response.body()!!.subsonicResponse.artist
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return artist
    }

    fun getInstantMix(
        artist: ArtistID3,
        count: Int,
    ): MutableLiveData<MutableList<Child>> {
        val instantMix = MutableLiveData<MutableList<Child>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getSimilarSongs2(artist.id, count)
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
                                ?.similarSongs2
                                ?.songs
                                ?.let {
                                    instantMix.value = it.toMutableList()
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return instantMix
    }

    fun getRandomSong(
        artist: ArtistID3,
        count: Int,
    ): MutableLiveData<MutableList<Child>> {
        val randomSongs = MutableLiveData<MutableList<Child>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getTopSongs(artist.name, count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            response
                                .body()!!
                                .subsonicResponse.topSongs!!
                                .songs
                                ?.let {
                                    val songs = it.toMutableList()
                                    if (songs.isNotEmpty()) {
                                        songs.shuffle()
                                    }
                                    randomSongs.value = songs
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return randomSongs
    }

    fun getTopSongs(
        artistName: String?,
        count: Int,
    ): MutableLiveData<MutableList<Child>> {
        val topSongs = MutableLiveData<MutableList<Child>>(ArrayList())

        getSubsonicClientInstance(false)
            .browsingClient
            .getTopSongs(artistName, count)
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
                                ?.topSongs
                                ?.songs
                                ?.let {
                                    topSongs.value = it.toMutableList()
                                }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO
                    }
                },
            )

        return topSongs
    }

    private fun addToMutableLiveData(
        liveData: MutableLiveData<MutableList<ArtistID3>>,
        artist: ArtistID3,
    ) {
        val liveArtists = liveData.getValue()
        liveArtists?.add(artist)
        liveData.value = liveArtists
    }
}
