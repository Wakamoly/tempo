package com.cappielloantonio.tempo.subsonic.models

import android.os.Parcelable
import androidx.annotation.Keep
import kotlinx.parcelize.Parcelize

@Keep
@Parcelize
data class InternetRadioStation(
    val id: String? = null,
    val name: String? = null,
    val streamUrl: String? = null,
    val homePageUrl: String? = null,
) : Parcelable