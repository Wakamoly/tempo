package com.cappielloantonio.tempo.util

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.Uri
import android.text.Html
import android.util.Log
import com.cappielloantonio.tempo.App.Companion.getContext
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.repository.DownloadRepository
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.MusicUtil.getReadableDurationString
import com.cappielloantonio.tempo.util.Preferences.askForEstimateContentLength
import com.cappielloantonio.tempo.util.Preferences.getAudioTranscodeFormatMobile
import com.cappielloantonio.tempo.util.Preferences.getAudioTranscodeFormatTranscodedDownload
import com.cappielloantonio.tempo.util.Preferences.getAudioTranscodeFormatWifi
import com.cappielloantonio.tempo.util.Preferences.getBitrateTranscodedDownload
import com.cappielloantonio.tempo.util.Preferences.getMaxBitrateMobile
import com.cappielloantonio.tempo.util.Preferences.getMaxBitrateWifi
import com.cappielloantonio.tempo.util.Preferences.getMinStarRatingAccepted
import com.cappielloantonio.tempo.util.Preferences.isServerPrioritized
import com.cappielloantonio.tempo.util.Preferences.isServerPrioritizedInTranscodedDownload
import com.cappielloantonio.tempo.util.Preferences.showAudioQuality
import java.lang.Long
import java.text.CharacterIterator
import java.text.DecimalFormat
import java.text.StringCharacterIterator
import java.util.Locale
import java.util.function.IntFunction
import java.util.stream.Collectors
import kotlin.Boolean
import kotlin.Int
import kotlin.String
import kotlin.compareTo
import kotlin.div
import kotlin.math.abs
import kotlin.math.min
import kotlin.rem
import kotlin.shr
import kotlin.text.StringBuilder
import kotlin.text.format
import kotlin.text.isEmpty
import kotlin.text.replace
import kotlin.text.toRegex
import kotlin.times
import kotlin.toString

object MusicUtil {
    private const val TAG = "MusicUtil"

    fun getStreamUri(id: String?): Uri? {
        val params = getSubsonicClientInstance(false).getParams()

        val uri = StringBuilder()

        uri.append(getSubsonicClientInstance(false).getUrl())
        uri.append("stream")

        if (params.containsKey("u") && params.get("u") != null) {
            uri
                .append("?u=")
                .append(Util.encode(params.get("u")))
        }
        if (params.containsKey("p") && params.get("p") != null) {
            uri
                .append("&p=")
                .append(params.get("p"))
        }
        if (params.containsKey("s") && params.get("s") != null) {
            uri
                .append("&s=")
                .append(params.get("s"))
        }
        if (params.containsKey("t") && params.get("t") != null) {
            uri
                .append("&t=")
                .append(params.get("t"))
        }
        if (params.containsKey("v") && params.get("v") != null) {
            uri
                .append("&v=")
                .append(params.get("v"))
        }
        if (params.containsKey("c") && params.get("c") != null) {
            uri
                .append("&c=")
                .append(params.get("c"))
        }

        if (!isServerPrioritized()) {
            uri.append("&maxBitRate=").append(
                bitratePreference,
            )
        }
        if (!isServerPrioritized()) {
            uri.append("&format=").append(
                transcodingFormatPreference,
            )
        }
        if (askForEstimateContentLength()) uri.append("&estimateContentLength=true")

        uri.append("&id=").append(id)

        Log.d(TAG, "getStreamUri: " + uri)

        return Uri.parse(uri.toString())
    }

    fun getDownloadUri(id: String?): Uri? {
        val uri = StringBuilder()

        val download = DownloadRepository().getDownload(id)

        if (download == null || download.downloadUri!!.isEmpty()) {
            val params = getSubsonicClientInstance(false).getParams()

            uri.append(getSubsonicClientInstance(false).getUrl())
            uri.append("download")

            if (params.containsKey("u") && params.get("u") != null) {
                uri
                    .append("?u=")
                    .append(Util.encode(params.get("u")))
            }
            if (params.containsKey("p") && params.get("p") != null) {
                uri
                    .append("&p=")
                    .append(params.get("p"))
            }
            if (params.containsKey("s") && params.get("s") != null) {
                uri
                    .append("&s=")
                    .append(params.get("s"))
            }
            if (params.containsKey("t") && params.get("t") != null) {
                uri
                    .append("&t=")
                    .append(params.get("t"))
            }
            if (params.containsKey("v") && params.get("v") != null) {
                uri
                    .append("&v=")
                    .append(params.get("v"))
            }
            if (params.containsKey("c") && params.get("c") != null) {
                uri
                    .append("&c=")
                    .append(params.get("c"))
            }

            uri.append("&id=").append(id)
        } else {
            uri.append(download.downloadUri)
        }

        Log.d(TAG, "getDownloadUri: " + uri)

        return Uri.parse(uri.toString())
    }

