package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.Child
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import kotlin.math.min

class SongRepository {
    fun getStarredSongs(
        random: Boolean,
        size: Int,
    ): MutableLiveData<MutableList<Child>> {
        val starredSongs = MutableLiveData<MutableList<Child>>(mutableListOf())

        getSubsonicClientInstance(false)
            .albumSongListClient
            .starred2
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.starred2 != null) {
                            val songs =
                                (response
                                    .body()
                                    ?.subsonicResponse
                                    ?.starred2
                                    ?.songs
                                    ?: emptyList())
                                    .toMutableList()

                            if (!random) {
                                starredSongs.value = songs
                            } else {
                                songs.shuffle()
                                starredSongs.value = songs.subList(0, min(size, songs.size))
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return starredSongs
    }

    fun getInstantMix(
        id: String?,
        count: Int,
    ): MutableLiveData<MutableList<Child>> {
        val instantMix = MutableLiveData<MutableList<Child>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getSimilarSongs2(id, count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val instaMix = (response
                                .body()
                                ?.subsonicResponse
                                ?.similarSongs2
                                ?.songs
                                ?: emptyList())
                                .toMutableList()
                            instantMix.value = instaMix
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        instantMix.value = mutableListOf()
                    }
                },
            )

        return instantMix
    }

    fun getRandomSample(
        number: Int,
        fromYear: Int?,
        toYear: Int?,
    ): MutableLiveData<MutableList<Child>> {
        val randomSongsSample = MutableLiveData<MutableList<Child>>()

        getSubsonicClientInstance(false)
            .albumSongListClient
            .getRandomSongs(number, fromYear, toYear)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        val songs: MutableList<Child> = ArrayList()

                        if (response.isSuccessful) {
                            val songList = response
                                .body()
                                ?.subsonicResponse
                                ?.randomSongs
                                ?.songs
                                ?: emptyList()
                            songs.addAll(songList)
                        }

                        randomSongsSample.value = songs
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return randomSongsSample
    }

    fun scrobble(
        id: String?,
        submission: Boolean,
    ) {
        getSubsonicClientInstance(false)
            .mediaAnnotationClient
            .scrobble(id, submission)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )
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
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )
    }

    fun getSongsByGenre(
        id: String?,
        page: Int,
    ): MutableLiveData<MutableList<Child>> {
        val songsByGenre = MutableLiveData<MutableList<Child>>()

        getSubsonicClientInstance(false)
            .albumSongListClient
            .getSongsByGenre(id, 100, 100 * page)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.songsByGenre != null) {
                            val songs = (response
                                .body()
                                ?.subsonicResponse
                                ?.songsByGenre
                                ?.songs
                                ?: emptyList())
                                .toMutableList()
                            songsByGenre.value = songs
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return songsByGenre
    }

    fun getSongsByGenres(genresId: ArrayList<String?>): MutableLiveData<MutableList<Child>> {
        val songsByGenre = MutableLiveData<MutableList<Child>>()

        for (id in genresId) {
            getSubsonicClientInstance(false)
                .albumSongListClient
                .getSongsByGenre(id, 500, 0)
                .enqueue(
                    object : Callback<ApiResponse?> {
                        override fun onResponse(
                            call: Call<ApiResponse?>,
                            response: Response<ApiResponse?>,
                        ) {
                            val songs: MutableList<Child> = ArrayList()

                            if (response.isSuccessful && response.body() != null &&
                                response.body()!!.subsonicResponse.songsByGenre != null
                            ) {
                                songs.addAll(
                                    response
                                        .body()!!
                                        .subsonicResponse.songsByGenre!!
                                        .songs!!,
                                )
                            }

                            songsByGenre.value = songs
                        }

                        override fun onFailure(
                            call: Call<ApiResponse?>,
                            t: Throwable,
                        ) {
                        }
                    },
                )
        }

        return songsByGenre
    }

    fun getSong(id: String?): MutableLiveData<Child?> {
        val song = MutableLiveData<Child?>()

        getSubsonicClientInstance(false)
            .browsingClient
            .getSong(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            song.value = response.body()!!.subsonicResponse.song
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return song
    }

    fun getSongLyrics(song: Child): MutableLiveData<String?> {
        val lyrics = MutableLiveData<String?>(null)

        getSubsonicClientInstance(false)
            .mediaRetrievalClient
            .getLyrics(song.artist, song.title)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.lyrics != null) {
                            lyrics.value =
                                response
                                    .body()!!
                                    .subsonicResponse.lyrics!!
                                    .value
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return lyrics
    }

    companion object {
        private const val TAG = "SongRepository"
    }
}
