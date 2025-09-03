package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cappielloantonio.tempo.repository.PodcastRepository
import com.cappielloantonio.tempo.subsonic.models.PodcastChannel

class PodcastChannelBottomSheetViewModel(application: Application) : AndroidViewModel(application) {
    private val podcastRepository: PodcastRepository

    var podcastChannel: PodcastChannel? = null

    init {
        podcastRepository = PodcastRepository()
    }

    fun deletePodcastChannel() {
        if (podcastChannel != null) podcastRepository.deletePodcastChannel(podcastChannel!!.id)
    }
}