    fun getTranscodedDownloadUri(id: String?): Uri? {
        val params = getSubsonicClientInstance(false).getParams()

        val uri = StringBuilder()

        uri.append(getSubsonicClientInstance(false).getUrl())
        uri.append("stream")

        if (params.containsKey("u") && params.get("u") != null) {
            uri
                .append("?u=")
                .append(Util.encode(params.get("u")))
        }
        if (params.containsKey("p") && params.get("p") != null) {
            uri
                .append("&p=")
                .append(params.get("p"))
        }
        if (params.containsKey("s") && params.get("s") != null) {
            uri
                .append("&s=")
                .append(params.get("s"))
        }
        if (params.containsKey("t") && params.get("t") != null) {
            uri
                .append("&t=")
                .append(params.get("t"))
        }
        if (params.containsKey("v") && params.get("v") != null) {
            uri
                .append("&v=")
                .append(params.get("v"))
        }
        if (params.containsKey("c") && params.get("c") != null) {
            uri
                .append("&c=")
                .append(params.get("c"))
        }

        if (!isServerPrioritizedInTranscodedDownload()) {
            uri.append("&maxBitRate=").append(
                bitratePreferenceForDownload,
            )
        }
        if (!isServerPrioritizedInTranscodedDownload()) {
            uri.append("&format=").append(
                transcodingFormatPreferenceForDownload,
            )
        }

        uri.append("&id=").append(id)

        Log.d(TAG, "getTranscodedDownloadUri: " + uri)

        return Uri.parse(uri.toString())
    }

