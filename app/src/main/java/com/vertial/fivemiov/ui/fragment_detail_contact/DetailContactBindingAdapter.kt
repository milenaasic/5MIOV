package com.vertial.fivemiov.ui.fragment_detail_contact

import android.widget.TextView
import androidx.databinding.BindingAdapter
import com.vertial.fivemiov.model.PhoneItem

const val MOBILE_PHONE_TYPE="Mobile"
const val HOME_PHONE_TYPE="Home"
const val WORK_PHONE_TYPE="Work"

@BindingAdapter("setPhoneType")
fun setPhoneType(view: TextView, item: PhoneItem){

    when (item.phoneType){
        1->view.text= HOME_PHONE_TYPE
        2->view.text= MOBILE_PHONE_TYPE
        3->view.text= WORK_PHONE_TYPE
        else->view.text=""
    }

}