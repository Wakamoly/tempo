package com.cappielloantonio.tempo.ui.dialog

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.media3.common.MediaMetadata
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.DialogTrackInfoBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.isServerPrioritized
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class TrackInfoDialog(private val mediaMetadata: MediaMetadata) : DialogFragment() {
    private var bind: DialogTrackInfoBinding? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        bind = DialogTrackInfoBinding.inflate(getLayoutInflater())

        return MaterialAlertDialogBuilder(requireActivity())
            .setView(bind!!.getRoot())
            .setPositiveButton(
                R.string.track_info_dialog_positive_button,
                DialogInterface.OnClickListener { dialog: DialogInterface?, id: Int -> dialog!!.cancel() })
            .create()
    }

    override fun onStart() {
        super.onStart()

        setTrackInfo()
        setTrackTranscodingInfo()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun setTrackInfo() {
        bind!!.trakTitleInfoTextView.text = mediaMetadata.title
        bind!!.trakArtistInfoTextView.text = if (mediaMetadata.artist != null)
            mediaMetadata.artist
        else
            if (mediaMetadata.extras != null && mediaMetadata.extras!!.getString("type") == Constants.MEDIA_TYPE_RADIO)
                mediaMetadata.extras!!.getString("uri", getString(R.string.label_placeholder))
            else
                ""

        if (mediaMetadata.extras != null) {
            CustomGlideRequest.Builder.Companion.from(
                requireContext(),
                mediaMetadata.extras!!.getString("coverArtId", ""),
                CustomGlideRequest.ResourceType.Song
            )
                .build()
                .into(bind!!.trackCoverInfoImageView)

            bind!!.titleValueSector.text = mediaMetadata.extras!!.getString(
                "title",
                getString(R.string.label_placeholder)
            )
            bind!!.albumValueSector.text = mediaMetadata.extras!!.getString(
                "album",
                getString(R.string.label_placeholder)
            )
            bind!!.artistValueSector.text = mediaMetadata.extras!!.getString(
                "artist",
                getString(R.string.label_placeholder)
            )
            bind!!.trackNumberValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "track",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("track", 0)
                .toString() else getString(R.string.label_placeholder)
            bind!!.yearValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "year",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("year", 0)
                .toString() else getString(R.string.label_placeholder)
            bind!!.genreValueSector.text = mediaMetadata.extras!!.getString(
                "genre",
                getString(R.string.label_placeholder)
            )
            bind!!.sizeValueSector.text = if (mediaMetadata.extras!!.getLong(
                    "size",
                    0
                ) != 0L
            ) MusicUtil.getReadableByteCount(
                mediaMetadata.extras!!.getLong(
                    "size",
                    0
                )
            ) else getString(R.string.label_placeholder)
            bind!!.contentTypeValueSector.text = mediaMetadata.extras!!.getString(
                "contentType",
                getString(R.string.label_placeholder)
            )
            bind!!.suffixValueSector.text = mediaMetadata.extras!!.getString(
                "suffix",
                getString(R.string.label_placeholder)
            )
            bind!!.transcodedContentTypeValueSector.text = mediaMetadata.extras!!.getString(
                "transcodedContentType",
                getString(R.string.label_placeholder)
            )
            bind!!.transcodedSuffixValueSector.text = mediaMetadata.extras!!.getString(
                "transcodedSuffix",
                getString(R.string.label_placeholder)
            )
            bind!!.durationValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "duration",
                    0
                ) != 0
            ) MusicUtil.getReadableDurationString(
                mediaMetadata.extras!!.getInt("duration", 0),
                false
            ) else getString(R.string.label_placeholder)
            bind!!.bitrateValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "bitrate",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("bitrate", 0)
                .toString() + " kbps" else getString(R.string.label_placeholder)
            bind!!.samplingRateValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "samplingRate",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("samplingRate", 0)
                .toString() + " Hz" else getString(R.string.label_placeholder)
            bind!!.bitDepthValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "bitDepth",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("bitDepth", 0)
                .toString() + " bits" else getString(R.string.label_placeholder)
            bind!!.pathValueSector.text = mediaMetadata.extras!!.getString(
                "path",
                getString(R.string.label_placeholder)
            )
            bind!!.discNumberValueSector.text = if (mediaMetadata.extras!!.getInt(
                    "discNumber",
                    0
                ) != 0
            ) mediaMetadata.extras!!.getInt("discNumber", 0)
                .toString() else getString(R.string.label_placeholder)
        }
    }

    private fun setTrackTranscodingInfo() {
        val info = StringBuilder()

        val prioritizeServerTranscoding = isServerPrioritized()

        val transcodingExtension = MusicUtil.getTranscodingFormatPreference()
        val transcodingBitrate =
            if (MusicUtil.getBitratePreference().toInt() != 0) MusicUtil.getBitratePreference()
                .toInt().toString() + "kbps" else "Original"

        if (mediaMetadata.extras != null && mediaMetadata.extras!!.getString("uri", "")
                .contains(Constants.DOWNLOAD_URI)
        ) {
            info.append(getString(R.string.track_info_summary_downloaded_file))

            bind!!.trakTranscodingInfoTextView.text = info
            return
        }

        if (prioritizeServerTranscoding) {
            info.append(getString(R.string.track_info_summary_server_prioritized))

            bind!!.trakTranscodingInfoTextView.text = info
            return
        }

        if (!prioritizeServerTranscoding && transcodingExtension == "raw" && transcodingBitrate == "Original") {
            info.append(getString(R.string.track_info_summary_original_file))

            bind!!.trakTranscodingInfoTextView.text = info
            return
        }

        if (!prioritizeServerTranscoding && (transcodingExtension != "raw") && transcodingBitrate == "Original") {
            info.append(
                getString(
                    R.string.track_info_summary_transcoding_codec,
                    transcodingExtension
                )
            )

            bind!!.trakTranscodingInfoTextView.text = info
            return
        }

        if (!prioritizeServerTranscoding && transcodingExtension == "raw" && (transcodingBitrate != "Original")) {
            info.append(
                getString(
                    R.string.track_info_summary_transcoding_bitrate,
                    transcodingBitrate
                )
            )

            bind!!.trakTranscodingInfoTextView.text = info
            return
        }

        if (!prioritizeServerTranscoding && (transcodingExtension != "raw") && (transcodingBitrate != "Original")) {
            info.append(
                getString(
                    R.string.track_info_summary_full_transcode,
                    transcodingExtension,
                    transcodingBitrate
                )
            )

            bind!!.trakTranscodingInfoTextView.text = info
        }
    }
}
