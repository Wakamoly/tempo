package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.transition.Fade
import android.transition.Transition
import android.transition.TransitionManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.InnerFragmentPlayerCoverBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.ui.dialog.PlaylistChooserDialog
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.Preferences.isSyncronizationEnabled
import com.cappielloantonio.tempo.viewmodel.PlayerBottomSheetViewModel
import com.google.android.material.snackbar.Snackbar
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class PlayerCoverFragment : Fragment() {
    private var playerBottomSheetViewModel: PlayerBottomSheetViewModel? = null
    private var bind: InnerFragmentPlayerCoverBinding? = null
    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser>? = null

    private val handler = Handler()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = InnerFragmentPlayerCoverBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()

        playerBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<PlayerBottomSheetViewModel>(
                PlayerBottomSheetViewModel::class.java,
            )

        initOverlay()
        initInnerButton()

        return view
    }

    override fun onStart() {
        super.onStart()
        initializeBrowser()
        bindMediaController()
        toggleOverlayVisibility(false)
    }

    override fun onStop() {
        releaseBrowser()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun initTapButtonHideTransition() {
        bind!!.nowPlayingTapButton.visibility = View.VISIBLE

        handler.removeCallbacksAndMessages(null)

        val runnable =
            Runnable {
                if (bind != null) bind!!.nowPlayingTapButton.visibility = View.GONE
            }

        handler.postDelayed(runnable, 10000)
    }

    private fun initOverlay() {
        bind!!.nowPlayingSongCoverImageView.setOnClickListener(
            View.OnClickListener { view: View? ->
                toggleOverlayVisibility(
                    true,
                )
            },
        )
        bind!!.nowPlayingSongCoverButtonGroup.setOnClickListener(
            View.OnClickListener { view: View? ->
                toggleOverlayVisibility(
                    false,
                )
            },
        )
        bind!!.nowPlayingTapButton.setOnClickListener(
            View.OnClickListener { view: View? ->
                toggleOverlayVisibility(
                    true,
                )
            },
        )
    }

    private fun toggleOverlayVisibility(isVisible: Boolean) {
        val transition: Transition = Fade()
        transition.setDuration(200)
        transition.addTarget(bind!!.nowPlayingSongCoverButtonGroup)

        TransitionManager.beginDelayedTransition(bind!!.getRoot(), transition)
        bind!!.nowPlayingSongCoverButtonGroup.visibility = if (isVisible) View.VISIBLE else View.GONE
        bind!!.nowPlayingTapButton.visibility = if (isVisible) View.GONE else View.VISIBLE

        bind!!.innerButtonBottomRight.visibility = if (isSyncronizationEnabled()) View.VISIBLE else View.GONE
        bind!!.innerButtonBottomRightAlternative.visibility = if (isSyncronizationEnabled()) View.GONE else View.VISIBLE

        if (!isVisible) initTapButtonHideTransition()
    }

    private fun initInnerButton() {
        playerBottomSheetViewModel!!
            .getLiveMedia()
            .observe(
                getViewLifecycleOwner(),
                Observer { song: Child? ->
                    if (song != null && bind != null) {
                        bind!!.innerButtonTopLeft.setOnClickListener(
                            View.OnClickListener { view: View? ->
                                DownloadUtil.getDownloadTracker(requireContext()).download(
                                    MappingUtil.mapDownload(song),
                                    Download(song),
                                )
                            },
                        )

                        bind!!.innerButtonTopRight.setOnClickListener(
                            View.OnClickListener { view: View? ->
                                val tracks = ArrayList<Child?>()
                                tracks.add(song)
                                val bundle = Bundle()
                                bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, tracks)

                                val dialog = PlaylistChooserDialog()
                                dialog.setArguments(bundle)
                                dialog.show(requireActivity().supportFragmentManager, null)
                            },
                        )

                        bind!!.innerButtonBottomLeft.setOnClickListener(
                            View.OnClickListener { view: View? ->
                                playerBottomSheetViewModel!!
                                    .getMediaInstantMix(
                                        getViewLifecycleOwner(),
                                        song,
                                    ).observe(
                                        getViewLifecycleOwner(),
                                        Observer { media: MutableList<Child?>? ->
                                            MediaManager.enqueue(mediaBrowserListenableFuture, media, true)
                                        },
                                    )
                            },
                        )

                        bind!!.innerButtonBottomRight.setOnClickListener(
                            View.OnClickListener { view: View? ->
                                if (playerBottomSheetViewModel!!.savePlayQueue()) {
                                    Snackbar
                                        .make(
                                            requireView(),
                                            R.string.player_queue_save_queue_success,
                                            Snackbar.LENGTH_LONG,
                                        ).show()
                                }
                            },
                        )

                        bind!!.innerButtonBottomRightAlternative.setOnClickListener(
                            View.OnClickListener { view: View? ->
                                if (activity != null) {
                                    val playerBottomSheetFragment =
                                        requireActivity()
                                            .supportFragmentManager
                                            .findFragmentByTag("PlayerBottomSheet") as PlayerBottomSheetFragment?
                                    if (playerBottomSheetFragment != null) {
                                        playerBottomSheetFragment.goToLyricsPage()
                                    }
                                }
                            },
                        )
                    }
                },
            )
    }

    private fun initializeBrowser() {
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

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture!!)
    }

    private fun bindMediaController() {
        mediaBrowserListenableFuture!!.addListener(
            Runnable {
                try {
                    val mediaBrowser = mediaBrowserListenableFuture!!.get()
                    setMediaBrowserListener(mediaBrowser)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    private fun setMediaBrowserListener(mediaBrowser: MediaBrowser) {
        setCover(mediaBrowser.getMediaMetadata())

        mediaBrowser.addListener(
            object : Player.Listener {
                override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                    setCover(mediaMetadata)
                    toggleOverlayVisibility(false)
                }
            },
        )
    }

    private fun setCover(mediaMetadata: MediaMetadata) {
        CustomGlideRequest.Builder.Companion
            .from(
                requireContext(),
                if (mediaMetadata.extras != null) mediaMetadata.extras!!.getString("coverArtId") else null,
                CustomGlideRequest.ResourceType.Song,
            ).build()
            .into(bind!!.nowPlayingSongCoverImageView)
    }
}
