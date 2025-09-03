package com.cappielloantonio.tempo.ui.adapter

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.media3.session.MediaBrowser
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.bumptech.glide.RequestBuilder
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemPlayerQueueSongBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.interfaces.MediaIndexCallback
import com.cappielloantonio.tempo.service.MediaManager
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.showItemRating
import com.google.common.util.concurrent.ListenableFuture
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class PlayerSongQueueAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<PlayerSongQueueAdapter.ViewHolder?>() {
    private var mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>? = null
    private var songs: MutableList<Child>?

    init {
        this.songs = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemPlayerQueueSongBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PlayerSongQueueAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs!!.get(holder.layoutPosition)

        holder.item.queueSongTitleTextView.text = song.title
        holder.item.queueSongSubtitleTextView.text = holder.itemView.context.getString(
            R.string.song_subtitle_formatter,
            song.artist,
            MusicUtil.getReadableDurationString(song.duration, false),
            MusicUtil.getReadableAudioQualityString(song)
        )

        val thumbnail: RequestBuilder<Drawable?> = CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .sizeMultiplier(0.1f)

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .thumbnail(thumbnail)
            .into(holder.item.queueSongCoverImageView)

        MediaManager.getCurrentIndex(mediaBrowserListenableFuture, object : MediaIndexCallback {
            override fun onRecovery(index: Int) {
                if (holder.layoutPosition < index) {
                    holder.item.queueSongTitleTextView.setAlpha(0.2f)
                    holder.item.queueSongSubtitleTextView.setAlpha(0.2f)
                    holder.item.ratingIndicatorImageView.setAlpha(0.2f)
                } else {
                    holder.item.queueSongTitleTextView.setAlpha(1.0f)
                    holder.item.queueSongSubtitleTextView.setAlpha(1.0f)
                    holder.item.ratingIndicatorImageView.setAlpha(1.0f)
                }
            }
        })

        if (showItemRating()) {
            if (song.starred == null && song.userRating == null) {
                holder.item.ratingIndicatorImageView.visibility = View.GONE
            }

            holder.item.preferredIcon.setVisibility(if (song.starred != null) View.VISIBLE else View.GONE)
            holder.item.ratingBarLayout.visibility = if (song.userRating != null) View.VISIBLE else View.GONE

            if (song.userRating != null) {
                holder.item.oneStarIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        if (song.userRating!! >= 1) R.drawable.ic_star else R.drawable.ic_star_outlined
                    )
                )
                holder.item.twoStarIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        if (song.userRating!! >= 2) R.drawable.ic_star else R.drawable.ic_star_outlined
                    )
                )
                holder.item.threeStarIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        if (song.userRating!! >= 3) R.drawable.ic_star else R.drawable.ic_star_outlined
                    )
                )
                holder.item.fourStarIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        if (song.userRating!! >= 4) R.drawable.ic_star else R.drawable.ic_star_outlined
                    )
                )
                holder.item.fiveStarIcon.setImageDrawable(
                    AppCompatResources.getDrawable(
                        holder.itemView.context,
                        if (song.userRating!! >= 5) R.drawable.ic_star else R.drawable.ic_star_outlined
                    )
                )
            }
        } else {
            holder.item.ratingIndicatorImageView.visibility = View.GONE
        }
    }

    var items: MutableList<Child>?
        get() = this.songs
        set(songs) {
            this.songs = songs
            notifyDataSetChanged()
        }

    override fun getItemCount(): Int {
        if (songs == null) {
            return 0
        }
        return songs!!.size
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    fun setMediaBrowserListenableFuture(mediaBrowserListenableFuture: ListenableFuture<MediaBrowser?>?) {
        this.mediaBrowserListenableFuture = mediaBrowserListenableFuture
    }

    fun getItem(id: Int): Child? {
        return songs!!.get(id)
    }

    inner class ViewHolder internal constructor(var item: ItemPlayerQueueSongBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.queueSongTitleTextView.setSelected(true)
            item.queueSongSubtitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, ArrayList<Child?>(songs))
            bundle.putInt(Constants.ITEM_POSITION, getBindingAdapterPosition())

            click.onMediaClick(bundle)
        }
    }
}
