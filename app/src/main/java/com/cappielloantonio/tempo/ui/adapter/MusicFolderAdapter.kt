package com.cappielloantonio.tempo.ui.adapter

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.media3.common.util.UnstableApi
import androidx.recyclerview.widget.RecyclerView
import androidx.room.RoomDatabase.Builder.build
import com.cappielloantonio.tempo.databinding.ItemLibraryMusicFolderBinding
import com.cappielloantonio.tempo.glide.CustomGlideRequest
import com.cappielloantonio.tempo.interfaces.ClickCallback
import com.cappielloantonio.tempo.subsonic.models.MusicFolder
import com.cappielloantonio.tempo.util.Constants
import okhttp3.Request.Builder.build
import okhttp3.Response.Builder.build

@UnstableApi
class MusicFolderAdapter(
    private val click: ClickCallback,
) : RecyclerView.Adapter<MusicFolderAdapter.ViewHolder?>() {
    private var musicFolders: MutableList<MusicFolder>

    init {
        this.musicFolders = mutableListOf<MusicFolder?>()
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int,
    ): ViewHolder {
        val view =
            ItemLibraryMusicFolderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false,
            )
        return MusicFolderAdapter.ViewHolder(view)
    }

    override fun onBindViewHolder(
        holder: ViewHolder,
        position: Int,
    ) {
        val musicFolder = musicFolders.get(position)

        holder.item.musicFolderTitleTextView.text = musicFolder.name

        CustomGlideRequest.Builder.Companion
            .from(
                holder.itemView.context,
                musicFolder.name,
                CustomGlideRequest.ResourceType.Folder,
            ).build()
            .into(holder.item.musicFolderCoverImageView)
    }

    override fun getItemCount(): Int = musicFolders.size

    fun setItems(musicFolders: MutableList<MusicFolder>) {
        this.musicFolders = musicFolders
        notifyDataSetChanged()
    }

    fun getItem(position: Int): MusicFolder? = musicFolders.get(position)

    inner class ViewHolder internal constructor(
        var item: ItemLibraryMusicFolderBinding,
    ) : RecyclerView.ViewHolder(
            item.getRoot(),
        ) {
        init {
            item.musicFolderTitleTextView.setSelected(true)

            itemView.setOnClickListener(View.OnClickListener { v: View? -> onClick() })

            item.musicFolderMoreButton.setOnClickListener(View.OnClickListener { v: View? -> onClick() })
        }

        fun onClick() {
            val bundle = Bundle()
            bundle.putParcelable(
                Constants.MUSIC_FOLDER_OBJECT,
                musicFolders.get(getBindingAdapterPosition()),
            )
            click.onMusicFolderClick(bundle)
        }
    }
}
