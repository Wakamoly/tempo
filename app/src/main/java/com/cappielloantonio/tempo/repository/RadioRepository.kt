package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RadioRepository {
    val internetRadioStations: MutableLiveData<MutableList<InternetRadioStation>>
        get() {
            val radioStation =
                MutableLiveData<MutableList<InternetRadioStation>>(ArrayList())

            getSubsonicClientInstance(false)
                .internetRadioClient
                .internetRadioStations
                .enqueue(
                    object : Callback<ApiResponse?> {
                        override fun onResponse(
                            call: Call<ApiResponse?>,
                            response: Response<ApiResponse?>,
                        ) {
                            if (response.isSuccessful && response.body() != null &&
                                response.body()!!.subsonicResponse.internetRadioStations != null &&
                                response
                                    .body()!!
                                    .subsonicResponse.internetRadioStations!!
                                    .internetRadioStations != null
                            ) {
                                response
                                    .body()
                                    ?.subsonicResponse
                                    ?.internetRadioStations
                                    ?.internetRadioStations
                                    ?.let {
                                        radioStation.value = it.toMutableList()
                                    }
                            }
                        }

                        override fun onFailure(
                            call: Call<ApiResponse?>,
                            t: Throwable,
                        ) {
                        }
                    },
                )

            return radioStation
        }

    fun createInternetRadioStation(
        name: String?,
        streamURL: String?,
        homepageURL: String?,
    ) {
        getSubsonicClientInstance(false)
            .internetRadioClient
            .createInternetRadioStation(streamURL, name, homepageURL)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )
    }

    fun updateInternetRadioStation(
        id: String?,
        name: String?,
        streamURL: String?,
        homepageURL: String?,
    ) {
        getSubsonicClientInstance(false)
            .internetRadioClient
            .updateInternetRadioStation(id, streamURL, name, homepageURL)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )
    }

    fun deleteInternetRadioStation(id: String?) {
        getSubsonicClientInstance(false)
            .internetRadioClient
            .deleteInternetRadioStation(id)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )
    }
}
