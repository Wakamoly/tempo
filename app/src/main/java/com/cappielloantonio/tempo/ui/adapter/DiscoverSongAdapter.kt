package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateDecelerateInterpolator
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHomeDiscoverSongBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class DiscoverSongAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<DiscoverSongAdapter.ViewHolder?>() {
    private var songs: MutableList<Child>

    init {
        this.songs = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHomeDiscoverSongBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return DiscoverSongAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val song = songs.get(position)

        holder.item.titleDiscoverSongLabel.text = song.title
        holder.item.albumDiscoverSongLabel.text = song.album

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                song.coverArtId,
                CustomGlideRequest.ResourceType.Song,
            ).build()
            .into(holder.item.discoverSongCoverImageView)
    }

    override fun onViewAttachedToWindow(holder: ViewHolder) {
        super.onViewAttachedToWindow(holder)
        startAnimation(holder)
    }

    override fun getItemCount(): Int = songs.size

    fun setItems(songs: MutableList<Child>) {
        this.songs = songs
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(
        var item: ItemHomeDiscoverSongBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.TRACK_OBJECT, songs.get(getBindingAdapterPosition()))
            bundle.putBoolean(Constants.MEDIA_MIX, true)

            click.onMediaClick(bundle)
        }
    }

    private fun startAnimation(holder: ViewHolder) {
        holder.item.discoverSongCoverImageView
            .animate()
            .setDuration(20000)
            .setStartDelay(10)
            .setInterpolator(AccelerateDecelerateInterpolator())
            .scaleX(1.4f)
            .scaleY(1.4f)
            .start()
    }
}
