package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import com.cappielloantonio.tempo.repository.RadioRepository
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation

class RadioEditorViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val radioRepository: RadioRepository

    var radioToEdit: InternetRadioStation? = null

    init {
        radioRepository = RadioRepository()
    }

    fun createRadio(
        name: String?,
        streamURL: String?,
        homepageURL: String?,
    ) {
        radioRepository.createInternetRadioStation(name, streamURL, homepageURL)
    }

    fun updateRadio(
        name: String?,
        streamURL: String?,
        homepageURL: String?,
    ) {
        if (this.radioToEdit != null) {
            radioRepository.updateInternetRadioStation(
                radioToEdit!!.id,
                name,
                streamURL,
                homepageURL,
            )
        }
    }

    fun deleteRadio() {
        if (this.radioToEdit != null) radioRepository.deleteInternetRadioStation(radioToEdit!!.id)
    }

    companion object {
        private const val TAG = "RadioEditorViewModel"
    }
}
