package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.text.TextUtils
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.RatingBar.OnRatingBarChangeListener
import android.widget.TextView
import android.widget.ToggleButton
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.MediaMetadata
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.util.RepeatModeUtil
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.fragment.NavHostFragment
import androidx.viewpager2.widget.ViewPager2
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.InnerFragmentPlayerControllerBinding
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.dialog.RatingDialog
import com.cappielloantonio.tempo.ui.dialog.TrackInfoDialog
import com.cappielloantonio.tempo.ui.fragment.pager.PlayerControllerHorizontalPager
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.getPlaybackSpeed
import com.cappielloantonio.tempo.util.Preferences.getRepeatMode
import com.cappielloantonio.tempo.util.Preferences.isShuffleModeEnabled
import com.cappielloantonio.tempo.util.Preferences.isSkipSilenceMode
import com.cappielloantonio.tempo.util.Preferences.setPlaybackSpeed
import com.cappielloantonio.tempo.util.Preferences.setRepeatMode
import com.cappielloantonio.tempo.util.Preferences.setShuffleModeEnabled
import com.cappielloantonio.tempo.util.Preferences.setSkipSilenceMode
import com.cappielloantonio.tempo.util.Preferences.showItemStarRating
import com.cappielloantonio.tempo.viewmodel.PlayerBottomSheetViewModel
import com.cappielloantonio.tempo.viewmodel.RatingViewModel
import com.google.android.material.chip.Chip
import com.google.android.material.elevation.SurfaceColors
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.text.DecimalFormat

@UnstableApi
class PlayerControllerFragment : Fragment() {
    private var bind: InnerFragmentPlayerControllerBinding? = null
    private var playerMediaCoverViewPager: ViewPager2? = null
    private var buttonFavorite: ToggleButton? = null
    private var ratingViewModel: RatingViewModel? = null
    private var songRatingBar: RatingBar? = null
    private var playerMediaTitleLabel: TextView? = null
    private var playerArtistNameLabel: TextView? = null
    private var playbackSpeedButton: Button? = null
    private var skipSilenceToggleButton: ToggleButton? = null
    private var playerMediaExtension: Chip? = null
    private var playerMediaBitrate: TextView? = null
    private var playerQuickActionView: ConstraintLayout? = null
    private var playerOpenQueueButton: ImageButton? = null
    private var playerTrackInfo: ImageButton? = null
    private var ratingContainer: LinearLayout? = null

    private var activity: MainActivity? = null
    private var playerBottomSheetViewModel: PlayerBottomSheetViewModel? = null
    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = activity as MainActivity?

        bind = InnerFragmentPlayerControllerBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()

        playerBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<PlayerBottomSheetViewModel>(
                PlayerBottomSheetViewModel::class.java
            )
        ratingViewModel =
            ViewModelProvider(requireActivity()).get<RatingViewModel>(RatingViewModel::class.java)

        init()
        initQuickActionView()
        initCoverLyricsSlideView()
        initMediaListenable()
        initMediaLabelButton()
        initArtistLabelButton()

