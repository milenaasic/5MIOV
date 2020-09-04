package com.vertial.fivemiov.utils

import android.app.Application
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.sip.SipManager
import android.os.Build
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.widget.EditText
import com.vertial.fivemiov.model.PhoneItem
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

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



private val MY_TAG="functions"

fun String.isPhoneNumberValid():Boolean{
    val normalizedNumber=PhoneNumberUtils.normalizeNumber(this)
    Log.i(MY_TAG," number:$this, normalized :$normalizedNumber")
    if(normalizedNumber!=null) return (PhoneNumberUtils.isGlobalPhoneNumber(normalizedNumber ) && this.length> PHONE_NUMBER_MIN_LENGHT)
    else return false

}

fun String.isInternationalPhoneNumber():Boolean{

    return true
}

fun String.isPasswordValid():Boolean{

    val passPattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}:;<>,?/~_+=|'\"-]).{8,32}$"
    val passwordMatcher = Regex(passPattern)
    return passwordMatcher.find(this) !=null

}

fun CharSequence?.isEmailValid() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.removeDoubleZeroAtBegining():String{
    if(!this.trim().startsWith(DOUBLE_ZERO)) return this
    return this.trim().removePrefix(DOUBLE_ZERO)
}

fun String.removeFirstZeroAddPrefix():String{

    if(!this.trim().startsWith(ONE_ZERO)) return this
    else return "$NIGERIAN_PREFIX${this.trim().removePrefix(ONE_ZERO)}"
}


fun String.removePlus():String{
    if(this.trim().startsWith("+",false)){
        return this.removePrefix("+")
    }else return this
}


fun isVOIPsupported(context:Context):Boolean{
    //return (SipManager.isVoipSupported(context)&& SipManager.isApiSupported(context))
    return true
}

fun did24HoursPass(currentTime:Long, databaseE1Timestamp:Long):Boolean{
    if(currentTime.minus(databaseE1Timestamp)> HOURS_24_IN_MILLIS)  return true
    else return false

}


fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
        }

        override fun afterTextChanged(editable: Editable?) {
            afterTextChanged.invoke(editable.toString())
        }
    })
}

//Contact List

fun formatDateFromMillis(timeInMillis:Long):String{
    val df = DateFormat.getDateInstance(DateFormat.MEDIUM)
    val calendar = Calendar.getInstance().apply {
        this.timeInMillis=timeInMillis
     }

    return df.format(calendar.time)

}

fun formatTimeFromMillis(mytimeInMillis: Long): String {
    val df = DateFormat.getTimeInstance(DateFormat.SHORT)
    val calendar = Calendar.getInstance().apply {
        this.timeInMillis=mytimeInMillis
    }
    return df.format(calendar.time)
}


