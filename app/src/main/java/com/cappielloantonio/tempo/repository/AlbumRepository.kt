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
        toYear: Int?
    ): MutableLiveData<MutableList<AlbumID3?>?> {
        val listLiveAlbums = MutableLiveData<MutableList<AlbumID3?>?>(ArrayList<AlbumID3?>())

        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getAlbumList2(type, size, 0, fromYear, toYear)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.albumList2 != null && response.body()!!.subsonicResponse.albumList2!!.albums != null) {
                        listLiveAlbums.setValue(response.body()!!.subsonicResponse.albumList2!!.albums)
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return listLiveAlbums
    }

    fun getStarredAlbums(random: Boolean, size: Int): MutableLiveData<MutableList<AlbumID3?>?> {
        val starredAlbums = MutableLiveData<MutableList<AlbumID3?>?>(ArrayList<AlbumID3?>())

        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getStarred2()
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.starred2 != null) {
                        val albums: MutableList<AlbumID3?>? =
                            response.body()!!.subsonicResponse.starred2!!.albums

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

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return starredAlbums
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

    fun getAlbumTracks(id: String?): MutableLiveData<MutableList<Child?>?> {
        val albumTracks = MutableLiveData<MutableList<Child?>?>()

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getAlbum(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    val tracks: MutableList<Child?> = ArrayList<Child?>()

                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.album != null) {
                        if (response.body()!!.subsonicResponse.album!!.songs != null) {
                            tracks.addAll(response.body()!!.subsonicResponse.album!!.songs!!)
                        }
                    }

                    albumTracks.value = tracks
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return albumTracks
    }

    fun getArtistAlbums(id: String?): MutableLiveData<MutableList<AlbumID3?>?> {
        val artistsAlbum = MutableLiveData<MutableList<AlbumID3?>?>(ArrayList<AlbumID3?>())

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getArtist(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.artist != null && response.body()!!.subsonicResponse.artist!!.albums != null) {
                        val albums: MutableList<AlbumID3?>? =
                            response.body()!!.subsonicResponse.artist!!.albums
                        albums!!.sort(Comparator.comparing<AlbumID3?, Int?>(AlbumID3::year))
                        Collections.reverse(albums)
                        artistsAlbum.value = albums
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return artistsAlbum
    }

    fun getAlbum(id: String?): MutableLiveData<AlbumID3?> {
        val album = MutableLiveData<AlbumID3?>()

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getAlbum(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.album != null) {
                        album.value = response.body()!!.subsonicResponse.album
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return album
    }

    fun getAlbumInfo(id: String?): MutableLiveData<AlbumInfo?> {
        val albumInfo = MutableLiveData<AlbumInfo?>()

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getAlbumInfo2(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.albumInfo != null) {
                        albumInfo.value = response.body()!!.subsonicResponse.albumInfo
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return albumInfo
    }

    fun getInstantMix(album: AlbumID3, count: Int, callback: MediaCallback) {
        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getSimilarSongs2(album.id, count)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    val songs: MutableList<Child?> = ArrayList<Child?>()

                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.similarSongs2 != null) {
                        songs.addAll(response.body()!!.subsonicResponse.similarSongs2!!.songs!!)
                    }

                    callback.onLoadMedia(songs)
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    callback.onLoadMedia(ArrayList<Any?>())
                }
            })
    }

    val decades: MutableLiveData<MutableList<Int?>?>
        get() {
            val decades =
                MutableLiveData<MutableList<Int?>?>()

            getFirstAlbum(object : DecadesCallback {
                override fun onLoadYear(first: Int) {
                    getLastAlbum(object : DecadesCallback {
                        override fun onLoadYear(last: Int) {
                            if (first != -1 && last != -1) {
                                val decadeList: MutableList<Int?> =
                                    ArrayList<Any?>()

                                var startDecade = first - (first % 10)
                                val lastDecade = last - (last % 10)

                                while (startDecade <= lastDecade) {
                                    decadeList.add(startDecade)
                                    startDecade = startDecade + 10
                                }

                                decades.value = decadeList
                            }
                        }
                    })
                }
            })

            return decades
        }

    private fun getFirstAlbum(callback: DecadesCallback) {
        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getAlbumList2("byYear", 1, 0, 1900, Calendar.getInstance().get(Calendar.YEAR))
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.albumList2 != null && response.body()!!.subsonicResponse.albumList2!!.albums != null && !response.body()!!.subsonicResponse.albumList2!!.albums!!.isEmpty()) {
                        callback.onLoadYear(
                            response.body()!!.subsonicResponse.albumList2!!.albums!!.get(
                                0
                            ).year
                        )
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    callback.onLoadYear(-1)
                }
            })
    }

    private fun getLastAlbum(callback: DecadesCallback) {
        getSubsonicClientInstance(false)
            .getAlbumSongListClient()
            .getAlbumList2("byYear", 1, 0, Calendar.getInstance().get(Calendar.YEAR), 1900)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.albumList2 != null && response.body()!!.subsonicResponse.albumList2!!.albums != null) {
                        if (!response.body()!!.subsonicResponse.albumList2!!.albums!!.isEmpty() && !response.body()!!.subsonicResponse.albumList2!!.albums!!.isEmpty()) {
                            callback.onLoadYear(
                                response.body()!!.subsonicResponse.albumList2!!.albums!!.get(
                                    0
                                ).year
                            )
                        } else {
                            callback.onLoadYear(-1)
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    callback.onLoadYear(-1)
                }
            })
    }
}
