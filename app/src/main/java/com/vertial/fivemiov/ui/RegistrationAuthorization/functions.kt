package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.telephony.PhoneNumberUtils
import android.util.Log

private val MY_TAG="functions"

fun String.isPhoneNumberValid():Boolean{
    return PhoneNumberUtils.isGlobalPhoneNumber(this)
}

fun String.isEmailValid():Boolean{
    if (this.contains("@")) return true
    else return false
}

fun String.isPasswordValid():Boolean{
    //if(this.isNullOrBlank()) return false
    if (this.length>5) return true
    else return false
}

 fun String.removePlus():String{
    Log.i(MY_TAG,"pre obrade ${this.trim().startsWith("+",false)}")
    if(this.trim().startsWith("+",false)){
        Log.i(MY_TAG,"posle obrade ${this.removePrefix("+")}")
        return this.removePrefix("+")
    }else return this

}