package com.vertial.fivemiov.utils

import android.app.Activity
import android.telephony.PhoneNumberUtils

fun String.isValidPhoneNumber():Boolean{

    return PhoneNumberUtils.isGlobalPhoneNumber(this)

}