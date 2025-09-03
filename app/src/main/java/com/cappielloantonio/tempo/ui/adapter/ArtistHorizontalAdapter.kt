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
import com.cappielloantonio.tempo.databinding.ItemHorizontalArtistBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Date
import java.util.Locale

class ArtistHorizontalAdapter(private val click: ClickCallback) :
    RecyclerView.Adapter<ArtistHorizontalAdapter.ViewHolder?>(), Filterable {
    private var artistsFull: MutableList<ArtistID3>
    private var artists: MutableList<ArtistID3>
    private var currentFilter: String? = ""

    private val filtering: Filter = object : Filter() {
        override fun performFiltering(constraint: CharSequence?): FilterResults {
            val filteredList: MutableList<ArtistID3?> = ArrayList<ArtistID3?>()

            if (constraint == null || constraint.length == 0) {
                filteredList.addAll(artistsFull)
            } else {
                val filterPattern =
                    constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }
                currentFilter = filterPattern

                for (item in artistsFull) {
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
            artists = results.values as MutableList<ArtistID3>
            notifyDataSetChanged()
        }
    }

    init {
        this.artists = mutableListOf<ArtistID3?>()
        this.artistsFull = mutableListOf<ArtistID3?>()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = ItemHorizontalArtistBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ArtistHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val artist = artists.get(position)

        holder.item.artistNameTextView.text = artist.name

        if (artist.albumCount > 0) {
            holder.item.artistInfoTextView.text = "Album count: " + artist.albumCount
        } else {
            holder.item.artistInfoTextView.visibility = View.GONE
        }

        CustomGlideRequest.Builder.Companion.from(
            holder.itemView.context,
            artist.coverArtId,
            CustomGlideRequest.ResourceType.Artist
        )
            .build()
            .into(holder.item.artistCoverImageView)
    }

    override fun getItemCount(): Int {
        return artists.size
    }

    fun setItems(artists: MutableList<ArtistID3>?) {
        this.artistsFull = if (artists != null) artists else mutableListOf<ArtistID3?>()
        filtering.filter(currentFilter)
        notifyDataSetChanged()
    }

    override fun getFilter(): Filter {
        return filtering
    }

    fun getItem(id: Int): ArtistID3? {
        return artists.get(id)
    }

    override fun getItemViewType(position: Int): Int {
        return position
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    inner class ViewHolder internal constructor(var item: ItemHorizontalArtistBinding) :
        RecyclerView.ViewHolder(
            item.getRoot()
        ) {
        init {
            item.artistNameTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.artistMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        private fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ARTIST_OBJECT, artists.get(getBindingAdapterPosition()))

            click.onArtistClick(bundle)
        }

        fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.ARTIST_OBJECT, artists.get(getBindingAdapterPosition()))

            click.onArtistLongClick(bundle)

            return true
        }
    }

    fun sort(order: String) {
        when (order) {
            Constants.ARTIST_ORDER_BY_NAME -> artists.sort(
                Comparator.comparing<ArtistID3?, String?>(
                    ArtistID3::name
                )
            )

            Constants.ARTIST_ORDER_BY_MOST_RECENTLY_STARRED -> artists.sort(
                Comparator.comparing<ArtistID3?, Date?>(
                    ArtistID3::starred, Comparator.nullsLast<Date?>(
                        Comparator.reverseOrder<Date?>()
                    )
                )
            )

            Constants.ARTIST_ORDER_BY_LEAST_RECENTLY_STARRED -> artists.sort(
                Comparator.comparing<ArtistID3?, Date?>(
                    ArtistID3::starred, Comparator.nullsLast<Date?>(
                        Comparator.naturalOrder<Date?>()
                    )
                )
            )

        }

        notifyDataSetChanged()
    }
}
