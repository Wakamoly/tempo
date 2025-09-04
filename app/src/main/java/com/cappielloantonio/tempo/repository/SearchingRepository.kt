package com.cappielloantonio.tempo.repository

import androidx.lifecycle.MutableLiveData
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.database.AppDatabase
import com.cappielloantonio.tempo.database.dao.RecentSearchDao
import com.cappielloantonio.tempo.model.RecentSearch
import com.cappielloantonio.tempo.subsonic.base.ApiResponse
import com.cappielloantonio.tempo.subsonic.models.SearchResult2
import com.cappielloantonio.tempo.subsonic.models.SearchResult3
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

@UnstableApi
class SearchingRepository {
    private val recentSearchDao: RecentSearchDao =
        AppDatabase.Companion.instance.recentSearchDao()

    fun search2(query: String?): MutableLiveData<SearchResult2?> {
        val result = MutableLiveData<SearchResult2?>()

        getSubsonicClientInstance(false)
            .searchingClient
            .search3(query, 20, 20, 20)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            result.value = response.body()!!.subsonicResponse.searchResult2
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return result
    }

    fun search3(query: String?): MutableLiveData<SearchResult3?> {
        val result = MutableLiveData<SearchResult3?>()

        getSubsonicClientInstance(false)
            .searchingClient
            .search3(query, 20, 20, 20)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            result.value = response.body()!!.subsonicResponse.searchResult3
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return result
    }

    fun getSuggestions(query: String?): MutableLiveData<MutableList<String>> {
        val suggestions = MutableLiveData<MutableList<String>>()

        getSubsonicClientInstance(false)
            .searchingClient
            .search3(query, 5, 5, 5)
            .enqueue(
                object : Callback<ApiResponse?> {
                    override fun onResponse(
                        call: Call<ApiResponse?>,
                        response: Response<ApiResponse?>,
                    ) {
                        val newSuggestions: MutableList<String> = ArrayList()

                        if (response.isSuccessful && response.body() != null && response.body()!!.subsonicResponse.searchResult3 != null) {
                            if (response
                                    .body()
                                    ?.subsonicResponse
                                    ?.searchResult3
                                    ?.artists != null
                            ) {
                                for (artistID3 in response
                                    .body()!!
                                    .subsonicResponse.searchResult3!!
                                    .artists!!) {
                                    newSuggestions.add(artistID3.name)
                                }
                            }

                            if (response
                                    .body()!!
                                    .subsonicResponse.searchResult3!!
                                    .albums != null
                            ) {
                                for (albumID3 in response
                                    .body()!!
                                    .subsonicResponse.searchResult3!!
                                    .albums!!) {
                                    newSuggestions.add(albumID3.name)
                                }
                            }

                            if (response
                                    .body()!!
                                    .subsonicResponse.searchResult3!!
                                    .songs != null
                            ) {
                                for (song in response
                                    .body()!!
                                    .subsonicResponse.searchResult3!!
                                    .songs!!) {
                                    newSuggestions.add(song.title)
                                }
                            }

                            val hashSet = LinkedHashSet<String>(newSuggestions)
                            val suggestionsWithoutDuplicates = ArrayList(hashSet)

                            suggestions.value = suggestionsWithoutDuplicates
                        }
                    }

                    override fun onFailure(
                        call: Call<ApiResponse?>,
                        t: Throwable,
                    ) {
                    }
                },
            )

        return suggestions
    }

    fun insert(recentSearch: RecentSearch?) {
        val insert = InsertThreadSafe(recentSearchDao, recentSearch)
        val thread = Thread(insert)
        thread.start()
    }

    fun delete(recentSearch: RecentSearch?) {
        val delete = DeleteThreadSafe(recentSearchDao, recentSearch)
        val thread = Thread(delete)
        thread.start()
    }

    val recentSearchSuggestion: MutableList<String>
        get() {
            val recent: MutableList<String> = ArrayList()

            val suggestionsThread = RecentThreadSafe(recentSearchDao)
            val thread = Thread(suggestionsThread)
            thread.start()

            try {
                thread.join()
                suggestionsThread.recent?.let {
                    recent.addAll(it)
                }
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }

            return recent
        }

    private class DeleteThreadSafe(
        private val recentSearchDao: RecentSearchDao,
        private val recentSearch: RecentSearch?,
    ) : Runnable {
        override fun run() {
            recentSearchDao.delete(recentSearch)
        }
    }

    private class InsertThreadSafe(
        private val recentSearchDao: RecentSearchDao,
        private val recentSearch: RecentSearch?,
    ) : Runnable {
        override fun run() {
            recentSearchDao.insert(recentSearch)
        }
    }

    private class RecentThreadSafe(
        private val recentSearchDao: RecentSearchDao,
    ) : Runnable {
        var recent: MutableList<String>? = ArrayList()
            private set

        override fun run() {
            recent = recentSearchDao.recent
        }
    }
}
