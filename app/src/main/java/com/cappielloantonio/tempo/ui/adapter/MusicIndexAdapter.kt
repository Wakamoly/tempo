package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemLibraryMusicIndexBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.helper.recyclerview.FastScrollbar.BubbleTextGetter
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Artist
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Locale
import java.util.Objects

@UnstableApi
class MusicIndexAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<MusicIndexAdapter.ViewHolder?>(),
    BubbleTextGetter {
    private var artists: MutableList<Artist>?

    init {
        this.artists = mutableListOf<Artist?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemLibraryMusicIndexBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return MusicIndexAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val artist = artists!!.get(position)

        holder.item.musicIndexTitleTextView.text = artist.name

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                artist.name,
                CustomGlideRequest.ResourceType.Directory,
            ).build()
            .into(holder.item.musicIndexCoverImageView)
    }

    override fun getItemCount(): Int = artists!!.size

    fun setItems(artists: MutableList<Artist>?) {
        this.artists = artists
        notifyDataSetChanged()
    }

    override fun getTextToShowInBubble(pos: Int): String? =
        if (artists != null && !artists!!.isEmpty()) {
            Objects
                .requireNonNull<String?>(
                    artists!!.get(pos).name!!.uppercase(
                        Locale.getDefault(),
                    ),
                ).get(0)
                .toString()
        } else {
            null
        }

    inner class ViewHolder internal constructor(
        var item: ItemLibraryMusicIndexBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.musicIndexTitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            item.musicIndexMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putString(
                Constants.MUSIC_DIRECTORY_ID,
                artists!!.get(getBindingAdapterPosition()).id,
            )
            click.onMusicIndexClick(bundle)
        }
    }
}