    fun getReadableDurationString(
        duration: Long?,
        millis: Boolean,
    ): String {
        val lenght = if (duration != null) duration else 0

        var minutes: Long
        val seconds: Long

        if (millis) {
            minutes = (lenght / 1000) / 60
            seconds = (lenght / 1000) % 60
        } else {
            minutes = lenght / 60
            seconds = lenght % 60
        }

        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%01d:%02d", minutes, seconds)
        } else {
            val hours = minutes / 60
            minutes = minutes % 60
            return String.format(Locale.getDefault(), "%d:%02d:%02d", hours, minutes, seconds)
        }
    }

    fun getReadableDurationString(
        duration: Int?,
        millis: Boolean,
    ): String {
        val lenght = (if (duration != null) duration else 0).toLong()
        return getReadableDurationString(lenght, millis)
    }

    fun getReadableAudioQualityString(child: Child): String {
        if (!showAudioQuality() || child.bitrate == null) return ""

        return "•" +
            " " +
            child.bitrate +
            "kbps" +
            " • " +
            (
                if (child.bitDepth != null && child.bitDepth != 0) {
                    child.bitDepth.toString() + "/" + (if (child.samplingRate != null) child.samplingRate!! / 1000 else "")
                } else {
                    (
                        if (child.samplingRate != null) {
                            DecimalFormat("0.#").format(child.samplingRate!! / 1000.0) + "kHz"
                        } else {
                            ""
                        }
                    )
                }
            ) +
            " " +
            child.suffix
    }

    fun getReadablePodcastDurationString(duration: Long): String {
        var minutes = duration / 60

        if (minutes < 60) {
            return String.format(Locale.getDefault(), "%01d min", minutes)
        } else {
            val hours = minutes / 60
            minutes = minutes % 60
            return String.format(Locale.getDefault(), "%d h %02d min", hours, minutes)
        }
    }

    fun getReadableTrackNumber(
        context: Context,
        trackNumber: Int?,
    ): String {
        if (trackNumber != null) {
            return trackNumber.toString()
        }

        return context.getString(R.string.label_placeholder)
    }

    fun getReadableString(string: String?): String {
        if (string != null) {
            return Html.fromHtml(string, Html.FROM_HTML_MODE_COMPACT).toString()
        }

        return ""
    }

    fun forceReadableString(string: String?): String {
        if (string != null) {
            return getReadableString(string)
                .replace("&#34;".toRegex(), "\"")
                .replace("&#39;".toRegex(), "'")
                .replace("&amp;".toRegex(), "'")
                .replace("<a\\s+([^>]+)>((?:.(?!</a>))*.)</a>".toRegex(), "")
        }

        return ""
    }

    fun getReadableLyrics(string: String?): String {
        if (string != null) {
            return string
                .replace("&#34;".toRegex(), "\"")
                .replace("&#39;".toRegex(), "'")
                .replace("&amp;".toRegex(), "'")
                .replace("&#xA;".toRegex(), "\n")
        }

        return ""
    }

    fun getReadableByteCount(bytes: Long): String {
        val absB = if (bytes == Long.Companion.MIN_VALUE) Long.Companion.MAX_VALUE else abs(bytes)

        if (absB < 1024) {
            return bytes.toString() + " B"
        }

        var value = absB

        val ci: CharacterIterator = StringCharacterIterator("KMGTPE")

        var i = 40
        while (i >= 0 && absB > 0xfffccccccccccccL shr i) {
            value = value shr 10
            ci.next()
            i -= 10
        }

        value *= Long.signum(bytes).toLong()

        return String.format("%.1f %ciB", value / 1024.0, ci.current())
    }

    fun passwordHexEncoding(plainPassword: String): String =
        "enc:" +
            plainPassword
                .chars()
                .mapToObj<String?>(IntFunction { i: Int -> Integer.toHexString(i) })
                .collect(Collectors.joining())

    val bitratePreference: String
        get() {
            val network: Network? = connectivityManager!!.activeNetwork
            val networkCapabilities: NetworkCapabilities? =
                connectivityManager!!.getNetworkCapabilities(network)
            val audioTranscodeFormat: String = transcodingFormatPreference

            if (audioTranscodeFormat == "raw" || network == null || networkCapabilities == null) return "0"

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return getMaxBitrateWifi()
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return getMaxBitrateMobile()
            } else {
                return getMaxBitrateWifi()
            }
        }

    val transcodingFormatPreference: String
        get() {
            val network: Network? = connectivityManager!!.activeNetwork
            val networkCapabilities: NetworkCapabilities? =
                connectivityManager!!.getNetworkCapabilities(network)

            if (network == null || networkCapabilities == null) return "raw"

            if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
                return getAudioTranscodeFormatWifi()
            } else if (networkCapabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) {
                return getAudioTranscodeFormatMobile()
            } else {
                return getAudioTranscodeFormatWifi()
            }
        }

    val bitratePreferenceForDownload: String
        get() {
            val audioTranscodeFormat: String =
                transcodingFormatPreferenceForDownload

            if (audioTranscodeFormat == "raw") return "0"

            return getBitrateTranscodedDownload()
        }

    val transcodingFormatPreferenceForDownload: String
        get() = getAudioTranscodeFormatTranscodedDownload()

    fun limitPlayableMedia(
        toLimit: MutableList<Child?>,
        position: Int,
    ): MutableList<Child?> {
        if (!toLimit.isEmpty() && toLimit.size > Constants.PLAYABLE_MEDIA_LIMIT) {
            val from =
                if (position < Constants.PRE_PLAYABLE_MEDIA) 0 else position - Constants.PRE_PLAYABLE_MEDIA
            val to = min(from + Constants.PLAYABLE_MEDIA_LIMIT, toLimit.size)

            return toLimit.subList(from, to)
        }

        return toLimit
    }

    fun getPlayableMediaPosition(
        toLimit: MutableList<Child?>,
        position: Int,
    ): Int {
        if (!toLimit.isEmpty() && toLimit.size > Constants.PLAYABLE_MEDIA_LIMIT) {
            return min(position, Constants.PRE_PLAYABLE_MEDIA)
        }

        return position
    }

    private val connectivityManager: ConnectivityManager?
        get() =
            getContext()
                .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager?

    fun ratingFilter(toFilter: MutableList<Child?>?) {
        if (toFilter == null || toFilter.isEmpty()) return

        val filtered =
            toFilter
                .stream()
                .filter { child: Child? ->
                    (child!!.userRating != null && child.userRating!! >= getMinStarRatingAccepted()) ||
                        (child.userRating == null)
                }.collect(Collectors.toList())

        toFilter.clear()

        toFilter.addAll(filtered)
    }
}
