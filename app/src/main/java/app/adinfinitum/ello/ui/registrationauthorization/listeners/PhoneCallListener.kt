package app.adinfinitum.ello.ui.registrationauthorization.listeners

import android.content.Context
import android.os.CountDownTimer
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import app.adinfinitum.ello.ui.myapplication.MyApplication

private val MY_TAG="MyPhoneCallListener"

interface MyPhoneCallListenerResult{
    fun onPhoneCallReceiver(incomingNumber:String)
    fun onTimerFinished()
    //fun onFailure()
}

class MyPhoneCallListener(val context: Context, val timeToWaitForCall:Long, val myPhoneCallListenerResult:MyPhoneCallListenerResult) {

    private var telephonyManager: TelephonyManager? = null
    private var callStateListener: PhoneStateListener? = null
    var isListeningToPhoneCall:Boolean=false
        private set
    private val timer=MyTimer()

    init {
        if (telephonyManager == null) telephonyManager =
            context.getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
        if (callStateListener == null) {
            callStateListener = object : PhoneStateListener() {
                override fun onCallStateChanged(state: Int, incomingNumber: String) {

                    //  React to incoming call.
                    Log.i(MY_TAG, "state $state,  $incomingNumber")
                    // If phone ringing
                    if (state == TelephonyManager.CALL_STATE_RINGING) {
                        myPhoneCallListenerResult.onPhoneCallReceiver(incomingNumber)

                    }
                }
            }
        }
    }



    fun startListening() {
        callStateListener?.let {
            if(timer.isTicking) timer.cancelTimer()
            timer.startTimer()
            telephonyManager?.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)
            isListeningToPhoneCall=true
            Log.i(MY_TAG, "phone call startListening()")
        }
    }

    fun stopListening() {
        callStateListener?.let {
            telephonyManager?.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
            isListeningToPhoneCall=false
            timer.cancelTimer()
            Log.i(MY_TAG, "phone call stopListening()")
        }
    }


    inner class MyTimer {
        var isTicking=false
        val myTimer = object : CountDownTimer(timeToWaitForCall, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                Log.i(MY_TAG, "my timer:$millisUntilFinished")
            }
            override fun onFinish() {
                myPhoneCallListenerResult.onTimerFinished()
            }
        }

        fun startTimer(){
            myTimer.start()
            isTicking=true
            Log.i(MY_TAG, "startTimer()")
        }
        fun cancelTimer(){
            myTimer.cancel()
            isTicking=false
            Log.i(MY_TAG, "cancelTimer()")
        }
    }



}