package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.databinding.ItemHorizontalShareBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.Share
import com.cappielloantonio.tempo.util.Constants
import com.cappielloantonio.tempo.util.UIUtil
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

class ShareHorizontalAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<ShareHorizontalAdapter.ViewHolder?>() {
    private var shares: MutableList<Share>

    init {
        this.shares = mutableListOf<Share?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHorizontalShareBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return ShareHorizontalAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val share = shares.get(position)

        holder.item.shareTitleTextView.text = share.description
        holder.item.shareSubtitleTextView.text =
            holder.itemView.context
                .getString(R.string.share_subtitle_item, UIUtil.getReadableDate(share.expires))

        if (share.entries != null && !share.entries!!.isEmpty()) {
            CustomGlideRequest.Builder.Companion
                .from(
                    holder.itemView.context,
                    share.entries!!.get(0).coverArtId,
                    CustomGlideRequest.ResourceType.Album,
                ).build()
                .into(holder.item.shareCoverImageView)
        }
    }

    override fun getItemCount(): Int = shares.size

    fun setItems(shares: MutableList<Share>) {
        this.shares = shares
        notifyDataSetChanged()
    }

    fun getItem(id: Int): Share? = shares.get(id)

    inner class ViewHolder internal constructor(
        var item: ItemHorizontalShareBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.shareTitleTextView.setSelected(true)
            item.shareSubtitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.shareButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        private fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(Constants.SHARE_OBJECT, shares.get(getBindingAdapterPosition()))

            click.onShareClick(bundle)
        }

        private fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(Constants.SHARE_OBJECT, shares.get(getBindingAdapterPosition()))

            click.onShareLongClick(bundle)

            return true
        }
    }
}
