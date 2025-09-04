package com.cappielloantonio.tempo.subsonic.api.podcast

import android.util.Log
import com.cappielloantonio.tempo.subsonic.RetrofitClient
import com.cappielloantonio.tempo.subsonic.Subsonic
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import retrofit2.Call

class PodcastClient(private val subsonic: Subsonic) {
    private val podcastService: PodcastService

    init {
        this.podcastService =
            RetrofitClient(subsonic).retrofit.create<PodcastService>(PodcastService::class.java)
    }

    fun getPodcasts(includeEpisodes: Boolean, channelId: String?): Call<ApiResponse> {
        Log.d(TAG, "getPodcasts()")
        return podcastService.getPodcasts(subsonic.getParams(), includeEpisodes, channelId)
    }

    fun getNewestPodcasts(count: Int): Call<ApiResponse> {
        Log.d(TAG, "getNewestPodcasts()")
        return podcastService.getNewestPodcasts(subsonic.getParams(), count)
    }

    fun refreshPodcasts(): Call<ApiResponse> {
        Log.d(TAG, "refreshPodcasts()")
        return podcastService.refreshPodcasts(subsonic.getParams())
    }

    fun createPodcastChannel(url: String?): Call<ApiResponse> {
        Log.d(TAG, "createPodcastChannel()")
        return podcastService.createPodcastChannel(subsonic.getParams(), url)
    }

    fun deletePodcastChannel(channelId: String?): Call<ApiResponse> {
        Log.d(TAG, "deletePodcastChannel()")
        return podcastService.deletePodcastChannel(subsonic.getParams(), channelId)
    }

    fun deletePodcastEpisode(episodeId: String?): Call<ApiResponse> {
        Log.d(TAG, "deletePodcastEpisode()")
        return podcastService.deletePodcastEpisode(subsonic.getParams(), episodeId)
    }

    fun downloadPodcastEpisode(episodeId: String?): Call<ApiResponse> {
        Log.d(TAG, "downloadPodcastEpisode()")
        return podcastService.downloadPodcastEpisode(subsonic.getParams(), episodeId)
    }

    companion object {
        private const val TAG = "PodcastClient"
    }
}
