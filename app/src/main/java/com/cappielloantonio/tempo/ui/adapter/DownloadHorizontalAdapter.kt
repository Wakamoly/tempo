package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemHorizontalDownloadBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Child
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.MusicUtil
import com.cappielloantonio.tempo.util.Util
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Objects
import java.util.stream.Collectors

@UnstableApi
class DownloadHorizontalAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<DownloadHorizontalAdapter.ViewHolder?>() {
    private var view: String
    private var filterKey: String? = null
    private var filterValue: String? = null

    private var songs: MutableList<Child>
    var shuffling: MutableList<Child?>? = null
        private set
    private var grouped: MutableList<Child>

    init {
        this.view = Constants.DOWNLOAD_TYPE_TRACK
        this.songs = mutableListOf<Child?>()
        this.grouped = mutableListOf<Child?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHorizontalDownloadBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DownloadHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        when (view) {
            Constants.DOWNLOAD_TYPE_TRACK -> initTrackLayout(holder, position)
            Constants.DOWNLOAD_TYPE_ALBUM -> initAlbumLayout(holder, position)
            Constants.DOWNLOAD_TYPE_ARTIST -> initArtistLayout(holder, position)
            Constants.DOWNLOAD_TYPE_GENRE -> initGenreLayout(holder, position)
            Constants.DOWNLOAD_TYPE_YEAR -> initYearLayout(holder, position)
        }
    }

    override fun getItemCount(): Int {
        return grouped.size
    }

    fun setItems(view: String, filterKey: String, filterValue: String?, songs: MutableList<Child>) {
        this.view = if (filterValue != null) view else filterKey
        this.filterKey = filterKey
        this.filterValue = filterValue

        this.songs = songs
        this.grouped = groupSong(songs)
        this.shuffling = shufflingSong(ArrayList<Child?>(songs))

        notifyDataSetChanged()
    }

