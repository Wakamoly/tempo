package com.cappielloantonio.tempo.ui.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.FragmentIndexBinding
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Indexes
import com.cappielloantonio.tempo.subsonic.models.MusicFolder
import com.cappielloantonio.tempo.ui.activity.MainActivity
import com.cappielloantonio.tempo.ui.adapter.MusicIndexAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.IndexUtil
import com.cappielloantonio.tempo.viewmodel.IndexViewModel
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.appbar.AppBarLayout.OnOffsetChangedListener

@UnstableApi
class IndexFragment : Fragment(), ClickCallback {
    private var bind: FragmentIndexBinding? = null
    private var activity: MainActivity? = null
    private var indexViewModel: IndexViewModel? = null

    private var musicIndexAdapter: MusicIndexAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        activity = activity as MainActivity?

        bind = FragmentIndexBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()
        indexViewModel =
            ViewModelProvider(requireActivity()).get<IndexViewModel>(IndexViewModel::class.java)

        initAppBar()
        initDirectoryListView()
        init()

        return view
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
    }

    private fun init() {
        val musicFolder =
            arguments!!.getParcelable<MusicFolder?>(Constants.MUSIC_FOLDER_OBJECT)

        if (musicFolder != null) {
            indexViewModel!!.setMusicFolder(musicFolder)
            bind!!.indexTitleLabel.text = musicFolder.name
        }
    }

    private fun initAppBar() {
        activity!!.setSupportActionBar(bind!!.toolbar)

        if (activity!!.supportActionBar != null) {
            activity!!.supportActionBar!!.setDisplayHomeAsUpEnabled(true)
            activity!!.supportActionBar!!.setDisplayShowHomeEnabled(true)
        }

        if (bind != null) bind!!.toolbar.setNavigationOnClickListener(View.OnClickListener { v: View? -> activity!!.navController.navigateUp() })

        if (bind != null) bind!!.appBarLayout.addOnOffsetChangedListener(OnOffsetChangedListener { appBarLayout: AppBarLayout?, verticalOffset: Int ->
            if ((bind!!.indexInfoSector.height + verticalOffset) < (2 * ViewCompat.getMinimumHeight(
                    bind!!.toolbar
                ))
            ) {
                bind!!.toolbar.setTitle(indexViewModel!!.getMusicFolderName())
            } else {
                bind!!.toolbar.setTitle(R.string.empty_string)
            }
        })
    }

    private fun initDirectoryListView() {
        val musicFolder =
            arguments!!.getParcelable<MusicFolder?>(Constants.MUSIC_FOLDER_OBJECT)

        bind!!.indexRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.indexRecyclerView.setHasFixedSize(true)

        musicIndexAdapter = MusicIndexAdapter(this)
        bind!!.indexRecyclerView.setAdapter(musicIndexAdapter)

        indexViewModel!!.getIndexes(if (musicFolder != null) musicFolder.id else null)
            .observe(getViewLifecycleOwner(), Observer { indexes: Indexes? ->
                if (indexes != null) {
                    musicIndexAdapter!!.setItems(IndexUtil.getArtist(indexes))
                }
            })

        bind!!.fastScrollbar.setRecyclerView(bind!!.indexRecyclerView)
        bind!!.fastScrollbar.setViewsToUse(
            R.layout.layout_fast_scrollbar,
            R.id.fastscroller_bubble,
            R.id.fastscroller_handle
        )
    }

    override fun onMusicIndexClick(bundle: Bundle?) {
        findNavController(requireView()).navigate(R.id.directoryFragment, bundle)
    }

    companion object {
        private const val TAG = "IndexFragment"
    }
}