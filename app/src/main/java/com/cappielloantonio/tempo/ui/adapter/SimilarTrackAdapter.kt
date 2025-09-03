package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHomeSimilarTrackBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class SimilarTrackAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<SimilarTrackAdapter.ViewHolder?>() {
    private var songs: MutableList<Child>

    init {
        this.songs = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHomeSimilarTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SimilarTrackAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs.get(position)

        holder.item.titleTrackLabel.text = song.title

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .into(holder.item.trackCoverImageView)
    }

    override fun getItemCount(): Int {
        return songs.size
    }

    fun getItem(position: Int): Child? {
        return songs.get(position)
    }

    fun setItems(songs: MutableList<Child>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(var item: ItemHomeSimilarTrackBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.TRACK_OBJECT, songs.get(getBindingAdapterPosition()))
            bundle.putBoolean(Constants.MEDIA_MIX, true)

            click.onMediaClick(bundle)
        }

        fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.TRACK_OBJECT, songs.get(getBindingAdapterPosition()))

            click.onMediaLongClick(bundle)

            return true
        }
    }
}
