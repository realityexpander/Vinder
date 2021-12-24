package com.realityexpander.vinder.utils

import android.content.Context
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.databinding.BindingAdapter
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.request.RequestOptions
import com.realityexpander.vinder.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.contract

fun ImageView.loadUrl(url: String?, errorDrawable: Int = R.drawable.empty) {
    if (url.isNullOrEmpty()) return

    context?.let {
        val options = RequestOptions()
            .diskCacheStrategy(DiskCacheStrategy.ALL)
            .placeholder(progressDrawable(context))
            .fallback(progressDrawable(context))
            .error(errorDrawable)
            .override(1000, 750)
            .fitCenter()

        CoroutineScope(Dispatchers.Main).launch {
            Glide.with(context.applicationContext)
                .load(url)
                .thumbnail(0.5f)
                .apply(options)
                .into(this@loadUrl)
        }
    }
}

fun progressDrawable(context: Context): CircularProgressDrawable {
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        setColorSchemeColors(ContextCompat.getColor(context, R.color.colorAccent))
        start()
    }
}


// Allows android:imageUrl to load URL images
@BindingAdapter("android:imageUrl")
fun loadImage(view: ImageView, url: String?) {
    view.loadUrl(url)
}

// opposite of isNullOrEmpty()
fun CharSequence?.isNotNullAndNotEmpty(): Boolean {
    return this != null && this.isNotEmpty()
}