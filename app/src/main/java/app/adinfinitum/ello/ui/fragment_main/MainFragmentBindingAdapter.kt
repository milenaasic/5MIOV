package app.adinfinitum.ello.ui.fragment_main

import android.graphics.Color
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import app.adinfinitum.ello.R
import app.adinfinitum.ello.model.ContactItem
import app.adinfinitum.ello.utils.EMPTY_NAME

private val MYTAG="MY_MainFragBindAdapter"

@BindingAdapter("setNameWithColoredLetters","textToColor","mycolor")
fun setNameWithColoredLetters(view:TextView, item: ContactItem, textToColor:String?, color:String){

    if(item.name== EMPTY_NAME) view.text=""
    else {
        if (textToColor == null) view.text = item.name
        else {
            view.text = item.name.collorLetters(textToColor, Color.parseColor(color)) ?: item.name
        }
    }
}


@BindingAdapter("setThumbPhoto")
fun setThumbnailPhoto(view: ImageView, item: ContactItem?){
    if(item?.name== EMPTY_NAME){
        try{
            Glide.with(view)
            .load(R.drawable.thumbnail_background_white)
            .apply(RequestOptions().error(R.drawable.thumbnail_background_white).fallback(R.drawable.thumbnail_background_white))
            .apply(RequestOptions().circleCrop())
            .into(view)
        }catch (t:Throwable){
            Log.i(MYTAG, "setThumbPhoto error ${t.message}")
        }
    }else {
        try {
            Glide.with(view)
                .load(item?.photoThumbUri)
                .apply(RequestOptions().error(R.drawable.thumbnail_background).fallback(R.drawable.thumbnail_background))
                .apply(RequestOptions().circleCrop())
                .into(view)
        } catch (t: Throwable) {
            Log.i(MYTAG, "setThumbPhoto error ${t.message}")
        }
    }
}

@BindingAdapter("setFirstLetter")
fun setFirstLetter(view: TextView, item: ContactItem?){
    if((item?.name==EMPTY_NAME))view.text= ""
    else   {if(item?.photoThumbUri==null ) view.text= item?.name?.first().toString()
            else view.text= ""}
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