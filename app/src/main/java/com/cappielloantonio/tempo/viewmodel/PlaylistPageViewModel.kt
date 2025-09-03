package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cappielloantonio.tempo.repository.PlaylistRepository
import com.cappielloantonio.tempo.subsonic.models.Playlist

class PlaylistPageViewModel(application: Application) : AndroidViewModel(application) {
    private val playlistRepository: PlaylistRepository

    private var playlist: Playlist? = null
    private val isOffline = false

    init {
        playlistRepository = PlaylistRepository()
    }

    val playlistSongLiveList: LiveData<MutableList<Child?>?>?
        get() = playlistRepository.getPlaylistSongs(playlist!!.id)

    fun getPlaylist(): Playlist {
        return playlist!!
    }

    fun setPlaylist(playlist: Playlist) {
        this.playlist = playlist
    }

    fun isPinned(owner: LifecycleOwner): LiveData<Boolean?> {
        val isPinnedLive = MutableLiveData<Boolean?>()

        playlistRepository.getPinnedPlaylists()
            .observe(owner, Observer { playlists: MutableList<Playlist?>? ->
                isPinnedLive.postValue(
                    playlists!!.stream().anyMatch { obj: Playlist? -> obj!!.id == playlist!!.id })
            })

        return isPinnedLive
    }

    fun setPinned(isNowPinned: Boolean) {
        if (isNowPinned) {
            playlistRepository.insert(playlist)
        } else {
            playlistRepository.delete(playlist)
        }
    }
}
