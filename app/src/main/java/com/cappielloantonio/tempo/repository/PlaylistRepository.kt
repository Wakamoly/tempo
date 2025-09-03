package com.cappielloantonio.tempo.repository

import android.widget.Toast
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.App.Companion.getContext
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.database.AppDatabase
import com.cappielloantonio.tempo.database.dao.PlaylistDao
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Playlist
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections
import kotlin.math.min

class PlaylistRepository {
    @UnstableApi
    private val playlistDao: PlaylistDao = AppDatabase.Companion.getInstance().playlistDao()
    fun getPlaylists(random: Boolean, size: Int): MutableLiveData<MutableList<Playlist?>?> {
        val listLivePlaylists = MutableLiveData<MutableList<Playlist?>?>(ArrayList<Playlist?>())

        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .getPlaylists()
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.playlists != null && response.body()!!.subsonicResponse.playlists!!.playlists != null) {
                        val playlists: MutableList<Playlist?>? =
                            response.body()!!.subsonicResponse.playlists!!.playlists

                        if (random) {
                            Collections.shuffle(playlists)
                            listLivePlaylists.value = playlists!!.subList(
                                0,
                                min(playlists.size, size)
                            )
                        } else {
                            listLivePlaylists.value = playlists
                        }
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return listLivePlaylists
    }

    fun getPlaylistSongs(id: String?): MutableLiveData<MutableList<Child?>?> {
        val listLivePlaylistSongs = MutableLiveData<MutableList<Child?>?>()

        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .getPlaylist(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.playlist != null) {
                        val songs: MutableList<Child?>? =
                            response.body()!!.subsonicResponse.playlist!!.entries
                        listLivePlaylistSongs.value = songs
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })

        return listLivePlaylistSongs
    }

    fun addSongToPlaylist(playlistId: String?, songsId: ArrayList<String?>?) {
        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .updatePlaylist(playlistId, null, true, songsId, null)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    Toast.makeText(
                        getContext(),
                        getContext().getString(R.string.playlist_chooser_dialog_toast_add_success),
                        Toast.LENGTH_SHORT
                    ).show()
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    Toast.makeText(
                        getContext(),
                        getContext().getString(R.string.playlist_chooser_dialog_toast_add_failure),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }

    fun createPlaylist(playlistId: String?, name: String?, songsId: ArrayList<String?>?) {
        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .createPlaylist(playlistId, name, songsId)
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

    fun updatePlaylist(playlistId: String?, name: String?, songsId: ArrayList<String?>?) {
        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .deletePlaylist(playlistId)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    createPlaylist(null, name, songsId)
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })
    }

    fun updatePlaylist(
        playlistId: String?,
        name: String?,
        isPublic: Boolean,
        songIdToAdd: ArrayList<String?>?,
        songIndexToRemove: ArrayList<Int?>?
    ) {
        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .updatePlaylist(playlistId, name, isPublic, songIdToAdd, songIndexToRemove)
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

    fun deletePlaylist(playlistId: String?) {
        getSubsonicClientInstance(false)
            .getPlaylistClient()
            .deletePlaylist(playlistId)
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

    @get:UnstableApi
    val pinnedPlaylists: LiveData<MutableList<Playlist?>?>?
        get() = playlistDao.getAll()

    @UnstableApi
    fun insert(playlist: Playlist?) {
        val insert = InsertThreadSafe(playlistDao, playlist)
        val thread = Thread(insert)
        thread.start()
    }

    @UnstableApi
    fun delete(playlist: Playlist?) {
        val delete = DeleteThreadSafe(playlistDao, playlist)
        val thread = Thread(delete)
        thread.start()
    }

    private class InsertThreadSafe(
        private val playlistDao: PlaylistDao,
        private val playlist: Playlist?
    ) : Runnable {
        override fun run() {
            playlistDao.insert(playlist)
        }
    }

    private class DeleteThreadSafe(
        private val playlistDao: PlaylistDao,
        private val playlist: Playlist?
    ) : Runnable {
        override fun run() {
            playlistDao.delete(playlist)
        }
    }
}
