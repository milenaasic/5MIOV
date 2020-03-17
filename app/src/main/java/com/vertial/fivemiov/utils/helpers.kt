package com.vertial.fivemiov.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.util.Patterns


fun isOnline(application: Application):Boolean{

    var online=false
    val connMgr= application.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        if(connMgr.activeNetwork!=null) online=true
        else online=false
    }

    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
        if (connMgr.activeNetworkInfo != null) {
            online = connMgr.activeNetworkInfo.isConnected
        } else online = false
    }

    return online
}

fun String.isValidPhoneNumber():Boolean{

    return PhoneNumberUtils.isGlobalPhoneNumber(this)

}

private val MY_TAG="functions"

fun String.isPhoneNumberValid():Boolean{
    return PhoneNumberUtils.isGlobalPhoneNumber(this)
}


fun CharSequence?.isEmailValid() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.isPasswordValid():Boolean{
    val passwordPattern = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&+-=])(?=\\S+$).{8,32}$"
    val passwordMatcher = Regex(passwordPattern)
    return passwordMatcher.find(this) !=null

}

fun String.removePlus():String{
    if(this.trim().startsWith("+",false)){
        return this.removePrefix("+")
    }else return this
}


