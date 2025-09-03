package com.cappielloantonio.tempo.subsonic.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import com.cappielloantonio.tempo.App.Companion.getContext
import okhttp3.Interceptor

class CacheUtil(
// 60 seconds
    private var maxAge: Int, // 60 * 60 * 24 * 30 = 30 days (60 seconds * 60 minutes * 24 hours * 30 days)
    private var maxStale: Int
) {
    var onlineInterceptor: Interceptor = Interceptor { chain: Interceptor.Chain? ->
        val response = chain!!.proceed(chain.request())
        response.newBuilder()
            .header("Cache-Control", "public, max-age=" + maxAge)
            .removeHeader("Pragma")
            .build()
    }

    var offlineInterceptor: Interceptor = Interceptor { chain: Interceptor.Chain? ->
        var request = chain!!.request()
        if (!this.isConnected) {
            request = request.newBuilder()
                .header("Cache-Control", "public, only-if-cached, max-stale=" + maxStale)
                .removeHeader("Pragma")
                .build()
        }
        chain.proceed(request)
    }

    private val isConnected: Boolean
        get() {
            val connectivityManager =
                getContext()
                    .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

            if (connectivityManager != null) {
                val network = connectivityManager.activeNetwork

                if (network != null) {
                    val capabilities =
                        connectivityManager.getNetworkCapabilities(network)

                    if (capabilities != null) {
                        return capabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                    }
                }
            }

            return false
        }
}
