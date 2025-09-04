package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cappielloantonio.tempo.repository.PodcastRepository
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation

class PodcastChannelEditorViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val podcastRepository: PodcastRepository

    private val toEdit: InternetRadioStation? = null

    init {
        podcastRepository = PodcastRepository()
    }

    fun createChannel(url: String?) {
        podcastRepository.createPodcastChannel(url)
    }

    companion object {
        private const val TAG = "RadioEditorViewModel"
    }
}
