package com.cappielloantonio.tempo.github

import com.cappielloantonio.tempo.github.api.release.ReleaseClient

class Github {
    var releaseClient: ReleaseClient? = null
        get() {
            if (field == null) {
                field = ReleaseClient(this)
            }

            return field
        }
        private set

    val url: String
        get() = "https://api.github.com/"

    companion object {
        const val OWNER: String = "Wakamoly" // CappielloAntonio, eddyizm
        const val REPO: String = "Tempo"
    }
}
