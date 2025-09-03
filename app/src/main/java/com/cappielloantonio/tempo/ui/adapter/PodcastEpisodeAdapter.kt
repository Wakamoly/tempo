package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemHomePodcastEpisodeBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.PodcastEpisode
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.text.SimpleDateFormat
import java.util.stream.Collectors

class PodcastEpisodeAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<PodcastEpisodeAdapter.ViewHolder?>() {
    private var podcastEpisodes: MutableList<PodcastEpisode>
    private var podcastEpisodesFull: MutableList<PodcastEpisode>? = null

    init {
        this.podcastEpisodes = mutableListOf<PodcastEpisode?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHomePodcastEpisodeBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PodcastEpisodeAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val podcastEpisode = podcastEpisodes.get(position)
        val simpleDateFormat = SimpleDateFormat("MMM d")

        holder.item.podcastTitleLabel.text = podcastEpisode.title
        holder.item.podcastSubtitleLabel.text = podcastEpisode.artist
        holder.item.podcastReleasesAndDurationLabel.text = holder.itemView.context.getString(
            R.string.podcast_release_date_duration_formatter,
            simpleDateFormat.format(podcastEpisode.publishDate),
            MusicUtil.getReadablePodcastDurationString(
                podcastEpisode.duration!!.toLong()
            )
        )
        holder.item.podcastDescriptionText.text = MusicUtil.getReadableString(podcastEpisode.description)

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            podcastEpisode.coverArtId,
            CustomGlideRequest.ResourceType.Podcast
        )
            .build()
            .into(holder.item.podcastCoverImageView)

        holder.item.podcastPlayButton.setEnabled(podcastEpisode.status == "completed")
        holder.item.podcastMoreButton.visibility = if (podcastEpisode.status == "completed") View.VISIBLE else View.GONE
        holder.item.podcastDownloadRequestButton.visibility = if (podcastEpisode.status == "completed") View.GONE else View.VISIBLE
    }

    override fun getItemCount(): Int {
        return podcastEpisodes.size
    }

    fun setItems(podcastEpisodes: MutableList<PodcastEpisode>) {
        this.podcastEpisodesFull = podcastEpisodes
        this.podcastEpisodes = podcastEpisodesFull!!.stream()
            .filter { podcastEpisode: PodcastEpisode? -> podcastEpisode!!.status == "completed" }
            .collect(
                Collectors.toList()
            )
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ViewHolder internal constructor(var item: ItemHomePodcastEpisodeBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> openMore() })

            item.podcastPlayButton.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            item.podcastMoreButton.setOnClickListener(View.OnClickListener { v: View? -> openMore() })
            item.podcastDownloadRequestButton.setOnClickListener(View.OnClickListener { v: View? -> requestDownload() })
        }

        fun onClick() {
            val podcastEpisode = podcastEpisodes.get(getBindingAdapterPosition())

            if (podcastEpisode.status == "completed") {
                val bundle = Bundle()
                bundle.putParcelable(
                    Constants.PODCAST_OBJECT,
                    podcastEpisodes.get(getBindingAdapterPosition())
                )

                click.onPodcastEpisodeClick(bundle)
            }
        }

        private fun openMore(): Boolean {
            val podcastEpisode = podcastEpisodes.get(getBindingAdapterPosition())

            if (podcastEpisode.status == "completed") {
                val bundle = Bundle()
                bundle.putParcelable(
                    Constants.PODCAST_OBJECT,
                    podcastEpisodes.get(getBindingAdapterPosition())
                )

                click.onPodcastEpisodeLongClick(bundle)

                return true
            }

            return false
        }

        fun requestDownload() {
            val podcastEpisode = podcastEpisodes.get(getBindingAdapterPosition())

            if (podcastEpisode.status != "completed") {
                val bundle = Bundle()
                bundle.putParcelable(
                    Constants.PODCAST_OBJECT,
                    podcastEpisodes.get(getBindingAdapterPosition())
                )

                click.onPodcastEpisodeAltClick(bundle)
            }
        }
    }

    fun sort(order: String) {
        when (order) {
            Constants.PODCAST_FILTER_BY_DOWNLOAD -> podcastEpisodes = podcastEpisodesFull!!.stream()
                .filter { podcastEpisode: PodcastEpisode? -> podcastEpisode!!.status == "completed" }
                .collect(
                    Collectors.toList()
                )

            Constants.PODCAST_FILTER_BY_ALL -> podcastEpisodes = podcastEpisodesFull!!
        }

        notifyDataSetChanged()
    }
}