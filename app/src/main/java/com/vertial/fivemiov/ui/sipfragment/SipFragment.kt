package com.vertial.fivemiov.ui.sipfragment



import android.content.Context
import android.media.AudioManager
import android.media.AudioManager.STREAM_VOICE_CALL
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.PowerManager
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoSIPE1
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentSipBinding
import com.vertial.fivemiov.ui.initializeSharedPrefToFalse
import com.vertial.fivemiov.ui.myapplication.MyApplication
import org.linphone.core.*
import java.util.*


private val MYTAG="MY_Sip fragment"
class SipFragment : Fragment() {

    private lateinit var binding:FragmentSipBinding
    private lateinit var args:SipFragmentArgs
    private lateinit var viewModel: SipViewModel

    private lateinit var wl:PowerManager.WakeLock

    //Linphone
    private var mCore:Core?=null
    private var mListener: CoreListenerStub? = null
    private var mProxyConfig:ProxyConfig?=null
    private var mTimer: Timer=Timer("Linphone scheduler")
    private var mSipServer:String?=null

    private var mCall: Call? = null
    private var callAlreadyStartedAfterRegistration=false

    private var mAudioManager:AudioManager? = null
    private var mAudioFocused:Boolean=false
    private var mIsMicMuted = false
    private var mIsSpeakerEnabled = false

    private var navigationUpInProcess=false

    private val sHandler: Handler = Handler(Looper.getMainLooper())

    private val myLoggingServiceListener =
        LoggingServiceListener { logService, domain, lev, message ->
            when (lev) {
                LogLevel.Debug -> {Log.d(domain, message)
                    Log.d(MYTAG,"$domain, $message") }

                LogLevel.Message -> {Log.i(domain, message)
                    Log.i(MYTAG,"$domain, $message")   }
                LogLevel.Warning -> {Log.w(domain, message)
                    Log.w(MYTAG,"$domain, $message")  }
                LogLevel.Error -> {Log.e(domain, message)
                    Log.e(MYTAG,"$domain, $message")    }
                LogLevel.Fatal -> Log.wtf(domain, message)
                else -> Log.wtf(domain, message)

            }
        }

    val mIterateRunnable = Runnable {
        if (mCore != null) {
            mCore?.iterate()
            Log.i(MYTAG," u iterate")
        }
    }

    val lTask: TimerTask = object : TimerTask() {
        override fun run() {
            dispatchOnUIThread(mIterateRunnable)
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args= SipFragmentArgs.fromBundle(requireArguments())
        setHasOptionsMenu(true)
        Log.i(MYTAG, "ONLIFE onCreate")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.i(MYTAG, "ONLIFE onCreateView")
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_sip,container,false)
        binding.nametextView.text=args.contactName

        if(args.contactName==args.contactNumber) binding.sipnumbertextView.text=" "
        else binding.sipnumbertextView.text=args.contactNumber

        val database= MyDatabase.getInstance(requireActivity().application).myDatabaseDao
        val mApi= MyAPI.retrofitService

        val mySipRepo=RepoSIPE1(database,
                                mApi,
                                resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
            )


        viewModel = ViewModelProvider(this, SipViewModelFactory(mySipRepo,requireActivity().application))
            .get(SipViewModel::class.java)

        val pwm = requireActivity().getSystemService(Context.POWER_SERVICE) as PowerManager
        wl=pwm.newWakeLock(PowerManager.PROXIMITY_SCREEN_OFF_WAKE_LOCK,"com.vertial.fivemiov:SipCall")

            binding.sipendbutton.setOnClickListener {
                Log.i(MYTAG, "call end button clicked ")
                val currentcall=mCore?.currentCall
                if (currentcall != null) currentcall.terminate()
                else startNavigation()

            }

            binding.sipMicButton.setOnClickListener {
                toggleSipMicButton()
            }

            binding.speakerFAB.setOnClickListener {
               toggleSpeakerButton()
            }

            val activity=requireActivity()
            mAudioManager= (activity.getSystemService(Context.AUDIO_SERVICE)) as AudioManager

            if(mListener==null){
                initializeCoreListener()
            }

            if(mCore==null) viewModel.getSipAccountCredentials()

            return binding.root

    }

