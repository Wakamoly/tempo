package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemLibraryMusicDirectoryBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class MusicDirectoryAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<MusicDirectoryAdapter.ViewHolder?>() {
    private var children: MutableList<Child>

    init {
        this.children = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemLibraryMusicDirectoryBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return MusicDirectoryAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val child = children.get(position)

        holder.item.musicDirectoryTitleTextView.text = child.title

        val type = if (child.isDir)
            CustomGlideRequest.ResourceType.Directory
        else
            CustomGlideRequest.ResourceType.Song

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            child.coverArtId,
            type
        )
            .build()
            .into(holder.item.musicDirectoryCoverImageView)

        holder.item.musicDirectoryMoreButton.setVisibility(if (child.isDir) View.VISIBLE else View.INVISIBLE)
        holder.item.musicDirectoryPlayButton.setVisibility(if (child.isDir) View.INVISIBLE else View.VISIBLE)
    }

    override fun getItemCount(): Int {
        return children.size
    }

    fun setItems(children: MutableList<Child>?) {
        this.children = if (children != null) children else mutableListOf<Child?>()
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(var item: ItemLibraryMusicDirectoryBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.musicDirectoryTitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.musicDirectoryMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()

            if (children.get(getBindingAdapterPosition()).isDir) {
                bundle.putString(
                    Constants.MUSIC_DIRECTORY_ID,
                    children.get(getBindingAdapterPosition()).id
                )
                click.onMusicDirectoryClick(bundle)
            } else {
                bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, ArrayList<Child?>(children))
                bundle.putInt(Constants.ITEM_POSITION, getBindingAdapterPosition())
                click.onMediaClick(bundle)
            }
        }

        private fun onLongClick(): Boolean {
            if (!children.get(getBindingAdapterPosition()).isDir) {
                val bundle = Bundle()
                bundle.putParcelable(
                    Constants.TRACK_OBJECT,
                    children.get(getBindingAdapterPosition())
                )

                click.onMediaLongClick(bundle)

                return true
            } else {
                return false
            }
        }
    }
}
