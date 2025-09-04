package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentArtistPageBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.helper.recyclerview.CustomLinearSnapHelper
import com.cappielloantonio.tempo.helper.recyclerview.GridItemDecoration
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.subsonic.models.ArtistInfo2
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.AlbumCatalogueAdapter
import com.cappielloantonio.tempo.ui.adapter.ArtistCatalogueAdapter
import com.cappielloantonio.tempo.ui.adapter.SongHorizontalAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.viewmodel.ArtistPageViewModel
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class ArtistPageFragment :
    Fragment(),
    ClickCallback {
    private var bind: FragmentArtistPageBinding? = null
    private var activity: MainActivity? = null
    private var artistPageViewModel: ArtistPageViewModel? = null

    private var songHorizontalAdapter: SongHorizontalAdapter? = null
    private var albumCatalogueAdapter: AlbumCatalogueAdapter? = null
    private var artistCatalogueAdapter: ArtistCatalogueAdapter? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        activity = activity as MainActivity?

        bind = FragmentArtistPageBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        artistPageViewModel =
            ViewModelProvider(requireActivity()).get<ArtistPageViewModel>(ArtistPageViewModel::class.java)

        init()
        initAppBar()
        initArtistInfo()
        initPlayButtons()
        initTopSongsView()
        initAlbumsView()
        initSimilarArtistsView()

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

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun init() {
        artistPageViewModel!!.setArtist(requireArguments().getParcelable<ArtistID3?>(Constants.ARTIST_OBJECT))

        bind!!.mostStreamedSongTextViewClickable.setOnClickListener(
            View.OnClickListener { v: View? ->
                val bundle = Bundle()
                bundle.putString(Constants.MEDIA_BY_ARTIST, Constants.MEDIA_BY_ARTIST)
                bundle.putParcelable(Constants.ARTIST_OBJECT, artistPageViewModel!!.getArtist())
                activity!!.navController.navigate(
                    R.id.action_artistPageFragment_to_songListPageFragment,
                    bundle,
                )
            },
        )
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.animToolbar)
        if (activity!!.supportActionBar != null) {
            activity!!
                .supportActionBar!!
                .setDisplayHomeAsUpEnabled(true)
        }

        bind!!.collapsingToolbar.setTitle(artistPageViewModel!!.getArtist().name)
        bind!!.animToolbar.setNavigationOnClickListener(View.OnClickListener { v: View? -> activity!!.navController.navigateUp() })
        bind!!.collapsingToolbar.setExpandedTitleColor(resources.getColor(R.color.white, null))
    }

    private fun initArtistInfo() {
        artistPageViewModel!!
            .getArtistInfo(artistPageViewModel!!.getArtist().id)
            .observe(
                getViewLifecycleOwner(),
                Observer { artistInfo: ArtistInfo2? ->
                    if (artistInfo == null) {
                        if (bind != null) bind!!.artistPageBioSector.visibility = View.GONE
                    } else {
                        val normalizedBio = MusicUtil.forceReadableString(artistInfo.biography)

                        if (bind != null) {
                            bind!!.artistPageBioSector.visibility =
                                if (!normalizedBio
                                        .trim { it <= ' ' }
                                        .isEmpty()
                                ) {
                                    View.VISIBLE
                                } else {
                                    View.GONE
                                }
                        }
                        if (bind !=
                            null
                        ) {
                            bind!!.bioMoreTextViewClickable.visibility = if (artistInfo.lastFmUrl != null) View.VISIBLE else View.GONE
                        }

                        if (context != null && bind != null) {
                            CustomGlideRequest.Builder.Companion
                                .from(
                                    requireContext(),
                                    artistPageViewModel!!.getArtist().id,
                                    CustomGlideRequest.ResourceType.Artist,
                                ).build()
                                .into(bind!!.artistBackdropImageView)
                        }

                        if (bind != null) bind!!.bioTextView.text = normalizedBio

                        if (bind != null) {
                            bind!!.bioMoreTextViewClickable.setOnClickListener(
                                View.OnClickListener { v: View? ->
                                    val intent = Intent(Intent.ACTION_VIEW)
                                    intent.setData(Uri.parse(artistInfo.lastFmUrl))
                                    startActivity(intent)
                                },
                            )
                        }

                        if (bind != null) bind!!.artistPageBioSector.visibility = View.VISIBLE
                    }
                },
            )
    }

    private fun initPlayButtons() {
        bind!!.artistPageShuffleButton.setOnClickListener(
            View.OnClickListener { v: View? ->
                artistPageViewModel!!
                    .getArtistShuffleList()
                    .observe(
                        getViewLifecycleOwner(),
                        Observer { songs: MutableList<Child?>? ->
                            if (!songs!!.isEmpty()) {
                                MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                                activity!!.setBottomSheetInPeek(true)
                            } else {
                                Toast
                                    .makeText(
                                        requireContext(),
                                        getString(R.string.artist_error_retrieving_tracks),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        },
                    )
            },
        )

        bind!!.artistPageRadioButton.setOnClickListener(
            View.OnClickListener { v: View? ->
                artistPageViewModel!!
                    .getArtistInstantMix()
                    .observe(
                        getViewLifecycleOwner(),
                        Observer { songs: MutableList<Child?>? ->
                            if (!songs!!.isEmpty()) {
                                MediaManager.startQueue(mediaBrowserListenableFuture, songs, 0)
                                activity!!.setBottomSheetInPeek(true)
                            } else {
                                Toast
                                    .makeText(
                                        requireContext(),
                                        getString(R.string.artist_error_retrieving_radio),
                                        Toast.LENGTH_SHORT,
                                    ).show()
                            }
                        },
                    )
            },
        )
    }

    private fun initTopSongsView() {
        bind!!.mostStreamedSongRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))

        songHorizontalAdapter = SongHorizontalAdapter(this, true, true, null)
        bind!!.mostStreamedSongRecyclerView.setAdapter(songHorizontalAdapter)
        artistPageViewModel!!
            .getArtistTopSongList()
            .observe(
                getViewLifecycleOwner(),
                Observer { songs: MutableList<Child?>? ->
                    if (songs == null) {
                        if (bind != null) bind!!.artistPageTopSongsSector.visibility = View.GONE
                    } else {
                        if (bind != null) bind!!.artistPageTopSongsSector.visibility = if (!songs.isEmpty()) View.VISIBLE else View.GONE
                        if (bind != null) bind!!.artistPageShuffleButton.setEnabled(!songs.isEmpty())
                        songHorizontalAdapter!!.setItems(songs)
                    }
                },
            )
    }

    private fun initAlbumsView() {
        bind!!.albumsRecyclerView.setLayoutManager(GridLayoutManager(requireContext(), 2))
        bind!!.albumsRecyclerView.addItemDecoration(GridItemDecoration(2, 20, false))
        bind!!.albumsRecyclerView.setHasFixedSize(true)

        albumCatalogueAdapter = AlbumCatalogueAdapter(this, false)
        bind!!.albumsRecyclerView.setAdapter(albumCatalogueAdapter)

        artistPageViewModel!!
            .getAlbumList()
            .observe(
                getViewLifecycleOwner(),
                Observer { albums: MutableList<AlbumID3?>? ->
                    if (albums == null) {
                        if (bind != null) bind!!.artistPageAlbumsSector.visibility = View.GONE
                    } else {
                        if (bind != null) bind!!.artistPageAlbumsSector.visibility = if (!albums.isEmpty()) View.VISIBLE else View.GONE
                        albumCatalogueAdapter!!.setItems(albums)
                    }
                },
            )
    }

    private fun initSimilarArtistsView() {
        bind!!.similarArtistsRecyclerView.setLayoutManager(GridLayoutManager(requireContext(), 2))
        bind!!.similarArtistsRecyclerView.addItemDecoration(GridItemDecoration(2, 20, false))
        bind!!.similarArtistsRecyclerView.setHasFixedSize(true)

        artistCatalogueAdapter = ArtistCatalogueAdapter(this)
        bind!!.similarArtistsRecyclerView.setAdapter(artistCatalogueAdapter)

        artistPageViewModel!!
            .getArtistInfo(artistPageViewModel!!.getArtist().id)
            .observe(
                getViewLifecycleOwner(),
                Observer { artist: ArtistInfo2? ->
                    if (artist == null) {
                        if (bind != null) bind!!.similarArtistSector.visibility = View.GONE
                    } else {
                        if (bind != null && artist.similarArtists != null) {
                            bind!!.similarArtistSector.visibility =
                                if (!artist.similarArtists!!.isEmpty()) View.VISIBLE else View.GONE
                        }

                        val artists: MutableList<ArtistID3?> = ArrayList<ArtistID3?>()

                        if (artist.similarArtists != null) {
                            artists.addAll(artist.similarArtists!!)
                        }

                        artistCatalogueAdapter!!.setItems(artists)
                    }
                },
            )

        val similarArtistSnapHelper = CustomLinearSnapHelper()
        similarArtistSnapHelper.attachToRecyclerView(bind!!.similarArtistsRecyclerView)
    }

    private fun initializeMediaBrowser() {
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

    private fun releaseMediaBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture)
    }

    override fun onMediaClick(bundle: Bundle) {
        MediaManager.startQueue(
            mediaBrowserListenableFuture,
            bundle.getParcelableArrayList<Child?>(
                Constants.TRACKS_OBJECT,
            ),
            bundle.getInt(Constants.ITEM_POSITION),
        )
        activity!!.setBottomSheetInPeek(true)
    }

    override fun onMediaLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.songBottomSheetDialog, bundle)
    }

    override fun onAlbumClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.albumPageFragment, bundle)
    }

    override fun onAlbumLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.albumBottomSheetDialog, bundle)
    }

    override fun onArtistClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.artistPageFragment, bundle)
    }

    override fun onArtistLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.artistBottomSheetDialog, bundle)
    }
}