    private fun startNavigation() {
        if(!navigationUpInProcess) {
            navigationUpInProcess=true
            viewModel.navigateBack()
            Log.i(MYTAG," start navigate back function")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(MYTAG, "ONLIFE onViewCreated")

        viewModel.timeout.observe(viewLifecycleOwner, Observer {
            if(it){
                makeSipAudioCall()
                viewModel.timeoutFinished()
            }
        })

       /* viewModel.setMicMode.observe(viewLifecycleOwner, Observer {
            if(it){
                Log.i(MYTAG, "ONLIFE VIEW created,lokalna promenljiva $mIsMicMuted isMIcEnabled ${mCore?.micEnabled()}")
               // mCore?.enableMic(mIsMicMuted)

                viewModel.setMicroophoneModeFinished()
            }
        })*/


        viewModel.navigateUp.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigateUp()
                viewModel.navigateBackFinished()
            }
        })

        viewModel.getSipCredentialsNetSuccess.observe(viewLifecycleOwner, Observer{response->
            if(response!=null) {
                if (   response.sipUserName.isNotEmpty() && response.sipUserName.isNotBlank()
                        && response.sipPassword.isNotEmpty() && response.sipPassword.isNotBlank()
                        && response.sipServer.isNotEmpty() && response.sipServer.isNotBlank()
                ){
                    initializeCore(sipUserName = response.sipUserName,sipPassword = response.sipPassword,sipServer = response.sipServer,sipCallerId = response.sipCallerId)
                    viewModel.resetgetSipAccountCredentialsNetSuccess()
                }else {
                        showToast(resources.getString(R.string.something_went_wrong))
                        viewModel.navigateBack()
                        Log.i(MYTAG,"get sip credentials net success, but response is $response")
                }
            }
         })

         viewModel.getSipAccessCredentialsNetError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                viewModel.resetgetSipAccountCredentialsNetError()
                showToast(resources.getString(R.string.something_went_wrong))
                viewModel.navigateBack()
                Log.i(MYTAG,"getsip credentials error $it")
            }

          })

        viewModel.loggingOut.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                if(it) {
                    initializeSharedPrefToFalse(requireActivity().application)
                    viewModel.resetLoggingOutToFalse()
                }
            }
        })

    }



    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_item_share).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false
    }

    private fun initializeCore(sipUserName:String, sipPassword:String, sipServer:String,sipCallerId:String) {
        Log.i(MYTAG, "INITIALIZE CORE funkcija")
        viewModel.logCredentialsForSipCall(sipUsername =sipUserName,sipPassword = sipPassword,sipDisplayname =sipCallerId,sipServer = sipServer)
        mSipServer=sipServer
        mCore =
            Factory.instance()
                .createCore(
                    null,
                    null,
                    requireActivity().applicationContext)

        configureCore(sipUserName,sipPassword,sipCallerId)
        configureLogging()

        mProxyConfig= mCore?.createProxyConfig()
        configureProxy(sipUserName,sipPassword,sipServer)
        mCore?.addProxyConfig(mProxyConfig)
        mCore?.defaultProxyConfig=mProxyConfig

        /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = Timer("Linphone scheduler")
        mCore?.start()
        mTimer.schedule(lTask, 0, 20)

    }

    private fun makeSipAudioCall() {
        //start a call
        Log.i(MYTAG," proxy config list je ${mCore?.proxyConfigList?.size}")
        Log.i(MYTAG," username je ${mCore?.defaultProxyConfig?.identityAddress?.username}")
        Log.i(MYTAG,"stanje registracije ${mCore?.defaultProxyConfig?.state} ")
        Log.i(MYTAG," avpf mode iz Call ${mCore?.defaultProxyConfig?.avpfMode}, enabled ${mCore?.defaultProxyConfig?.avpfEnabled()} ")
        Log.i(MYTAG," media encryption iz Call je ${mCore?.mediaEncryption}")
        Log.i(MYTAG,"broj koji zovem je ${args.contactNumber}, server je $mSipServer")

        val numberToCall=PhoneNumberUtils.normalizeNumber(args.contactNumber)
        Log.i(MYTAG,"broj koji zovem normalizovan je ${numberToCall}")

        if(numberToCall!=null) {
            var callParams = mCore?.createCallParams(null)
            callParams?.enableEarlyMediaSending(true)
            Log.i(MYTAG," call params ${callParams?.usedAudioPayloadType},audio enabled ${callParams?.audioEnabled()}, early media enabled ${callParams?.earlyMediaSendingEnabled()} ")
            if(mSipServer!=null) mCall = mCore?.inviteWithParams("sip:$numberToCall@$mSipServer",callParams)


            /*mCall?.update(callParams)
            Log.i(MYTAG,"call params posle setovanja ${callParams?.usedAudioPayloadType},${callParams?.audioEnabled()}, early media enabled ${callParams?.earlyMediaSendingEnabled()} ")
            var callParams2 = mCore?.createCallParams(mCall)
            Log.i( MYTAG," call params2 posle setovanja ${callParams?.usedAudioPayloadType},${callParams?.audioEnabled()}, early media enabled ${callParams?.earlyMediaSendingEnabled()} ")*/
        }

    }


    fun dispatchOnUIThread(r: Runnable) {

        sHandler.post(r)
    }



    private fun updateCallStatus(status: String?) {
        binding.statustextView.text=status
    }

    override fun onStart() {
        super.onStart()
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(!wl.isHeld) wl.acquire()
        Log.i(MYTAG, "ONLIFE START")
        mCore?.enterForeground()
    }


    override fun onResume() {
        super.onResume()
        Log.i(MYTAG, "ONLIFE RESUME, mCore= $mCore")
        /*if(mCore!=null ){
                val call=mCore?.currentCall
            Log.i(MYTAG, "ONLIFE RESUME,currentCallState $currentCallState, call je $call}")
                if(call!=null) {
                    Log.i(MYTAG, "ONLIFE RESUME,currentCallState $currentCallState, call state ${call.state}")
                    if(call.state== Call.State.Paused) {
                        mCore?.addListener(mListener)
                        call.resume()
                        Factory.instance().loggingService.addListener(myLoggingServiceListener)
                    }

                }
        }*/


    }

    override fun onPause() {
        super.onPause()
        Log.i(MYTAG, "ONLIFE PAUSE,  mCore= $mCore")
       /* if(mCore!=null ){
            val call=mCore?.currentCall
            Log.i(MYTAG, "ONLIFE PAUSE, current call je $call, state je ${call?.state} ")
            if(call!=null) {
                if (call.state == Call.State.StreamsRunning) {
                    val succes =call.pause()
                    Log.i(MYTAG, "ONLIFE PAUSE, ${call.state}, ${succes}")
                    mCore?.removeListener(mListener)
                    Factory.instance().loggingService.removeListener(myLoggingServiceListener)
                }
            }
        }*/
    }

    override fun onStop() {
        super.onStop()
        Log.i(MYTAG, "ONLIFE STOP,")
        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(wl.isHeld) wl.release()
        mCore?.enterBackground()

    }



    private fun configureCore(username:String,password:String,callerID:String){
        val myAuthInfo=Factory.instance().createAuthInfo(username,username,password,null,null,null)

        mCore?.apply {
            addListener(mListener)
            enableIpv6(true)
            setAudioPortRange(10000,20000)
            mediaEncryption=MediaEncryption.None
            isMediaEncryptionMandatory=false
            avpfMode=AVPFMode.Enabled
            enableWifiOnly(false)
            addAuthInfo(myAuthInfo)
            enableLogCollection(LogCollectionState.Enabled)
            logCollectionUploadServerUrl="https://5miov.vertial.net/api/mobileLog"
            Log.i(MYTAG," media device is ${mCore?.mediaDevice}, noRTP Timeout ${mCore?.nortpTimeout}, sdp200AckEnabled ${mCore?.sdp200AckEnabled()}," +
                    "soud device list ${mCore?.soundDevicesList?.size}")


        }

        // set only GSM codec
        for(item in mCore?.audioPayloadTypes?.toList()!!){

            if(item.mimeType=="GSM") item.enable(true)
            else item.enable(false)
            Log.i(
                MYTAG,"audio payload types : ${item.mimeType},${item.enabled()}" +
                    " ${item.description},${item.isUsable}")
        }

    }

    private fun configureLogging(){
        Factory.instance().setDebugMode(true, "MY_LIN");
        Factory.instance().loggingService.addListener(myLoggingServiceListener)
    }

    private fun configureProxy(username: String,password: String,sipServer: String){

        var fromAdress=mCore?.createAddress("sip:$username@$sipServer")
        fromAdress?.password=password

        mProxyConfig?.apply {
            serverAddr=sipServer
            expires=90
            setIdentityAddress(fromAdress)
            enableRegister(true)
        }

    }

    private fun initializeCoreListener(){
        mListener = object : CoreListenerStub() {

            override fun onRegistrationStateChanged(
                lc: Core?,
                cfg: ProxyConfig?,
                cstate: RegistrationState?,
                message: String?
            ) {
                when (cstate){

                    RegistrationState.None->{
                        updateCallStatus("$message")
                        Log.i(MYTAG," registration state NONE, $message,$cstate")
                    }

                    RegistrationState.Cleared->{
                        updateCallStatus("$message")
                        Log.i(MYTAG," registration state Cleared, $message,$cstate")
                    }

                    RegistrationState.Failed->{
                        updateCallStatus("$message")
                        Log.i(MYTAG," registration state FAILED,$message,$cstate ")
                    }

                    RegistrationState.Ok->{
                        updateCallStatus("$message")
                        if(!callAlreadyStartedAfterRegistration){
                            callAlreadyStartedAfterRegistration=true
                            viewModel.startTimeout()
                        }

                        Log.i(MYTAG," registration state OK,$message, $cstate")
                    }

                    RegistrationState.Progress->{
                        updateCallStatus("$message")
                        Log.i(MYTAG," registration state PROGRESS,$message, $cstate")
                    }

                }
                super.onRegistrationStateChanged(lc, cfg, cstate, message)
            }

            override fun onCallStateChanged(
                lc: Core?,
                call: Call?,
                cstate: Call.State?,
                message: String?
            ) {
                when (cstate) {

                    Call.State.Idle->{
                        Log.i(MYTAG,"idle, $message")
                        updateCallStatus("Idle")
                    }

                    Call.State.OutgoingInit->{
                        Log.i(MYTAG, "$message")
                        // ringback is heard normally in earpiece or bluetooth receiver.
                        //setAudioManagerInCallMode();
                       // requestAudioFocus(STREAM_VOICE_CALL);
                        updateCallStatus(message)
                    }

                    Call.State.OutgoingProgress->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                    }

                    Call.State.OutgoingRinging->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)

                    }

                    Call.State.OutgoingEarlyMedia->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                    }

                    Call.State.Connected->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                    }

                    Call.State.StreamsRunning->{
                        //setAudioManagerInCallMode()
                        Log.i(MYTAG, "$message")

                    }

                    Call.State.Error -> {
                        Log.i(MYTAG,"call state error $message")
                        if (call?.getErrorInfo()?.getReason() == Reason.Declined) {
                            updateCallStatus("Call declined")
                            showToast("Call declined")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.NotFound) {
                            updateCallStatus("Not Found")
                            showToast("Not Found")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.NotAcceptable) {
                            updateCallStatus("Not Acceptable")
                            showToast("Not Acceptable")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.Busy) {
                            updateCallStatus("Busy")
                            showToast("Busy")

                        } else if (message != null) {
                            updateCallStatus(message)
                        }

                    }

                    Call.State.End-> {
                        // Convert Core message for internalization
                        Log.i(MYTAG," $message")
                        if (call?.getErrorInfo()?.getReason() == Reason.Declined) {
                            updateCallStatus(message)
                            Log.i(MYTAG,"error_call_declined $message")


                        }

                    }

                    Call.State.Paused->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                        showToast("Paused")

                    }

                    Call.State.Resuming->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                        showToast("Resuming")
                    }

                    else->{  Log.i(MYTAG, "else branch:$message")}

                }

               /* if(cstate == Call.State.End || cstate == Call.State.Error){

                    if (mCore?.getCallsNb() == 0) {
                        if (mAudioFocused) {
                            val res = mAudioManager?.abandonAudioFocus(null)
                            /*Log.d(
                                MYTAG,
                                "[Audio Manager] Audio focus released a bit later: "
                                        + (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED? "Granted": "Denied"))*/
                            mAudioFocused = false
                        }

                    }
                }*/

                if (cstate == Call.State.End || cstate == Call.State.Released) {
                    startNavigation()
                }
            }

        }

    }


    private fun toggleSpeakerButton(){
        Log.i(MYTAG,"AudioManager is $mAudioManager")
        if(mAudioManager==null) return
        if(mIsSpeakerEnabled) {
                    mAudioManager?.isSpeakerphoneOn=false
                    binding.speakerFAB.apply {
                        setImageResource(R.drawable.ic_speaker_disabled)
                        elevation=1F
                     }
                    mIsSpeakerEnabled=false
        } else {
            mAudioManager?.isSpeakerphoneOn=true
            binding.speakerFAB.apply {
                setImageResource(R.drawable.ic_speaker_enabled)
                elevation=12F
             }
            mIsSpeakerEnabled=true
        }
    }

    private fun toggleSipMicButton(){
        Log.i(MYTAG,"toggleSipMicButton, mCore is $mCore")
       if(mCore==null) return
        if(!mIsMicMuted) {
            binding.sipMicButton.apply {
                setImageResource(R.drawable.ic_mic_off)
                elevation=1F
            }
            mCore?.enableMic(false)
            mIsMicMuted=true
        } else {
            binding.sipMicButton.apply {
                setImageResource(R.drawable.ic_mic_on)
                elevation=12F
            }
            mCore?.enableMic(true)
            mIsMicMuted=false
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.resetSipCredentials()
        Log.i(MYTAG,"ONLIFE DESTROY VIEW")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"ONLIFE DESTROY")
        if(mTimer!=null) mTimer.cancel()
        if(mCore!=null){
            mCore?.enableMic(true)
            mCore?.stop()
            mCore?.removeListener(mListener)
            mCore=null
        }
        Factory.instance().loggingService.removeListener(myLoggingServiceListener)
        if(mCall!=null) mCall=null
        if(mListener!=null) mListener=null
        mAudioManager?.isSpeakerphoneOn=false
        mAudioManager=null

    }

    private fun setAudioManagerInCallMode() {
        if (mAudioManager?.mode == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w(MYTAG,"[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...")
            return
        }
        Log.d(MYTAG,"[Audio Manager] Mode: MODE_IN_COMMUNICATION")
        mAudioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
    }

    private fun requestAudioFocus(stream: Int) {
        if (!mAudioFocused) {
            val res = mAudioManager!!.requestAudioFocus(
                null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE
            )
            Log.d(
                MYTAG,
                "[Audio Manager] Audio focus requested: "
                        + if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied"
            )
            if (res == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) mAudioFocused = true
        }
    }

}
