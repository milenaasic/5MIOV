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

fun String.isValidPhoneNumber():Boolean{

    return PhoneNumberUtils.isGlobalPhoneNumber(this)

}



private val MY_TAG="functions"

fun String.isPhoneNumberValid():Boolean{

    return (PhoneNumberUtils.isGlobalPhoneNumber(this) && this.length> PHONE_NUMBER_MIN_LENGHT)
    //return true
}

fun String.isPasswordValid():Boolean{

   // val passPattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}:;<>,?/~_+-=|]).{8,32}$"

    val passPattern="^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[*.!@$%^&(){}:;<>,?/~_+=|'\"-]).{8,32}$"
    val passwordMatcher = Regex(passPattern)
    return passwordMatcher.find(this) !=null

}

fun CharSequence?.isEmailValid() = !isNullOrEmpty() && Patterns.EMAIL_ADDRESS.matcher(this).matches()

fun String.removeDoubleZeroAtBegining():String{
    if(!this.trim().startsWith(DOUBLE_ZERO)) return this
    return this.trim().removePrefix(DOUBLE_ZERO)
}

fun String.removeFirstZeroAddPrefix(prefix:String):String{

    if(!this.trim().startsWith(ONE_ZERO)) return this
    else return "$NIGERIAN_PREFIX${this.trim().removePrefix(ONE_ZERO)}"
}

fun convertPhoneListToPhoneArray(phoneList: List<PhoneItem>): Array<String> {
    val resultList= mutableListOf<String>()

    for(item in phoneList){
        //noramlizuj broj, ukloni +, ukoloni dve 00 (ako ima), jednu nulu zameni sa 234
        if(item!=null){
            if(!item.phoneNumber.isNullOrEmpty() && !item.phoneNumber.isNullOrBlank()){
                val myPhoneNumber=PhoneNumberUtils.normalizeNumber(item.phoneNumber)
                resultList.add(myPhoneNumber.removePlus().removeDoubleZeroAtBegining().removeFirstZeroAddPrefix(NIGERIAN_PREFIX))
            }

        }

    }
    return resultList.toTypedArray()

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

 fun PackageInfo.getMobAppVersion():String{

    var myversionName=""
    var versionCode=-1L

    try {
        //val packageInfo: PackageInfo = packageManager.getPackageInfo(requireActivity().packageName, 0);
        myversionName = this.versionName
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            versionCode=this.longVersionCode
        }else{
            versionCode= this.versionCode.toLong()

        }
    } catch ( e:Throwable) {
        e.printStackTrace();
    }

    return myversionName
}

