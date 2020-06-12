package com.vertial.fivemiov.ui.registrationauthorization

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status

private val MYTAG="MY_SMSAuthBroadcastRece"
class SMSAuthorizationBroadcastReceiver : BroadcastReceiver() {

    //received SMS Message
    private val _receivedSMSMessage= MutableLiveData<String?>()
    val receivedSMSMessage: LiveData<String?>
        get() = _receivedSMSMessage

    fun resetReceivedSMSMessage(){
        _receivedSMSMessage.value=null
    }

    companion object{
        const val timeout="TIMEOUT"
    }

    override fun onReceive(context: Context?, intent: Intent) {

        if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {

            val extras = intent.extras
            val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?

            when (status?.getStatusCode()) {

                CommonStatusCodes.SUCCESS ->  {
                        var  message : String?= extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                        if(!message.isNullOrEmpty()) _receivedSMSMessage.value=message
                    Log.i(MYTAG," on receive success $message")
                }

                CommonStatusCodes.TIMEOUT -> {
                    Log.i(MYTAG," on receive is TIMEOUT")
                    _receivedSMSMessage.value= timeout
                }
            }
        }
    }
}