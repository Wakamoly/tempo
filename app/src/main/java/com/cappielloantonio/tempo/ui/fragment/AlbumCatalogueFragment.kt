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
import androidx.annotation.OptIn
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentAlbumCatalogueBinding
import com.cappielloantonio.tempo.helper.recyclerview.GridItemDecoration
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.AlbumCatalogueAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.viewmodel.AlbumCatalogueViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener

@OptIn(markerClass = UnstableApi::class)
class AlbumCatalogueFragment :
    Fragment(),
    ClickCallback {
    private var bind: FragmentAlbumCatalogueBinding? = null
    private var activity: MainActivity? = null
    private var albumCatalogueViewModel: AlbumCatalogueViewModel? = null

    private var albumAdapter: AlbumCatalogueAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)

        initData()
    }

    override fun onDestroy() {
        super.onDestroy()
        albumCatalogueViewModel!!.stopLoading()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        activity = activity as MainActivity?

        bind = FragmentAlbumCatalogueBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()

        initAppBar()
        initAlbumCatalogueView()
        initProgressLoader()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun initData() {
        albumCatalogueViewModel =
            ViewModelProvider(requireActivity()).get<AlbumCatalogueViewModel>(
                AlbumCatalogueViewModel::class.java,
            )
        albumCatalogueViewModel!!.loadAlbums()
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.toolbar)

        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity!!.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        bind!!.toolbar.setNavigationOnClickListener(
            View.OnClickListener { v: View? ->
                hideKeyboard(v!!)
                activity!!.navController.navigateUp()
            },
        )

        bind!!.appBarLayout.addOnOffsetChangedListener(
            OnOffsetChangedListener { appBarLayout: AppBarLayout?, verticalOffset: Int ->
                if ((bind!!.albumInfoSector.height + verticalOffset) < (
                        2 *
                            ViewCompat.getMinimumHeight(
                                bind!!.toolbar,
                            )
                    )
                ) {
                    bind!!.toolbar.setTitle(R.string.album_catalogue_title)
                } else {
                    bind!!.toolbar.setTitle(R.string.empty_string)
                }
            },
        )
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun initAlbumCatalogueView() {
        bind!!.albumCatalogueRecyclerView.setLayoutManager(GridLayoutManager(requireContext(), 2))
        bind!!.albumCatalogueRecyclerView.addItemDecoration(GridItemDecoration(2, 20, false))
        bind!!.albumCatalogueRecyclerView.setHasFixedSize(true)

        albumAdapter = AlbumCatalogueAdapter(this, true)
        albumAdapter!!.setStateRestorationPolicy(RecyclerView.Adapter.StateRestorationPolicy.PREVENT_WHEN_EMPTY)
        bind!!.albumCatalogueRecyclerView.setAdapter(albumAdapter)
        albumCatalogueViewModel!!.getAlbumList().observe(
            getViewLifecycleOwner(),
            Observer { albums: MutableList<AlbumID3?>? -> albumAdapter!!.setItems(albums) },
        )

        bind!!.albumCatalogueRecyclerView.setOnTouchListener(
            OnTouchListener { v: View?, event: MotionEvent? ->
                hideKeyboard(v!!)
                false
            },
        )

        bind!!.albumListSortImageView.setOnClickListener(
            View.OnClickListener { view: View? ->
                showPopupMenu(
                    view,
                    R.menu.sort_album_popup_menu,
                )
            },
        )
    }

    private fun initProgressLoader() {
        albumCatalogueViewModel!!
            .getLoadingStatus()
            .observe(
                getViewLifecycleOwner(),
                Observer { isLoading: Boolean? ->
                    if (isLoading) {
                        bind!!.albumListSortImageView.setEnabled(false)
                        bind!!.albumListProgressLoader.visibility = View.VISIBLE
                    } else {
                        bind!!.albumListSortImageView.setEnabled(true)
                        bind!!.albumListProgressLoader.visibility = View.GONE
                    }
                },
            )
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        inflater.inflate(R.menu.toolbar_menu, menu)

        val searchItem = menu.findItem(R.id.action_search)

        val searchView = searchItem.actionView as SearchView?
        searchView!!.imeOptions = EditorInfo.IME_ACTION_DONE
        searchView.setOnQueryTextListener(
            object : SearchView.OnQueryTextListener {
                override fun onQueryTextSubmit(query: String?): Boolean {
                    searchView.clearFocus()
                    return false
                }

                override fun onQueryTextChange(newText: String?): Boolean {
                    albumAdapter!!.filter.filter(newText)
                    return false
                }
            },
        )

        searchView.setPadding(-32, 0, 0, 0)
    }

    private fun hideKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun showPopupMenu(
        view: View?,
        menuResource: Int,
    ) {
        val popup = PopupMenu(requireContext(), view)
        popup.menuInflater.inflate(menuResource, popup.menu)

        popup.setOnMenuItemClickListener(
            PopupMenu.OnMenuItemClickListener { menuItem: MenuItem? ->
                if (menuItem!!.itemId == R.id.menu_album_sort_name) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_NAME)
                    return@setOnMenuItemClickListener true
                } else if (menuItem.itemId == R.id.menu_album_sort_artist) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_ARTIST)
                    return@setOnMenuItemClickListener true
                } else if (menuItem.itemId == R.id.menu_album_sort_year) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_YEAR)
                    return@setOnMenuItemClickListener true
                } else if (menuItem.itemId == R.id.menu_album_sort_random) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_RANDOM)
                    return@setOnMenuItemClickListener true
                } else if (menuItem.itemId == R.id.menu_album_sort_recently_added) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_RECENTLY_ADDED)
                    return@setOnMenuItemClickListener true
                } else if (menuItem.itemId == R.id.menu_album_sort_recently_played) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_RECENTLY_PLAYED)
                    return@setOnMenuItemClickListener true
                } else if (menuItem.itemId == R.id.menu_album_sort_most_played) {
                    albumAdapter!!.sort(Constants.ALBUM_ORDER_BY_MOST_PLAYED)
                    return@setOnMenuItemClickListener true
                }
                false
            },
        )

        popup.show()
    }

    override fun onAlbumClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.albumPageFragment, bundle)
        hideKeyboard(requireView())
    }

    override fun onAlbumLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.albumBottomSheetDialog, bundle)
    }

    companion object {
        private const val TAG = "ArtistCatalogueFragment"
    }
}
