package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cappielloantonio.tempo.repository.PlaylistRepository
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.google.common.collect.Lists

class PlaylistChooserViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val playlistRepository: PlaylistRepository

    private val playlists = MutableLiveData<MutableList<Playlist?>?>(null)
    var songsToAdd: ArrayList<Child?> = ArrayList<Child?>()

    init {
        playlistRepository = PlaylistRepository()
    }

    fun getPlaylistList(owner: LifecycleOwner): LiveData<MutableList<Playlist?>?> {
        playlistRepository.getPlaylists(false, -1).observe(
            owner,
            Observer { value: MutableList<Playlist?>? -> playlists.postValue(value) },
        )
        return playlists
    }

    fun addSongsToPlaylist(playlistId: String?) {
        playlistRepository.addSongToPlaylist(
            playlistId,
            ArrayList<String?>(
                Lists.transform<Child?, String?>(
                    this.songsToAdd,
                    Child::id,
                ),
            ),
        )
    }
}
