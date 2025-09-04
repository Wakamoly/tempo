package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.TextView.OnEditorActionListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentSearchBinding
import com.cappielloantonio.tempo.helper.recyclerview.CustomLinearSnapHelper
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.SearchResult3
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.AlbumAdapter
import com.cappielloantonio.tempo.ui.adapter.ArtistAdapter
import com.cappielloantonio.tempo.ui.adapter.SongHorizontalAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.viewmodel.SearchViewModel
import com.google.common.util.concurrent.ListenableFuture

@UnstableApi
class SearchFragment :
    Fragment(),
    ClickCallback {
    private var bind: FragmentSearchBinding? = null
    private var activity: MainActivity? = null
    private var searchViewModel: SearchViewModel? = null

    private var artistAdapter: ArtistAdapter? = null
    private var albumAdapter: AlbumAdapter? = null
    private var songHorizontalAdapter: SongHorizontalAdapter? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        activity = activity as MainActivity?

        bind = FragmentSearchBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        searchViewModel =
            ViewModelProvider(requireActivity()).get<SearchViewModel>(SearchViewModel::class.java)

        initSearchResultView()
        initSearchView()
        inputFocus()

        return view
    }

    override fun onStart() {
        super.onStart()
        initializeMediaBrowser()
    }

    override fun onStop() {
        releaseMediaBrowser()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun initSearchResultView() {
        // Artists
        bind!!.searchResultArtistRecyclerView.setLayoutManager(
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false,
            ),
        )
        bind!!.searchResultArtistRecyclerView.setHasFixedSize(true)

        artistAdapter = ArtistAdapter(this, false, false)
        bind!!.searchResultArtistRecyclerView.setAdapter(artistAdapter)

        val artistSnapHelper = CustomLinearSnapHelper()
        artistSnapHelper.attachToRecyclerView(bind!!.searchResultArtistRecyclerView)

        // Albums
        bind!!.searchResultAlbumRecyclerView.setLayoutManager(
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false,
            ),
        )
        bind!!.searchResultAlbumRecyclerView.setHasFixedSize(true)

        albumAdapter = AlbumAdapter(this)
        bind!!.searchResultAlbumRecyclerView.setAdapter(albumAdapter)

        val albumSnapHelper = CustomLinearSnapHelper()
        albumSnapHelper.attachToRecyclerView(bind!!.searchResultAlbumRecyclerView)

        // Songs
        bind!!.searchResultTracksRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.searchResultTracksRecyclerView.setHasFixedSize(true)

        songHorizontalAdapter = SongHorizontalAdapter(this, true, false, null)
        bind!!.searchResultTracksRecyclerView.setAdapter(songHorizontalAdapter)
    }

    private fun initSearchView() {
        setRecentSuggestions()

        bind!!
            .searchView
            .getEditText()
            .setOnEditorActionListener(
                OnEditorActionListener { textView: TextView?, actionId: Int, keyEvent: KeyEvent? ->
                    val query = bind!!.searchView.text.toString()
                    if (isQueryValid(query)) {
                        search(query)
                        return@setOnEditorActionListener true
                    }
                    false
                },
            )

        bind!!
            .searchView
            .getEditText()
            .addTextChangedListener(
                object : TextWatcher {
                    override fun beforeTextChanged(
                        charSequence: CharSequence?,
                        start: Int,
                        count: Int,
                        after: Int,
                    ) {
                    }

                    override fun onTextChanged(
                        charSequence: CharSequence,
                        start: Int,
                        before: Int,
                        count: Int,
                    ) {
                        if (start + count > 1) {
                            setSearchSuggestions(charSequence.toString())
                        } else {
                            setRecentSuggestions()
                        }
                    }

                    override fun afterTextChanged(editable: Editable?) {
                    }
                },
            )
    }

    fun setRecentSuggestions() {
        bind!!.searchViewSuggestionContainer.removeAllViews()

        for (suggestion in searchViewModel!!.getRecentSearchSuggestion()) {
            val view =
                LayoutInflater
                    .from(bind!!.searchViewSuggestionContainer.context)
                    .inflate(
                        R.layout.item_search_suggestion,
                        bind!!.searchViewSuggestionContainer,
                        false,
                    )

            val leadingImageView = view.findViewById<ImageView>(R.id.search_suggestion_icon)
            val titleView = view.findViewById<TextView>(R.id.search_suggestion_title)
            val tailingImageView = view.findViewById<ImageView>(R.id.search_suggestion_delete_icon)

            leadingImageView.setImageDrawable(
                resources.getDrawable(
                    R.drawable.ic_history,
                    null,
                ),
            )
            titleView.setText(suggestion)

            view.setOnClickListener(View.OnClickListener { v: View? -> search(suggestion) })

            tailingImageView.setOnClickListener(
                View.OnClickListener { v: View? ->
                    searchViewModel!!.deleteRecentSearch(suggestion)
                    setRecentSuggestions()
                },
            )

            bind!!.searchViewSuggestionContainer.addView(view)
        }
    }

    fun setSearchSuggestions(query: String?) {
        searchViewModel!!
            .getSearchSuggestion(query)
            .observe(
                getViewLifecycleOwner(),
                Observer { suggestions: MutableList<String>? ->
                    bind!!.searchViewSuggestionContainer.removeAllViews()
                    for (suggestion in suggestions!!) {
                        val view =
                            LayoutInflater
                                .from(bind!!.searchViewSuggestionContainer.context)
                                .inflate(
                                    R.layout.item_search_suggestion,
                                    bind!!.searchViewSuggestionContainer,
                                    false,
                                )

                        val leadingImageView = view.findViewById<ImageView>(R.id.search_suggestion_icon)
                        val titleView = view.findViewById<TextView>(R.id.search_suggestion_title)
                        val tailingImageView =
                            view.findViewById<ImageView>(R.id.search_suggestion_delete_icon)

                        leadingImageView.setImageDrawable(
                            resources.getDrawable(
                                R.drawable.ic_search,
                                null,
                            ),
                        )
                        titleView.text = suggestion
                        tailingImageView.setVisibility(View.GONE)

                        view.setOnClickListener(View.OnClickListener { v: View? -> search(suggestion) })

                        bind!!.searchViewSuggestionContainer.addView(view)
                    }
                },
            )
    }

    fun search(query: String) {
        searchViewModel!!.setQuery(query)
        bind!!.searchBar.setText(query)
        bind!!.searchView.hide()
        performSearch(query)
    }

    private fun performSearch(query: String?) {
        searchViewModel!!
            .search3(query)
            .observe(
                getViewLifecycleOwner(),
                Observer { result: SearchResult3? ->
                    if (bind != null) {
                        if (result!!.artists != null) {
                            bind!!.searchArtistSector.visibility = if (!result.artists!!.isEmpty()) View.VISIBLE else View.GONE
                            artistAdapter!!.setItems(result.artists)
                        } else {
                            artistAdapter!!.setItems(mutableListOf<ArtistID3?>())
                            bind!!.searchArtistSector.visibility = View.GONE
                        }

                        if (result.albums != null) {
                            bind!!.searchAlbumSector.visibility = if (!result.albums!!.isEmpty()) View.VISIBLE else View.GONE
                            albumAdapter!!.setItems(result.albums)
                        } else {
                            albumAdapter!!.setItems(mutableListOf<AlbumID3?>())
                            bind!!.searchAlbumSector.visibility = View.GONE
                        }

                        if (result.songs != null) {
                            bind!!.searchSongSector.visibility = if (!result.songs!!.isEmpty()) View.VISIBLE else View.GONE
                            songHorizontalAdapter!!.setItems(result.songs)
                        } else {
                            songHorizontalAdapter!!.setItems(mutableListOf<Child?>())
                            bind!!.searchSongSector.visibility = View.GONE
                        }
                    }
                },
            )

        bind!!.searchResultLayout.visibility = View.VISIBLE
    }

    private fun isQueryValid(query: String): Boolean = query != "" && query.trim { it <= ' ' }.length > 2

    private fun inputFocus() {
        bind!!.searchView.show()
    }

    private fun initializeMediaBrowser() {
        mediaBrowserListenableFuture =
            MediaBrowser
                .Builder(
                    requireContext(),
                    SessionToken(
                        requireContext(),
                        ComponentName(requireContext(), MediaService::class.java),
                    ),
                ).buildAsync()
    }

    private fun releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture)
    }

    override fun onMediaClick(bundle: Bundle) {
        MediaManager.startQueue(
            mediaBrowserListenableFuture,
            bundle.getParcelableArrayList<Child?>(
                Constants.TRACKS_OBJECT,
            ),
            bundle.getInt(Constants.ITEM_POSITION),
        )
        activity!!.setBottomSheetInPeek(true)
    }

    override fun onMediaLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.songBottomSheetDialog, bundle)
    }

    override fun onAlbumClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.albumPageFragment, bundle)
    }

    override fun onAlbumLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.albumBottomSheetDialog, bundle)
    }

    override fun onArtistClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.artistPageFragment, bundle)
    }

    override fun onArtistLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.artistBottomSheetDialog, bundle)
    }

    companion object {
        private const val TAG = "SearchFragment"
    }
}
