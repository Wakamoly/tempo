package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHomeGridTrackBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.model.Chronology
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class GridTrackAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<GridTrackAdapter.ViewHolder?>() {
    private var items: MutableList<Chronology>

    init {
        this.items = mutableListOf<Chronology?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHomeGridTrackBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return GridTrackAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val item = items.get(position)

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                item.coverArtId,
                CustomGlideRequest.ResourceType.Song,
            ).build()
            .into(holder.item.trackCoverImageView)
    }

    override fun getItemCount(): Int = items.size

    fun getItem(position: Int): Chronology? = items.get(position)

    fun setItems(items: MutableList<Chronology>) {
        this.items = items
        notifyDataSetChanged()
    }

    inner class ViewHolder internal constructor(
        var item: ItemHomeGridTrackBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelableArrayList(Constants.TRACKS_OBJECT, ArrayList<Chronology?>(items))
            bundle.putBoolean(Constants.MEDIA_CHRONOLOGY, true)
            bundle.putInt(Constants.ITEM_POSITION, getBindingAdapterPosition())

            click.onMediaClick(bundle)
        }
    }
}
