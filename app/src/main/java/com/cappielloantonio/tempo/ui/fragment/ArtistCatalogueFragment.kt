package com.cappielloantonio.tempo.ui.fragment

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.PopupMenu
import android.widget.SearchView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentArtistCatalogueBinding
import com.cappielloantonio.tempo.helper.recyclerview.GridItemDecoration
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.ArtistCatalogueAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.viewmodel.ArtistCatalogueViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import java.util.Locale

@UnstableApi
class ArtistCatalogueFragment : Fragment(), ClickCallback {
    private var bind: FragmentArtistCatalogueBinding? = null
    private var activity: MainActivity? = null
    private var artistCatalogueViewModel: ArtistCatalogueViewModel? = null

    private var artistAdapter: ArtistCatalogueAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        initData()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = activity as MainActivity?

        bind = FragmentArtistCatalogueBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()

        initAppBar()
        initArtistCatalogueView()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun initData() {
        artistCatalogueViewModel =
            ViewModelProvider(requireActivity()).get<ArtistCatalogueViewModel>(
                ArtistCatalogueViewModel::class.java
            )
        artistCatalogueViewModel!!.loadArtists()
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.toolbar)

        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity!!.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        bind!!.toolbar.setNavigationOnClickListener(View.OnClickListener { v: View? ->
            hideKeyboard(v!!)
            activity!!.navController.navigateUp()
        })


        bind!!.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout: AppBarLayout?, verticalOffset: Int ->
            if ((bind!!.artistInfoSector.height + verticalOffset) < (2 * ViewCompat.getMinimumHeight(
                    bind!!.toolbar
                ))
            ) {
                bind!!.toolbar.setTitle(R.string.artist_catalogue_title)
            } else {
                bind!!.toolbar.setTitle(R.string.empty_string)
            }
        })
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initArtistCatalogueView() {
        bind!!.artistCatalogueRecyclerView.setLayoutManager(GridLayoutManager(requireContext(), 2))
        bind!!.artistCatalogueRecyclerView.addItemDecoration(GridItemDecoration(2, 20, false))
        bind!!.artistCatalogueRecyclerView.setHasFixedSize(true)

        artistAdapter = ArtistCatalogueAdapter(this)
        artistAdapter!!.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY)
        bind!!.artistCatalogueRecyclerView.setAdapter(artistAdapter)
        artistCatalogueViewModel!!.getArtistList().observe(
            getViewLifecycleOwner(),
            Observer { artistList: MutableList<ArtistID3>? -> artistAdapter!!.setItems(artistList) })

        bind!!.artistCatalogueRecyclerView.setOnTouchListener(OnTouchListener { v: View?, event: MotionEvent? ->
            hideKeyboard(v!!)
            false
        })

        bind!!.artistListSortImageView.setOnClickListener(View.OnClickListener { view: View? ->
            showPopupMenu(
                view,
                R.menu.sort_artist_popup_menu
            )
        })
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView?
        searchView!!.imeOptions = EditorInfo.IME_ACTION_DONE

        searchView.setQueryHint(getString(R.string.filter_artist))
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                // this toast may be overkill...
                Toast.makeText(requireContext(), "Search: " + query, Toast.LENGTH_SHORT).show()
                filterArtists(query)
                return true
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                filterArtists(newText)
                return true
            }
        })

        searchView.setPadding(-32, 0, 0, 0)
    }

    private fun filterArtists(query: String?) {
        val allArtists = artistCatalogueViewModel!!.getArtistList().getValue()

        if (allArtists == null || allArtists.isEmpty()) {
            return
        }

        if (query == null || query.trim { it <= ' ' }.isEmpty()) {
            artistAdapter!!.setItems(allArtists)
        } else {
            val searchQuery = query.lowercase(Locale.getDefault()).trim { it <= ' ' }
            val filteredArtists: MutableList<ArtistID3> = ArrayList<ArtistID3>()

            for (artist in allArtists) {
                if (artist.name != null &&
                    artist.name!!.lowercase(Locale.getDefault()).contains(searchQuery)
                ) {
                    filteredArtists.add(artist)
                }
            }
            artistAdapter!!.setItems(filteredArtists)
        }
    }

    private fun hideKeyboard(view: View) {
        val imm = activity!!.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showPopupMenu(view: View?, menuResource: Int) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuResource, popup.menu)

        popup.setOnMenuItemClickListener(PopupMenu.OnMenuItemClickListener { menuItem: MenuItem? ->
            if (menuItem!!.itemId == R.id.menu_artist_sort_name) {
                artistAdapter!!.sort(Constants.ARTIST_ORDER_BY_NAME)
                return@setOnMenuItemClickListener true
            } else if (menuItem.itemId == R.id.menu_artist_sort_random) {
                artistAdapter!!.sort(Constants.ARTIST_ORDER_BY_RANDOM)
                return@setOnMenuItemClickListener true
            }
            false
        })

        popup.show()
    }

    override fun onArtistClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.artistPageFragment, bundle)
        hideKeyboard(requireView())
    }

    override fun onArtistLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.artistBottomSheetDialog, bundle)
    }

    companion object {
        private const val TAG = "ArtistCatalogueFragment"
    }
}