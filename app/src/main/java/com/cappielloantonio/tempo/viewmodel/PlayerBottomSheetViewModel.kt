package com.cappielloantonio.tempo.viewmodel

import android.app.Application
import android.content.Context
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.media3.common.util.UnstableApi
import com.cappielloantonio.tempo.interfaces.StarCallback
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.repository.AlbumRepository
import com.cappielloantonio.tempo.repository.ArtistRepository
import com.cappielloantonio.tempo.repository.FavoriteRepository
import com.cappielloantonio.tempo.repository.OpenRepository
import com.cappielloantonio.tempo.repository.QueueRepository
import com.cappielloantonio.tempo.repository.SongRepository
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.LyricsList
import com.cappielloantonio.tempo.subsonic.models.PlayQueue
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.util.NetworkUtil
import com.cappielloantonio.tempo.util.OpenSubsonicExtensionsUtil
import com.cappielloantonio.tempo.util.Preferences.isStarredSyncEnabled
import java.util.Date
import java.util.stream.Collectors

@OptIn(markerClass = UnstableApi::class)
class PlayerBottomSheetViewModel(
    application: Application,
) : AndroidViewModel(application) {
    private val songRepository: SongRepository
    private val albumRepository: AlbumRepository
    private val artistRepository: ArtistRepository
    private val queueRepository: QueueRepository
    private val favoriteRepository: FavoriteRepository
    private val openRepository: OpenRepository
    private val lyricsLiveData = MutableLiveData<String?>(null)
    private val lyricsListLiveData = MutableLiveData<LyricsList?>(null)
    private val descriptionLiveData = MutableLiveData<String?>(null)
    private val liveMedia = MutableLiveData<Child?>(null)
    private val liveAlbum = MutableLiveData<AlbumID3?>(null)
    private val liveArtist = MutableLiveData<ArtistID3?>(null)
    private val instantMix = MutableLiveData<MutableList<Child?>?>(null)
    var syncLyricsState: Boolean = true
        private set

    init {
        songRepository = SongRepository()
        albumRepository = AlbumRepository()
        artistRepository = ArtistRepository()
        queueRepository = QueueRepository()
        favoriteRepository = FavoriteRepository()
        openRepository = OpenRepository()
    }

    val queueSong: LiveData<MutableList<Queue?>?>?
        get() = queueRepository.getLiveQueue()

    fun setFavorite(
        context: Context?,
        media: Child?,
    ) {
        if (media != null) {
            if (media.starred != null) {
                if (NetworkUtil.isOffline()) {
                    removeFavoriteOffline(media)
                } else {
                    removeFavoriteOnline(media)
                }
            } else {
                if (NetworkUtil.isOffline()) {
                    setFavoriteOffline(media)
                } else {
                    setFavoriteOnline(context, media)
                }
            }
        }
    }

    private fun removeFavoriteOffline(media: Child) {
        favoriteRepository.starLater(media.id, null, null, false)
        media.starred = null
    }

    private fun removeFavoriteOnline(media: Child) {
        favoriteRepository.unstar(
            media.id,
            null,
            null,
            object : StarCallback {
                override fun onError() {
                    // media.setStarred(new Date());
                    favoriteRepository.starLater(media.id, null, null, false)
                }
            },
        )
        media.starred = null
    }

    private fun setFavoriteOffline(media: Child) {
        favoriteRepository.starLater(media.id, null, null, true)
        media.starred = Date()
    }

    private fun setFavoriteOnline(
        context: Context?,
        media: Child,
    ) {
        favoriteRepository.star(
            media.id,
            null,
            null,
            object : StarCallback {
                override fun onError() {
                    // media.setStarred(null);
                    favoriteRepository.starLater(media.id, null, null, true)
                }
            },
        )

        media.starred = Date()

        if (isStarredSyncEnabled()) {
            DownloadUtil.getDownloadTracker(context).download(
                MappingUtil.mapDownload(media),
                Download(media),
            )
        }
    }

    val liveLyrics: LiveData<String?>
        get() = lyricsLiveData

    val liveLyricsList: LiveData<LyricsList?>
        get() = lyricsListLiveData

    fun refreshMediaInfo(
        owner: LifecycleOwner,
        media: Child,
    ) {
        if (OpenSubsonicExtensionsUtil.isSongLyricsExtensionAvailable()) {
            openRepository.getLyricsBySongId(media.id).observe(
                owner,
                Observer { value: LyricsList? -> lyricsListLiveData.postValue(value) },
            )
            lyricsLiveData.postValue(null)
        } else {
            songRepository
                .getSongLyrics(media)
                .observe(owner, Observer { value: String? -> lyricsLiveData.postValue(value) })
            lyricsListLiveData.postValue(null)
        }
    }

    fun getLiveMedia(): LiveData<Child?> = liveMedia

    fun setLiveMedia(
        owner: LifecycleOwner,
        mediaType: String?,
        mediaId: String?,
    ) {
        if (mediaType != null) {
            when (mediaType) {
                Constants.MEDIA_TYPE_MUSIC -> {
                    songRepository
                        .getSong(mediaId)
                        .observe(owner, Observer { value: Child? -> liveMedia.postValue(value) })
                    descriptionLiveData.postValue(null)
                }

                Constants.MEDIA_TYPE_PODCAST -> liveMedia.postValue(null)
            }
        }
    }

    fun getLiveAlbum(): LiveData<AlbumID3?> = liveAlbum

    fun setLiveAlbum(
        owner: LifecycleOwner,
        mediaType: String?,
        AlbumId: String?,
    ) {
        if (mediaType != null) {
            when (mediaType) {
                Constants.MEDIA_TYPE_MUSIC ->
                    albumRepository
                        .getAlbum(AlbumId)
                        .observe(owner, Observer { value: AlbumID3? -> liveAlbum.postValue(value) })

                Constants.MEDIA_TYPE_PODCAST -> liveAlbum.postValue(null)
            }
        }
    }

    fun getLiveArtist(): LiveData<ArtistID3?> = liveArtist

    fun setLiveArtist(
        owner: LifecycleOwner,
        mediaType: String?,
        ArtistId: String?,
    ) {
        if (mediaType != null) {
            when (mediaType) {
                Constants.MEDIA_TYPE_MUSIC ->
                    artistRepository
                        .getArtist(ArtistId)
                        .observe(owner, Observer { value: ArtistID3? -> liveArtist.postValue(value) })

                Constants.MEDIA_TYPE_PODCAST -> liveArtist.postValue(null)
            }
        }
    }

    fun setLiveDescription(description: String?) {
        descriptionLiveData.postValue(description)
    }

    val liveDescription: LiveData<String?>
        get() = descriptionLiveData

    fun getMediaInstantMix(
        owner: LifecycleOwner,
        media: Child,
    ): LiveData<MutableList<Child?>?> {
        instantMix.value = mutableListOf<Child?>()

        songRepository
            .getInstantMix(media.id, 20)
            .observe(owner, Observer { value: MutableList<Child?>? -> instantMix.postValue(value) })

        return instantMix
    }

    val playQueue: LiveData<PlayQueue?>?
        get() = queueRepository.getPlayQueue()

    fun savePlayQueue(): Boolean {
        val media = getLiveMedia().getValue()
        val queue = queueRepository.getMedia()
        val ids = queue.stream().map<String?>(Child::id).collect(Collectors.toList())

        if (media != null) {
            queueRepository.savePlayQueue(ids, media.id, 0)
            return true
        }

        return false
    }

    fun changeSyncLyricsState() {
        this.syncLyricsState = !this.syncLyricsState
    }

    companion object {
        private const val TAG = "PlayerBottomSheetViewModel"
    }
}
