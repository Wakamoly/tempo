package com.cappielloantonio.tempo.ui.fragment.bottomsheetdialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.ToggleButton
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.NavHostFragment
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.MediaCallback
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.repository.AlbumRepository
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Share
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.dialog.PlaylistChooserDialog
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.isSharingEnabled
import com.cappielloantonio.tempo.viewmodel.AlbumBottomSheetViewModel
import com.cappielloantonio.tempo.viewmodel.HomeViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Collections
import java.util.stream.Collectors

@UnstableApi
class AlbumBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private var homeViewModel: HomeViewModel? = null
    private var albumBottomSheetViewModel: AlbumBottomSheetViewModel? = null
    private var album: AlbumID3? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_album_dialog, container, false)

        album = this.requireArguments().getParcelable<AlbumID3?>(Constants.ALBUM_OBJECT)

        homeViewModel =
            ViewModelProvider(requireActivity()).get<HomeViewModel>(HomeViewModel::class.java)
        albumBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<AlbumBottomSheetViewModel>(
                AlbumBottomSheetViewModel::class.java
            )
        albumBottomSheetViewModel!!.setAlbum(album)

        init(view)

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

    private fun init(view: View) {
        val coverAlbum = view.findViewById<ImageView>(R.id.album_cover_image_view)
        CustomGlideRequest.Builder.Companion.from(
            requireContext(),
            albumBottomSheetViewModel!!.getAlbum().coverArtId,
            CustomGlideRequest.ResourceType.Album
        )
            .build()
            .into(coverAlbum)

        val titleAlbum = view.findViewById<TextView>(R.id.album_title_text_view)
        titleAlbum.text = albumBottomSheetViewModel!!.getAlbum().name
        titleAlbum.setSelected(true)

        val artistAlbum = view.findViewById<TextView>(R.id.album_artist_text_view)
        artistAlbum.text = albumBottomSheetViewModel!!.getAlbum().artist

        val favoriteToggle = view.findViewById<ToggleButton>(R.id.button_favorite)
        favoriteToggle.setChecked(albumBottomSheetViewModel!!.getAlbum().starred != null)
        favoriteToggle.setOnClickListener(View.OnClickListener { v: View? ->
            albumBottomSheetViewModel!!.setFavorite(requireContext())
        })

        val playRadio = view.findViewById<TextView>(R.id.play_radio_text_view)
        playRadio.setOnClickListener(View.OnClickListener { v: View? ->
            val albumRepository = AlbumRepository()
            albumRepository.getInstantMix(album, 20, object : MediaCallback {
                override fun onError(exception: Exception) {
                    exception.printStackTrace()
                }

                override fun onLoadMedia(media: MutableList<*>) {
                    MusicUtil.ratingFilter(media as ArrayList<Child?>?)

                    if (!media.isEmpty()) {
                        MediaManager.startQueue(mediaBrowserListenableFuture, media, 0)
                        (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                    }

                    dismissBottomSheet()
                }
            })
        })

        val playRandom = view.findViewById<TextView>(R.id.play_random_text_view)
        playRandom.setOnClickListener(View.OnClickListener { v: View? ->
            val albumRepository = AlbumRepository()
            albumRepository.getAlbumTracks(album!!.id)
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    Collections.shuffle(songs)
                    MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                    (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                    dismissBottomSheet()
                })
        })

        val playNext = view.findViewById<TextView>(R.id.play_next_text_view)
        playNext.setOnClickListener(View.OnClickListener { v: View? ->
            albumBottomSheetViewModel!!.getAlbumTracks()
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    MediaManager.enqueue(mediaBrowserListenableFuture, songs, true)
                    (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                    dismissBottomSheet()
                })
        })

        val addToQueue = view.findViewById<TextView>(R.id.add_to_queue_text_view)
        addToQueue.setOnClickListener(View.OnClickListener { v: View? ->
            albumBottomSheetViewModel!!.getAlbumTracks()
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    MediaManager.enqueue(mediaBrowserListenableFuture, songs, false)
                    (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                    dismissBottomSheet()
                })
        })

        val downloadAll = view.findViewById<TextView>(R.id.download_all_text_view)
        albumBottomSheetViewModel!!.getAlbumTracks()
            .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                val mediaItems = MappingUtil.mapDownloads(songs)
                val downloads =
                    songs!!.stream().map<Download?> { child: Child? -> Download(child) }.collect(
                        Collectors.toList()
                    )
                downloadAll.setOnClickListener(View.OnClickListener { v: View? ->
                    DownloadUtil.getDownloadTracker(requireContext())
                        .download(mediaItems, downloads)
                    dismissBottomSheet()
                })
            })

        val addToPlaylist = view.findViewById<TextView>(R.id.add_to_playlist_text_view)
        addToPlaylist.setOnClickListener(View.OnClickListener { v: View? ->
            albumBottomSheetViewModel!!.getAlbumTracks()
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    val bundle = Bundle()
                    bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, ArrayList<Child?>(songs))

                    val dialog = PlaylistChooserDialog()
                    dialog.setArguments(bundle)
                    dialog.show(requireActivity().supportFragmentManager, null)
                    dismissBottomSheet()
                })
        })

        val removeAll = view.findViewById<TextView>(R.id.remove_all_text_view)
        albumBottomSheetViewModel!!.getAlbumTracks()
            .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                val mediaItems = MappingUtil.mapDownloads(songs)
                val downloads =
                    songs!!.stream().map<Download?> { child: Child? -> Download(child) }.collect(
                        Collectors.toList()
                    )
                removeAll.setOnClickListener(View.OnClickListener { v: View? ->
                    DownloadUtil.getDownloadTracker(requireContext()).remove(mediaItems, downloads)
                    dismissBottomSheet()
                })
            })

        initDownloadUI(removeAll)

        val goToArtist = view.findViewById<TextView>(R.id.go_to_artist_text_view)
        goToArtist.setOnClickListener(View.OnClickListener { v: View? ->
            albumBottomSheetViewModel!!.getArtist()
                .observe(getViewLifecycleOwner(), Observer { artist: ArtistID3? ->
                    if (artist != null) {
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.ARTIST_OBJECT, artist)
                        NavHostFragment.findNavController(this)
                            .navigate(R.id.artistPageFragment, bundle)
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.album_error_retrieving_artist),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dismissBottomSheet()
                })
        })

        val share = view.findViewById<TextView>(R.id.share_text_view)
        share.setOnClickListener(View.OnClickListener { v: View? ->
            albumBottomSheetViewModel!!.shareAlbum()
                .observe(getViewLifecycleOwner(), Observer { sharedAlbum: Share? ->
                    if (sharedAlbum != null) {
                        val clipboardManager =
                            requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val clipData =
                            ClipData.newPlainText(getString(R.string.app_name), sharedAlbum.url)
                        clipboardManager.setPrimaryClip(clipData)
                        refreshShares()
                        dismissBottomSheet()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.share_unsupported_error),
                            Toast.LENGTH_SHORT
                        ).show()
                        dismissBottomSheet()
                    }
                })
        })

        share.visibility = if (isSharingEnabled()) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View?) {
        dismissBottomSheet()
    }

    private fun dismissBottomSheet() {
        dismiss()
    }

    private fun initDownloadUI(removeAll: TextView) {
        albumBottomSheetViewModel!!.getAlbumTracks()
            .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                val mediaItems = MappingUtil.mapDownloads(songs)
                if (DownloadUtil.getDownloadTracker(requireContext()).areDownloaded(mediaItems)) {
                    removeAll.visibility = View.VISIBLE
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

    private fun refreshShares() {
        homeViewModel!!.refreshShares(requireActivity())
    }
}