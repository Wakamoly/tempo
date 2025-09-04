package com.cappielloantonio.tempo.util

import android.net.Uri
import android.os.Bundle
import androidx.annotation.OptIn
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

@OptIn(markerClass = UnstableApi::class)
object MappingUtil {
    fun mapMediaItems(items: MutableList<Child?>): MutableList<MediaItem?> {
        val mediaItems = ArrayList<MediaItem?>()

        for (i in items.indices) {
            mediaItems.add(mapMediaItem(items.get(i)!!))
        }

        return mediaItems
    }

    fun mapMediaItem(media: Child): MediaItem {
        val uri = getUri(media)
        val artworkUri = Uri.parse(CustomGlideRequest.createUrl(media.coverArtId, getImageSize()))

        val bundle = Bundle()
        bundle.putString("id", media.id)
        bundle.putString("parentId", media.parentId)
        bundle.putBoolean("isDir", media.isDir)
        bundle.putString("title", media.title)
        bundle.putString("album", media.album)
        bundle.putString("artist", media.artist)
        bundle.putInt("track", (if (media.track != null) media.track else 0)!!)
        bundle.putInt("year", (if (media.year != null) media.year else 0)!!)
        bundle.putString("genre", media.genre)
        bundle.putString("coverArtId", media.coverArtId)
        bundle.putLong("size", (if (media.size != null) media.size else 0)!!)
        bundle.putString("contentType", media.contentType)
        bundle.putString("suffix", media.suffix)
        bundle.putString("transcodedContentType", media.transcodedContentType)
        bundle.putString("transcodedSuffix", media.transcodedSuffix)
        bundle.putInt("duration", (if (media.duration != null) media.duration else 0)!!)
        bundle.putInt("bitrate", (if (media.bitrate != null) media.bitrate else 0)!!)
        bundle.putInt("samplingRate", (if (media.samplingRate != null) media.samplingRate else 0)!!)
        bundle.putInt("bitDepth", (if (media.bitDepth != null) media.bitDepth else 0)!!)
        bundle.putString("path", media.path)
        bundle.putBoolean("isVideo", media.isVideo)
        bundle.putInt("userRating", (if (media.userRating != null) media.userRating else 0)!!)
        bundle.putDouble(
            "averageRating",
            (if (media.averageRating != null) media.averageRating else 0.0)!!,
        )
        bundle.putLong("playCount", (if (media.playCount != null) media.playCount else 0)!!)
        bundle.putInt("discNumber", (if (media.discNumber != null) media.discNumber else 0)!!)
        bundle.putLong("created", if (media.created != null) media.created!!.time else 0)
        bundle.putLong("starred", if (media.starred != null) media.starred!!.time else 0)
        bundle.putString("albumId", media.albumId)
        bundle.putString("artistId", media.artistId)
        bundle.putString("type", Constants.MEDIA_TYPE_MUSIC)
        bundle.putLong(
            "bookmarkPosition",
            (if (media.bookmarkPosition != null) media.bookmarkPosition else 0)!!,
        )
        bundle.putInt(
            "originalWidth",
            (if (media.originalWidth != null) media.originalWidth else 0)!!,
        )
        bundle.putInt(
            "originalHeight",
            (if (media.originalHeight != null) media.originalHeight else 0)!!,
        )
        bundle.putString("uri", uri.toString())

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
        val uri = Uri.parse(internetRadioStation.streamUrl)

        val bundle = Bundle()
        bundle.putString("id", internetRadioStation.id)
        bundle.putString("title", internetRadioStation.name)
        bundle.putString("uri", uri.toString())
        bundle.putString("type", Constants.MEDIA_TYPE_RADIO)

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

    fun mapMediaItem(podcastEpisode: PodcastEpisode): MediaItem {
        val uri = getUri(podcastEpisode)
        val artworkUri =
            Uri.parse(CustomGlideRequest.createUrl(podcastEpisode.coverArtId, getImageSize()))

        val bundle = Bundle()
        bundle.putString("id", podcastEpisode.id)
        bundle.putString("parentId", podcastEpisode.parentId)
        bundle.putBoolean("isDir", podcastEpisode.isDir)
        bundle.putString("title", podcastEpisode.title)
        bundle.putString("album", podcastEpisode.album)
        bundle.putString("artist", podcastEpisode.artist)
        bundle.putInt("year", (if (podcastEpisode.year != null) podcastEpisode.year else 0)!!)
        bundle.putString("coverArtId", podcastEpisode.coverArtId)
        bundle.putLong("size", (if (podcastEpisode.size != null) podcastEpisode.size else 0)!!)
        bundle.putString("contentType", podcastEpisode.contentType)
        bundle.putString("suffix", podcastEpisode.suffix)
        bundle.putInt(
            "duration",
            (if (podcastEpisode.duration != null) podcastEpisode.duration else 0)!!,
        )
        bundle.putInt(
            "bitrate",
            (if (podcastEpisode.bitrate != null) podcastEpisode.bitrate else 0)!!,
        )
        bundle.putBoolean("isVideo", podcastEpisode.isVideo)
        bundle.putLong(
            "created",
            if (podcastEpisode.created != null) podcastEpisode.created!!.time else 0,
        )
        bundle.putString("artistId", podcastEpisode.artistId)
        bundle.putString("description", podcastEpisode.description)
        bundle.putString("type", Constants.MEDIA_TYPE_PODCAST)
        bundle.putString("uri", uri.toString())

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
        if (DownloadUtil.getDownloadTracker(getContext()).isDownloaded(media.id)) {
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

    private fun getDownloadUri(id: String?): Uri {
        val download = DownloadRepository().getDownload(id)
        return if (download != null && !download.downloadUri!!.isEmpty()) {
            Uri.parse(download.downloadUri)
        } else {
            MusicUtil.getDownloadUri(
                id,
            )
        }
    }
}
