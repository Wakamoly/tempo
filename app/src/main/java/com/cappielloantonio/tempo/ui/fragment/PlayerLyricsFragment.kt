package com.cappielloantonio.tempo.ui.fragment

import android.annotation.SuppressLint
import android.content.ComponentName
import android.os.Bundle
import android.os.Handler
import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.InnerFragmentPlayerLyricsBinding
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.Line
import com.cappielloantonio.tempo.subsonic.models.LyricsList
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.OpenSubsonicExtensionsUtil
import com.cappielloantonio.tempo.util.Preferences.isDisplayAlwaysOn
import com.cappielloantonio.tempo.viewmodel.PlayerBottomSheetViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import kotlin.math.max

@OptIn(markerClass = [UnstableApi::class])
class PlayerLyricsFragment : Fragment() {
    private var bind: InnerFragmentPlayerLyricsBinding? = null
    private var playerBottomSheetViewModel: PlayerBottomSheetViewModel? = null
    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser>? = null
    private var mediaBrowser: MediaBrowser? = null
    private var syncLyricsHandler: Handler? = null
    private var syncLyricsRunnable: Runnable? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = InnerFragmentPlayerLyricsBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()

        playerBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<PlayerBottomSheetViewModel>(
                PlayerBottomSheetViewModel::class.java,
            )

        initOverlay()

