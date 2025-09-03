package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.Share
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SharingRepository {
    val shares: MutableLiveData<MutableList<Share?>?>
        get() {
            val shares =
                MutableLiveData<MutableList<Share?>?>(ArrayList<Share?>())

            getSubsonicClientInstance(false)
                .getSharingClient()
                .getShares()
                .enqueue(object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.shares != null && response.body()!!.subsonicResponse.shares!!.shares != null) {
                            shares.setValue(response.body()!!.subsonicResponse.shares!!.shares)
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable
                    ) {
                    }
                })

            return shares
        }

    fun createShare(id: String?, description: String?, expires: Long?): MutableLiveData<Share?> {
        val share = MutableLiveData<Share?>()

        getSubsonicClientInstance(false)
            .getSharingClient()
            .createShare(id, description, expires)
            .enqueue(object : Callback<ApiResponse?> {
                override fun onResponse(
                    call: Call<ApiResponse?>,
                    response: Response<ApiResponse?>
                ) {
                    if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.shares != null && response.body()!!.subsonicResponse.shares!!.shares != null && response.body()!!.subsonicResponse.shares!!.shares!!.get(
                            0
                        ) != null
                    ) {
                        share.value = response.body()!!.subsonicResponse.shares!!.shares!!.get(0)
                    } else {
                        share.value = null
                    }
                }

                override fun onFailure(call: Call<ApiResponse?>, t: Throwable) {
                    share.value = null
                }
            })

        return share
    }

    fun updateShare(id: String?, description: String?, expires: Long?) {
        getSubsonicClientInstance(false)
            .getSharingClient()
            .updateShare(id, description, expires)
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

    fun deleteShare(id: String?) {
        getSubsonicClientInstance(false)
            .getSharingClient()
            .deleteShare(id)
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
