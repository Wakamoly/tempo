package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemHorizontalPlaylistBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Playlist
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Collections
import java.util.Locale

class PlaylistHorizontalAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<PlaylistHorizontalAdapter.ViewHolder?>(),
    Filterable {
    private var playlists: MutableList<Playlist>
    private var playlistsFull: MutableList<Playlist>? = null

    private val filtering: Filter =
        object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<Playlist?> = ArrayList<Playlist?>()

                if (constraint == null || constraint.length == 0) {
                    filteredList.addAll(playlistsFull!!)
                } else {
                    val filterPattern =
                        constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }

                    for (item in playlistsFull!!) {
                        if (item.name!!.lowercase(Locale.getDefault()).contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList

                return results
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults,
            ) {
                playlists.clear()
                if (results.count > 0) playlists.addAll(results.values as MutableList<*>?)
                notifyDataSetChanged()
            }
        }

    init {
        this.playlists = mutableListOf<Playlist?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHorizontalPlaylistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return PlaylistHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val playlist = playlists.get(position)

        holder.item.playlistTitleTextView.text = playlist.name
        holder.item.playlistSubtitleTextView.text =
            holder.itemView.context.getString(
                R.string.playlist_counted_tracks,
                playlist.songCount,
                MusicUtil.getReadableDurationString(playlist.duration, false),
            )

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                playlist.coverArtId,
                CustomGlideRequest.ResourceType.Playlist,
            ).build()
            .into(holder.item.playlistCoverImageView)
    }

    override fun getItemCount(): Int = playlists.size

    fun getItem(id: Int): Playlist? = playlists.get(id)

    fun setItems(playlists: MutableList<Playlist>) {
        this.playlists = playlists
        this.playlistsFull = ArrayList<Playlist>(playlists)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = filtering

    inner class ViewHolder internal constructor(
        var item: ItemHorizontalPlaylistBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.playlistTitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.playlistMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.PLAYLIST_OBJECT,
                playlists.get(getBindingAdapterPosition()),
            )

            click.onPlaylistClick(bundle)
        }

        fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.PLAYLIST_OBJECT,
                playlists.get(getBindingAdapterPosition()),
            )

            click.onPlaylistLongClick(bundle)

            return true
        }
    }

    fun sort(order: String) {
        when (order) {
            Constants.PLAYLIST_ORDER_BY_NAME ->
                playlists.sort(
                    Comparator.comparing<Playlist?, String?>(
                        Playlist::name,
                    ),
                )

            Constants.PLAYLIST_ORDER_BY_RANDOM -> Collections.shuffle(playlists)
        }

        notifyDataSetChanged()
    }
}
