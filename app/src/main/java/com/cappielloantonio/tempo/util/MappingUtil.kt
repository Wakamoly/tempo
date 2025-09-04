package com.cappielloantonio.tempo.util

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.App.Companion.getContext
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.repository.DownloadRepository
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation
import com.cappielloantonio.tempo.subsonic.models.PodcastEpisode
import com.cappielloantonio.tempo.util.Preferences.getImageSize
import com.cappielloantonio.tempo.util.Preferences.preferTranscodedDownload

@OptIn(markerClass = [UnstableApi::class])
object MappingUtil {
    fun mapMediaItems(items: MutableList<Child>): MutableList<MediaItem> {
        val mediaItems = ArrayList<MediaItem>()
        for (i in items.indices) {
            mediaItems.add(mapMediaItem(items[i]))
        }
        return mediaItems
    }

    @Suppress("D")
    fun mapMediaItem(media: Child): MediaItem {
        val uri = getUri(media)
        val artworkUri = CustomGlideRequest.createUrl(media.coverArtId, getImageSize()).toUri()

        val bundle =
            Bundle().apply {
                putString("id", media.id)
                putString("parentId", media.parentId)
                putBoolean("isDir", media.isDir)
                putString("title", media.title)
                putString("album", media.album)
                putString("artist", media.artist)
                putInt("track", (if (media.track != null) media.track else 0)!!)
                putInt("year", (if (media.year != null) media.year else 0)!!)
                putString("genre", media.genre)
                putString("coverArtId", media.coverArtId)
                putLong("size", (if (media.size != null) media.size else 0)!!)
                putString("contentType", media.contentType)
                putString("suffix", media.suffix)
                putString("transcodedContentType", media.transcodedContentType)
                putString("transcodedSuffix", media.transcodedSuffix)
                putInt("duration", (if (media.duration != null) media.duration else 0)!!)
                putInt("bitrate", (if (media.bitrate != null) media.bitrate else 0)!!)
                putInt(
                    "samplingRate",
                    (if (media.samplingRate != null) media.samplingRate else 0)!!,
                )
                putInt("bitDepth", (if (media.bitDepth != null) media.bitDepth else 0)!!)
                putString("path", media.path)
                putBoolean("isVideo", media.isVideo)
                putInt("userRating", (if (media.userRating != null) media.userRating else 0)!!)
                putDouble(
                    "averageRating",
                    (if (media.averageRating != null) media.averageRating else 0.0)!!,
                )
                putLong("playCount", (if (media.playCount != null) media.playCount else 0)!!)
                putInt("discNumber", (if (media.discNumber != null) media.discNumber else 0)!!)
                putLong("created", if (media.created != null) media.created!!.time else 0)
                putLong("starred", if (media.starred != null) media.starred!!.time else 0)
                putString("albumId", media.albumId)
                putString("artistId", media.artistId)
                putString("type", Constants.MEDIA_TYPE_MUSIC)
                putLong(
                    "bookmarkPosition",
                    (if (media.bookmarkPosition != null) media.bookmarkPosition else 0)!!,
                )
                putInt(
                    "originalWidth",
                    (if (media.originalWidth != null) media.originalWidth else 0)!!,
                )
                putInt(
                    "originalHeight",
                    (if (media.originalHeight != null) media.originalHeight else 0)!!,
                )
                putString("uri", uri.toString())
            }

        return MediaItem
            .Builder()
            .setMediaId(media.id)
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(media.title)
                    .setTrackNumber(if (media.track != null) media.track else 0)
                    .setDiscNumber(if (media.discNumber != null) media.discNumber else 0)
                    .setReleaseYear(if (media.year != null) media.year else 0)
                    .setAlbumTitle(media.album)
                    .setArtist(media.artist)
                    .setArtworkUri(artworkUri)
                    .setExtras(bundle)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build(),
            ).setRequestMetadata(
                MediaItem.RequestMetadata
                    .Builder()
                    .setMediaUri(uri)
                    .setExtras(bundle)
                    .build(),
            ).setMimeType(MimeTypes.BASE_TYPE_AUDIO)
            .setUri(uri)
            .build()
    }

    fun mapDownloads(items: MutableList<Child?>): MutableList<MediaItem?> {
        val downloads = ArrayList<MediaItem?>()

        for (i in items.indices) {
            downloads.add(mapDownload(items.get(i)!!))
        }

        return downloads
    }

    fun mapDownload(media: Child): MediaItem =
        MediaItem
            .Builder()
            .setMediaId(media.id)
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(media.title)
                    .setTrackNumber(if (media.track != null) media.track else 0)
                    .setDiscNumber(if (media.discNumber != null) media.discNumber else 0)
                    .setReleaseYear(if (media.year != null) media.year else 0)
                    .setAlbumTitle(media.album)
                    .setArtist(media.artist)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build(),
            ).setRequestMetadata(
                MediaItem.RequestMetadata
                    .Builder()
                    .setMediaUri(
                        if (preferTranscodedDownload()) {
                            MusicUtil.getTranscodedDownloadUri(
                                media.id,
                            )
                        } else {
                            MusicUtil.getDownloadUri(media.id)
                        },
                    ).build(),
            ).setMimeType(MimeTypes.BASE_TYPE_AUDIO)
            .setUri(
                if (preferTranscodedDownload()) {
                    MusicUtil.getTranscodedDownloadUri(media.id)
                } else {
                    MusicUtil.getDownloadUri(
                        media.id,
                    )
                },
            ).build()

    fun mapInternetRadioStation(internetRadioStation: InternetRadioStation): MediaItem {
        val uri = internetRadioStation.streamUrl?.toUri()

        val bundle =
            Bundle().apply {
                putString("id", internetRadioStation.id)
                putString("title", internetRadioStation.name)
                putString("uri", uri.toString())
                putString("type", Constants.MEDIA_TYPE_RADIO)
            }

        return MediaItem
            .Builder()
            .setMediaId(internetRadioStation.id!!)
            .setMediaMetadata(
                MediaMetadata
                    .Builder()
                    .setTitle(internetRadioStation.name)
                    .setExtras(bundle)
                    .setIsBrowsable(false)
                    .setIsPlayable(true)
                    .build(),
            ).setRequestMetadata(
                MediaItem.RequestMetadata
                    .Builder()
                    .setMediaUri(uri)
                    .setExtras(bundle)
                    .build(),
            ) // .setMimeType(MimeTypes.BASE_TYPE_AUDIO)
            .setUri(uri)
            .build()
    }

    @Suppress("D")
    fun mapMediaItem(podcastEpisode: PodcastEpisode): MediaItem {
        val uri = getUri(podcastEpisode)
        val artworkUri =
            CustomGlideRequest.createUrl(podcastEpisode.coverArtId, getImageSize()).toUri()

        val bundle =
            Bundle().apply {
                putString("id", podcastEpisode.id)
                putString("parentId", podcastEpisode.parentId)
                putBoolean("isDir", podcastEpisode.isDir)
                putString("title", podcastEpisode.title)
                putString("album", podcastEpisode.album)
                putString("artist", podcastEpisode.artist)
                putInt("year", (if (podcastEpisode.year != null) podcastEpisode.year else 0)!!)
                putString("coverArtId", podcastEpisode.coverArtId)
                putLong("size", (if (podcastEpisode.size != null) podcastEpisode.size else 0)!!)
                putString("contentType", podcastEpisode.contentType)
                putString("suffix", podcastEpisode.suffix)
                putInt(
                    "duration",
                    (if (podcastEpisode.duration != null) podcastEpisode.duration else 0)!!,
                )
                putInt(
                    "bitrate",
                    (if (podcastEpisode.bitrate != null) podcastEpisode.bitrate else 0)!!,
                )
                putBoolean("isVideo", podcastEpisode.isVideo)
                putLong(
                    "created",
                    if (podcastEpisode.created != null) podcastEpisode.created!!.time else 0,
                )
                putString("artistId", podcastEpisode.artistId)
                putString("description", podcastEpisode.description)
                putString("type", Constants.MEDIA_TYPE_PODCAST)
                putString("uri", uri.toString())
            }

        val item =
            MediaItem
                .Builder()
                .setMediaId(podcastEpisode.id!!)
                .setMediaMetadata(
                    MediaMetadata
                        .Builder()
                        .setTitle(podcastEpisode.title)
                        .setReleaseYear(if (podcastEpisode.year != null) podcastEpisode.year else 0)
                        .setAlbumTitle(podcastEpisode.album)
                        .setArtist(podcastEpisode.artist)
                        .setArtworkUri(artworkUri)
                        .setExtras(bundle)
                        .setIsBrowsable(false)
                        .setIsPlayable(true)
                        .build(),
                ).setRequestMetadata(
                    MediaItem.RequestMetadata
                        .Builder()
                        .setMediaUri(uri)
                        .setExtras(bundle)
                        .build(),
                ).setMimeType(MimeTypes.BASE_TYPE_AUDIO)
                .setUri(uri)
                .build()

        return item
    }

    private fun getUri(media: Child): Uri =
        if (DownloadUtil.getDownloadTracker(getContext())?.isDownloaded(media.id)) {
            getDownloadUri(media.id)
        } else {
            MusicUtil.getStreamUri(media.id)
        }

    private fun getUri(podcastEpisode: PodcastEpisode): Uri =
        if (DownloadUtil
                .getDownloadTracker(getContext())
                .isDownloaded(podcastEpisode.streamId)
        ) {
            getDownloadUri(podcastEpisode.streamId)
        } else {
            MusicUtil.getStreamUri(podcastEpisode.streamId)
        }

    private fun getDownloadUri(id: String): Uri {
        val download = DownloadRepository().getDownload(id)
        return if (download != null && !download.downloadUri?.isEmpty()) {
            Uri.parse(download.downloadUri)
        } else {
            MusicUtil.getDownloadUri(id)
        }
    }
}
