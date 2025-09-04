package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentDirectoryBinding
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.interfaces.DialogClickCallback
import com.cappielloantonio.tempo.model.Download
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.Directory
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.MusicDirectoryAdapter
import com.cappielloantonio.tempo.ui.dialog.DownloadDirectoryDialog
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MappingUtil
import com.cappielloantonio.tempo.viewmodel.DirectoryViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener
import com.google.common.util.concurrent.ListenableFuture
import java.util.stream.Collectors

@UnstableApi
class DirectoryFragment :
    Fragment(),
    ClickCallback {
    private var bind: FragmentDirectoryBinding? = null
    private var activity: MainActivity? = null
    private var directoryViewModel: DirectoryViewModel? = null

    private var musicDirectoryAdapter: MusicDirectoryAdapter? = null

    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null

    private var menuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateOptionsMenu(
        menu: Menu,
        inflater: MenuInflater,
    ) {
        super.onCreateOptionsMenu(menu, inflater)
        inflater.inflate(R.menu.directory_page_menu, menu)

        menuItem = menu.getItem(0)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        activity = activity as MainActivity?

        bind = FragmentDirectoryBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        directoryViewModel =
            ViewModelProvider(requireActivity()).get<DirectoryViewModel>(DirectoryViewModel::class.java)

        initAppBar()
        initDirectoryListView()

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

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_download_directory) {
            val dialog =
                DownloadDirectoryDialog(
                    object : DialogClickCallback {
                        override fun onPositiveClick() {
                            directoryViewModel!!
                                .loadMusicDirectory(arguments!!.getString(Constants.MUSIC_DIRECTORY_ID))
                                .observe(
                                    getViewLifecycleOwner(),
                                    Observer { directory: Directory? ->
                                        if (isVisible && activity != null) {
                                            val songs =
                                                directory!!
                                                    .children!!
                                                    .stream()
                                                    .filter { child: Child? -> !child!!.isDir }
                                                    .collect(
                                                        Collectors.toList(),
                                                    )
                                            DownloadUtil.getDownloadTracker(requireContext()).download(
                                                MappingUtil.mapDownloads(songs),
                                                songs
                                                    .stream()
                                                    .map<Download?> { child: Child? -> Download(child) }
                                                    .collect(
                                                        Collectors.toList(),
                                                    ),
                                            )
                                        }
                                    },
                                )
                        }
                    },
                )

            dialog.show(activity!!.supportFragmentManager, null)

            return true
        }

        return false
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.toolbar)

        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity!!.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        if (bind != null) {
            bind!!.toolbar.setNavigationOnClickListener(View.OnClickListener { v: View? -> activity!!.navController.navigateUp() })
            bind!!.directoryBackImageView.setOnClickListener(View.OnClickListener { v: View? -> activity!!.navController.navigateUp() })
        }
    }

    private fun initDirectoryListView() {
        bind!!.directoryRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.directoryRecyclerView.setHasFixedSize(true)

        musicDirectoryAdapter = MusicDirectoryAdapter(this)
        bind!!.directoryRecyclerView.setAdapter(musicDirectoryAdapter)
        directoryViewModel!!
            .loadMusicDirectory(arguments!!.getString(Constants.MUSIC_DIRECTORY_ID))
            .observe(
                getViewLifecycleOwner(),
                Observer { directory: Directory? ->
                    bind!!.appBarLayout.addOnOffsetChangedListener(
                        OnOffsetChangedListener { appBarLayout: AppBarLayout?, verticalOffset: Int ->
                            if ((bind!!.directoryInfoSector.height + verticalOffset) < (
                                    2 *
                                        ViewCompat.getMinimumHeight(
                                            bind!!.toolbar,
                                        )
                                )
                            ) {
                                bind!!.toolbar.setTitle(directory!!.name)
                            } else {
                                bind!!.toolbar.setTitle(R.string.empty_string)
                            }
                        },
                    )
                    bind!!.directoryTitleLabel.text = directory!!.name

                    musicDirectoryAdapter!!.setItems(directory.children)
                    menuItem!!.isVisible = directory.children != null && directory.children!!
                        .stream()
                        .filter { child: Child? -> !child!!.isDir }
                        .findFirst()
                        .orElse(null) != null
                },
            )
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
    }

    override fun onMediaLongClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.songBottomSheetDialog, bundle)
    }

    override fun onMusicDirectoryClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.directoryFragment, bundle)
    }

    companion object {
        private const val TAG = "DirectoryFragment"
    }
}
