package com.cappielloantonio.tempo.subsonic.api.system

import android.util.Log
import com.cappielloantonio.tempo.subsonic.RetrofitClient
import com.cappielloantonio.tempo.subsonic.Subsonic
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import retrofit2.Call

class SystemClient(private val subsonic: Subsonic) {
    private val systemService: SystemService

    init {
        this.systemService =
            RetrofitClient(subsonic).retrofit.create<SystemService>(SystemService::class.java)
    }

    fun ping(): Call<ApiResponse?>? {
        Log.d(TAG, "ping()")
        return systemService.ping(subsonic.getParams())
    }

    val license: Call<ApiResponse?>?
        get() {
            Log.d(TAG, "getLicense()")
            return systemService.getLicense(subsonic.getParams())
        }

    val openSubsonicExtensions: Call<ApiResponse?>?
        get() {
            Log.d(TAG, "getOpenSubsonicExtensions()")
            return systemService.getOpenSubsonicExtensions(subsonic.getParams())
        }

    companion object {
        private const val TAG = "SystemClient"
    }
}
