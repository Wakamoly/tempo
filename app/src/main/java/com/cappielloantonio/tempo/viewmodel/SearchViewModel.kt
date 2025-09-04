package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import com.cappielloantonio.tempo.model.RecentSearch
import com.cappielloantonio.tempo.repository.SearchingRepository
import com.cappielloantonio.tempo.subsonic.models.SearchResult2
import com.cappielloantonio.tempo.subsonic.models.SearchResult3

class SearchViewModel(
    application: Application,
) : AndroidViewModel(application) {
    var query: String? = ""
        set(query) {
            field = query

            if (!query!!.isEmpty()) {
                insertNewSearch(query)
            }
        }

    private val searchingRepository: SearchingRepository

    init {
        searchingRepository = SearchingRepository()
    }

    fun search2(title: String?): LiveData<SearchResult2?>? = searchingRepository.search2(title)

    fun search3(title: String?): LiveData<SearchResult3?>? = searchingRepository.search3(title)

    fun insertNewSearch(search: String) {
        searchingRepository.insert(RecentSearch(search))
    }

    fun deleteRecentSearch(search: String) {
        searchingRepository.delete(RecentSearch(search))
    }

    fun getSearchSuggestion(query: String?): LiveData<MutableList<String?>?>? = searchingRepository.getSuggestions(query)

    val recentSearchSuggestion: MutableList<String?>
        get() {
            val suggestions =
                ArrayList<String?>()
            suggestions.addAll(searchingRepository.recentSearchSuggestion)

            return suggestions
        }

    companion object {
        private const val TAG = "SearchViewModel"
    }
}
