package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemLibraryArtistPageOrSimilarAlbumBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class AlbumArtistPageOrSimilarAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<AlbumArtistPageOrSimilarAdapter.ViewHolder?>() {
    private var albums: MutableList<AlbumID3>

    init {
        this.albums = mutableListOf<AlbumID3?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemLibraryArtistPageOrSimilarAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumArtistPageOrSimilarAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums.get(position)

        holder.item.albumNameLabel.text = album.name
        holder.item.artistNameLabel.text = album.artist

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            album.coverArtId,
            CustomGlideRequest.ResourceType.Album
        )
            .build()
            .into(holder.item.artistPageAlbumCoverImageView)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun getItem(position: Int): AlbumID3? {
        return albums.get(position)
    }

    fun setItems(albums: MutableList<AlbumID3>) {
        this.albums = albums
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(var item: ItemLibraryArtistPageOrSimilarAlbumBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.albumNameLabel.setSelected(true)
            item.artistNameLabel.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })
        }

        private fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ALBUM_OBJECT, albums.get(getBindingAdapterPosition()))

            click.onAlbumClick(bundle)
        }

        private fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ALBUM_OBJECT, albums.get(getBindingAdapterPosition()))

            click.onAlbumLongClick(bundle)

            return true
        }
    }
}
