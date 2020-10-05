package app.adinfinitum.ello.ui.sipfragment



import android.content.Context
import android.media.AudioFocusRequest
import android.media.AudioManager
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
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.RepoSIPE1
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.FragmentSipBinding
import app.adinfinitum.ello.ui.initializeSharedPrefToFalse
import app.adinfinitum.ello.ui.myapplication.MyApplication
import org.linphone.core.*
import java.util.*
import java.util.concurrent.TimeUnit


private val MYTAG="MY_Sip fragment"
private val SERVER_LOG_TAG="SIP_Fragment"
class SipFragment : Fragment() {

    private lateinit var binding:FragmentSipBinding
    private lateinit var args:SipFragmentArgs
    private lateinit var viewModel: SipViewModel

    private lateinit var wl:PowerManager.WakeLock

    //Linphone
    private var mCore:Core?=null
    private var mListener: CoreListenerStub? = null
    private var mProxyConfig:ProxyConfig?=null
    private var mSipServer:String?=null

    private var mCall: Call? = null
    private var callAlreadyStartedAfterRegistration=false
    private var isAlreadyRegisteredOnce=false

    private var mAudioManager:AudioManager? = null
    private var mAudioFocused:Boolean=false
    private var audioFocusRequest: AudioFocusRequest?=null
    private var mIsMicMuted = false
    private var mIsSpeakerEnabled = false

    private var navigationUpInProcess=false

    //Linphone process
    private val sHandler: Handler = Handler(Looper.getMainLooper())
    private var mTimer: Timer=Timer("Linphone scheduler")
    val mIterateRunnable = Runnable {
        if (mCore != null) {
            mCore?.iterate()
            //Log.i(MYTAG," u iterate")
        }
    }

    val lTask: TimerTask = object : TimerTask() {
        override fun run() {
            dispatchOnUIThread(mIterateRunnable)
        }
    }

    //Call timer
    private val callDurationTimer=Timer("Call Duration")
    private var callStartTime:Long=0L
    val callTimerRunnable = Runnable {
      updateTimer()
    }

    val callTimerTask: TimerTask = object : TimerTask() {
        override fun run() {
            dispatchOnUIThread(callTimerRunnable)
        }
    }

