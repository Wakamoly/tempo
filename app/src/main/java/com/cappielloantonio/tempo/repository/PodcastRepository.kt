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
    ): MutableLiveData<MutableList<PodcastChannel>> {
        val livePodcastChannel =
            MutableLiveData<MutableList<PodcastChannel>>(ArrayList())

        getSubsonicClientInstance(false)
            .podcastClient
            .getPodcasts(includeEpisodes, channelId)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            response
                                .body()
                                ?.subsonicResponse
                                ?.podcasts
                                ?.channels
                                ?.let {
                                    livePodcastChannel.value = it.toMutableList()
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

        return livePodcastChannel
    }

    fun getNewestPodcastEpisodes(count: Int): MutableLiveData<MutableList<PodcastEpisode>> {
        val liveNewestPodcastEpisodes =
            MutableLiveData<MutableList<PodcastEpisode>>(ArrayList())

        getSubsonicClientInstance(false)
            .podcastClient
            .getNewestPodcasts(count)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.newestPodcasts != null) {
                            response
                                .body()
                                ?.subsonicResponse
                                ?.newestPodcasts
                                ?.episodes
                                ?.let {
                                    liveNewestPodcastEpisodes.value = it.toMutableList()
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

        return liveNewestPodcastEpisodes
    }

    fun refreshPodcasts() {
        getSubsonicClientInstance(false)
            .podcastClient
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
            .podcastClient
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
            .podcastClient
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
            .podcastClient
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
            .podcastClient
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
