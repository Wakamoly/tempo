package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.Child
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections
import kotlin.math.min

class SongRepository {
    fun getStarredSongs(random: Boolean, size: Int): MutableLiveData<MutableList<Child?>?> {
        val starredSongs = MutableLiveData<MutableList<Child?>?>(mutableListOf<Child?>())

        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getStarred2()
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.starred2 != null) {
                        val songs: MutableList<Child?>? =
                            response.body()!!.subsonicResponse.starred2!!.songs

                        if (songs != null) {
                            if (!random) {
                                starredSongs.value = songs
                            } else {
                                Collections.shuffle(songs)
                                starredSongs.value = songs.subList(0, min(size, songs.size))
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return starredSongs
    }

    fun getInstantMix(id: String?, count: Int): MutableLiveData<MutableList<Child?>?> {
        val instantMix = MutableLiveData<MutableList<Child?>?>()

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getSimilarSongs2(id, count)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.similarSongs2 != null) {
                        instantMix.setValue(response.body()!!.subsonicResponse.similarSongs2!!.songs)
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    instantMix.value = null
                }
            })

        return instantMix
    }

    fun getRandomSample(
        number: Int,
        fromYear: Int?,
        toYear: Int?
    ): MutableLiveData<MutableList<Child?>?> {
        val randomSongsSample = MutableLiveData<MutableList<Child?>?>()

        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getRandomSongs(number, fromYear, toYear)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    val songs: MutableList<Child?> = ArrayList<Child?>()

                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.randomSongs != null && response.body()!!.subsonicResponse.randomSongs!!.songs != null) {
                        songs.addAll(response.body()!!.subsonicResponse.randomSongs!!.songs!!)
                    }

                    randomSongsSample.value = songs
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return randomSongsSample
    }

    fun scrobble(id: String?, submission: Boolean) {
        getSubsonicClientInstance(false)
            .getMediaAnnotationClient()
            .scrobble(id, submission)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })
    }

    fun setRating(id: String?, rating: Int) {
        getSubsonicClientInstance(false)
            .getMediaAnnotationClient()
            .setRating(id, rating)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })
    }

    fun getSongsByGenre(id: String?, page: Int): MutableLiveData<MutableList<Child?>?> {
        val songsByGenre = MutableLiveData<MutableList<Child?>?>()

        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getSongsByGenre(id, 100, 100 * page)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.songsByGenre != null) {
                        songsByGenre.setValue(response.body()!!.subsonicResponse.songsByGenre!!.songs)
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return songsByGenre
    }

    fun getSongsByGenres(genresId: ArrayList<String?>): MutableLiveData<MutableList<Child?>?> {
        val songsByGenre = MutableLiveData<MutableList<Child?>?>()

        for (id in genresId) getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getSongsByGenre(id, 500, 0)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    val songs: MutableList<Child?> = ArrayList<Child?>()

                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.songsByGenre != null) {
                        songs.addAll(response.body()!!.subsonicResponse.songsByGenre!!.songs!!)
                    }

                    songsByGenre.value = songs
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return songsByGenre
    }

    fun getSong(id: String?): MutableLiveData<Child?> {
        val song = MutableLiveData<Child?>()

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getSong(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        song.value = response.body()!!.subsonicResponse.song
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return song
    }

    fun getSongLyrics(song: Child): MutableLiveData<String?> {
        val lyrics = MutableLiveData<String?>(null)

        getSubsonicClientInstance(false)
            .getMediaRetrievalClient()
            .getLyrics(song.artist, song.title)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.lyrics != null) {
                        lyrics.value = response.body()!!.subsonicResponse.lyrics!!.value
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return lyrics
    }

    companion object {
        private const val TAG = "SongRepository"
    }
}
