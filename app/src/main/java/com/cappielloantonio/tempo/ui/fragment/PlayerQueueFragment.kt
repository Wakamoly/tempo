package com.cappielloantonio.tempo.ui.fragment

import android.content.ComponentName
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.media3.common.util.UnstableApi
import androidx.media3.session.MediaBrowser
import androidx.media3.session.SessionToken
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cappielloantonio.tempo.databinding.InnerFragmentPlayerQueueBinding
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.model.Queue
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.service.MediaService
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.ui.adapter.PlayerSongQueueAdapter
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.viewmodel.PlayerBottomSheetViewModel
import com.google.common.util.concurrent.ListenableFuture
import com.google.common.util.concurrent.MoreExecutors
import java.util.Collections
import java.util.stream.Collectors

@UnstableApi
class PlayerQueueFragment :
    Fragment(),
    ClickCallback {
    private var bind: InnerFragmentPlayerQueueBinding? = null

    private var playerBottomSheetViewModel: PlayerBottomSheetViewModel? = null
    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser>? = null

    private var playerSongQueueAdapter: PlayerSongQueueAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?,
    ): View {
        bind = InnerFragmentPlayerQueueBinding.inflate(inflater, container, false)
        val view: View = bind!!.getRoot()

        playerBottomSheetViewModel =
            ViewModelProvider(requireActivity()).get<PlayerBottomSheetViewModel>(
                PlayerBottomSheetViewModel::class.java,
            )

        initQueueRecyclerView()

        return view
    }

    override fun onStart() {
        super.onStart()
        initializeBrowser()
        bindMediaController()
    }

    override fun onResume() {
        super.onResume()
        setMediaBrowserListenableFuture()
        updateNowPlayingItem()
    }

    override fun onStop() {
        releaseBrowser()
        super.onStop()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        bind = null
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

    private fun releaseBrowser() {
        MediaBrowser.releaseFuture(mediaBrowserListenableFuture!!)
    }

    private fun bindMediaController() {
        mediaBrowserListenableFuture!!.addListener(
            Runnable {
                try {
                    val mediaBrowser = mediaBrowserListenableFuture!!.get()
                    initShuffleButton(mediaBrowser)
                    initCleanButton(mediaBrowser)
                } catch (exception: Exception) {
                    exception.printStackTrace()
                }
            },
            MoreExecutors.directExecutor(),
        )
    }

    private fun setMediaBrowserListenableFuture() {
        playerSongQueueAdapter!!.setMediaBrowserListenableFuture(mediaBrowserListenableFuture)
    }

    private fun initQueueRecyclerView() {
        bind!!.playerQueueRecyclerView.setLayoutManager(LinearLayoutManager(requireContext()))
        bind!!.playerQueueRecyclerView.setHasFixedSize(true)

        playerSongQueueAdapter = PlayerSongQueueAdapter(this)
        bind!!.playerQueueRecyclerView.setAdapter(playerSongQueueAdapter)
        playerBottomSheetViewModel!!
            .getQueueSong()
            .observe(
                getViewLifecycleOwner(),
                Observer { queue: MutableList<Queue?>? ->
                    if (queue != null) {
                        playerSongQueueAdapter!!.setItems(
                            queue.stream().map<Child?> { item: Queue? -> item as Child? }.collect(
                                Collectors.toList(),
                            ),
                        )
                    }
                },
            )

        ItemTouchHelper(
            object : ItemTouchHelper.SimpleCallback(
                ItemTouchHelper.UP or ItemTouchHelper.DOWN,
                ItemTouchHelper.LEFT,
            ) {
                var originalPosition: Int = -1
                var fromPosition: Int = -1
                var toPosition: Int = -1

                override fun onMove(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                    target: RecyclerView.ViewHolder,
                ): Boolean {
                    if (originalPosition == -1) {
                        originalPosition = viewHolder.getBindingAdapterPosition()
                    }

                    fromPosition = viewHolder.getBindingAdapterPosition()
                    toPosition = target.getBindingAdapterPosition()

                /*
                 * Per spostare un elemento nella coda devo:
                 *    - Spostare graficamente la traccia da una posizione all'altra con Collections.swap()
                 *    - Spostare nel db la traccia, tramite QueueRepository
                 *    - Notificare il Service dell'avvenuto spostamento con MusicPlayerRemote.moveSong()
                 *
                 * In onMove prendo la posizione di inizio e fine, ma solo al rilascio dell'elemento procedo allo spostamento
                 * In questo modo evito che ad ogni cambio di posizione vada a riscrivere nel db
                 * Al rilascio dell'elemento chiamo il metodo clearView()
                 */
                    Collections.swap(playerSongQueueAdapter!!.getItems(), fromPosition, toPosition)
                    recyclerView.adapter!!.notifyItemMoved(fromPosition, toPosition)

                    return false
                }

                override fun clearView(
                    recyclerView: RecyclerView,
                    viewHolder: RecyclerView.ViewHolder,
                ) {
                    super.clearView(recyclerView, viewHolder)

                    if (originalPosition != -1 && fromPosition != -1 && toPosition != -1) {
                        MediaManager.swap(
                            mediaBrowserListenableFuture,
                            playerSongQueueAdapter!!.getItems(),
                            originalPosition,
                            toPosition,
                        )
                    }

                    originalPosition = -1
                    fromPosition = -1
                    toPosition = -1
                }

                override fun onSwiped(
                    viewHolder: RecyclerView.ViewHolder,
                    direction: Int,
                ) {
                    MediaManager.remove(
                        mediaBrowserListenableFuture,
                        playerSongQueueAdapter!!.getItems(),
                        viewHolder.getBindingAdapterPosition(),
                    )
                    viewHolder.bindingAdapter!!.notifyDataSetChanged()
                }
            },
        ).attachToRecyclerView(bind!!.playerQueueRecyclerView)
    }

    private fun initShuffleButton(mediaBrowser: MediaBrowser) {
        bind!!.playerShuffleQueueFab.setOnClickListener(
            View.OnClickListener { view: View? ->
                val startPosition = mediaBrowser.getCurrentMediaItemIndex() + 1
                val endPosition = playerSongQueueAdapter!!.getItems().size - 1
                if (startPosition < endPosition) {
                    val pool = ArrayList<Int?>()

                    for (i in startPosition..endPosition) {
                        pool.add(i)
                    }

                    while (pool.size >= 2) {
                        val fromPosition = (Math.random() * (pool.size)).toInt()
                        val positionA: Int = pool.get(fromPosition)!!
                        pool.removeAt(fromPosition)

                        val toPosition = (Math.random() * (pool.size)).toInt()
                        val positionB: Int = pool.get(toPosition)!!
                        pool.removeAt(toPosition)

                        Collections.swap(playerSongQueueAdapter!!.getItems(), positionA, positionB)
                        bind!!
                            .playerQueueRecyclerView.adapter!!
                            .notifyItemMoved(positionA, positionB)
                    }

                    MediaManager.shuffle(
                        mediaBrowserListenableFuture,
                        playerSongQueueAdapter!!.getItems(),
                        startPosition,
                        endPosition,
                    )
                }
            },
        )
    }

    private fun initCleanButton(mediaBrowser: MediaBrowser) {
        bind!!.playerCleanQueueButton.setOnClickListener(
            View.OnClickListener { view: View? ->
                val startPosition = mediaBrowser.getCurrentMediaItemIndex() + 1
                val endPosition = playerSongQueueAdapter!!.getItems().size

                MediaManager.removeRange(
                    mediaBrowserListenableFuture,
                    playerSongQueueAdapter!!.getItems(),
                    startPosition,
                    endPosition,
                )
                bind!!
                    .playerQueueRecyclerView.adapter!!
                    .notifyItemRangeRemoved(startPosition, endPosition)
            },
        )
    }

    private fun updateNowPlayingItem() {
        playerSongQueueAdapter!!.notifyDataSetChanged()
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

    companion object {
        private const val TAG = "PlayerQueueFragment"
    }
}