        return view
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?,
    ) {
        super.onViewCreated(view, savedInstanceState)

        initPanelContent()
    }

    override fun onStart() {
        super.onStart()
        initializeBrowser()
    }

    override fun onResume() {
        super.onResume()
        bindMediaController()
        requireActivity().window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
    }

    override fun onPause() {
        super.onPause()
        releaseHandler()
        if (!isDisplayAlwaysOn()) {
            requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    override fun onStop() {
        releaseBrowser()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun initOverlay() {
        bind!!.syncLyricsTapButton.setOnClickListener(
            View.OnClickListener { view: View? ->
                playerBottomSheetViewModel!!.changeSyncLyricsState()
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

    private fun releaseHandler() {
        if (syncLyricsHandler != null) {
            syncLyricsHandler!!.removeCallbacks(syncLyricsRunnable!!)
            syncLyricsHandler = null
        }
    }

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture!!)
    }

    private fun bindMediaController() {
        mediaBrowserListenableFuture!!.addListener(
            Runnable {
                try {
                    mediaBrowser = mediaBrowserListenableFuture!!.get()
                    defineProgressHandler()
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    private fun initPanelContent() {
        if (OpenSubsonicExtensionsUtil.isSongLyricsExtensionAvailable()) {
            playerBottomSheetViewModel!!
                .getLiveLyricsList()
                .observe(
                    getViewLifecycleOwner(),
                    Observer { lyricsList: LyricsList? ->
                        setPanelContent(null, lyricsList)
                    },
                )
        } else {
            playerBottomSheetViewModel!!
                .getLiveLyrics()
                .observe(
                    getViewLifecycleOwner(),
                    Observer { lyrics: String? ->
                        setPanelContent(lyrics, null)
                    },
                )
        }
    }

    private fun setPanelContent(
        lyrics: String?,
        lyricsList: LyricsList?,
    ) {
        playerBottomSheetViewModel!!
            .getLiveDescription()
            .observe(
                getViewLifecycleOwner(),
                Observer { description: String? ->
                    if (bind != null) {
                        bind!!.nowPlayingSongLyricsSrollView.smoothScrollTo(0, 0)

                        if (lyrics != null && lyrics.trim { it <= ' ' } != "") {
                            bind!!.nowPlayingSongLyricsTextView.text =
                                MusicUtil.getReadableLyrics(
                                    lyrics,
                                )
                            bind!!.nowPlayingSongLyricsTextView.visibility = View.VISIBLE
                            bind!!.emptyDescriptionImageView.setVisibility(View.GONE)
                            bind!!.titleEmptyDescriptionLabel.visibility = View.GONE
                            bind!!.syncLyricsTapButton.visibility = View.GONE
                        } else if (lyricsList != null && lyricsList.structuredLyrics != null) {
                            setSyncLirics(lyricsList)
                            bind!!.nowPlayingSongLyricsTextView.visibility = View.VISIBLE
                            bind!!.emptyDescriptionImageView.setVisibility(View.GONE)
                            bind!!.titleEmptyDescriptionLabel.visibility = View.GONE
                            bind!!.syncLyricsTapButton.visibility = View.VISIBLE
                        } else if (description != null && description.trim { it <= ' ' } != "") {
                            bind!!.nowPlayingSongLyricsTextView.text =
                                MusicUtil.getReadableLyrics(
                                    description,
                                )
                            bind!!.nowPlayingSongLyricsTextView.visibility = View.VISIBLE
                            bind!!.emptyDescriptionImageView.setVisibility(View.GONE)
                            bind!!.titleEmptyDescriptionLabel.visibility = View.GONE
                            bind!!.syncLyricsTapButton.visibility = View.GONE
                        } else {
                            bind!!.nowPlayingSongLyricsTextView.visibility = View.GONE
                            bind!!.emptyDescriptionImageView.setVisibility(View.VISIBLE)
                            bind!!.titleEmptyDescriptionLabel.visibility = View.VISIBLE
                            bind!!.syncLyricsTapButton.visibility = View.GONE
                        }
                    }
                },
            )
    }

    @SuppressLint("DefaultLocale")
    private fun setSyncLirics(lyricsList: LyricsList) {
        if (lyricsList.structuredLyrics != null && !lyricsList.structuredLyrics!!.isEmpty() && lyricsList.structuredLyrics!!
                .get(
                    0,
                ).line != null
        ) {
            val lyricsBuilder = StringBuilder()
            val lines: MutableList<Line>? = lyricsList.structuredLyrics!!.get(0).line

            if (lines != null) {
                for (line in lines) {
                    lyricsBuilder.append(line.value.trim { it <= ' ' }).append("\n")
                }
            }

            bind!!.nowPlayingSongLyricsTextView.text = lyricsBuilder.toString()
        }
    }

    private fun defineProgressHandler() {
        playerBottomSheetViewModel!!
            .getLiveLyricsList()
            .observe(
                getViewLifecycleOwner(),
                Observer { lyricsList: LyricsList? ->
                    if (lyricsList != null) {
                        if (lyricsList.structuredLyrics != null && lyricsList.structuredLyrics!!.get(0) != null &&
                            !lyricsList.structuredLyrics!!
                                .get(
                                    0,
                                ).synced
                        ) {
                            releaseHandler()
                            return@observe
                        }

                        syncLyricsHandler = Handler()
                        syncLyricsRunnable =
                            Runnable {
                                if (syncLyricsHandler != null) {
                                    if (bind != null) {
                                        displaySyncedLyrics()
                                    }

                                    syncLyricsHandler!!.postDelayed(syncLyricsRunnable!!, 250)
                                }
                            }

                        syncLyricsHandler!!.postDelayed(syncLyricsRunnable!!, 250)
                    } else {
                        releaseHandler()
                    }
                },
            )
    }

    private fun displaySyncedLyrics() {
        val lyricsList = playerBottomSheetViewModel!!.getLiveLyricsList().getValue()
        val timestamp = (mediaBrowser!!.getCurrentPosition()).toInt()

        if (lyricsList != null && lyricsList.structuredLyrics != null && !lyricsList.structuredLyrics!!.isEmpty() &&
            lyricsList.structuredLyrics!!
                .get(
                    0,
                ).line != null
        ) {
            val lyricsBuilder = StringBuilder()
            val lines: MutableList<Line>? = lyricsList.structuredLyrics!!.get(0).line

            if (lines == null || lines.isEmpty()) return

            for (line in lines) {
                lyricsBuilder.append(line.value.trim { it <= ' ' }).append("\n")
            }

            val toHighlight =
                lines
                    .stream()
                    .filter { line: Line? -> line != null && line.start != null && line.start!! < timestamp }
                    .reduce { first: Line?, second: Line? -> second }
                    .orElse(null)

            if (toHighlight != null) {
                val lyrics = lyricsBuilder.toString()
                val spannableString: Spannable = SpannableString(lyrics)

                val startingPosition = getStartPosition(lines, toHighlight)
                val endingPosition = startingPosition + toHighlight.value.length

                spannableString.setSpan(
                    ForegroundColorSpan(
                        requireContext()
                            .resources
                            .getColor(R.color.shadowsLyricsTextColor, null),
                    ),
                    0,
                    lyrics.length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )
                spannableString.setSpan(
                    ForegroundColorSpan(
                        requireContext().resources.getColor(R.color.lyricsTextColor, null),
                    ),
                    startingPosition,
                    endingPosition,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE,
                )

                bind!!.nowPlayingSongLyricsTextView.text = spannableString

                if (playerBottomSheetViewModel!!.getSyncLyricsState()) {
                    bind!!.nowPlayingSongLyricsSrollView.smoothScrollTo(
                        0,
                        getScroll(lines, toHighlight),
                    )
                }
            }
        }
    }

    private fun getStartPosition(
        lines: MutableList<Line>,
        toHighlight: Line?,
    ): Int {
        var start = 0

        for (line in lines) {
            if (line != toHighlight) {
                start = start + line.value.length + 1
            } else {
                break
            }
        }

        return start
    }

    private fun getLineCount(
        lines: MutableList<Line>,
        toHighlight: Line?,
    ): Int {
        var start = 0

        for (line in lines) {
            if (line != toHighlight) {
                bind!!.tempLyricsLineTextView.text = line.value
                start = start + bind!!.tempLyricsLineTextView.lineCount
            } else {
                break
            }
        }

        return start
    }

    private fun getScroll(
        lines: MutableList<Line>,
        toHighlight: Line?,
    ): Int {
        val startIndex = getStartPosition(lines, toHighlight)
        val layout = bind!!.nowPlayingSongLyricsTextView.layout
        if (layout == null) return 0

        val line = layout.getLineForOffset(startIndex)
        val lineTop = layout.getLineTop(line)
        val lineBottom = layout.getLineBottom(line)
        val lineCenter = (lineTop + lineBottom) / 2

        val scrollViewHeight = bind!!.nowPlayingSongLyricsSrollView.height
        val scroll = lineCenter - scrollViewHeight / 2

        return max(scroll, 0)
    }

    companion object {
        private const val TAG = "PlayerLyricsFragment"
    }
}
