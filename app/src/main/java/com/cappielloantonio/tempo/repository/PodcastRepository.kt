package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.PodcastChannel
import com.cappielloantonio.tempo.subsonic.models.PodcastEpisode
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class PodcastRepository {
    fun getPodcastChannels(
        includeEpisodes: Boolean,
        channelId: String?,
    ): MutableLiveData<MutableList<PodcastChannel?>?> {
        val livePodcastChannel =
            MutableLiveData<MutableList<PodcastChannel?>?>(ArrayList<PodcastChannel?>())

        getSubsonicClientInstance(false)
            .getPodcastClient()
            .getPodcasts(includeEpisodes, channelId)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.podcasts != null) {
                            livePodcastChannel.setValue(
                                response
                                    .body()!!
                                    .subsonicResponse.podcasts!!
                                    .channels,
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return livePodcastChannel
    }

    fun getNewestPodcastEpisodes(count: Int): MutableLiveData<MutableList<PodcastEpisode?>?> {
        val liveNewestPodcastEpisodes =
            MutableLiveData<MutableList<PodcastEpisode?>?>(ArrayList<PodcastEpisode?>())

        getSubsonicClientInstance(false)
            .getPodcastClient()
            .getNewestPodcasts(count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.newestPodcasts != null) {
                            liveNewestPodcastEpisodes.setValue(
                                response
                                    .body()!!
                                    .subsonicResponse.newestPodcasts!!
                                    .episodes,
                            )
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return liveNewestPodcastEpisodes
    }

    fun refreshPodcasts() {
        getSubsonicClientInstance(false)
            .getPodcastClient()
            .refreshPodcasts()
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

    fun createPodcastChannel(url: String?) {
        getSubsonicClientInstance(false)
            .getPodcastClient()
            .createPodcastChannel(url)
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

    fun deletePodcastChannel(channelId: String?) {
        getSubsonicClientInstance(false)
            .getPodcastClient()
            .deletePodcastChannel(channelId)
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

    fun deletePodcastEpisode(episodeId: String?) {
        getSubsonicClientInstance(false)
            .getPodcastClient()
            .deletePodcastEpisode(episodeId)
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

    fun downloadPodcastEpisode(episodeId: String?) {
        getSubsonicClientInstance(false)
            .getPodcastClient()
            .downloadPodcastEpisode(episodeId)
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

    companion object {
        private const val TAG = "PodcastRepository"
    }
}