    fun getItem(id: Int): Child? {
        return grouped.get(id)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private fun groupSong(songs: MutableList<Child>): MutableList<Child> {
        when (view) {
            Constants.DOWNLOAD_TYPE_TRACK -> return filterSong(
                filterKey!!,
                filterValue,
                songs.stream().filter { song: Child? ->
                    Objects.nonNull(
                        song!!.id
                    )
                }.filter(Util.distinctByKey<Child?>(Child::id)).collect(Collectors.toList())
            )

            Constants.DOWNLOAD_TYPE_ALBUM -> return filterSong(
                filterKey!!,
                filterValue,
                songs.stream().filter { song: Child? ->
                    Objects.nonNull(
                        song!!.albumId
                    )
                }.filter(Util.distinctByKey<Child?>(Child::albumId)).collect(
                    Collectors.toList()
                )
            )

            Constants.DOWNLOAD_TYPE_ARTIST -> return filterSong(
                filterKey!!,
                filterValue,
                songs.stream().filter { song: Child? ->
                    Objects.nonNull(
                        song!!.artistId
                    )
                }.filter(Util.distinctByKey<Child?>(Child::artistId)).collect(
                    Collectors.toList()
                )
            )

            Constants.DOWNLOAD_TYPE_GENRE -> return filterSong(
                filterKey!!,
                filterValue,
                songs.stream().filter { song: Child? ->
                    Objects.nonNull(
                        song!!.genre
                    )
                }.filter(Util.distinctByKey<Child?>(Child::genre)).collect(Collectors.toList())
            )

            Constants.DOWNLOAD_TYPE_YEAR -> return filterSong(
                filterKey!!,
                filterValue,
                songs.stream().filter { song: Child? ->
                    Objects.nonNull(
                        song!!.year
                    )
                }.filter(Util.distinctByKey<Child?>(Child::year)).collect(Collectors.toList())
            )
        }

        return mutableListOf<Child?>()
    }

    private fun filterSong(
        filterKey: String,
        filterValue: String?,
        songs: MutableList<Child>
    ): MutableList<Child> {
        if (filterValue != null) {
            when (filterKey) {
                Constants.DOWNLOAD_TYPE_TRACK -> return songs.stream()
                    .filter { child: Child? -> child!!.id == filterValue }.collect(
                        Collectors.toList()
                    )

                Constants.DOWNLOAD_TYPE_ALBUM -> return songs.stream()
                    .filter { child: Child? -> child!!.albumId == filterValue }.collect(
                        Collectors.toList()
                    )

                Constants.DOWNLOAD_TYPE_GENRE -> return songs.stream()
                    .filter { child: Child? -> child!!.genre == filterValue }.collect(
                        Collectors.toList()
                    )

                Constants.DOWNLOAD_TYPE_YEAR -> return songs.stream()
                    .filter { child: Child? -> child!!.year == filterValue.toInt() }.collect(
                        Collectors.toList()
                    )

                Constants.DOWNLOAD_TYPE_ARTIST -> return songs.stream()
                    .filter { child: Child? -> child!!.artistId == filterValue }.collect(
                        Collectors.toList()
                    )
            }
        }

        return songs
    }

    private fun shufflingSong(songs: MutableList<Child?>): MutableList<Child?>? {
        if (filterValue == null) {
            return songs
        }

        when (filterKey) {
            Constants.DOWNLOAD_TYPE_TRACK -> return songs.stream()
                .filter { child: Child? -> child!!.id == filterValue }.collect(
                    Collectors.toList()
                )

            Constants.DOWNLOAD_TYPE_ALBUM -> return songs.stream()
                .filter { child: Child? -> child!!.albumId == filterValue }.collect(
                    Collectors.toList()
                )

            Constants.DOWNLOAD_TYPE_GENRE -> return songs.stream()
                .filter { child: Child? -> child!!.genre == filterValue }.collect(
                    Collectors.toList()
                )

            Constants.DOWNLOAD_TYPE_YEAR -> return songs.stream()
                .filter { child: Child? -> child!!.year == filterValue!!.toInt() }.collect(
                    Collectors.toList()
                )

            Constants.DOWNLOAD_TYPE_ARTIST -> return songs.stream()
                .filter { child: Child? -> child!!.artistId == filterValue }.collect(
                    Collectors.toList()
                )

            else -> return songs
        }
    }

    private fun countSong(
        filterKey: String,
        filterValue: String?,
        songs: MutableList<Child>
    ): String {
        if (filterValue != null) {
            when (filterKey) {
                Constants.DOWNLOAD_TYPE_TRACK -> return songs.stream()
                    .filter { child: Child? -> child!!.id == filterValue }.count().toString()

                Constants.DOWNLOAD_TYPE_ALBUM -> return songs.stream()
                    .filter { child: Child? -> child!!.albumId == filterValue }.count().toString()

                Constants.DOWNLOAD_TYPE_GENRE -> return songs.stream()
                    .filter { child: Child? -> child!!.genre == filterValue }.count().toString()

                Constants.DOWNLOAD_TYPE_YEAR -> return songs.stream()
                    .filter { child: Child? -> child!!.year == filterValue.toInt() }.count()
                    .toString()

                Constants.DOWNLOAD_TYPE_ARTIST -> return songs.stream()
                    .filter { child: Child? -> child!!.artistId == filterValue }.count().toString()
            }
        }

        return "0"
    }

    private fun initTrackLayout(holder: ViewHolder, position: Int) {
        val song = grouped.get(position)

        holder.item.downloadedItemTitleTextView.text = song.title
        holder.item.downloadedItemSubtitleTextView.text = holder.itemView.context.getString(
            R.string.song_subtitle_formatter,
            song.artist,
            MusicUtil.getReadableDurationString(song.duration, false),
            ""
        )

        holder.item.downloadedItemPreTextView.text = song.album

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .into(holder.item.itemCoverImageView)

        holder.item.itemCoverImageView.setVisibility(View.VISIBLE)
        holder.item.downloadedItemMoreButton.visibility = View.VISIBLE
        holder.item.divider.visibility = View.VISIBLE

        if (position > 0 && grouped.get(position - 1) != null && (grouped.get(position - 1).album != grouped.get(
                position
            ).album)
        ) {
            holder.item.divider.setPadding(
                0,
                holder.itemView.context.resources
                    .getDimension(R.dimen.downloaded_item_padding).toInt(),
                0,
                0
            )
        } else {
            if (position > 0) holder.item.divider.visibility = View.GONE
        }
    }

    private fun initAlbumLayout(holder: ViewHolder, position: Int) {
        val song = grouped.get(position)

        holder.item.downloadedItemTitleTextView.text = song.album
        holder.item.downloadedItemSubtitleTextView.text = holder.itemView.context.getString(
            R.string.download_item_single_subtitle_formatter, countSong(
                Constants.DOWNLOAD_TYPE_ALBUM, song.albumId, songs
            )
        )
        holder.item.downloadedItemPreTextView.text = song.artist

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .into(holder.item.itemCoverImageView)

        holder.item.itemCoverImageView.setVisibility(View.VISIBLE)
        holder.item.downloadedItemMoreButton.visibility = View.VISIBLE
        holder.item.divider.visibility = View.VISIBLE

        if (position > 0 && grouped.get(position - 1) != null && (grouped.get(position - 1).artist != grouped.get(
                position
            ).artist)
        ) {
            holder.item.divider.setPadding(
                0,
                holder.itemView.context.resources
                    .getDimension(R.dimen.downloaded_item_padding).toInt(),
                0,
                0
            )
        } else {
            if (position > 0) holder.item.divider.visibility = View.GONE
        }
    }

    private fun initArtistLayout(holder: ViewHolder, position: Int) {
        val song = grouped.get(position)

        holder.item.downloadedItemTitleTextView.text = song.artist
        holder.item.downloadedItemSubtitleTextView.text = holder.itemView.context.getString(
            R.string.download_item_single_subtitle_formatter, countSong(
                Constants.DOWNLOAD_TYPE_ARTIST, song.artistId, songs
            )
        )

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            song.coverArtId,
            CustomGlideRequest.ResourceType.Song
        )
            .build()
            .into(holder.item.itemCoverImageView)

        holder.item.itemCoverImageView.setVisibility(View.VISIBLE)
        holder.item.downloadedItemMoreButton.visibility = View.VISIBLE
        holder.item.divider.visibility = View.GONE
    }

