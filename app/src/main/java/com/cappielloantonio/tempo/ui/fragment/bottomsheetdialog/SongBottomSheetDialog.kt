package com.cappielloantonio.tempo.ui.fragment.bottomsheetdialog

import android.content.ClipData
import android.content.ClipboardManager
import android.content.ComponentName
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
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
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Share
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.dialog.PlaylistChooserDialog
import com.cappielloantonio.tempo.ui.dialog.RatingDialog
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.isSharingEnabled
import com.cappielloantonio.tempo.viewmodel.HomeViewModel
import com.cappielloantonio.tempo.viewmodel.SongBottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class SongBottomSheetDialog :
    BottomSheetDialogFragment(),
    View.OnClickListener {
    private var homeViewModel: HomeViewModel? = null
    private var songBottomSheetViewModel: SongBottomSheetViewModel? = null
    private var song: Child? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_song_dialog, container, false)

        song = requireArguments().getParcelable<Child?>(Constants.TRACK_OBJECT)

        homeViewModel =
            ViewModelProvider(requireActivity()).get<HomeViewModel>(HomeViewModel::class.java)
        songBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<SongBottomSheetViewModel>(
                SongBottomSheetViewModel::class.java,
            )
        songBottomSheetViewModel!!.setSong(song)

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
        val coverSong = view.findViewById<ImageView>(R.id.song_cover_image_view)
        CustomGlideRequest.Builder.Companion
            .from(
                requireContext(),
                songBottomSheetViewModel!!.getSong().coverArtId,
                CustomGlideRequest.ResourceType.Song,
            ).build()
            .into(coverSong)

        val titleSong = view.findViewById<TextView>(R.id.song_title_text_view)
        titleSong.text = songBottomSheetViewModel!!.getSong().title

        titleSong.setSelected(true)

        val artistSong = view.findViewById<TextView>(R.id.song_artist_text_view)
        artistSong.text = songBottomSheetViewModel!!.getSong().artist

        val favoriteToggle = view.findViewById<ToggleButton>(R.id.button_favorite)
        favoriteToggle.setChecked(songBottomSheetViewModel!!.getSong().starred != null)
        favoriteToggle.setOnClickListener(
            View.OnClickListener { v: View? ->
                songBottomSheetViewModel!!.setFavorite(requireContext())
            },
        )
        favoriteToggle.setOnLongClickListener(
            OnLongClickListener { v: View? ->
                val bundle = Bundle()
                bundle.putParcelable(Constants.TRACK_OBJECT, song)

                val dialog = RatingDialog()
                dialog.setArguments(bundle)
                dialog.show(requireActivity().supportFragmentManager, null)

                dismissBottomSheet()
                true
            },
        )

        val playRadio = view.findViewById<TextView>(R.id.play_radio_text_view)
        playRadio.setOnClickListener(
            View.OnClickListener { v: View? ->
                MediaManager.startQueue(mediaBrowserListenableFuture, song)
                (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                songBottomSheetViewModel!!
                    .getInstantMix(getViewLifecycleOwner(), song)
                    .observe(
                        getViewLifecycleOwner(),
                        Observer { songs: MutableList<Child?>? ->
                            MusicUtil.ratingFilter(songs)
                            if (songs == null) {
                                dismissBottomSheet()
                                return@observe
                            }
                            if (!songs.isEmpty()) {
                                MediaManager.enqueue(mediaBrowserListenableFuture, songs, true)
                                dismissBottomSheet()
                            }
                        },
                    )
            },
        )

        val playNext = view.findViewById<TextView>(R.id.play_next_text_view)
        playNext.setOnClickListener(
            View.OnClickListener { v: View? ->
                MediaManager.enqueue(mediaBrowserListenableFuture, song, true)
                (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                dismissBottomSheet()
            },
        )

        val addToQueue = view.findViewById<TextView>(R.id.add_to_queue_text_view)
        addToQueue.setOnClickListener(
            View.OnClickListener { v: View? ->
                MediaManager.enqueue(mediaBrowserListenableFuture, song, false)
                (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                dismissBottomSheet()
            },
        )

        val rate = view.findViewById<TextView>(R.id.rate_text_view)
        rate.setOnClickListener(
            View.OnClickListener { v: View? ->
                val bundle = Bundle()
                bundle.putParcelable(Constants.TRACK_OBJECT, song)

                val dialog = RatingDialog()
                dialog.setArguments(bundle)
                dialog.show(requireActivity().supportFragmentManager, null)
                dismissBottomSheet()
            },
        )

        val download = view.findViewById<TextView>(R.id.download_text_view)
        download.setOnClickListener(
            View.OnClickListener { v: View? ->
                DownloadUtil.getDownloadTracker(requireContext()).download(
                    MappingUtil.mapDownload(song),
                    Download(song!!),
                )
                dismissBottomSheet()
            },
        )

        val remove = view.findViewById<TextView>(R.id.remove_text_view)
        remove.setOnClickListener(
            View.OnClickListener { v: View? ->
                DownloadUtil.getDownloadTracker(requireContext()).remove(
                    MappingUtil.mapDownload(song),
                    Download(song!!),
                )
                dismissBottomSheet()
            },
        )

        initDownloadUI(download, remove)

        val addToPlaylist = view.findViewById<TextView>(R.id.add_to_playlist_text_view)
        addToPlaylist.setOnClickListener(
            View.OnClickListener { v: View? ->
                val bundle = Bundle()
                bundle.putParcelableArrayList(
                    Constants.TRACKS_OBJECT,
                    ArrayList<Child?>(mutableListOf<Child?>(song)),
                )

                val dialog = PlaylistChooserDialog()
                dialog.setArguments(bundle)
                dialog.show(requireActivity().supportFragmentManager, null)
                dismissBottomSheet()
            },
        )

        val goToAlbum = view.findViewById<TextView>(R.id.go_to_album_text_view)
        goToAlbum.setOnClickListener(
            View.OnClickListener { v: View? ->
                songBottomSheetViewModel!!
                    .getAlbum()
                    .observe(
                        getViewLifecycleOwner(),
                        Observer { album: AlbumID3? ->
                            if (album != null) {
                                val bundle = Bundle()
                                bundle.putParcelable(Constants.ALBUM_OBJECT, album)
                                NavHostFragment
                                    .findNavController(this)
                                    .navigate(R.id.albumPageFragment, bundle)
                            } else {
                                Toast
                                    .makeText(
                                        requireContext(),
                                        getString(R.string.song_bottom_sheet_error_retrieving_album),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                            dismissBottomSheet()
                        },
                    )
            },
        )

        goToAlbum.visibility = if (songBottomSheetViewModel!!.getSong().albumId != null) View.VISIBLE else View.GONE

        val goToArtist = view.findViewById<TextView>(R.id.go_to_artist_text_view)
        goToArtist.setOnClickListener(
            View.OnClickListener { v: View? ->
                songBottomSheetViewModel!!
                    .getArtist()
                    .observe(
                        getViewLifecycleOwner(),
                        Observer { artist: ArtistID3? ->
                            if (artist != null) {
                                val bundle = Bundle()
                                bundle.putParcelable(Constants.ARTIST_OBJECT, artist)
                                NavHostFragment
                                    .findNavController(this)
                                    .navigate(R.id.artistPageFragment, bundle)
                            } else {
                                Toast
                                    .makeText(
                                        requireContext(),
                                        getString(R.string.song_bottom_sheet_error_retrieving_artist),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                            dismissBottomSheet()
                        },
                    )
            },
        )

        goToArtist.visibility = if (songBottomSheetViewModel!!.getSong().artistId != null) View.VISIBLE else View.GONE

        val share = view.findViewById<TextView>(R.id.share_text_view)
        share.setOnClickListener(
            View.OnClickListener { v: View? ->
                songBottomSheetViewModel!!
                    .shareTrack()
                    .observe(
                        getViewLifecycleOwner(),
                        Observer { sharedTrack: Share? ->
                            if (sharedTrack != null) {
                                val clipboardManager =
                                    requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                                val clipData =
                                    ClipData.newPlainText(getString(R.string.app_name), sharedTrack.url)
                                clipboardManager.setPrimaryClip(clipData)
                                refreshShares()
                                dismissBottomSheet()
                            } else {
                                Toast
                                    .makeText(
                                        requireContext(),
                                        getString(R.string.share_unsupported_error),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                                dismissBottomSheet()
                            }
                        },
                    )
            },
        )

        share.visibility = if (isSharingEnabled()) View.VISIBLE else View.GONE
    }

    override fun onClick(v: View?) {
        dismissBottomSheet()
    }

    private fun dismissBottomSheet() {
        dismiss()
    }

    private fun initDownloadUI(
        download: TextView,
        remove: TextView,
    ) {
        if (DownloadUtil.getDownloadTracker(requireContext()).isDownloaded(song!!.id)) {
            remove.visibility = View.VISIBLE
        } else {
            download.visibility = View.VISIBLE
            remove.visibility = View.GONE
        }
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

    private fun refreshShares() {
        homeViewModel!!.refreshShares(requireActivity())
    }
}
