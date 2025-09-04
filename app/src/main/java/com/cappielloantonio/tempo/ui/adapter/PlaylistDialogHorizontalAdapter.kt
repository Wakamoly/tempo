package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemHorizontalPlaylistDialogBinding
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil

class PlaylistDialogHorizontalAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<PlaylistDialogHorizontalAdapter.ViewHolder?>() {
    private var playlists: MutableList<Playlist>

    init {
        this.playlists = mutableListOf<Playlist?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHorizontalPlaylistDialogBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return PlaylistDialogHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val playlist = playlists.get(position)

        holder.item.playlistDialogTitleTextView.text = playlist.name
        holder.item.playlistDialogCountTextView.text =
            holder.itemView.context.getString(
                R.string.playlist_counted_tracks,
                playlist.songCount,
                MusicUtil.getReadableDurationString(playlist.duration, false),
            )
    }

    override fun getItemCount(): Int = playlists.size

    fun setItems(playlists: MutableList<Playlist>) {
        this.playlists = playlists
        notifyDataSetChanged()
    }

    fun getItem(id: Int): Playlist? = playlists.get(id)

    inner class ViewHolder internal constructor(
        var item: ItemHorizontalPlaylistDialogBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.playlistDialogTitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.PLAYLIST_OBJECT,
                playlists.get(getBindingAdapterPosition()),
            )

            click.onPlaylistClick(bundle)
        }
    }
}
