package com.cappielloantonio.tempo.ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHorizontalPlaylistDialogTrackBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.MusicUtil
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class PlaylistDialogSongHorizontalAdapter : RecyclerView.Adapter<PlaylistDialogSongHorizontalAdapter.ViewHolder?>() {
    private var songs: MutableList<Child>

    init {
        this.songs = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHorizontalPlaylistDialogTrackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val song = songs.get(position)

        holder.item.playlistDialogSongTitleTextView.text = song.title
        holder.item.playlistDialogAlbumArtistTextView.text = song.artist
        holder.item.playlistDialogSongDurationTextView.text =
            MusicUtil.getReadableDurationString(
                song.duration,
                false,
            )

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                song.coverArtId,
                CustomGlideRequest.ResourceType.Song,
            ).build()
            .into(holder.item.playlistDialogSongCoverImageView)
    }

    override fun getItemCount(): Int = songs.size

    var items: MutableList<Child>
        get() = this.songs
        set(songs) {
            this.songs = songs
            notifyDataSetChanged()
        }

    fun getItem(id: Int): Child? = songs.get(id)

    class ViewHolder internal constructor(
        var item: ItemHorizontalPlaylistDialogTrackBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.playlistDialogSongTitleTextView.setSelected(true)
        }
    }
}
