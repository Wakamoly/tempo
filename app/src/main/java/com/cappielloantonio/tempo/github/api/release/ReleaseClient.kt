package com.cappielloantonio.tempo.github.api.release

import android.util.Log
import com.cappielloantonio.tempo.github.Github
import com.cappielloantonio.tempo.github.GithubRetrofitClient
import com.cappielloantonio.tempo.github.models.LatestRelease
import retrofit2.Call

class ReleaseClient(github: Github) {
    private val releaseService: ReleaseService =
        GithubRetrofitClient(github).retrofit.create(ReleaseService::class.java)

    val latestRelease: Call<LatestRelease?>?
        get() {
            Log.d(TAG, "getLatestRelease()")
            return releaseService.getLatestRelease(
                Github.Companion.OWNER,
                Github.Companion.REPO
            )
        }

    companion object {
        private const val TAG = "ReleaseClient"
    }
}