        return view
    }

    override fun onStart() {
        super.onStart()
        initializeBrowser()
        bindMediaController()
    }

    override fun onStop() {
        releaseBrowser()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun init() {
        playerMediaCoverViewPager =
            bind!!.getRoot().findViewById<ViewPager2>(R.id.player_media_cover_view_pager)
        buttonFavorite = bind!!.getRoot().findViewById<ToggleButton>(R.id.button_favorite)
        playerMediaTitleLabel =
            bind!!.getRoot().findViewById<TextView>(R.id.player_media_title_label)
        playerArtistNameLabel =
            bind!!.getRoot().findViewById<TextView>(R.id.player_artist_name_label)
        playbackSpeedButton =
            bind!!.getRoot().findViewById<Button>(R.id.player_playback_speed_button)
        skipSilenceToggleButton =
            bind!!.getRoot().findViewById<ToggleButton>(R.id.player_skip_silence_toggle_button)
        playerMediaExtension = bind!!.getRoot().findViewById<Chip>(R.id.player_media_extension)
        playerMediaBitrate = bind!!.getRoot().findViewById<TextView>(R.id.player_media_bitrate)
        playerQuickActionView =
            bind!!.getRoot().findViewById<ConstraintLayout>(R.id.player_quick_action_view)
        playerOpenQueueButton =
            bind!!.getRoot().findViewById<ImageButton>(R.id.player_open_queue_button)
        playerTrackInfo = bind!!.getRoot().findViewById<ImageButton>(R.id.player_info_track)
        songRatingBar = bind!!.getRoot().findViewById<RatingBar>(R.id.song_rating_bar)
        ratingContainer = bind!!.getRoot().findViewById<LinearLayout?>(R.id.rating_container)
        checkAndSetRatingContainerVisibility()
    }

    private fun initQuickActionView() {
        playerQuickActionView!!.setBackgroundColor(
            SurfaceColors.getColorForElevation(
                requireContext(),
                8f
            )
        )

        playerOpenQueueButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            val playerBottomSheetFragment = requireActivity().supportFragmentManager
                .findFragmentByTag("PlayerBottomSheet") as PlayerBottomSheetFragment?
            if (playerBottomSheetFragment != null) {
                playerBottomSheetFragment.goToQueuePage()
            }
        })
    }

    private fun initializeBrowser() {
        mediaBrowserListenableFuture = MediaBrowser.Builder(
            requireContext(),
            SessionToken(
                requireContext(),
                ComponentName(requireContext(), MediaService::class.java)
            )
        ).buildAsync()
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture!!)
    }

    private fun bindMediaController() {
        mediaBrowserListenableFuture!!.addListener(Runnable {
            try {
                val mediaBrowser = mediaBrowserListenableFuture!!.get()

                bind!!.nowPlayingMediaControllerView.setPlayer(mediaBrowser)
                mediaBrowser.setShuffleModeEnabled(isShuffleModeEnabled())
                mediaBrowser.setRepeatMode(getRepeatMode())
                setMediaControllerListener(mediaBrowser)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, MoreExecutors.directExecutor())
    }

    private fun setMediaControllerListener(mediaBrowser: MediaBrowser) {
        setMediaControllerUI(mediaBrowser)
        setMetadata(mediaBrowser.getMediaMetadata())
        setMediaInfo(mediaBrowser.getMediaMetadata())

        mediaBrowser.addListener(object : Player.Listener {
            override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
                setMediaControllerUI(mediaBrowser)
                setMetadata(mediaMetadata)
                setMediaInfo(mediaMetadata)
            }

            override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
                setShuffleModeEnabled(shuffleModeEnabled)
            }

            override fun onRepeatModeChanged(repeatMode: Int) {
                setRepeatMode(repeatMode)
            }
        })
    }

    private fun setMetadata(mediaMetadata: MediaMetadata) {
        playerMediaTitleLabel!!.text = mediaMetadata.title.toString()
        playerArtistNameLabel!!.text = if (mediaMetadata.artist != null) mediaMetadata.artist.toString() else
            if (mediaMetadata.extras != null && mediaMetadata.extras!!.getString("type") == Constants.MEDIA_TYPE_RADIO)
                mediaMetadata.extras!!.getString("uri", getString(R.string.label_placeholder))
            else
                ""

        playerMediaTitleLabel!!.setSelected(true)
        playerArtistNameLabel!!.setSelected(true)

        playerMediaTitleLabel!!.visibility = if (mediaMetadata.title != null && mediaMetadata.title != "") View.VISIBLE else View.GONE
        playerArtistNameLabel!!.visibility = if ((mediaMetadata.artist != null && mediaMetadata.artist != "")
            || mediaMetadata.extras != null && mediaMetadata.extras!!.getString("type") == Constants.MEDIA_TYPE_RADIO && mediaMetadata.extras!!.getString(
                "uri"
            ) != null
        )
            View.VISIBLE
        else
            View.GONE
    }

    private fun setMediaInfo(mediaMetadata: MediaMetadata) {
        if (mediaMetadata.extras != null) {
            val extension = mediaMetadata.extras!!.getString(
                "suffix",
                getString(R.string.player_unknown_format)
            )
            val bitrate =
                if (mediaMetadata.extras!!.getInt("bitrate", 0) != 0) mediaMetadata.extras!!.getInt(
                    "bitrate",
                    0
                ).toString() + "kbps" else "Original"
            val samplingRate = if (mediaMetadata.extras!!.getInt(
                    "samplingRate",
                    0
                ) != 0
            ) DecimalFormat("0.#").format(
                mediaMetadata.extras!!.getInt("samplingRate", 0) / 1000.0
            ) + "kHz" else ""
            val bitDepth = if (mediaMetadata.extras!!.getInt(
                    "bitDepth",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("bitDepth", 0).toString() + "b" else ""

            playerMediaExtension!!.text = extension

            if (bitrate == "Original") {
                playerMediaBitrate!!.visibility = View.GONE
            } else {
                val mediaQualityItems: MutableList<String?> = ArrayList<String?>()

                if (!bitrate.trim { it <= ' ' }.isEmpty()) mediaQualityItems.add(bitrate)
                if (!bitDepth.trim { it <= ' ' }.isEmpty()) mediaQualityItems.add(bitDepth)
                if (!samplingRate.trim { it <= ' ' }.isEmpty()) mediaQualityItems.add(samplingRate)

                val mediaQuality = TextUtils.join(" • ", mediaQualityItems)
                playerMediaBitrate!!.visibility = View.VISIBLE
                playerMediaBitrate!!.text = mediaQuality
            }
        }

        val isTranscodingExtension = MusicUtil.getTranscodingFormatPreference() != "raw"
        val isTranscodingBitrate = MusicUtil.getBitratePreference() != "0"

        if (isTranscodingExtension || isTranscodingBitrate) {
            playerMediaExtension!!.setText(
                MusicUtil.getTranscodingFormatPreference() + " (" + getString(
                    R.string.player_transcoding
                ) + ")"
            )
            playerMediaBitrate!!.text = if (MusicUtil.getBitratePreference() != "0") MusicUtil.getBitratePreference() + "kbps" else getString(
                R.string.player_transcoding_requested
            )
        }

        playerTrackInfo!!.setOnClickListener(View.OnClickListener { view: View? ->
            val dialog = TrackInfoDialog(mediaMetadata)
            dialog.show(activity!!.supportFragmentManager, null)
        })
    }

    private fun setMediaControllerUI(mediaBrowser: MediaBrowser) {
        initPlaybackSpeedButton(mediaBrowser)

        if (mediaBrowser.getMediaMetadata().extras != null) {
            when (mediaBrowser.getMediaMetadata().extras!!.getString(
                "type",
                Constants.MEDIA_TYPE_MUSIC
            )) {
                Constants.MEDIA_TYPE_PODCAST -> {
                    bind!!.getRoot().setShowShuffleButton(false)
                    bind!!.getRoot().setShowRewindButton(true)
                    bind!!.getRoot().setShowPreviousButton(false)
                    bind!!.getRoot().setShowNextButton(false)
                    bind!!.getRoot().setShowFastForwardButton(true)
                    bind!!.getRoot().setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE)
                    bind!!.getRoot().findViewById<View?>(R.id.player_playback_speed_button).visibility =
                        View.VISIBLE
                    bind!!.getRoot().findViewById<View?>(R.id.player_skip_silence_toggle_button).visibility =
                        View.VISIBLE
                    bind!!.getRoot().findViewById<View?>(R.id.button_favorite).visibility = View.GONE
                    setPlaybackParameters(mediaBrowser)
                }

                Constants.MEDIA_TYPE_RADIO -> {
                    bind!!.getRoot().setShowShuffleButton(false)
                    bind!!.getRoot().setShowRewindButton(false)
                    bind!!.getRoot().setShowPreviousButton(false)
                    bind!!.getRoot().setShowNextButton(false)
                    bind!!.getRoot().setShowFastForwardButton(false)
                    bind!!.getRoot().setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_NONE)
                    bind!!.getRoot().findViewById<View?>(R.id.player_playback_speed_button).visibility =
                        View.GONE
                    bind!!.getRoot().findViewById<View?>(R.id.player_skip_silence_toggle_button).visibility =
                        View.GONE
                    bind!!.getRoot().findViewById<View?>(R.id.button_favorite).visibility = View.GONE
                    setPlaybackParameters(mediaBrowser)
                }

                Constants.MEDIA_TYPE_MUSIC -> {
                    bind!!.getRoot().setShowShuffleButton(true)
                    bind!!.getRoot().setShowRewindButton(false)
                    bind!!.getRoot().setShowPreviousButton(true)
                    bind!!.getRoot().setShowNextButton(true)
                    bind!!.getRoot().setShowFastForwardButton(false)
                    bind!!.getRoot()
                        .setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL or RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE)
                    bind!!.getRoot().findViewById<View?>(R.id.player_playback_speed_button).visibility =
                        View.GONE
                    bind!!.getRoot().findViewById<View?>(R.id.player_skip_silence_toggle_button).visibility =
                        View.GONE
                    bind!!.getRoot().findViewById<View?>(R.id.button_favorite).visibility = View.VISIBLE
                    resetPlaybackParameters(mediaBrowser)
                }

                else -> {
                    bind!!.getRoot().setShowShuffleButton(true)
                    bind!!.getRoot().setShowRewindButton(false)
                    bind!!.getRoot().setShowPreviousButton(true)
                    bind!!.getRoot().setShowNextButton(true)
                    bind!!.getRoot().setShowFastForwardButton(false)
                    bind!!.getRoot()
                        .setRepeatToggleModes(RepeatModeUtil.REPEAT_TOGGLE_MODE_ALL or RepeatModeUtil.REPEAT_TOGGLE_MODE_ONE)
                    bind!!.getRoot().findViewById<View?>(R.id.player_playback_speed_button).visibility =
                        View.GONE
                    bind!!.getRoot().findViewById<View?>(R.id.player_skip_silence_toggle_button).visibility =
                        View.GONE
                    bind!!.getRoot().findViewById<View?>(R.id.button_favorite).visibility = View.VISIBLE
                    resetPlaybackParameters(mediaBrowser)
                }
            }
        }
    }

    private fun initCoverLyricsSlideView() {
        playerMediaCoverViewPager!!.setOrientation(ViewPager2.ORIENTATION_HORIZONTAL)
        playerMediaCoverViewPager!!.setAdapter(PlayerControllerHorizontalPager(this))

        playerMediaCoverViewPager!!.registerOnPageChangeCallback(object :
            ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

                val playerBottomSheetFragment = requireActivity().supportFragmentManager
                    .findFragmentByTag("PlayerBottomSheet") as PlayerBottomSheetFragment?

                if (position == 0) {
                    activity!!.setBottomSheetDraggableState(true)

                    if (playerBottomSheetFragment != null) {
                        playerBottomSheetFragment.setPlayerControllerVerticalPagerDraggableState(
                            true
                        )
                    }
                } else if (position == 1) {
                    activity!!.setBottomSheetDraggableState(false)

                    if (playerBottomSheetFragment != null) {
                        playerBottomSheetFragment.setPlayerControllerVerticalPagerDraggableState(
                            false
                        )
                    }
                }
            }
        })
    }

    private fun initMediaListenable() {
        playerBottomSheetViewModel!!.getLiveMedia()
            .observe(getViewLifecycleOwner(), Observer { media: Child? ->
                if (media != null) {
                    ratingViewModel!!.setSong(media)
                    buttonFavorite!!.setChecked(media.starred != null)
                    buttonFavorite!!.setOnClickListener(View.OnClickListener { v: View? ->
                        playerBottomSheetViewModel!!.setFavorite(
                            requireContext(),
                            media
                        )
                    })
                    buttonFavorite!!.setOnLongClickListener(OnLongClickListener { v: View? ->
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.TRACK_OBJECT, media)

                        val dialog = RatingDialog()
                        dialog.setArguments(bundle)
                        dialog.show(requireActivity().supportFragmentManager, null)
                        true
                    })

                    val currentRating = media.userRating

                    if (currentRating != null) {
                        songRatingBar!!.rating = currentRating.toFloat()
                    } else {
                        songRatingBar!!.rating = 0f
                    }

                    songRatingBar!!.onRatingBarChangeListener = object :
                        OnRatingBarChangeListener {
                        override fun onRatingChanged(
                            ratingBar: RatingBar?,
                            rating: Float,
                            fromUser: Boolean
                        ) {
                            if (fromUser) {
                                ratingViewModel!!.rate(rating.toInt())
                                media.userRating = rating.toInt()
                            }
                        }
                    }


                    if (activity != null) {
                        playerBottomSheetViewModel!!.refreshMediaInfo(requireActivity(), media)
                    }
                }
            })
    }

    private fun initMediaLabelButton() {
        playerBottomSheetViewModel!!.getLiveAlbum()
            .observe(getViewLifecycleOwner(), Observer { album: AlbumID3? ->
                if (album != null) {
                    playerMediaTitleLabel!!.setOnClickListener(View.OnClickListener { view: View? ->
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.ALBUM_OBJECT, album)
                        NavHostFragment.findNavController(this)
                            .navigate(R.id.albumPageFragment, bundle)
                        activity!!.collapseBottomSheetDelayed()
                    })
                }
            })
    }

    private fun initArtistLabelButton() {
        playerBottomSheetViewModel!!.getLiveArtist()
            .observe(getViewLifecycleOwner(), Observer { artist: ArtistID3? ->
                if (artist != null) {
                    playerArtistNameLabel!!.setOnClickListener(View.OnClickListener { view: View? ->
                        val bundle = Bundle()
                        bundle.putParcelable(Constants.ARTIST_OBJECT, artist)
                        NavHostFragment.findNavController(this)
                            .navigate(R.id.artistPageFragment, bundle)
                        activity!!.collapseBottomSheetDelayed()
                    })
                }
            })
    }

    private fun initPlaybackSpeedButton(mediaBrowser: MediaBrowser) {
        playbackSpeedButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            val currentSpeed = getPlaybackSpeed()
            if (currentSpeed == Constants.MEDIA_PLAYBACK_SPEED_080) {
                mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_100))
                playbackSpeedButton!!.text = getString(
                    R.string.player_playback_speed,
                    Constants.MEDIA_PLAYBACK_SPEED_100
                )
                setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_100)
            } else if (currentSpeed == Constants.MEDIA_PLAYBACK_SPEED_100) {
                mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_125))
                playbackSpeedButton!!.text = getString(
                    R.string.player_playback_speed,
                    Constants.MEDIA_PLAYBACK_SPEED_125
                )
                setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_125)
            } else if (currentSpeed == Constants.MEDIA_PLAYBACK_SPEED_125) {
                mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_150))
                playbackSpeedButton!!.text = getString(
                    R.string.player_playback_speed,
                    Constants.MEDIA_PLAYBACK_SPEED_150
                )
                setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_150)
            } else if (currentSpeed == Constants.MEDIA_PLAYBACK_SPEED_150) {
                mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_175))
                playbackSpeedButton!!.text = getString(
                    R.string.player_playback_speed,
                    Constants.MEDIA_PLAYBACK_SPEED_175
                )
                setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_175)
            } else if (currentSpeed == Constants.MEDIA_PLAYBACK_SPEED_175) {
                mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_200))
                playbackSpeedButton!!.text = getString(
                    R.string.player_playback_speed,
                    Constants.MEDIA_PLAYBACK_SPEED_200
                )
                setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_200)
            } else if (currentSpeed == Constants.MEDIA_PLAYBACK_SPEED_200) {
                mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_080))
                playbackSpeedButton!!.text = getString(
                    R.string.player_playback_speed,
                    Constants.MEDIA_PLAYBACK_SPEED_080
                )
                setPlaybackSpeed(Constants.MEDIA_PLAYBACK_SPEED_080)
            }
        })

        skipSilenceToggleButton!!.setOnClickListener(View.OnClickListener { view: View? ->
            setSkipSilenceMode(!skipSilenceToggleButton!!.isChecked)
        })
    }

    fun goToControllerPage() {
        playerMediaCoverViewPager!!.setCurrentItem(0, false)
    }

    fun goToLyricsPage() {
        playerMediaCoverViewPager!!.setCurrentItem(1, true)
    }

    private fun checkAndSetRatingContainerVisibility() {
        if (ratingContainer == null) return

        if (showItemStarRating()) {
            ratingContainer!!.visibility = View.VISIBLE
        } else {
            ratingContainer!!.visibility = View.GONE
        }
    }

    private fun setPlaybackParameters(mediaBrowser: MediaBrowser) {
        val playbackSpeedButton =
            bind!!.getRoot().findViewById<Button>(R.id.player_playback_speed_button)
        val currentSpeed = getPlaybackSpeed()
        val skipSilence = isSkipSilenceMode()

        mediaBrowser.setPlaybackParameters(PlaybackParameters(currentSpeed))
        playbackSpeedButton.text = getString(R.string.player_playback_speed, currentSpeed)

        // TODO Skippare il silenzio
        skipSilenceToggleButton!!.setChecked(skipSilence)
    }

    private fun resetPlaybackParameters(mediaBrowser: MediaBrowser) {
        mediaBrowser.setPlaybackParameters(PlaybackParameters(Constants.MEDIA_PLAYBACK_SPEED_100))
        // TODO Resettare lo skip del silenzio
    }

    companion object {
        private const val TAG = "PlayerCoverFragment"
    }
}