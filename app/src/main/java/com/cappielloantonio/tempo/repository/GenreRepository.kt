package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.Genre
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.stream.Collectors
import kotlin.math.min

class GenreRepository {
    fun getGenres(
        random: Boolean,
        size: Int,
    ): MutableLiveData<MutableList<Genre>> {
        val genres = MutableLiveData<MutableList<Genre>>()

        getSubsonicClientInstance(false)
            .browsingClient
            .genres
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful) {
                            val genreList: MutableList<Genre> =
                                (
                                        response
                                            .body()
                                            ?.subsonicResponse
                                            ?.genres
                                            ?.genres
                                            ?: emptyList()
                                        ).toMutableList()

                            if (genreList.isEmpty()) {
                                genres.value = mutableListOf()
                                return
                            }

                            if (random) {
                                genreList.shuffle()
                            }

                            if (size != -1) {
                                genres.value = genreList.subList(0, min(size, genreList.size))
                            } else {
                                genres.value =
                                    genreList
                                        .stream()
                                        .sorted { o1: Genre, o2: Genre ->
                                            val g1 = o1.genre ?: ""
                                            val g2 = o2.genre ?: ""
                                            g1.compareTo(g2)
                                        }.collect(
                                            Collectors.toList(),
                                        )
                            }
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                        // TODO (BA, 9/4/25)
                        genres.value = mutableListOf()
                    }
                },
            )

        return genres
    }
}
