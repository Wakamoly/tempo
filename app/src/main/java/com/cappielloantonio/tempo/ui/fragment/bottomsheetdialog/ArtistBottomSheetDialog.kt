package com.cappielloantonio.tempo.ui.fragment.bottomsheetdialog

import android.content.ComponentName
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
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.repository.ArtistRepository
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.viewmodel.ArtistBottomSheetViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class ArtistBottomSheetDialog : BottomSheetDialogFragment(), View.OnClickListener {
    private var artistBottomSheetViewModel: ArtistBottomSheetViewModel? = null
    private var artist: ArtistID3? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.bottom_sheet_artist_dialog, container, false)

        artist = this.requireArguments().getParcelable<ArtistID3?>(Constants.ARTIST_OBJECT)

        artistBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<ArtistBottomSheetViewModel>(
                ArtistBottomSheetViewModel::class.java
            )
        artistBottomSheetViewModel!!.setArtist(artist)

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

    // TODO Utilizzare il viewmodel come tramite ed evitare le chiamate dirette
    private fun init(view: View) {
        val coverArtist = view.findViewById<ImageView>(R.id.artist_cover_image_view)
        CustomGlideRequest.Builder.Companion.from(
            requireContext(),
            artistBottomSheetViewModel!!.getArtist().coverArtId,
            CustomGlideRequest.ResourceType.Artist
        )
            .build()
            .into(coverArtist)

        val nameArtist = view.findViewById<TextView>(R.id.song_title_text_view)
        nameArtist.text = artistBottomSheetViewModel!!.getArtist().name
        nameArtist.setSelected(true)

        val favoriteToggle = view.findViewById<ToggleButton>(R.id.button_favorite)
        favoriteToggle.setChecked(artistBottomSheetViewModel!!.getArtist().starred != null)
        favoriteToggle.setOnClickListener(View.OnClickListener { v: View? ->
            artistBottomSheetViewModel!!.setFavorite()
        })

        val playRadio = view.findViewById<TextView>(R.id.play_radio_text_view)
        playRadio.setOnClickListener(View.OnClickListener { v: View? ->
            val artistRepository = ArtistRepository()
            artistRepository.getInstantMix(artist, 20)
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    MusicUtil.ratingFilter(songs)
                    if (!songs!!.isEmpty()) {
                        MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                        (requireActivity() as MainActivity).setBottomSheetInPeek(true)
                    }
                    dismissBottomSheet()
                })
        })

        val playRandom = view.findViewById<TextView>(R.id.play_random_text_view)
        playRandom.setOnClickListener(View.OnClickListener { v: View? ->
            val artistRepository = ArtistRepository()
            artistRepository.getRandomSong(artist, 50)
                .observe(getViewLifecycleOwner(), Observer { songs: MutableList<Child?>? ->
                    MusicUtil.ratingFilter(songs)
                    if (!songs!!.isEmpty()) {
                        MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                        (requireActivity() as MainActivity).setBottomSheetInPeek(true)

                        dismissBottomSheet()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            getString(R.string.artist_error_retrieving_tracks),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                    dismissBottomSheet()
                })
        })
    }

    override fun onClick(v: View?) {
        dismissBottomSheet()
    }

    private fun dismissBottomSheet() {
        dismiss()
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

    companion object {
        private const val TAG = "AlbumBottomSheetDialog"
    }
}