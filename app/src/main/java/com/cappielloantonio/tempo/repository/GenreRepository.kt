package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.Genre
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.util.Collections
import java.util.stream.Collectors
import kotlin.math.min

class GenreRepository {
    fun getGenres(
        random: Boolean,
        size: Int,
    ): MutableLiveData<MutableList<Genre?>?> {
        val genres = MutableLiveData<MutableList<Genre?>?>()

        getSubsonicClientInstance(false)
            .getBrowsingClient()
            .getGenres()
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse != null &&
                            response.body()!!.subsonicResponse.genres != null
                        ) {
                            val genreList: MutableList<Genre?>? =
                                response
                                    .body()!!
                                    .subsonicResponse.genres!!
                                    .genres

                            if (genreList == null || genreList.isEmpty()) {
                                genres.value = mutableListOf<Genre?>()
                                return
                            }

                            if (random) {
                                Collections.shuffle(genreList)
                            }

                            if (size != -1) {
                                genres.value = genreList.subList(0, min(size, genreList.size))
                            } else {
                                genres.value =
                                    genreList
                                        .stream()
                                        .sorted(Comparator.comparing<Genre?, String?>(Genre::genre))
                                        .collect(
                                            Collectors.toList(),
                                        )
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

        return genres
    }
}
