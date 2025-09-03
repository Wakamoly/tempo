package com.cappielloantonio.tempo.glide

import android.content.Context
import com.bumptech.glide.GlideBuilder
import com.bumptech.glide.annotation.GlideModule
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.cache.InternalCacheDiskCacheFactory
import com.bumptech.glide.module.AppGlideModule
import com.bumptech.glide.request.RequestOptions
import com.cappielloantonio.tempo.util.Preferences.getImageCacheSize

@GlideModule
class CustomGlideModule : AppGlideModule() {
    override fun applyOptions(context: Context, builder: GlideBuilder) {
        val diskCacheSize = getImageCacheSize() * 1024 * 1024
        builder.setDiskCache(
            InternalCacheDiskCacheFactory(
                context,
                "cache",
                diskCacheSize.toLong()
            )
        )
        builder.setDefaultRequestOptions(RequestOptions().format(DecodeFormat.PREFER_RGB_565))
    }
}
