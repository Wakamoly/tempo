package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemLibraryArtistBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class ArtistAdapter(
    private val click: ClickCallback,
    private val mix: Boolean,
    private val bestOf: Boolean,
) : RecyclerView.Adapter<ArtistAdapter.ViewHolder?>() {
    private var artists: MutableList<ArtistID3>

    init {
        this.artists = mutableListOf<ArtistID3?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemLibraryArtistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ArtistAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val artist = artists.get(position)

        holder.item.artistNameLabel.text = artist.name

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                artist.coverArtId,
                CustomGlideRequest.ResourceType.Artist,
            ).build()
            .into(holder.item.artistCoverImageView)
    }

    override fun getItemCount(): Int = artists.size

    fun getItem(position: Int): ArtistID3? = artists.get(position)

    fun setItems(artists: MutableList<ArtistID3>) {
        this.artists = artists
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    inner class ViewHolder internal constructor(
        var item: ItemLibraryArtistBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.artistNameLabel.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ARTIST_OBJECT, artists.get(getBindingAdapterPosition()))
            bundle.putBoolean(Constants.MEDIA_MIX, mix)
            bundle.putBoolean(Constants.MEDIA_BEST_OF, bestOf)

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
