package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.content.Context
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.RoomDatabase.Builder.build
import com.bumptech.glide.load.resource.bitmap.GranularRoundedCorners
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentPlaylistPageBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.SongHorizontalAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.viewmodel.PlaylistPageViewModel
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Collections
import java.util.Objects
import java.util.stream.Collectors

@UnstableApi
class PlaylistPageFragment :
    Fragment(),
    ClickCallback {
    private var bind: FragmentPlaylistPageBinding? = null
    private var activity: MainActivity? = null
    private var playlistPageViewModel: PlaylistPageViewModel? = null

    private var songHorizontalAdapter: SongHorizontalAdapter? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        inflater.inflate(R.menu.playlist_page_menu, menu)

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
                    songHorizontalAdapter!!.filter.filter(newText)
                    return false
                }
            },
        )

        searchView.setPadding(-32, 0, 0, 0)

        initMenuOption(menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        activity = activity as MainActivity?

        bind = FragmentPlaylistPageBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        playlistPageViewModel =
            ViewModelProvider(requireActivity()).get<PlaylistPageViewModel>(PlaylistPageViewModel::class.java)

        init()
        initAppBar()
        initMusicButton()
        initBackCover()
        initSongsView()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_download_playlist) {
            playlistPageViewModel!!
                .getPlaylistSongLiveList()
                .observe(
                    getViewLifecycleOwner(),
                    Observer { songs: MutableList<Child?>? ->
                        if (isVisible && activity != null) {
                            DownloadUtil.getDownloadTracker(requireContext()).download(
                                MappingUtil.mapDownloads(songs),
                                songs!!
                                    .stream()
                                    .map<Download?> { child: Child? ->
                                        val toDownload = Download(child!!)
                                        toDownload.playlistId = playlistPageViewModel!!.getPlaylist().id
                                        toDownload.playlistName = playlistPageViewModel!!.getPlaylist().name
                                        toDownload
                                    }.collect(Collectors.toList()),
                            )
                        }
                    },
                )
            return true
        } else if (item.itemId == R.id.action_pin_playlist) {
            playlistPageViewModel!!.setPinned(true)
            return true
        } else if (item.itemId == R.id.action_unpin_playlist) {
            playlistPageViewModel!!.setPinned(false)
            return true
        }

        return false
    }

    private fun init() {
        playlistPageViewModel!!.setPlaylist(requireArguments().getParcelable<Playlist?>(Constants.PLAYLIST_OBJECT))
    }

    private fun initMenuOption(menu: Menu) {
        playlistPageViewModel!!
            .isPinned(getViewLifecycleOwner())
            .observe(
                getViewLifecycleOwner(),
                Observer { isPinned: Boolean? ->
                    menu.findItem(R.id.action_unpin_playlist).isVisible = isPinned!!
                    menu.findItem(R.id.action_pin_playlist).isVisible = !isPinned
                },
            )
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.animToolbar)

        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity!!.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        bind!!.animToolbar.setTitle(playlistPageViewModel!!.getPlaylist().name)

        bind!!.playlistNameLabel.text = playlistPageViewModel!!.getPlaylist().name
        bind!!.playlistSongCountLabel.text =
            getString(
                R.string.playlist_song_count,
                playlistPageViewModel!!.getPlaylist().songCount,
            )
        bind!!.playlistDurationLabel.text =
            getString(
                R.string.playlist_duration,
                MusicUtil.getReadableDurationString(
                    playlistPageViewModel!!.getPlaylist().duration,
                    false,
                ),
            )

        bind!!.animToolbar.setNavigationOnClickListener(
            View.OnClickListener { v: View? ->
                hideKeyboard(v!!)
                activity!!.navController.navigateUp()
            },
        )

        Objects
            .requireNonNull<Drawable?>(bind!!.animToolbar.getOverflowIcon())
            .setTint(requireContext().resources.getColor(R.color.titleTextColor, null))
    }

    private fun hideKeyboard(view: View) {
        val imm =
            requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, 0)
    }

    private fun initMusicButton() {
        playlistPageViewModel!!
            .getPlaylistSongLiveList()
            .observe(
                getViewLifecycleOwner(),
                Observer { songs: MutableList<Child?>? ->
                    if (bind != null) {
                        bind!!.playlistPagePlayButton.setOnClickListener(
                            View.OnClickListener { v: View? ->
                                MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                                activity!!.setBottomSheetInPeek(true)
                            },
                        )

                        bind!!.playlistPageShuffleButton.setOnClickListener(
                            View.OnClickListener { v: View? ->
                                Collections.shuffle(songs)
                                MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                                activity!!.setBottomSheetInPeek(true)
                            },
                        )
                    }
                },
            )
    }

    private fun initBackCover() {
        playlistPageViewModel!!
            .getPlaylistSongLiveList()
            .observe(
                requireActivity(),
                Observer { songs: MutableList<Child?>? ->
                    if (bind != null && songs != null && !songs.isEmpty()) {
                        Collections.shuffle(songs)

                        // Pic top-left
                        CustomGlideRequest.Builder.Companion
                            .from(
                                requireContext(),
                                if (!songs.isEmpty()) songs.get(0)!!.coverArtId else playlistPageViewModel!!.getPlaylist().coverArtId,
                                CustomGlideRequest.ResourceType.Song,
                            ).build()
                            .transform(
                                GranularRoundedCorners(
                                    CustomGlideRequest.CORNER_RADIUS.toFloat(),
                                    0f,
                                    0f,
                                    0f,
                                ),
                            ).into(bind!!.playlistCoverImageViewTopLeft)

                        // Pic top-right
                        CustomGlideRequest.Builder.Companion
                            .from(
                                requireContext(),
                                if (songs.size > 1) songs.get(1)!!.coverArtId else playlistPageViewModel!!.getPlaylist().coverArtId,
                                CustomGlideRequest.ResourceType.Song,
                            ).build()
                            .transform(
                                GranularRoundedCorners(
                                    0f,
                                    CustomGlideRequest.CORNER_RADIUS.toFloat(),
                                    0f,
                                    0f,
                                ),
                            ).into(bind!!.playlistCoverImageViewTopRight)

                        // Pic bottom-left
                        CustomGlideRequest.Builder.Companion
                            .from(
                                requireContext(),
                                if (songs.size > 2) songs.get(2)!!.coverArtId else playlistPageViewModel!!.getPlaylist().coverArtId,
                                CustomGlideRequest.ResourceType.Song,
                            ).build()
                            .transform(
                                GranularRoundedCorners(
                                    0f,
                                    0f,
                                    0f,
                                    CustomGlideRequest.CORNER_RADIUS.toFloat(),
                                ),
                            ).into(bind!!.playlistCoverImageViewBottomLeft)

                        // Pic bottom-right
                        CustomGlideRequest.Builder.Companion
                            .from(
                                requireContext(),
                                if (songs.size > 3) songs.get(3)!!.coverArtId else playlistPageViewModel!!.getPlaylist().coverArtId,
                                CustomGlideRequest.ResourceType.Song,
                            ).build()
                            .transform(
                                GranularRoundedCorners(
                                    0f,
                                    0f,
                                    CustomGlideRequest.CORNER_RADIUS.toFloat(),
                                    0f,
                                ),
                            ).into(bind!!.playlistCoverImageViewBottomRight)
                    }
                },
            )
    }

    private fun initSongsView() {
        bind!!.songRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.songRecyclerView.setHasFixedSize(true)

        songHorizontalAdapter = SongHorizontalAdapter(this, true, false, null)
        bind!!.songRecyclerView.setAdapter(songHorizontalAdapter)

        playlistPageViewModel!!.getPlaylistSongLiveList().observe(
            getViewLifecycleOwner(),
            Observer { songs: MutableList<Child?>? -> songHorizontalAdapter!!.setItems(songs) },
        )
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
}
