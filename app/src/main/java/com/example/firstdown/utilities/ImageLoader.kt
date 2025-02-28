package com.example.firstdown.utilities

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.example.firstdown.R

object ImageLoader {
    fun loadImage(url: String?, imageView: ImageView) {
        if (url.isNullOrEmpty()) {
            imageView.setImageResource(R.drawable.default_avatar)
            return
        }

        Glide.with(imageView.context)
            .load(url)
            .placeholder(R.drawable.default_avatar)
            .error(R.drawable.default_avatar)
            .centerCrop()
            .into(imageView)
    }
}