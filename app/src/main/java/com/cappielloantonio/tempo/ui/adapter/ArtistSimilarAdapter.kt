package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemLibrarySimilarArtistBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.SimilarArtistID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class ArtistSimilarAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<ArtistSimilarAdapter.ViewHolder?>() {
    private var artists: MutableList<SimilarArtistID3>

    init {
        this.artists = mutableListOf<SimilarArtistID3?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemLibrarySimilarArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistSimilarAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artists.get(position)

        holder.item.artistNameLabel.text = artist.name

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            artist.coverArtId,
            CustomGlideRequest.ResourceType.Artist
        )
            .build()
            .into(holder.item.similarArtistCoverImageView)
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    fun getItem(position: Int): SimilarArtistID3? {
        return artists.get(position)
    }

    fun setItems(artists: MutableList<SimilarArtistID3>) {
        this.artists = artists
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ViewHolder internal constructor(var item: ItemLibrarySimilarArtistBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.artistNameLabel.setSelected(true)
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ARTIST_OBJECT, artists.get(getBindingAdapterPosition()))

            click.onArtistClick(bundle)
        }

        fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ARTIST_OBJECT, artists.get(getBindingAdapterPosition()))

            click.onArtistLongClick(bundle)

            return true
        }
    }
}
