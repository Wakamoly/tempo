package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.appcompat.content.res.AppCompatResources
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemHorizontalTrackBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.subsonic.models.DiscTitle
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.DownloadUtil
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Preferences.showItemRating
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Date
import java.util.Locale
import java.util.Objects

@UnstableApi
class SongHorizontalAdapter(
    private val click: ClickCallback,
    private val showCoverArt: Boolean,
    private val showAlbum: Boolean,
    private val album: AlbumID3
) : RecyclerView.Adapter<SongHorizontalAdapter.ViewHolder?>(), Filterable {
    private var songsFull: MutableList<Child>
    private var songs: MutableList<Child>
    private var currentFilter: String? = ""

    private val filtering: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<Child?> = ArrayList<Child?>()

            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(songsFull)
            } else {
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                currentFilter = filterPattern

                for (item in songsFull) {
                    if (item.title!!.lowercase(Locale.getDefault()).contains(filterPattern)) {
                        filteredList.add(item)
                    }
                }
            }

            val results = FilterResults()
            results.values = filteredList

            return results
        }

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            songs = results.values as MutableList<Child>
            notifyDataSetChanged()
        }
    }

    init {
        this.songs = mutableListOf<Child?>()
        this.songsFull = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHorizontalTrackBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return SongHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val song = songs.get(position)

        holder.item.searchResultSongTitleTextView.text = song.title

        holder.item.searchResultSongSubtitleTextView.text = holder.itemView.context.getString(
            R.string.song_subtitle_formatter,
            if (this.showAlbum) song.album else song.artist,
            MusicUtil.getReadableDurationString(song.duration, false),
            MusicUtil.getReadableAudioQualityString(song)
        )

        holder.item.trackNumberTextView.text = MusicUtil.getReadableTrackNumber(
            holder.itemView.context,
            song.track
        )

        if (DownloadUtil.getDownloadTracker(holder.itemView.context).isDownloaded(song.id)) {
            holder.item.searchResultDownloadIndicatorImageView.visibility = View.VISIBLE
        } else {
            holder.item.searchResultDownloadIndicatorImageView.visibility = View.GONE
        }

        if (showCoverArt) CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .into(holder.item.songCoverImageView)

        holder.item.trackNumberTextView.visibility = if (showCoverArt) View.INVISIBLE else View.VISIBLE
        holder.item.songCoverImageView.setVisibility(if (showCoverArt) View.VISIBLE else View.INVISIBLE)

        if (!showCoverArt &&
            (position == 0 ||
                    (position > 0 && songs.get(position - 1) != null && songs.get(position - 1).discNumber != null && songs.get(
                        position
                    ).discNumber != null && songs.get(position - 1).discNumber!! < songs.get(
                        position
                    ).discNumber!!
                            )
                    )
        ) {
            holder.item.differentDiskDividerSector.visibility = View.VISIBLE

            if (songs.get(position).discNumber != null && !Objects.requireNonNull<Int?>(
                    songs.get(
                        position
                    ).discNumber
                ).toString().isBlank()
            ) {
                holder.item.discTitleTextView.text = holder.itemView.context.getString(
                    R.string.disc_titleless,
                    songs.get(position).discNumber.toString()
                )
            }

            if (album.discTitles != null) {
                val discTitle = album.discTitles!!.stream()
                    .filter { title: DiscTitle? -> title!!.disc == songs.get(position).discNumber }
                    .findFirst()

                if (discTitle.isPresent && discTitle.get().disc != null && discTitle.get().title != null && !discTitle.get().title!!.isEmpty()) {
                    holder.item.discTitleTextView.text = holder.itemView.context.getString(
                        R.string.disc_titlefull,
                        discTitle.get().disc.toString(),
                        discTitle.get().title
                    )
                }
            }
        }

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

    override fun getItemCount(): Int {
        return songs.size
    }

    fun setItems(songs: MutableList<Child>?) {
        this.songsFull = if (songs != null) songs else mutableListOf<Child?>()
        filtering.filter(currentFilter)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    override fun getFilter(): Filter {
        return filtering
    }

    fun getItem(id: Int): Child? {
        return songs.get(id)
    }

    inner class ViewHolder internal constructor(var item: ItemHorizontalTrackBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.searchResultSongTitleTextView.setSelected(true)
            item.searchResultSongSubtitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.searchResultSongMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelableArrayList(
                Constants.TRACKS_OBJECT,
                ArrayList<Child?>(MusicUtil.limitPlayableMedia(songs, getBindingAdapterPosition()))
            )
            bundle.putInt(
                Constants.ITEM_POSITION,
                MusicUtil.getPlayableMediaPosition(songs, getBindingAdapterPosition())
            )

            click.onMediaClick(bundle)
        }

        private fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.TRACK_OBJECT, songs.get(getBindingAdapterPosition()))

            click.onMediaLongClick(bundle)

            return true
        }
    }

    fun sort(order: String) {
        when (order) {
            Constants.MEDIA_BY_TITLE -> songs.sort(Comparator.comparing<Child?, String?>(Child::title))
            Constants.MEDIA_MOST_RECENTLY_STARRED -> songs.sort(
                Comparator.comparing<Child?, Date?>(
                    Child::starred, Comparator.nullsLast<Date?>(Comparator.reverseOrder<Date?>())
                )
            )

            Constants.MEDIA_LEAST_RECENTLY_STARRED -> songs.sort(
                Comparator.comparing<Child?, Date?>(
                    Child::starred, Comparator.nullsLast<Date?>(Comparator.naturalOrder<Date?>())
                )
            )
        }

        notifyDataSetChanged()
    }
}
