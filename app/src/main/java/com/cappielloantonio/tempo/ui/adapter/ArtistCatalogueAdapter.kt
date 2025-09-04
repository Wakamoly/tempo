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
import com.cappielloantonio.tempo.databinding.ItemLibraryCatalogueArtistBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.ArtistID3
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build
import java.util.Collections
import java.util.Locale

class ArtistCatalogueAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<ArtistCatalogueAdapter.ViewHolder?>(),
    Filterable {
    private val filtering: Filter =
        object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val filteredList: MutableList<ArtistID3?> = ArrayList<ArtistID3?>()

                if (constraint == null || constraint.length == 0) {
                    filteredList.addAll(artistFull!!)
                } else {
                    val filterPattern =
                        constraint.toString().lowercase(Locale.getDefault()).trim { it <= ' ' }

                    for (item in artistFull!!) {
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
                artists.clear()
                if (results.count > 0) artists.addAll(results.values as MutableList<*>?)
                notifyDataSetChanged()
            }
        }

    private var artists: MutableList<ArtistID3>
    private var artistFull: MutableList<ArtistID3>? = null

    init {
        this.artists = mutableListOf<ArtistID3?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemLibraryCatalogueArtistBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ArtistCatalogueAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val artist = artists.get(position)

        holder.item.artistNameLabel.text = artist.name

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                artist.coverArtId,
                CustomGlideRequest.ResourceType.Artist,
            ).build()
            .into(holder.item.artistCatalogueCoverImageView)
    }

    override fun getItemCount(): Int = artists.size

    fun getItem(position: Int): ArtistID3? = artists.get(position)

    fun setItems(artists: MutableList<ArtistID3>) {
        this.artists = artists
        this.artistFull = ArrayList<ArtistID3>(artists)
        notifyDataSetChanged()
    }

    override fun getItemViewType(position: Int): Int = position

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getFilter(): Filter = filtering

    inner class ViewHolder internal constructor(
        var item: ItemLibraryCatalogueArtistBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.artistNameLabel.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
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
            Constants.ARTIST_ORDER_BY_NAME ->
                artists.sort(
                    Comparator.comparing<ArtistID3?, String?>(
                        ArtistID3::name,
                    ),
                )

            Constants.ARTIST_ORDER_BY_RANDOM -> Collections.shuffle(artists)
        }

        notifyDataSetChanged()
    }
}
