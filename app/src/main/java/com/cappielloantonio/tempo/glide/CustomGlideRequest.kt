package com.cappielloantonio.tempo.glide

import android.content.Context
import android.graphics.drawable.Drawable
import android.util.Log
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.graphics.drawable.toDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestBuilder
import com.bumptech.glide.RequestManager
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.signature.ObjectKey
import com.cappielloantonio.tempo.App.Companion.getSubsonicClientInstance
import com.cappielloantonio.tempo.R
import com.cappielloantonio.tempo.util.Preferences.getImageSize
import com.cappielloantonio.tempo.util.Preferences.getRoundedCornerSize
import com.cappielloantonio.tempo.util.Preferences.isCornerRoundingEnabled
import com.cappielloantonio.tempo.util.Preferences.isDataSavingMode
import com.cappielloantonio.tempo.util.Util
import com.google.android.material.elevation.SurfaceColors

object CustomGlideRequest {
    private const val TAG = "CustomGlideRequest"

    val CORNER_RADIUS: Int = if (isCornerRoundingEnabled()) getRoundedCornerSize() else 1

    val DEFAULT_DISK_CACHE_STRATEGY: DiskCacheStrategy = DiskCacheStrategy.ALL

    fun createRequestOptions(context: Context, item: String?, type: ResourceType): RequestOptions {
        return RequestOptions()
            .placeholder(SurfaceColors.SURFACE_5.getColor(context).toDrawable())
            .fallback(getPlaceholder(context, type))
            .error(getPlaceholder(context, type))
            .diskCacheStrategy(DEFAULT_DISK_CACHE_STRATEGY)
            .signature(ObjectKey(item ?: 0))
            .transform(CenterCrop(), RoundedCorners(CORNER_RADIUS))
    }

    private fun getPlaceholder(context: Context, type: ResourceType): Drawable? =
        when (type) {
            ResourceType.Album -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_album
            )

            ResourceType.Artist -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_artist
            )

            ResourceType.Folder -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_folder
            )

            ResourceType.Directory -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_directory
            )

            ResourceType.Playlist -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_playlist
            )

            ResourceType.Podcast -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_podcast
            )

            ResourceType.Radio -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_radio
            )

            ResourceType.Song -> AppCompatResources.getDrawable(
                context,
                R.drawable.ic_placeholder_song
            )

            ResourceType.Unknown -> SurfaceColors.SURFACE_5.getColor(context).toDrawable()
        }

    fun createUrl(item: String?, size: Int): String {
        val params = getSubsonicClientInstance(false).params

        val uri = StringBuilder()

        uri.append(getSubsonicClientInstance(false).url)
        uri.append("getCoverArt")

        if (params.containsKey("u") && params["u"] != null) {
            uri.append("?u=")
                .append(Util.encode(params["u"]))
        }
        if (params.containsKey("p") && params["p"] != null) {
            uri.append("&p=")
                .append(params["p"])
        }
        if (params.containsKey("s") && params["s"] != null) {
            uri.append("&s=")
                .append(params["s"])
        }
        if (params.containsKey("t") && params["t"] != null) {
            uri.append("&t=")
                .append(params["t"])
        }
        if (params.containsKey("v") && params["v"] != null) {
            uri.append("&v=")
                .append(params["v"])
        }
        if (params.containsKey("c") && params["c"] != null) {
            uri.append("&c=")
                .append(params["c"])
        }
        if (size != -1) uri.append("&size=").append(size)

        uri.append("&id=").append(item)

        Log.d(TAG, "createUrl() $uri")

        return uri.toString()
    }

    enum class ResourceType {
        Unknown,
        Album,
        Artist,
        Folder,
        Directory,
        Playlist,
        Podcast,
        Radio,
        Song,
    }

    class Builder private constructor(context: Context, item: String?, type: ResourceType) {
        private val requestManager: RequestManager = Glide.with(context)
        private var item: Any? = null

        init {

            if (item != null && !isDataSavingMode()) {
                this.item = createUrl(item, getImageSize())
            }

            requestManager.applyDefaultRequestOptions(createRequestOptions(context, item, type))
        }

        fun build(): RequestBuilder<Drawable?> {
            return requestManager
                .load(item)
                .transition(DrawableTransitionOptions.withCrossFade())
        }

        companion object {
            fun from(context: Context, item: String?, type: ResourceType): Builder {
                return Builder(context, item, type)
            }
        }
    }
}
