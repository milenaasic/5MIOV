package com.vertial.fivemiov.ui.fragment_recent_calls

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.vertial.fivemiov.model.ContactItem
import com.vertial.fivemiov.model.RecentCall
import com.vertial.fivemiov.utils.EMPTY_NAME
import com.vertial.fivemiov.utils.formatDateFromMillis
import com.vertial.fivemiov.utils.formatTimeFromMillis

@SuppressLint("SetTextI18n")
@BindingAdapter("setRecentCallDate")
fun setRecentCallDate(view: TextView, item: RecentCall?){

    if(item!=null) {
        if(DateUtils.isToday(item.recentCallTime)) view.text= "Today, ${formatTimeFromMillis(item.recentCallTime)}"
        else view.text= "${formatDateFromMillis(item.recentCallTime)}, ${formatTimeFromMillis(item.recentCallTime)}"


    }
}

@BindingAdapter("setPhoneNumber")
fun setPhoneNumber(view: TextView, item: RecentCall?){
    if(item!=null) {
        if(item.recentCallName==item.recentCallPhone) view.text=" "
        else view.text=item.recentCallPhone

    }

}