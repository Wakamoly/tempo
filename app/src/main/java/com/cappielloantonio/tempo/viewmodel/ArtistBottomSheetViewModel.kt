package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cappielloantonio.tempo.interfaces.StarCallback
import com.cappielloantonio.tempo.repository.ArtistRepository
import com.cappielloantonio.tempo.repository.FavoriteRepository
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.util.NetworkUtil
import java.util.Date

class ArtistBottomSheetViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val artistRepository: ArtistRepository
    private val favoriteRepository: FavoriteRepository

    private var artist: ArtistID3? = null

    init {
        artistRepository = ArtistRepository()
        favoriteRepository = FavoriteRepository()
    }

    fun getArtist(): ArtistID3 = artist!!

    fun setArtist(artist: ArtistID3) {
        this.artist = artist
    }

    fun setFavorite() {
        if (artist!!.starred != null) {
            if (NetworkUtil.isOffline()) {
                removeFavoriteOffline()
            } else {
                removeFavoriteOnline()
            }
        } else {
            if (NetworkUtil.isOffline()) {
                setFavoriteOffline()
            } else {
                setFavoriteOnline()
            }
        }
    }

    private fun removeFavoriteOffline() {
        favoriteRepository.starLater(null, null, artist!!.id, false)
        artist!!.starred = null
    }

    private fun removeFavoriteOnline() {
        favoriteRepository.unstar(
            null,
            null,
            artist!!.id,
            object : StarCallback {
                override fun onError() {
                    // artist.setStarred(new Date());
                    favoriteRepository.starLater(null, null, artist!!.id, false)
                }
            },
        )

        artist!!.starred = null
    }

    private fun setFavoriteOffline() {
        favoriteRepository.starLater(null, null, artist!!.id, true)
        artist!!.starred = Date()
    }

    private fun setFavoriteOnline() {
        favoriteRepository.star(
            null,
            null,
            artist!!.id,
            object : StarCallback {
                override fun onError() {
                    // artist.setStarred(null);
                    favoriteRepository.starLater(null, null, artist!!.id, true)
                }
            },
        )

        artist!!.starred = Date()
    }
}
