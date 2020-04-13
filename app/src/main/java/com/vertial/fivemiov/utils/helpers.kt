package com.vertial.fivemiov.utils

import android.app.Activity
import android.app.Application
import android.content.Context
import android.net.ConnectivityManager
import android.net.sip.SipManager
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.util.Patterns

private val MYTAG="MY_helpers"



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

fun String.removeDoubleZeroAtBegining():String{
    if(!this.trim().startsWith(DOUBLE_ZERO)) return this
    return this.trim().removePrefix(DOUBLE_ZERO)
}

fun String.removeFirstZeroAddPrefix(prefix:String):String{

    if(!this.trim().startsWith(ONE_ZERO)) return this
    return this.trim().removePrefix(ONE_ZERO).plus(NIGERIAN_PREFIX)
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


fun isVOIPsupported(context:Context):Boolean{
    Log.i(MYTAG," is voip supoported ${SipManager.isVoipSupported(context)}")
    Log.i(MYTAG," is sip api supoported ${SipManager.isApiSupported(context)}")
    return (SipManager.isVoipSupported(context)&& SipManager.isApiSupported(context))

}

fun did24HoursPass(currentTime:Long, databaseE1Timestamp:Long):Boolean{
    if(currentTime.minus(databaseE1Timestamp)> HOURS_24_IN_MILLIS)  return true
    else return false

}