    //Linphone Logging
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
                    Log.e(MYTAG,"$domain, $message")
                    viewModel.logStateToMyServer("Linphone LoggingService","error:$domain, $message")  }
                LogLevel.Fatal -> Log.wtf(domain, message)
                else -> Log.wtf(domain, message)

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
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onCreateView")
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
                Log.i("SIP_FLOW"," call end button clicked")

                val currentcall=mCore?.currentCall
                Log.i("SIP_FLOW"," Sip Fragment currentCall : $currentcall")
                viewModel.logStateToMyServer("SIP:END BUTTON CLICKED","current call: ${mCore?.currentCall}")
                if (currentcall != null) {
                        currentcall.terminate()
                } else {
                        startNavigation()
                }

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
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"function: startNavigation(),navigationUpInProcess:$navigationUpInProcess")
        if(!navigationUpInProcess) {
            navigationUpInProcess=true
            viewModel.navigateBack()
            Log.i(MYTAG," start navigate back function")
            Log.i("SIP_FLOW"," start navigate back function")
            viewModel.logStateToMyServer(SERVER_LOG_TAG,"navigate back started")
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.i(MYTAG, "ONLIFE onViewCreated")
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onViewCreated")
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onViewCreated")

        viewModel.timeout.observe(viewLifecycleOwner, Observer {
            if(it){
                makeSipAudioCall()
                viewModel.timeoutFinished()
            }
        })


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
                        viewModel.resetgetSipAccountCredentialsNetSuccess()
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
                    viewModel.logStateToMyServer("SIP", "loggingOut")
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
        try {
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
        //mTimer = Timer("Linphone scheduler")
        mCore?.start()
        mTimer.schedule(lTask, 0, 20)

        }catch (t:Throwable){
            Log.e(MYTAG,"initializing mCore error ${t.message}")
            viewModel.logStateToMyServer("SIP:initializing mCore error","error message:${t.message}")
            startNavigation()

        }

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
            viewModel.logStateToMyServer("SIP - makeSipAudioCall","calling number: sip:$numberToCall@$mSipServer")
            try {
                if(mSipServer!=null) mCall = mCore?.inviteWithParams("sip:$numberToCall@$mSipServer",callParams)
            }catch (t:Throwable){
                viewModel.logStateToMyServer("make SIP Call","mCore.invite error: ${t.message}")
                Log.i(MYTAG,"mCore.invite error: ${t.message}")
            }


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
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onStart,mCore= $mCore")
        mCore?.enterForeground()
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onStart,mCore= $mCore")
    }


    override fun onResume() {
        super.onResume()
        Log.i(MYTAG, "ONLIFE RESUME, mCore= $mCore")
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onResume,mCore= $mCore")
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onResume,mCore= $mCore")


    }

    override fun onPause() {
        super.onPause()
        Log.i(MYTAG, "ONLIFE PAUSE,  mCore= $mCore")
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onPause,mCore= $mCore")
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onPause,mCore= $mCore")

    }

    override fun onStop() {
        super.onStop()
        Log.i(MYTAG, "ONLIFE STOP,")
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onStop,mCore= $mCore")
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onStop,mCore= $mCore")
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
        Factory.instance().setDebugMode(true, "MY_LINPHONE");
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
                        if(!isAlreadyRegisteredOnce) updateCallStatus("$message")
                        Log.i(MYTAG," registration state NONE, $message,$cstate")
                        viewModel.logStateToMyServer("SIP onRegistrationStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," registration  $message,$cstate")
                    }

                    RegistrationState.Cleared->{
                        updateCallStatus("$message")
                        Log.i(MYTAG," registration state Cleared, $message,$cstate")
                        viewModel.logStateToMyServer("SIP onRegistrationStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," registration  $message,$cstate")
                    }

                    RegistrationState.Failed->{
                        viewModel.logStateToMyServer("SIP:onRegistrationStateChanged"," $cstate,$message, isAlreadyRegisteredOnce=$isAlreadyRegisteredOnce")
                        if(!isAlreadyRegisteredOnce) {
                            updateCallStatus("$message")
                            showToast("$message")
                            startNavigation()
                        }
                        Log.i(MYTAG," registration state FAILED,$message,$cstate ")

                        Log.i("SIP_FLOW"," registration  $message,$cstate")
                    }

                    RegistrationState.Ok->{

                        if(!callAlreadyStartedAfterRegistration){
                            callAlreadyStartedAfterRegistration=true
                            viewModel.startTimeout()
                        }

                        if(!isAlreadyRegisteredOnce){
                            updateCallStatus("$message")
                            isAlreadyRegisteredOnce=true
                        }
                        viewModel.logStateToMyServer("SIP:onRegistrationStateChanged"," $cstate,$message")
                        Log.i(MYTAG," registration state OK,$message, $cstate")
                        Log.i("SIP_FLOW"," registration $message,$cstate")
                    }

                    RegistrationState.Progress->{
                        if(!isAlreadyRegisteredOnce) updateCallStatus("$message")
                        viewModel.logStateToMyServer("SIP:onRegistrationStateChanged"," $cstate,$message")
                        Log.i(MYTAG," registration state PROGRESS,$message, $cstate")
                        Log.i("SIP_FLOW"," registration, $message,$cstate")
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
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        updateCallStatus("Idle")
                    }

                    Call.State.OutgoingInit->{
                        Log.i(MYTAG, "$message")
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        // ringback is heard normally in earpiece or bluetooth receiver.
                        //setAudioManagerInCallMode()
                        //requestAudioFocus()
                        updateCallStatus(message)
                    }

                    Call.State.OutgoingProgress->{
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                    }

                    Call.State.OutgoingRinging->{
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)

                    }

                    Call.State.OutgoingEarlyMedia->{
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        Log.i(MYTAG, "$message")
                        //updateCallStatus(message)
                    }

                    Call.State.Connected->{
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                        callStartTime=System.currentTimeMillis()
                        callDurationTimer.schedule(callTimerTask, 0, 500)


                    }

                    Call.State.StreamsRunning->{
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        //setAudioManagerInCallMode()
                        Log.i(MYTAG, "$message")

                    }

                    Call.State.Error -> {
                        Log.i(MYTAG,"call state error message:$message")
                        Log.i("SIP_FLOW"," onCallStateChanged, cim udje u error $cstate,$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message,${call?.getErrorInfo()?.getReason()}")

                        if (call?.getErrorInfo()?.getReason() == Reason.Declined) {
                            updateCallStatus("Call declined")
                            showToast("Call declined")
                            Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.NotFound) {
                            updateCallStatus("Not Found")
                            showToast("Not Found")
                            Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.NotAcceptable) {
                            updateCallStatus("Not Acceptable")
                            showToast("Not Acceptable")
                            Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.Busy) {
                            updateCallStatus("Busy")
                            showToast("Busy")
                            Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")

                        } else if (message != null) {
                            updateCallStatus(message)
                            Log.i("SIP_FLOW"," onCallStateChanged $cstate,$message")
                        }

                    }

                    Call.State.End-> {
                        // Convert Core message for internalization
                        Log.i(MYTAG,"call state End message:$message")
                        Log.i("SIP_FLOW"," onCallStateChanged cim udje u end $cstate,$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message,${call?.getErrorInfo()?.getReason()}")
                        if (call?.getErrorInfo()?.getReason() == Reason.Declined) {
                            updateCallStatus(message)
                            Log.i(MYTAG,"error_call_declined $message")
                            showToast("Call declined")
                            Log.i("SIP_FLOW"," onCallStateChanged end i declined $cstate,$message")

                        }

                    }

                    Call.State.Released->{
                        Log.i(MYTAG, "$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        updateCallStatus(message)
                        Log.i("SIP_FLOW"," onCallStateChanged  $cstate,$message")

                    }

                    Call.State.Paused->{
                        Log.i(MYTAG, "$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        updateCallStatus(message)
                        showToast("Paused")
                        Log.i("SIP_FLOW"," onCallStateChanged  $cstate,$message")

                    }

                    Call.State.Resuming->{
                        Log.i(MYTAG, "$message")
                        viewModel.logStateToMyServer("SIP:onCallStateChanged"," $cstate,$message")
                        updateCallStatus(message)
                        showToast("Resuming")
                        Log.i("SIP_FLOW"," onCallStateChanged  $cstate,$message")
                    }

                    else->{  Log.i(MYTAG, "Call State Else branch:$message")
                            viewModel.logStateToMyServer("SIP:onCallStateChanged"," ELSE BRANCH:$cstate,$message")
                        Log.i("SIP_FLOW"," onCallStateChanged else branch  $cstate,$message")
                    }

                }


                if (cstate == Call.State.End || cstate == Call.State.Released) {
                    viewModel.logStateToMyServer("SIP:state == Call.State.End || state == Call.State.Released","navigateUpFromFragment")
                    callDurationTimer.cancel()
                    startNavigation()
                    Log.i("SIP_FLOW"," onCallStateChanged cstate == Call.State.End || cstate == Call.State.Released  $cstate,$message")
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
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onDestroyView,mCore= $mCore")
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onDestroyView,mCore= $mCore")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"ONLIFE DESTROY")
        Log.i("SIP_FLOW"," Sip Fragment ONLIFE onDestroy,mCore= $mCore")
        viewModel.logStateToMyServer(SERVER_LOG_TAG,"onDestroy,mCore= $mCore")
        if(mTimer!=null) mTimer.cancel()
        if(mCore!=null){
            mCore?.enableMic(true)
            mCore?.stop()
            mCore?.removeListener(mListener)
            mCore=null
        }
        try{
            Factory.instance().loggingService.removeListener(myLoggingServiceListener)
        }catch (t:Throwable){
            viewModel.logStateToMyServer(SERVER_LOG_TAG,"onDestroy , try-catch in Factory.instance().loggingService.removeListener(myLoggingServiceListener)")
        }
        if(mCall!=null) mCall=null
        if(mListener!=null) mListener=null
        mAudioManager?.isSpeakerphoneOn=false

        //release Audio resources
        /*mAudioManager?.setMode(AudioManager.MODE_NORMAL)
        if(audioFocusRequest!=null){
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {

                mAudioManager?.abandonAudioFocusRequest(audioFocusRequest)
            }else{
                mAudioManager?.abandonAudioFocus(null)
            }
        }*/

    }

    private fun updateTimer(){
        val timePassedSinceCallStarted=System.currentTimeMillis()-callStartTime
        //val timePassedSinceCallStarted=3603000L
        var allseconds = (timePassedSinceCallStarted / 1000)

        //call lasts more or less than an hour formating is different
        val minutes = allseconds / 60
        if(minutes<60){
            val seconds = allseconds % 60
            Log.i("MTIMER","timePassed:${String.format("%d:%02d", minutes, seconds)}")
            binding.callTimerTextView.text=String.format("%d:%02d", minutes, seconds)
        }else{
            val timePassed=String.format("%02d:%02d:%02d",
                TimeUnit.MILLISECONDS.toHours(timePassedSinceCallStarted),
                TimeUnit.MILLISECONDS.toMinutes(timePassedSinceCallStarted) -
                        TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timePassedSinceCallStarted)),
                TimeUnit.MILLISECONDS.toSeconds(timePassedSinceCallStarted) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timePassedSinceCallStarted)));
            binding.callTimerTextView.text=timePassed
            Log.i("MTIMER","timePassed: $timePassed")
        }

    }



    /*private fun setAudioManagerInCallMode() {
        if (mAudioManager?.mode == AudioManager.MODE_IN_COMMUNICATION) {
            Log.w(MYTAG,"[Audio Manager] already in MODE_IN_COMMUNICATION, skipping...")
            return
        }
        Log.d(MYTAG,"[Audio Manager] Mode: MODE_IN_COMMUNICATION")
        mAudioManager?.mode = AudioManager.MODE_IN_COMMUNICATION
    }*/


    /*private fun requestAudioFocus() {
        if (!mAudioFocused) {

            var audioFocusReqResult:Int? = null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val audioAttr = AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_VOICE_COMMUNICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build()

                audioFocusRequest =
                    AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT)
                        .setAudioAttributes(audioAttr)
                        .build()

            audioFocusReqResult= mAudioManager?.requestAudioFocus(audioFocusRequest)
            }

            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
                audioFocusReqResult = mAudioManager?.requestAudioFocus(
                    null,
                    // Use the  stream.
                    AudioManager.STREAM_VOICE_CALL,
                    // Requestfocus.
                    AudioManager.AUDIOFOCUS_GAIN_TRANSIENT
                )
                /*val res = mAudioManager?.requestAudioFocus(
               null, stream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_EXCLUSIVE)*/
            }

            Log.d(MYTAG,"[Audio Manager] Audio focus requested: "
                    + if (audioFocusReqResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) "Granted" else "Denied"
            )

            if(audioFocusReqResult == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
                mAudioFocused = true
            }

        }
    }*/

}
