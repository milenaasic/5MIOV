package com.vertial.sipdnidphone.ui.fragment_main

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.text.Html
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import androidx.recyclerview.widget.DividerItemDecoration
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.vertial.sipdnidphone.R

@BindingAdapter("setThumbPhoto")
fun setThumbnailPhoto(view: ImageView, item: ContactItem?){

    Glide.with(view)
        .load(item?.photoThumbUri)
        .apply(RequestOptions().error(R.drawable.thumbnail_background).fallback(R.drawable.thumbnail_background))
        .apply(RequestOptions().circleCrop())
        .into(view)

}

@BindingAdapter("setFirstLetter")
fun setFirstLetter(view: TextView, item: ContactItem?){
    if(item?.photoThumbUri==null) view.text= item?.name?.first().toString()
    else view.text= ""
}

@BindingAdapter("setABCLetters")
fun setABCLetters(view: TextView, item: ContactItem?){
    view.text= item?.name?.first().toString()

}

@BindingAdapter("setLetterVisibility")
fun setLetterVisibility(view: TextView, t:MyViewHolderType){
    if(t.type==MainFragmentAdapter.POSITION_0_IN_LIST || t.type==MainFragmentAdapter.SHOW_FIRST_LETTER) {
        view.visibility= View.VISIBLE
    } else view.visibility=View.GONE

}

@BindingAdapter("setDividerVisibility")
fun setDividerVisibility(view: View, t:MyViewHolderType){
    if(t.type==MainFragmentAdapter.SHOW_FIRST_LETTER)view.visibility= View.VISIBLE
    else view.visibility=View.GONE

}