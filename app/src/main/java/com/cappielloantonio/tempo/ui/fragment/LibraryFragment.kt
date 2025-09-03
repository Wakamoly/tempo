package com.cappielloantonio.tempo.ui.fragment

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentLibraryBinding
import com.cappielloantonio.tempo.helper.recyclerview.CustomLinearSnapHelper
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.interfaces.PlaylistCallback
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Genre
import com.cappielloantonio.tempo.subsonic.models.MusicFolder
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.AlbumAdapter
import com.cappielloantonio.tempo.ui.adapter.ArtistAdapter
import com.cappielloantonio.tempo.ui.adapter.GenreAdapter
import com.cappielloantonio.tempo.ui.adapter.MusicFolderAdapter
import com.cappielloantonio.tempo.ui.adapter.PlaylistHorizontalAdapter
import com.cappielloantonio.tempo.ui.dialog.PlaylistEditorDialog
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.Preferences.isMusicDirectorySectionVisible
import com.cappielloantonio.tempo.viewmodel.LibraryViewModel
import com.google.android.material.appbar.MaterialToolbar
import java.util.Objects

@UnstableApi
class LibraryFragment : Fragment(), ClickCallback {
    private var bind: FragmentLibraryBinding? = null
    private var activity: MainActivity? = null
    private var libraryViewModel: LibraryViewModel? = null

    private var musicFolderAdapter: MusicFolderAdapter? = null
    private var albumAdapter: AlbumAdapter? = null
    private var artistAdapter: ArtistAdapter? = null
    private var genreAdapter: GenreAdapter? = null
    private var playlistHorizontalAdapter: PlaylistHorizontalAdapter? = null

    private var materialToolbar: MaterialToolbar? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        activity = activity as MainActivity?

        bind = FragmentLibraryBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        libraryViewModel =
            ViewModelProvider(requireActivity()).get<LibraryViewModel?>(LibraryViewModel::class.java)

