package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.OnLongClickListener
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemHomeInternetRadioStationBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.InternetRadioStation
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class InternetRadioStationAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<InternetRadioStationAdapter.ViewHolder?>() {
    private var internetRadioStations: MutableList<InternetRadioStation>

    init {
        this.internetRadioStations = mutableListOf<InternetRadioStation?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemHomeInternetRadioStationBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return InternetRadioStationAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val internetRadioStation = internetRadioStations.get(position)

        holder.item.internetRadioStationTitleTextView.text = internetRadioStation.name
        holder.item.internetRadioStationSubtitleTextView.text = internetRadioStation.streamUrl

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                internetRadioStation.streamUrl,
                CustomGlideRequest.ResourceType.Radio,
            ).build()
            .into(holder.item.internetRadioStationCoverImageView)
    }

    override fun getItemCount(): Int = internetRadioStations.size

    fun setItems(internetRadioStations: MutableList<InternetRadioStation>) {
        this.internetRadioStations = internetRadioStations
        notifyDataSetChanged()
    }

    fun getItem(position: Int): InternetRadioStation? = internetRadioStations.get(position)

    inner class ViewHolder internal constructor(
        var item: ItemHomeInternetRadioStationBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.internetRadioStationTitleTextView.setSelected(true)
            item.internetRadioStationSubtitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
            itemView.setOnLongClickListener(OnLongClickListener { v: View? -> onLongClick() })

            item.internetRadioStationMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onLongClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.INTERNET_RADIO_STATION_OBJECT,
                internetRadioStations.get(getBindingAdapterPosition()),
            )

            click.onInternetRadioStationClick(bundle)
        }

        private fun onLongClick(): Boolean {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.INTERNET_RADIO_STATION_OBJECT,
                internetRadioStations.get(getBindingAdapterPosition()),
            )

            click.onInternetRadioStationLongClick(bundle)

            return true
        }
    }
}
