package com.cappielloantonio.tempo.service

import android.content.ComponentName
import androidx.annotation.OptIn
import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import androidx.media3.common.MediaItem
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import com.cappielloantonio.tempo.App.Companion.getContext
import com.cappielloantonio.tempo.interfaces.MediaIndexCallback
import com.cappielloantonio.tempo.model.Chronology
import com.cappielloantonio.tempo.repository.ChronologyRepository
import com.cappielloantonio.tempo.repository.QueueRepository
import com.cappielloantonio.tempo.repository.SongRepository
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation
import com.cappielloantonio.tempo.subsonic.models.PodcastEpisode
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.Preferences.isContinuousPlayEnabled
import com.cappielloantonio.tempo.util.Preferences.isInstantMixUsable
import com.cappielloantonio.tempo.util.Preferences.isScrobblingEnabled
import com.cappielloantonio.tempo.util.Preferences.setLastInstantMix
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.concurrent.ExecutionException

object MediaManager {
    private const val TAG = "MediaManager"

    fun reset(mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        if (mediaBrowserListenableFuture.get()!!.isPlaying()) {
                            mediaBrowserListenableFuture.get()!!.pause()
                        }

                        mediaBrowserListenableFuture.get()!!.stop()
                        mediaBrowserListenableFuture.get()!!.clearMediaItems()
                        clearDatabase()
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun hide(mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        if (mediaBrowserListenableFuture.get()!!.isPlaying()) {
                            mediaBrowserListenableFuture.get()!!.pause()
                        }
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun check(mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        if (mediaBrowserListenableFuture.get()!!.mediaItemCount < 1) {
                            val media: MutableList<Child?>? = queueRepository.getMedia()
                            if (media != null && media.size >= 1) {
                                init(mediaBrowserListenableFuture, media)
                            }
                        }
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun init(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.clearMediaItems()
                        mediaBrowserListenableFuture.get()!!
                            .setMediaItems(MappingUtil.mapMediaItems(media))
                        mediaBrowserListenableFuture.get()!!.seekTo(
                            queueRepository.getLastPlayedMediaIndex(),
                            queueRepository.getLastPlayedMediaTimestamp()
                        )
                        mediaBrowserListenableFuture.get()!!.prepare()
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun startQueue(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>,
        startIndex: Int
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.clearMediaItems()
                        mediaBrowserListenableFuture.get()!!
                            .setMediaItems(MappingUtil.mapMediaItems(media))
                        mediaBrowserListenableFuture.get()!!.prepare()
                        mediaBrowserListenableFuture.get()!!.seekTo(startIndex, 0)
                        mediaBrowserListenableFuture.get()!!.play()
                        enqueueDatabase(media, true, 0)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun startQueue(mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?, media: Child?) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.clearMediaItems()
                        mediaBrowserListenableFuture.get()!!
                            .setMediaItem(MappingUtil.mapMediaItem(media))
                        mediaBrowserListenableFuture.get()!!.prepare()
                        mediaBrowserListenableFuture.get()!!.play()
                        enqueueDatabase(media, true, 0)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun startRadio(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        internetRadioStation: InternetRadioStation
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.clearMediaItems()
                        mediaBrowserListenableFuture.get()!!
                            .setMediaItem(MappingUtil.mapInternetRadioStation(internetRadioStation))
                        mediaBrowserListenableFuture.get()!!.prepare()
                        mediaBrowserListenableFuture.get()!!.play()
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun startPodcast(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        podcastEpisode: PodcastEpisode?
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.clearMediaItems()
                        mediaBrowserListenableFuture.get()!!
                            .setMediaItem(MappingUtil.mapMediaItem(podcastEpisode))
                        mediaBrowserListenableFuture.get()!!.prepare()
                        mediaBrowserListenableFuture.get()!!.play()
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun enqueue(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>,
        playImmediatelyAfter: Boolean
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        if (playImmediatelyAfter && mediaBrowserListenableFuture.get()!!
                                .getNextMediaItemIndex() != -1
                        ) {
                            enqueueDatabase(
                                media,
                                false,
                                mediaBrowserListenableFuture.get()!!.getNextMediaItemIndex()
                            )
                            mediaBrowserListenableFuture.get()!!.addMediaItems(
                                mediaBrowserListenableFuture.get()!!.getNextMediaItemIndex(),
                                MappingUtil.mapMediaItems(media)
                            )
                        } else {
                            enqueueDatabase(
                                media,
                                false,
                                mediaBrowserListenableFuture.get()!!.mediaItemCount
                            )
                            mediaBrowserListenableFuture.get()!!
                                .addMediaItems(MappingUtil.mapMediaItems(media))
                        }
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun enqueue(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: Child?,
        playImmediatelyAfter: Boolean
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        if (playImmediatelyAfter && mediaBrowserListenableFuture.get()!!
                                .getNextMediaItemIndex() != -1
                        ) {
                            enqueueDatabase(
                                media,
                                false,
                                mediaBrowserListenableFuture.get()!!.getNextMediaItemIndex()
                            )
                            mediaBrowserListenableFuture.get()!!.addMediaItem(
                                mediaBrowserListenableFuture.get()!!.getNextMediaItemIndex(),
                                MappingUtil.mapMediaItem(media)
                            )
                        } else {
                            enqueueDatabase(
                                media,
                                false,
                                mediaBrowserListenableFuture.get()!!.mediaItemCount
                            )
                            mediaBrowserListenableFuture.get()!!
                                .addMediaItem(MappingUtil.mapMediaItem(media))
                        }
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun shuffle(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>,
        startIndex: Int,
        endIndex: Int
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!
                            .removeMediaItems(startIndex, endIndex + 1)
                        mediaBrowserListenableFuture.get()!!.addMediaItems(
                            MappingUtil.mapMediaItems(media).subList(startIndex, endIndex + 1)
                        )
                        swapDatabase(media)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun swap(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>?,
        from: Int,
        to: Int
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.moveMediaItem(from, to)
                        swapDatabase(media)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun remove(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>,
        toRemove: Int
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        if (mediaBrowserListenableFuture.get()!!
                                .mediaItemCount > 1 && mediaBrowserListenableFuture.get()!!
                                .getCurrentMediaItemIndex() != toRemove
                        ) {
                            mediaBrowserListenableFuture.get()!!.removeMediaItem(toRemove)
                            removeDatabase(media, toRemove)
                        } else {
                            removeDatabase(media, -1)
                        }
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun removeRange(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        media: MutableList<Child?>,
        fromItem: Int,
        toItem: Int
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        mediaBrowserListenableFuture.get()!!.removeMediaItems(fromItem, toItem)
                        removeRangeDatabase(media, fromItem, toItem)
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun getCurrentIndex(
        mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?,
        callback: MediaIndexCallback
    ) {
        if (mediaBrowserListenableFuture != null) {
            mediaBrowserListenableFuture.addListener(Runnable {
                try {
                    if (mediaBrowserListenableFuture.isDone) {
                        callback.onRecovery(
                            mediaBrowserListenableFuture.get()!!.getCurrentMediaItemIndex()
                        )
                    }
                } catch (e: ExecutionException) {
                    e.printStackTrace()
                } catch (e: InterruptedException) {
                    e.printStackTrace()
                }
            }, MoreExecutors.directExecutor())
        }
    }

    fun setLastPlayedTimestamp(mediaItem: MediaItem?) {
        if (mediaItem != null) queueRepository.setLastPlayedTimestamp(mediaItem.mediaId)
    }

    fun setPlayingPausedTimestamp(mediaItem: MediaItem?, ms: Long) {
        if (mediaItem != null) queueRepository.setPlayingPausedTimestamp(mediaItem.mediaId, ms)
    }

    fun scrobble(mediaItem: MediaItem?, submission: Boolean) {
        if (mediaItem != null && isScrobblingEnabled()) {
            songRepository.scrobble(mediaItem.mediaMetadata.extras!!.getString("id"), submission)
        }
    }

    @OptIn(markerClass = UnstableApi::class)
    fun continuousPlay(mediaItem: MediaItem?) {
        if (mediaItem != null && isContinuousPlayEnabled() && isInstantMixUsable()) {
            setLastInstantMix()

            val instantMix: LiveData<MutableList<Child?>?> =
                songRepository.getInstantMix(mediaItem.mediaId, 10)
            instantMix.observeForever(object : Observer<MutableList<Child?>?> {
                override fun onChanged(media: MutableList<Child?>?) {
                    if (media != null) {
                        val mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?> =
                            MediaBrowser.Builder(
                                getContext(),
                                SessionToken(
                                    getContext(),
                                    ComponentName(getContext(), MediaService::class.java)
                                )
                            ).buildAsync()

                        enqueue(mediaBrowserListenableFuture, media, true)
                    }

                    instantMix.removeObserver(this)
                }
            })
        }
    }

    fun saveChronology(mediaItem: MediaItem?) {
        if (mediaItem != null) {
            chronologyRepository.insert(Chronology(mediaItem))
        }
    }

    private val queueRepository: QueueRepository
        get() = QueueRepository()

    private val songRepository: SongRepository
        get() = SongRepository()

    private val chronologyRepository: ChronologyRepository
        get() = ChronologyRepository()

    private fun enqueueDatabase(media: MutableList<Child?>?, reset: Boolean, afterIndex: Int) {
        queueRepository.insertAll(media, reset, afterIndex)
    }

    private fun enqueueDatabase(media: Child?, reset: Boolean, afterIndex: Int) {
        queueRepository.insert(media, reset, afterIndex)
    }

    private fun swapDatabase(media: MutableList<Child?>?) {
        queueRepository.insertAll(media, true, 0)
    }

    private fun removeDatabase(media: MutableList<Child?>, toRemove: Int) {
        if (toRemove != -1) {
            media.removeAt(toRemove)
            queueRepository.insertAll(media, true, 0)
        }
    }

    private fun removeRangeDatabase(media: MutableList<Child?>, fromItem: Int, toItem: Int) {
        val toRemove = media.subList(fromItem, toItem)

        media.removeAll(toRemove)

        queueRepository.insertAll(media, true, 0)
    }

    fun clearDatabase() {
        queueRepository.deleteAll()
    }
}
