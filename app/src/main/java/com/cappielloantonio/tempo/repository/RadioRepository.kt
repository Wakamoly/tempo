package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class RadioRepository {
    val internetRadioStations: MutableLiveData<MutableList<InternetRadioStation?>?>
        get() {
            val radioStation =
                MutableLiveData<MutableList<InternetRadioStation?>?>(ArrayList<InternetRadioStation?>())

            getSubsonicClientInstance(false)
                .getInternetRadioClient()
                .getInternetRadioStations()
                .enqueue(object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.internetRadioStations != null && response.body()!!.subsonicResponse.internetRadioStations!!.internetRadioStations != null) {
                            radioStation.setValue(response.body()!!.subsonicResponse.internetRadioStations!!.internetRadioStations)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable
                    ) {
                    }
                })

            return radioStation
        }

    fun createInternetRadioStation(name: String?, streamURL: String?, homepageURL: String?) {
        getSubsonicClientInstance(false)
            .getInternetRadioClient()
            .createInternetRadioStation(streamURL, name, homepageURL)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })
    }

    fun updateInternetRadioStation(
        id: String?,
        name: String?,
        streamURL: String?,
        homepageURL: String?
    ) {
        getSubsonicClientInstance(false)
            .getInternetRadioClient()
            .updateInternetRadioStation(id, streamURL, name, homepageURL)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })
    }

    fun deleteInternetRadioStation(id: String?) {
        getSubsonicClientInstance(false)
            .getInternetRadioClient()
            .deleteInternetRadioStation(id)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                }
            })
    }
}
