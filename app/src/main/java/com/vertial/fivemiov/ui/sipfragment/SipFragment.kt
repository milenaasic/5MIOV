package com.vertial.fivemiov.ui.sipfragment



import android.content.Context
import android.media.RingtoneManager
import android.media.ToneGenerator
import android.net.Uri
import android.net.sip.*
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
import java.util.*
import org.linphone.core.*


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

    var mCall: Call? = null
    var mIsMicMuted = false;
    var mIsSpeakerEnabled = false;

    private var navigationUpInProcess=false

    private var setSpeakerMode:Boolean=false
    private var setMicMode:Boolean=true

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


    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

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


                startNavigation()
                Log.i(MYTAG, "call end button clicked ")
            }

            binding.sipMicButton.setOnClickListener {

                   /* if (setMicMode == true) {
                        if (sipAudioCall?.isMuted==true) sipAudioCall?.toggleMute()

                    } else {
                        if (sipAudioCall?.isMuted==false) sipAudioCall?.toggleMute()
                        Log.i(MYTAG, "mute button set to true ")
                    }*/


            }


            binding.speakerFAB.setOnClickListener {
                Log.i(MYTAG,"binding.speakerFAB.setOnClickListener")
               toggleSpeakerButton()
                when (setSpeakerMode) {
                        true -> {
                            Log.i(MYTAG,"binding.speakerFAB.setOnClickListener setSpekar mode : $setSpeakerMode")

                        }
                        false -> {


                        }
                    }

            }


            if(mListener==null){
                initializeCoreListener()
            }


            viewModel.getSipAccountCredentials()

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

       /* viewModel.timeoutReg.observe(viewLifecycleOwner, Observer {
            if(it){
                register()
                viewModel.timeoutRegFinished()
            }
        })*/

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
        mCore =
            Factory.instance()
                .createCore(
                    null,
                    null,
                    requireActivity().applicationContext)

        configureCore()
        configureLogging()

        mProxyConfig= mCore?.createProxyConfig()
        configureProxy()
        mCore?.addProxyConfig(mProxyConfig)
        mCore?.defaultProxyConfig=mProxyConfig

        /*use schedule instead of scheduleAtFixedRate to avoid iterate from being call in burst after cpu wake up*/
        mTimer = Timer("Linphone scheduler")
        // mTimer.schedule(lTask, 0, 20)
        mCore?.start()
        mTimer.schedule(lTask, 0, 20)

        //start a call
        Log.i(MYTAG," proxy config list je ${mCore?.proxyConfigList?.size}")
        Log.i(MYTAG," username je ${mCore?.defaultProxyConfig?.identityAddress?.username}")
        Log.i(MYTAG,"stanje registracije ${mCore?.defaultProxyConfig?.state} ")
        Log.i(MYTAG," avpf mode iz Call ${mCore?.defaultProxyConfig?.avpfMode}, enabled ${mCore?.defaultProxyConfig?.avpfEnabled()} ")
        Log.i(MYTAG," media encryption iz Call je ${mCore?.mediaEncryption}")

        mCall=mCore?.invite("sip:+381643292202@45.63.117.19")


    }







    private fun makeSipAudioCall() {

        /*viewModel.logCredentialsForSipCall(sipUsername  = me?.userName,
                                            sipPassword = me?.password,
                                            sipDisplayname = me?.displayName,
                                            sipServer = me?.sipDomain)*/


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
        //mCore?.start()
        //mTimer.schedule(lTask, 0, 20)
        Log.i(MYTAG, "ON PAUSE")
    }


    override fun onResume() {
        super.onResume()

        Log.i(MYTAG, "ON RESUME")

    }

    override fun onPause() {
        super.onPause()

        Log.i(MYTAG, "ON PAUSE")
    }

    override fun onStop() {
        super.onStop()

        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(wl.isHeld) wl.release()

        mTimer.cancel()
        mCore?.stop()
        mCore?.removeListener(mListener)
    }



    private fun configureCore(){
        val myAuthInfo=Factory.instance().createAuthInfo("381646408513","381646408513","1dd1c051df981",null,null,null)

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
        //val loggingService=Factory.instance().loggingService
        Factory.instance().loggingService.addListener(myLoggingServiceListener)
    }

    private fun configureProxy(){

        var fromAdress=mCore?.createAddress("sip:381646408513@45.63.117.19")
        fromAdress?.password="1dd1c051df981"

        mProxyConfig?.apply {
            serverAddr="45.63.117.19"
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
                        Log.i(MYTAG," registration state OK,$message, $cstate")
                    }

                    RegistrationState.Progress->{
                        updateCallStatus("$message")
                        Log.i(MYTAG," registration state PROGRESS,$message, $cstate")
                    }


                }
                //super.onRegistrationStateChanged(lc, cfg, cstate, message)
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

                    Call.State.Error -> {
                        Log.i(MYTAG,"call state error $message")
                        if (call?.getErrorInfo()?.getReason() == Reason.Declined) {
                            updateCallStatus("call declined")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.NotFound) {
                            updateCallStatus("Not Found")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.NotAcceptable) {
                            updateCallStatus("Not Acceptable")

                        } else if (call?.getErrorInfo()?.getReason() == Reason.Busy) {
                            updateCallStatus("Busy")

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
                        updateCallStatus(message)

                    }

                    Call.State.Paused->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                    }

                    Call.State.Resuming->{
                        Log.i(MYTAG, "$message")
                        updateCallStatus(message)
                    }

                }
            }

        }

    }


    private fun toggleSpeakerButton(){
        if(setSpeakerMode) {
                    binding.speakerFAB.apply {
                        setImageResource(R.drawable.ic_volume_off)
                        elevation=1F
                     }
                    setSpeakerMode=false
        } else {

            binding.speakerFAB.apply {
                setImageResource(R.drawable.ic_volume_mute)
                elevation=12F
             }
            setSpeakerMode=true
        }
    }

    private fun toggleSipMicButton(){
        if(setMicMode) {
            binding.sipMicButton.apply {
                setImageResource(R.drawable.ic_mic_off)
                elevation=1F
            }
            setMicMode=false
        } else {
            binding.sipMicButton.apply {
                setImageResource(R.drawable.ic_mic_on)
                elevation=12F
            }
            setMicMode=true
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(MYTAG,"on DESTROY VIEW")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"on DESTROY")
        if(mCore!=null) mCore=null

    }



}
