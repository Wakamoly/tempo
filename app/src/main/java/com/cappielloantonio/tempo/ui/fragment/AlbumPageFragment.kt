package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentAlbumPageBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.AlbumInfo
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.SongHorizontalAdapter
import com.cappielloantonio.tempo.ui.dialog.PlaylistChooserDialog
import com.cappielloantonio.tempo.ui.dialog.RatingDialog
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.viewmodel.AlbumPageViewModel
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Collections
import java.util.Objects
import java.util.stream.Collectors

@UnstableApi
class AlbumPageFragment : Fragment(), ClickCallback {
    private var bind: FragmentAlbumPageBinding? = null
    private var activity: MainActivity? = null
    private var albumPageViewModel: AlbumPageViewModel? = null
    private var songHorizontalAdapter: SongHorizontalAdapter? = null
    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.album_page_menu, menu)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = activity as MainActivity?

        bind = FragmentAlbumPageBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        albumPageViewModel =
            ViewModelProvider(requireActivity()).get<AlbumPageViewModel>(AlbumPageViewModel::class.java)

        init()
        initAppBar()
        initAlbumInfoTextButton()
        initAlbumNotes()
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
        if (item.itemId == R.id.action_rate_album) {
            val bundle = Bundle()
            val album = albumPageViewModel!!.getAlbum().getValue()
            bundle.putParcelable(Constants.ALBUM_OBJECT, album)
            val dialog = RatingDialog()
            dialog.setArguments(bundle)
            dialog.show(requireActivity().supportFragmentManager, null)
            return true
        }

        if (item.itemId == R.id.action_download_album) {
            albumPageViewModel!!.getAlbumSongLiveList()
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    DownloadUtil.getDownloadTracker(requireContext()).download(
                        MappingUtil.mapDownloads(songs),
                        songs!!.stream().map<Download?> { child: Child? -> Download(child) }
                            .collect(
                                Collectors.toList()
                            ))
                })
            return true
        }
        if (item.itemId == R.id.action_add_to_playlist) {
            albumPageViewModel!!.getAlbumSongLiveList()
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, ArrayList<Child?>(songs))

                    val dialog = PlaylistChooserDialog()
                    dialog.setArguments(bundle)
                    dialog.show(requireActivity().supportFragmentManager, null)
                })
            return true
        }

        return false
    }

    private fun init() {
        albumPageViewModel!!.setAlbum(
            getViewLifecycleOwner(), requireArguments().getParcelable<AlbumID3?>(
                Constants.ALBUM_OBJECT
            )
        )
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.animToolbar)

        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity!!.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        albumPageViewModel!!.getAlbum()
            .observe(getViewLifecycleOwner(), Observer { album: AlbumID3? ->
                if (bind != null && album != null) {
                    bind!!.animToolbar.setTitle(album.name)

                    bind!!.albumNameLabel.text = album.name
                    bind!!.albumArtistLabel.text = album.artist
                    bind!!.albumReleaseYearLabel.text = if (album.year != 0) album.year.toString() else ""
                    bind!!.albumReleaseYearLabel.visibility = if (album.year != 0) View.VISIBLE else View.GONE
                    bind!!.albumSongCountDurationTextview.text = getString(
                        R.string.album_page_tracks_count_and_duration,
                        album.songCount,
                        if (album.duration != null) album.duration!! / 60 else 0
                    )
                    if (album.genre != null && !album.genre!!.isEmpty()) {
                        bind!!.albumGenresTextview.text = album.genre
                        bind!!.albumGenresTextview.visibility = View.VISIBLE
                    } else {
                        bind!!.albumGenresTextview.visibility = View.GONE
                    }

                    if (album.releaseDate != null && album.originalReleaseDate != null) {
                        if (album.releaseDate!!.getFormattedDate() != null || album.originalReleaseDate!!.getFormattedDate() != null) bind!!.albumReleaseYearsTextview.visibility =
                            View.VISIBLE
                        else bind!!.albumReleaseYearsTextview.visibility = View.GONE

                        if (album.releaseDate!!.getFormattedDate() == null || album.originalReleaseDate!!.getFormattedDate() == null) {
                            bind!!.albumReleaseYearsTextview.text = getString(
                                R.string.album_page_release_date_label,
                                if (album.releaseDate != null) album.releaseDate!!.getFormattedDate() else album.originalReleaseDate!!.getFormattedDate()
                            )
                        }

                        if (album.releaseDate!!.getFormattedDate() != null && album.originalReleaseDate!!.getFormattedDate() != null) {
                            if (album.releaseDate!!.year == album.originalReleaseDate!!.year && album.releaseDate!!.month == album.originalReleaseDate!!.month && album.releaseDate!!.day == album.originalReleaseDate!!.day) {
                                bind!!.albumReleaseYearsTextview.text = getString(
                                    R.string.album_page_release_date_label,
                                    album.releaseDate!!.getFormattedDate()
                                )
                            } else {
                                bind!!.albumReleaseYearsTextview.text = getString(
                                    R.string.album_page_release_dates_label,
                                    album.releaseDate!!.getFormattedDate(),
                                    album.originalReleaseDate!!.getFormattedDate()
                                )
                            }
                        }
                    }
                }
            })

        bind!!.animToolbar.setNavigationOnClickListener(View.OnClickListener { v: View? -> activity!!.navController.navigateUp() })

        Objects.requireNonNull<Drawable?>(bind!!.animToolbar.getOverflowIcon())
            .setTint(requireContext().resources.getColor(R.color.titleTextColor, null))

        bind!!.albumOtherInfoButton.setOnClickListener(View.OnClickListener { v: View? ->
            if (bind!!.albumDetailView.visibility == View.GONE) {
                bind!!.albumDetailView.visibility = View.VISIBLE
            } else if (bind!!.albumDetailView.visibility == View.VISIBLE) {
                bind!!.albumDetailView.visibility = View.GONE
            }
        })
    }

    private fun initAlbumInfoTextButton() {
        bind!!.albumArtistLabel.setOnClickListener(View.OnClickListener { v: View? ->
            albumPageViewModel!!.getArtist()
                .observe(getViewLifecycleOwner(), Observer { artist: ArtistID3? ->
                    if (artist != null) {
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.ARTIST_OBJECT, artist)
                        activity!!.navController.navigate(
                            R.id.action_albumPageFragment_to_artistPageFragment,
                            bundle
                        )
                    } else Toast.makeText(
                        requireContext(),
                        getString(R.string.album_error_retrieving_artist),
                        Toast.LENGTH_SHORT
                    ).show()
                })
        })
    }

    private fun initAlbumNotes() {
        albumPageViewModel!!.getAlbumInfo()
            .observe(getViewLifecycleOwner(), Observer { albumInfo: AlbumInfo? ->
                if (albumInfo != null) {
                    if (bind != null) bind!!.albumNotesTextview.visibility = View.VISIBLE
                    if (bind != null) bind!!.albumNotesTextview.text = MusicUtil.forceReadableString(
                        albumInfo.notes
                    )

                    if (bind != null && albumInfo.lastFmUrl != null && !albumInfo.lastFmUrl!!.isEmpty()) {
                        bind!!.albumNotesTextview.setOnClickListener(View.OnClickListener { v: View? ->
                            val intent = Intent(Intent.ACTION_VIEW)
                            intent.setData(Uri.parse(albumInfo.lastFmUrl))
                            startActivity(intent)
                        })
                    }
                } else {
                    if (bind != null) bind!!.albumNotesTextview.visibility = View.GONE
                }
            })
    }

    private fun initMusicButton() {
        albumPageViewModel!!.getAlbumSongLiveList()
            .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                if (bind != null && !songs!!.isEmpty()) {
                    bind!!.albumPagePlayButton.setOnClickListener(View.OnClickListener { v: View? ->
                        MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                        activity!!.setBottomSheetInPeek(true)
                    })

                    bind!!.albumPageShuffleButton.setOnClickListener(View.OnClickListener { v: View? ->
                        Collections.shuffle(songs)
                        MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                        activity!!.setBottomSheetInPeek(true)
                    })
                }
                if (bind != null && songs!!.isEmpty()) {
                    bind!!.albumPagePlayButton.setEnabled(false)
                    bind!!.albumPageShuffleButton.setEnabled(false)
                }
            })
    }

    private fun initBackCover() {
        albumPageViewModel!!.getAlbum()
            .observe(getViewLifecycleOwner(), Observer { album: AlbumID3? ->
                if (bind != null && album != null) {
                    CustomGlideRequest.Builder.Companion.from(
                        requireContext(),
                        album.coverArtId,
                        CustomGlideRequest.ResourceType.Album
                    ).build().into(
                        bind!!.albumCoverImageView
                    )
                }
            })
    }

    private fun initSongsView() {
        albumPageViewModel!!.getAlbum()
            .observe(getViewLifecycleOwner(), Observer { album: AlbumID3? ->
                if (bind != null && album != null) {
                    bind!!.songRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
                    bind!!.songRecyclerView.setHasFixedSize(true)

                    songHorizontalAdapter = SongHorizontalAdapter(this, false, false, album)
                    bind!!.songRecyclerView.setAdapter(songHorizontalAdapter)

                    albumPageViewModel!!.getAlbumSongLiveList().observe(
                        getViewLifecycleOwner(),
                        Observer { songs: MutableList<Child?>? ->
                            songHorizontalAdapter!!.setItems(
                                songs
                            )
                        })
                }
            })
    }

    private fun initializeMediaBrowser() {
        mediaBrowserListenableFuture = MediaBrowser.Builder(
            requireContext(),
            SessionToken(
                requireContext(),
                ComponentName(requireContext(), MediaService::class.java)
            )
        ).buildAsync()
    }

    private fun releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture)
    }

    override fun onMediaClick(bundle: Bundle) {
        MediaManager.startQueue(
            mediaBrowserListenableFuture, bundle.getParcelableArrayList<Child?>(
                Constants.TRACKS_OBJECT
            ), bundle.getInt(Constants.ITEM_POSITION)
        )
        activity!!.setBottomSheetInPeek(true)
    }

    override fun onMediaLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.songBottomSheetDialog, bundle)
    }
}