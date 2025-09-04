package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import android.widget.Filter
import android.widget.Filterable
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHorizontalAlbumBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Date
import java.util.Locale

class AlbumHorizontalAdapter(
    private val click: ClickCallback,
    private val isOffline: Boolean,
) : RecyclerView.Adapter<AlbumHorizontalAdapter.ViewHolder?>(),
    Filterable {
    private var albumsFull: MutableList<AlbumID3>
    private var albums: MutableList<AlbumID3>
    private var currentFilter: String? = ""

    private val filtering: Filter =
        object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<AlbumID3?> = ArrayList<AlbumID3?>()

                if (constraint == null || constraint.length == 0) {
                    filteredList.addAll(albumsFull)
                } else {
                    val filterPattern =
                        constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                    currentFilter = filterPattern

                    for (item in albumsFull) {
                        if (item.name!!.lowercase(Locale.getDefault()).contains(filterPattern)) {
                            filteredList.add(item)
                        }
                    }
                }

                val results = FilterResults()
                results.values = filteredList

                return results
            }

            override fun publishResults(
                constraint: CharSequence?,
                results: FilterResults,
            ) {
                albums = results.values as MutableList<AlbumID3>
                notifyDataSetChanged()
            }
        }

    init {
        this.albums = mutableListOf<AlbumID3?>()
        this.albumsFull = mutableListOf<AlbumID3?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHorizontalAlbumBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return AlbumHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val album = albums.get(position)

        holder.item.albumTitleTextView.text = album.name
        holder.item.albumArtistTextView.text = album.artist

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                album.coverArtId,
                CustomGlideRequest.ResourceType.Album,
            ).build()
            .into(holder.item.albumCoverImageView)
    }

    override fun getItemCount(): Int = albums.size

    fun setItems(albums: MutableList<AlbumID3>?) {
        this.albumsFull = if (albums != null) albums else mutableListOf<AlbumID3?>()
        filtering.filter(currentFilter)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter = filtering

    fun getItem(id: Int): AlbumID3? = albums.get(id)

    inner class ViewHolder internal constructor(
        var item: ItemHorizontalAlbumBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.albumTitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.albumMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        private fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ALBUM_OBJECT, albums.get(getBindingAdapterPosition()))

            click.onAlbumClick(bundle)
        }

        private fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ALBUM_OBJECT, albums.get(getBindingAdapterPosition()))

            click.onAlbumLongClick(bundle)

            return true
        }
    }

    fun sort(order: String) {
        when (order) {
            Constants.ALBUM_ORDER_BY_NAME ->
                albums.sort(
                    Comparator.comparing<AlbumID3?, String?>(
                        AlbumID3::name,
                    ),
                )

            Constants.ALBUM_ORDER_BY_MOST_RECENTLY_STARRED ->
                albums.sort(
                    Comparator.comparing<AlbumID3?, Date?>(
                        AlbumID3::starred,
                        Comparator.nullsLast<Date?>(
                            Comparator.reverseOrder<Date?>(),
                        ),
                    ),
                )

            Constants.ALBUM_ORDER_BY_LEAST_RECENTLY_STARRED ->
                albums.sort(
                    Comparator.comparing<AlbumID3?, Date?>(
                        AlbumID3::starred,
                        Comparator.nullsLast<Date?>(
                            Comparator.naturalOrder<Date?>(),
                        ),
                    ),
                )
        }

        notifyDataSetChanged()
    }
}
