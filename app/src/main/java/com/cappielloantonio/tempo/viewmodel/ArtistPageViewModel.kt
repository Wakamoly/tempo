package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.cappielloantonio.tempo.repository.AlbumRepository
import com.cappielloantonio.tempo.repository.ArtistRepository
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.ArtistInfo2

class ArtistPageViewModel(application: Application) : AndroidViewModel(application) {
    private val albumRepository: AlbumRepository
    private val artistRepository: ArtistRepository

    private var artist: ArtistID3? = null

    init {
        albumRepository = AlbumRepository()
        artistRepository = ArtistRepository()
    }

    val albumList: LiveData<MutableList<AlbumID3?>?>?
        get() = albumRepository.getArtistAlbums(artist!!.id)

    fun getArtistInfo(id: String?): LiveData<ArtistInfo2?>? {
        return artistRepository.getArtistFullInfo(id)
    }

    val artistTopSongList: LiveData<MutableList<Child?>?>?
        get() = artistRepository.getTopSongs(artist!!.name, 20)

    val artistShuffleList: LiveData<MutableList<Child?>?>?
        get() = artistRepository.getRandomSong(artist, 50)

    val artistInstantMix: LiveData<MutableList<Child?>?>?
        get() = artistRepository.getInstantMix(artist, 20)

    fun getArtist(): ArtistID3 {
        return artist!!
    }

    fun setArtist(artist: ArtistID3) {
        this.artist = artist
    }
}