        init()

        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initAppBar()
        initMusicFolderView()
        initAlbumView()
        initArtistView()
        initGenreView()
        initPlaylistView()
    }

    override fun onStart() {
        super.onStart()
        activity!!.setBottomNavigationBarVisibility(true)
    }

    override fun onResume() {
        super.onResume()
        refreshPlaylistView()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun init() {
        bind!!.albumCatalogueTextViewClickable.setOnClickListener(View.OnClickListener { v: View? ->
            activity!!.navController.navigate(
                R.id.action_libraryFragment_to_albumCatalogueFragment
            )
        })
        bind!!.artistCatalogueTextViewClickable.setOnClickListener(View.OnClickListener { v: View? ->
            activity!!.navController.navigate(
                R.id.action_libraryFragment_to_artistCatalogueFragment
            )
        })
        bind!!.genreCatalogueTextViewClickable.setOnClickListener(View.OnClickListener { v: View? ->
            activity!!.navController.navigate(
                R.id.action_libraryFragment_to_genreCatalogueFragment
            )
        })
        bind!!.playlistCatalogueTextViewClickable.setOnClickListener(View.OnClickListener { v: View? ->
            val bundle = Bundle()
            bundle.putString(Constants.PLAYLIST_ALL, Constants.PLAYLIST_ALL)
            activity!!.navController.navigate(
                R.id.action_libraryFragment_to_playlistCatalogueFragment,
                bundle
            )
        })

        bind!!.albumCatalogueSampleTextViewRefreshable.setOnLongClickListener(OnLongClickListener { view: View? ->
            libraryViewModel!!.refreshAlbumSample(getViewLifecycleOwner())
            true
        })
        bind!!.artistCatalogueSampleTextViewRefreshable.setOnLongClickListener(OnLongClickListener { view: View? ->
            libraryViewModel!!.refreshArtistSample(getViewLifecycleOwner())
            true
        })
        bind!!.genreCatalogueSampleTextViewRefreshable.setOnLongClickListener(OnLongClickListener { view: View? ->
            libraryViewModel!!.refreshGenreSample(getViewLifecycleOwner())
            true
        })
        bind!!.playlistCatalogueSampleTextViewRefreshable.setOnLongClickListener(OnLongClickListener { view: View? ->
            libraryViewModel!!.refreshPlaylistSample(getViewLifecycleOwner())
            true
        })
    }

    private fun initAppBar() {
        materialToolbar = bind!!.getRoot().findViewById<MaterialToolbar>(R.id.toolbar)

        activity!!.setSupportActionBar(materialToolbar)
        Objects.requireNonNull<Drawable?>(materialToolbar!!.getOverflowIcon())
            .setTint(requireContext().resources.getColor(R.color.titleTextColor, null))
    }

    private fun initMusicFolderView() {
        if (!isMusicDirectorySectionVisible()) {
            bind!!.libraryMusicFolderSector.visibility = View.GONE
            return
        }

        bind!!.musicFolderRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.musicFolderRecyclerView.setHasFixedSize(true)

        musicFolderAdapter = MusicFolderAdapter(this)
        bind!!.musicFolderRecyclerView.setAdapter(musicFolderAdapter)
        libraryViewModel!!.getMusicFolders(getViewLifecycleOwner())
            .observe(getViewLifecycleOwner(), Observer { musicFolders: MutableList<MusicFolder?>? ->
                if (musicFolders == null) {
                    if (bind != null) bind!!.libraryMusicFolderSector.visibility = View.GONE
                } else {
                    if (bind != null) bind!!.libraryMusicFolderSector.visibility = if (!musicFolders.isEmpty()) View.VISIBLE else View.GONE

                    musicFolderAdapter!!.setItems(musicFolders)
                }
            })
    }

    private fun initAlbumView() {
        bind!!.albumRecyclerView.setLayoutManager(
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        bind!!.albumRecyclerView.setHasFixedSize(true)

        albumAdapter = AlbumAdapter(this)
        bind!!.albumRecyclerView.setAdapter(albumAdapter)
        libraryViewModel!!.getAlbumSample(getViewLifecycleOwner())
            .observe(getViewLifecycleOwner(), Observer { albums: MutableList<AlbumID3?>? ->
                if (albums == null) {
                    if (bind != null) bind!!.libraryAlbumSector.visibility = View.GONE
                } else {
                    if (bind != null) bind!!.libraryAlbumSector.visibility = if (!albums.isEmpty()) View.VISIBLE else View.GONE

                    albumAdapter!!.setItems(albums)
                }
            })

        val albumSnapHelper = CustomLinearSnapHelper()
        albumSnapHelper.attachToRecyclerView(bind!!.albumRecyclerView)
    }

    private fun initArtistView() {
        bind!!.artistRecyclerView.setLayoutManager(
            LinearLayoutManager(
                requireContext(),
                LinearLayoutManager.HORIZONTAL,
                false
            )
        )
        bind!!.artistRecyclerView.setHasFixedSize(true)

        artistAdapter = ArtistAdapter(this, false, false)
        bind!!.artistRecyclerView.setAdapter(artistAdapter)
        libraryViewModel!!.getArtistSample(getViewLifecycleOwner())
            .observe(getViewLifecycleOwner(), Observer { artists: MutableList<ArtistID3?>? ->
                if (artists == null) {
                    if (bind != null) bind!!.libraryArtistSector.visibility = View.GONE
                } else {
                    if (bind != null) bind!!.libraryArtistSector.visibility = if (!artists.isEmpty()) View.VISIBLE else View.GONE

                    artistAdapter!!.setItems(artists)
                }
            })

        val artistSnapHelper = CustomLinearSnapHelper()
        artistSnapHelper.attachToRecyclerView(bind!!.artistRecyclerView)
    }

    private fun initGenreView() {
        bind!!.genreRecyclerView.setLayoutManager(
            GridLayoutManager(
                requireContext(),
                3,
                GridLayoutManager.HORIZONTAL,
                false
            )
        )
        bind!!.genreRecyclerView.setHasFixedSize(true)

        genreAdapter = GenreAdapter(this)
        bind!!.genreRecyclerView.setAdapter(genreAdapter)

        libraryViewModel!!.getGenreSample(getViewLifecycleOwner())
            .observe(getViewLifecycleOwner(), Observer { genres: MutableList<Genre?>? ->
                if (genres == null) {
                    if (bind != null) bind!!.libraryGenresSector.visibility = View.GONE
                } else {
                    if (bind != null) bind!!.libraryGenresSector.visibility = if (!genres.isEmpty()) View.VISIBLE else View.GONE

                    genreAdapter!!.setItems(genres)
                }
            })

        val genreSnapHelper = CustomLinearSnapHelper()
        genreSnapHelper.attachToRecyclerView(bind!!.genreRecyclerView)
    }

    private fun initPlaylistView() {
        bind!!.playlistRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.playlistRecyclerView.setHasFixedSize(true)

        playlistHorizontalAdapter = PlaylistHorizontalAdapter(this)
        bind!!.playlistRecyclerView.setAdapter(playlistHorizontalAdapter)
        libraryViewModel!!.getPlaylistSample(getViewLifecycleOwner())
            .observe(getViewLifecycleOwner(), Observer { playlists: MutableList<Playlist?>? ->
                if (playlists == null) {
                    if (bind != null) bind!!.libraryPlaylistSector.visibility = View.GONE
                } else {
                    if (bind != null) bind!!.libraryPlaylistSector.visibility = if (!playlists.isEmpty()) View.VISIBLE else View.GONE

                    playlistHorizontalAdapter!!.setItems(playlists)
                }
            })
    }

    private fun refreshPlaylistView() {
        val handler = Handler()

        val runnable = Runnable {
            if (view != null && bind != null && libraryViewModel != null) libraryViewModel!!.refreshPlaylistSample(
                getViewLifecycleOwner()
            )
        }

        handler.postDelayed(runnable, 100)
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

    override fun onGenreClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.songListPageFragment, bundle)
    }

    override fun onPlaylistClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.playlistPageFragment, bundle)
    }

    override fun onPlaylistLongClick(bundle: Bundle?) {
        val dialog = PlaylistEditorDialog(object : PlaylistCallback {
            override fun onDismiss() {
                refreshPlaylistView()
            }
        })

        dialog.setArguments(bundle)
        dialog.show(activity!!.supportFragmentManager, null)
    }

    override fun onMusicFolderClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.indexFragment, bundle)
    }

    companion object {
        private const val TAG = "LibraryFragment"
    }
}