    private fun initGenreLayout(holder: ViewHolder, position: Int) {
        val song = grouped.get(position)

        holder.item.downloadedItemTitleTextView.text = song.genre
        holder.item.downloadedItemSubtitleTextView.text = holder.itemView.context.getString(
            R.string.download_item_single_subtitle_formatter, countSong(
                Constants.DOWNLOAD_TYPE_GENRE, song.genre, songs
            )
        )

        holder.item.itemCoverImageView.setVisibility(View.GONE)
        holder.item.downloadedItemMoreButton.visibility = View.VISIBLE
        holder.item.divider.visibility = View.GONE
    }

    private fun initYearLayout(holder: ViewHolder, position: Int) {
        val song = grouped.get(position)

        holder.item.downloadedItemTitleTextView.text = song.year.toString()
        holder.item.downloadedItemSubtitleTextView.text = holder.itemView.context.getString(
            R.string.download_item_single_subtitle_formatter, countSong(
                Constants.DOWNLOAD_TYPE_YEAR, song.year.toString(), songs
            )
        )

        holder.item.itemCoverImageView.setVisibility(View.GONE)
        holder.item.downloadedItemMoreButton.visibility = View.VISIBLE
        holder.item.divider.visibility = View.GONE
    }

    inner class ViewHolder internal constructor(var item: ItemHorizontalDownloadBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.downloadedItemTitleTextView.setSelected(true)
            item.downloadedItemSubtitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.downloadedItemMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
            val bundle = Bundle()

            when (view) {
                Constants.DOWNLOAD_TYPE_TRACK -> {
                    bundle.putParcelableArrayList(
                        Constants.TRACKS_OBJECT,
                        ArrayList<Child?>(grouped)
                    )
                    bundle.putInt(Constants.ITEM_POSITION, getBindingAdapterPosition())
                    click.onMediaClick(bundle)
                }

                Constants.DOWNLOAD_TYPE_ALBUM -> {
                    bundle.putString(
                        Constants.DOWNLOAD_TYPE_ALBUM,
                        grouped.get(getBindingAdapterPosition()).albumId
                    )
                    click.onAlbumClick(bundle)
                }

                Constants.DOWNLOAD_TYPE_ARTIST -> {
                    bundle.putString(
                        Constants.DOWNLOAD_TYPE_ARTIST,
                        grouped.get(getBindingAdapterPosition()).artistId
                    )
                    click.onArtistClick(bundle)
                }

                Constants.DOWNLOAD_TYPE_GENRE -> {
                    bundle.putString(
                        Constants.DOWNLOAD_TYPE_GENRE,
                        grouped.get(getBindingAdapterPosition()).genre
                    )
                    click.onGenreClick(bundle)
                }

                Constants.DOWNLOAD_TYPE_YEAR -> {
                    bundle.putString(
                        Constants.DOWNLOAD_TYPE_YEAR,
                        grouped.get(getBindingAdapterPosition()).year.toString()
                    )
                    click.onYearClick(bundle)
                }
            }
        }

        private fun onLongClick(): Boolean {
            val filteredSongs = ArrayList<Child>()

            val bundle = Bundle()

            when (view) {
                Constants.DOWNLOAD_TYPE_TRACK -> filteredSongs.add(
                    grouped.get(
                        getBindingAdapterPosition()
                    )
                )

                Constants.DOWNLOAD_TYPE_ALBUM -> filteredSongs.addAll(
                    filterSong(
                        Constants.DOWNLOAD_TYPE_ALBUM,
                        grouped.get(getBindingAdapterPosition()).albumId,
                        songs
                    )
                )

                Constants.DOWNLOAD_TYPE_ARTIST -> filteredSongs.addAll(
                    filterSong(
                        Constants.DOWNLOAD_TYPE_ARTIST,
                        grouped.get(getBindingAdapterPosition()).artistId,
                        songs
                    )
                )

                Constants.DOWNLOAD_TYPE_GENRE -> filteredSongs.addAll(
                    filterSong(
                        Constants.DOWNLOAD_TYPE_GENRE,
                        grouped.get(getBindingAdapterPosition()).genre,
                        songs
                    )
                )

                Constants.DOWNLOAD_TYPE_YEAR -> filteredSongs.addAll(
                    filterSong(
                        Constants.DOWNLOAD_TYPE_YEAR,
                        grouped.get(getBindingAdapterPosition()).year.toString(),
                        songs
                    )
                )
            }

            if (filteredSongs.isEmpty()) return false

            bundle.putParcelableArrayList(
                Constants.DOWNLOAD_GROUP,
                ArrayList<Child?>(filteredSongs)
            )
            bundle.putString(
                Constants.DOWNLOAD_GROUP_TITLE,
                item.downloadedItemTitleTextView.getText().toString()
            )
            bundle.putString(
                Constants.DOWNLOAD_GROUP_SUBTITLE,
                item.downloadedItemSubtitleTextView.getText().toString()
            )
            click.onDownloadGroupLongClick(bundle)

            return true
        }
    }
}
