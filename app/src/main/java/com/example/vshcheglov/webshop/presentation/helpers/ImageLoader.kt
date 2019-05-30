package com.example.vshcheglov.webshop.presentation.helpers

import android.graphics.Bitmap
import android.widget.ImageView
import androidx.annotation.DrawableRes
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.vshcheglov.webshop.R

object ImageLoaderManager {

    fun loadImage(imageView: ImageView, url: String?, @DrawableRes placeholderId: Int = R.drawable.no_image) {
        Glide.with(imageView)
            .load(url)
            .error(placeholderId)
            .into(imageView)
    }

    fun loadCircularImage(imageView: ImageView, @DrawableRes placeholderId: Int) {
        Glide.with(imageView).load(placeholderId)
            .apply(RequestOptions.circleCropTransform())
            .into(imageView)
    }

    fun loadCircularImage(
        imageView: ImageView, bitmap: Bitmap,
        @DrawableRes placeholderId: Int = R.drawable.profile_avatar_placeholder_large
    ) {
        Glide.with(imageView).load(bitmap)
            .apply(RequestOptions.circleCropTransform())
            .error(placeholderId)
            .into(imageView)
    }
}