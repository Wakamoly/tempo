package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHorizontalPodcastChannelBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.PodcastChannel
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class PodcastChannelHorizontalAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<PodcastChannelHorizontalAdapter.ViewHolder?>() {
    private var podcastChannels: MutableList<PodcastChannel>

    init {
        this.podcastChannels = mutableListOf<PodcastChannel?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHorizontalPodcastChannelBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return PodcastChannelHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val podcastChannel = podcastChannels.get(position)

        holder.item.podcastChannelTitleTextView.text = podcastChannel.title
        holder.item.podcastChannelDescriptionTextView.text =
            MusicUtil.getReadableString(
                podcastChannel.description,
            )

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                podcastChannel.coverArtId,
                CustomGlideRequest.ResourceType.Podcast,
            ).build()
            .into(holder.item.podcastChannelCoverImageView)
    }

    override fun getItemCount(): Int = podcastChannels.size

    fun setItems(podcastChannels: MutableList<PodcastChannel>) {
        this.podcastChannels = podcastChannels
        notifyDataSetChanged()
    }

    fun getItem(id: Int): PodcastChannel? = podcastChannels.get(id)

    inner class ViewHolder internal constructor(
        var item: ItemHorizontalPodcastChannelBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.podcastChannelTitleTextView.setSelected(true)
            item.podcastChannelDescriptionTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.podcastChannelMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        private fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.PODCAST_CHANNEL_OBJECT,
                podcastChannels.get(getBindingAdapterPosition()),
            )

            click.onPodcastChannelClick(bundle)
        }

        private fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.PODCAST_CHANNEL_OBJECT,
                podcastChannels.get(getBindingAdapterPosition()),
            )

            click.onPodcastChannelLongClick(bundle)

            return true
        }
    }
}
