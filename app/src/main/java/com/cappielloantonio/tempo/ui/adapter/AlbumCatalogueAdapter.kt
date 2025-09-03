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
import com.cappielloantonio.tempo.databinding.ItemLibraryCatalogueAlbumBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.AlbumID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Collections
import java.util.Date
import java.util.Locale
import kotlin.collections.ArrayList
import kotlin.collections.MutableList
import kotlin.collections.mutableListOf
import kotlin.collections.sort

class AlbumCatalogueAdapter(private val click: ClickCallback, private val showArtist: Boolean) :
    RecyclerView.Adapter<AlbumCatalogueAdapter.ViewHolder?>(), Filterable {
    private var currentFilter: String? = ""

    private val filtering: Filter = object : Filter() {
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

        override fun publishResults(constraint: CharSequence?, results: FilterResults) {
            albums = results.values as MutableList<AlbumID3>
            notifyDataSetChanged()
        }
    }

    private var albums: MutableList<AlbumID3>
    private var albumsFull: MutableList<AlbumID3>

    init {
        this.albums = mutableListOf<AlbumID3?>()
        this.albumsFull = mutableListOf<AlbumID3?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemLibraryCatalogueAlbumBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return AlbumCatalogueAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val album = albums.get(position)

        holder.item.albumNameLabel.text = album.name
        holder.item.artistNameLabel.text = album.artist
        holder.item.artistNameLabel.visibility = if (showArtist) View.VISIBLE else View.GONE

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            album.coverArtId,
            CustomGlideRequest.ResourceType.Album
        )
            .build()
            .into(holder.item.albumCatalogueCoverImageView)
    }

    override fun getItemCount(): Int {
        return albums.size
    }

    fun getItem(position: Int): AlbumID3? {
        return albums.get(position)
    }

    fun setItems(albums: MutableList<AlbumID3?>) {
        this.albumsFull = ArrayList<AlbumID3>(albums)
        filtering.filter(currentFilter)
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

    inner class ViewHolder internal constructor(var item: ItemLibraryCatalogueAlbumBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.albumNameLabel.setSelected(true)
            item.artistNameLabel.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })
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
            Constants.ALBUM_ORDER_BY_NAME -> albums.sort(
                Comparator.comparing<AlbumID3?, String?>(
                    AlbumID3::name
                )
            )

            Constants.ALBUM_ORDER_BY_ARTIST -> albums.sort(
                Comparator.comparing<AlbumID3?, String?>(
                    AlbumID3::artist, Comparator.nullsLast<String?>(
                        Comparator.naturalOrder<String?>()
                    )
                )
            )

            Constants.ALBUM_ORDER_BY_YEAR -> albums.sort(
                Comparator.comparing<AlbumID3?, Int?>(
                    AlbumID3::year
                )
            )

            Constants.ALBUM_ORDER_BY_RANDOM -> Collections.shuffle(albums)
            Constants.ALBUM_ORDER_BY_RECENTLY_ADDED -> {
                albums.sort(Comparator.comparing<AlbumID3?, Date?>(AlbumID3::created))
                Collections.reverse(albums)
            }

            Constants.ALBUM_ORDER_BY_RECENTLY_PLAYED -> {
                albums.sort(Comparator.comparing<AlbumID3?, Date?>(AlbumID3::played))
                Collections.reverse(albums)
            }

            Constants.ALBUM_ORDER_BY_MOST_PLAYED -> {
                albums.sort(Comparator.comparing<AlbumID3?, Long?>(AlbumID3::playCount))
                Collections.reverse(albums)
            }
        }

        notifyDataSetChanged()
    }
}