package com.vertial.sipdnidphone.ui.fragment_main

import android.os.Build
import android.text.Html
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vertial.sipdnidphone.R

@BindingAdapter("setThumbPhoto")
fun setThumbnailPhoto(view: ImageView, item: ContactItem?){
    Glide.with(view)
        .load(item?.photoThumbUri)
        .apply(RequestOptions().circleCrop())
        .into(view)

}