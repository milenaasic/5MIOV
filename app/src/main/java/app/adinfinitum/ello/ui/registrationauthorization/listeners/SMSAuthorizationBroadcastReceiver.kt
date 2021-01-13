package app.adinfinitum.ello.ui.registrationauthorization.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

private val MYTAG="MY_SMSAuthBroadcastRece"

/*interface SMSResultListener{
    fun onSMSReceived(result:String)
}*/

// class SMSAuthorizationBroadcastReceiver (val listener: SMSResultListener) : BroadcastReceiver() {
//
//
// override fun onReceive(context: Context?, intent: Intent) {
//
// if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {
//
// val extras = intent.extras
// val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?
//
// when (status?.getStatusCode()) {
//
// CommonStatusCodes.SUCCESS ->  {
// var  message : String?= extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
// if(!message.isNullOrEmpty()){
// val code=extractVerificationCode(message)
// listener.onSMSReceived(code)
// }
// Log.i(MYTAG," on receive success $message")
// }
//
// CommonStatusCodes.TIMEOUT -> {
// Log.i(MYTAG," on receive is TIMEOUT")
// listener.onSMSReceived("TIMEOUT")
//
// }
// }
// }
// }
//
// private fun extractVerificationCode( fulSMS: String): String {
//
// val s1=fulSMS.split(":")
// val verCode=s1[1].trim().substring(startIndex = 0,endIndex = 6)
//
// return verCode
// }
// }