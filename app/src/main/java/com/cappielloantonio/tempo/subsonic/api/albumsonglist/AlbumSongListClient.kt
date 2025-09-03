package com.cappielloantonio.tempo.subsonic.api.albumsonglist

import android.util.Log
import com.cappielloantonio.tempo.subsonic.RetrofitClient
import com.cappielloantonio.tempo.subsonic.Subsonic
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import retrofit2.Call

class AlbumSongListClient(private val subsonic: Subsonic) {
    private val albumSongListService: AlbumSongListService

    init {
        this.albumSongListService =
            RetrofitClient(subsonic).retrofit.create<AlbumSongListService>(AlbumSongListService::class.java)
    }

    fun getAlbumList(type: String?, size: Int, offset: Int): Call<ApiResponse?>? {
        Log.d(TAG, "getAlbumList()")
        return albumSongListService.getAlbumList(subsonic.getParams(), type, size, offset)
    }

    fun getAlbumList2(
        type: String?,
        size: Int,
        offset: Int,
        fromYear: Int?,
        toYear: Int?
    ): Call<ApiResponse?>? {
        Log.d(TAG, "getAlbumList2()")
        return albumSongListService.getAlbumList2(
            subsonic.getParams(),
            type,
            size,
            offset,
            fromYear,
            toYear
        )
    }

    fun getRandomSongs(size: Int, fromYear: Int?, toYear: Int?): Call<ApiResponse?>? {
        Log.d(TAG, "getRandomSongs()")
        return albumSongListService.getRandomSongs(subsonic.getParams(), size, fromYear, toYear)
    }

    fun getSongsByGenre(genre: String?, count: Int, offset: Int): Call<ApiResponse?>? {
        Log.d(TAG, "getSongsByGenre()")
        return albumSongListService.getSongsByGenre(subsonic.getParams(), genre, count, offset)
    }

    val nowPlaying: Call<ApiResponse?>?
        get() {
            Log.d(TAG, "getNowPlaying()")
            return albumSongListService.getNowPlaying(subsonic.getParams())
        }

    val starred: Call<ApiResponse?>?
        get() {
            Log.d(TAG, "getStarred()")
            return albumSongListService.getStarred(subsonic.getParams())
        }

    val starred2: Call<ApiResponse?>?
        get() {
            Log.d(TAG, "getStarred2()")
            return albumSongListService.getStarred2(subsonic.getParams())
        }

    companion object {
        private const val TAG = "BrowsingClient"
    }
}
