package app.adinfinitum.ello.ui.fragment_recent_calls

import android.annotation.SuppressLint
import android.text.format.DateUtils
import android.widget.TextView
import androidx.databinding.BindingAdapter
import app.adinfinitum.ello.model.RecentCall
import app.adinfinitum.ello.utils.formatDateFromMillis
import app.adinfinitum.ello.utils.formatTimeFromMillis

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