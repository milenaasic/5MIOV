package app.adinfinitum.ello.ui.registrationauthorization.listeners

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.util.Log
import app.adinfinitum.ello.ui.myapplication.MyApplication
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.common.api.CommonStatusCodes
import com.google.android.gms.common.api.Status
import com.google.android.gms.tasks.Task

private val MY_TAG="MySMSListener()"

interface MySMSListenerResult{
    fun onSMSReceived(code:String)
}

class MySMSListener(val context: Context, val mySMSListenerResult: MySMSListenerResult) {

    private var smsBroadcastReceiver: SMSAuthorizationBroadcastReceiver?=null
    private var timeLatestSMSRetreiverStarted: Long = 0L
    private val TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC = 120
    private val filter = IntentFilter().apply {
        addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
    }
    private var isReceiverRegistered=false

    //register SMS Broadcast Receiver
   init {
        if (smsBroadcastReceiver == null) {
            smsBroadcastReceiver = SMSAuthorizationBroadcastReceiver()
        }
    }

    fun startSMSListening() {
        registerReceiver()
        val currentTime=System.currentTimeMillis()
        Log.i(
            MY_TAG,
            "startSMSRetreiverFunction this $currentTime, latest $timeLatestSMSRetreiverStarted,${TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC * 1000}"
        )
        //start sms retreiver only if it hasn't been already started before TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC * 1000
        if ((currentTime - timeLatestSMSRetreiverStarted) > TIME_BETWEEN_TWO_SMS_RETREIVERS_IN_SEC * 1000) {
            Log.i(
                MY_TAG,
                "startSMSRetreiverFunction  ${timeLatestSMSRetreiverStarted - currentTime}"
            )
            startMySMSRetreiver()
            timeLatestSMSRetreiverStarted = currentTime
        }
    }

    private fun registerReceiver(){
        if(!isReceiverRegistered){
            context.registerReceiver(smsBroadcastReceiver, filter)
            isReceiverRegistered=true
        }
    }

    private fun startMySMSRetreiver() {

        Log.i(MY_TAG, "  entered function start MySMSReceiver")

        val client = SmsRetriever.getClient(context)

        val task: Task<Void> = client.startSmsRetriever()
        Log.i(MY_TAG, "  client $client, $task, ${client.apiOptions},${client.instanceId}")

        task.addOnSuccessListener {
            Log.i(MY_TAG, "  Successfully started retriever, expect broadcast intent")
            // Successfully started retriever, expect broadcast intent
        }

        task.addOnFailureListener {
            Log.i(MY_TAG, "  SMS  retriever failure, ${it.message}")
            // Failed to start retriever, inspect Exception for more details
        }

    }


    fun stopSMSListening() {
        smsBroadcastReceiver?.let {
            context.unregisterReceiver(it)
        }
    }


    inner class SMSAuthorizationBroadcastReceiver : BroadcastReceiver() {

        override fun onReceive(context: Context?, intent: Intent) {

            if (SmsRetriever.SMS_RETRIEVED_ACTION == intent.action) {

                val extras = intent.extras
                val status: Status? = extras!![SmsRetriever.EXTRA_STATUS] as Status?

                when (status?.getStatusCode()) {

                    CommonStatusCodes.SUCCESS ->  {
                        val  message : String?= extras[SmsRetriever.EXTRA_SMS_MESSAGE] as String?
                        if(!message.isNullOrEmpty()){
                            val code=extractVerificationCode(message)
                            mySMSListenerResult.onSMSReceived(code)
                        }
                        Log.i(MY_TAG," on receive success $message")
                    }

                    CommonStatusCodes.TIMEOUT -> {
                        Log.i(MY_TAG," on receive is TIMEOUT")
                        mySMSListenerResult.onSMSReceived("TIMEOUT")

                    }
                }
            }
        }

        private fun extractVerificationCode( fulSMS: String): String {

            val s1=fulSMS.split(":")
            val verCode=s1[1].trim().substring(startIndex = 0,endIndex = 6)

            return verCode
        }
    }


}