package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import com.cappielloantonio.tempo.repository.AlbumRepository
import com.cappielloantonio.tempo.repository.ArtistRepository
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.AlbumInfo
import com.cappielloantonio.tempo.subsonic.models.ArtistID3

class AlbumPageViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val albumRepository: AlbumRepository
    private val artistRepository: ArtistRepository
    private var albumId: String? = null
    private var artistId: String? = null
    val album: MutableLiveData<AlbumID3?> = MutableLiveData<AlbumID3?>(null)

    init {
        albumRepository = AlbumRepository()
        artistRepository = ArtistRepository()
    }

    val albumSongLiveList: LiveData<MutableList<Child?>?>?
        get() = albumRepository.getAlbumTracks(albumId)

    fun setAlbum(
        owner: LifecycleOwner,
        album: AlbumID3,
    ) {
        this.albumId = album.id
        this.album.postValue(album)
        this.artistId = album.artistId

        albumRepository.getAlbum(album.id).observe(
            owner,
            Observer { albums: AlbumID3? ->
                if (albums != null) this.album.value = albums
            },
        )
    }

    val artist: LiveData<ArtistID3?>?
        get() = artistRepository.getArtistInfo(artistId)

    val albumInfo: LiveData<AlbumInfo?>?
        get() = albumRepository.getAlbumInfo(albumId)
}
