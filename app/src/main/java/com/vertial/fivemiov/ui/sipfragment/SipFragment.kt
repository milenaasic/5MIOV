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


private val MYTAG="MY_Sip fragment"
class SipFragment : Fragment() {

    private lateinit var binding:FragmentSipBinding
    private lateinit var args:SipFragmentArgs
    private lateinit var viewModel: SipViewModel


    private lateinit var wl:PowerManager.WakeLock

    private var navigationUpInProcess=false


    private var sipManager: SipManager?=null
    private var me:SipProfile? = null
    private var peersipProfile:SipProfile?=null
    private var sipAudioCall:SipAudioCall?=null
    private var setSpeakerMode:Boolean=false
    private var setMicMode:Boolean=true

    //private var toneGenerator: ToneGenerator?=null



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
                Log.i(MYTAG,"end button clicked, before endCall command call state is ${sipAudioCall?.state}")
                sipAudioCall?.endCall()
                Log.i(MYTAG,"end button clicked, after endCall command call state is ${sipAudioCall?.state}")

                startNavigation()
                Log.i(MYTAG, "call end button clicked ")
            }

            binding.sipMicButton.setOnClickListener {
                if(sipAudioCall!=null){
                        sipAudioCall?.toggleMute()
                        toggleSipMicButton()
                }
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
                            sipAudioCall?.setSpeakerMode(true)
                        }
                        false -> {
                            sipAudioCall?.setSpeakerMode(false)

                        }
                    }

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

        viewModel.timeoutReg.observe(viewLifecycleOwner, Observer {
            if(it){
                register()
                viewModel.timeoutRegFinished()
            }
        })

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
                    initializeManager(sipUserName = response.sipUserName,sipPassword = response.sipPassword,sipServer = response.sipServer,sipCallerId = response.sipCallerId)
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

    private fun initializeManager(sipUserName:String, sipPassword:String, sipServer:String,sipCallerId:String) {
        sipManager=SipManager.newInstance(requireContext())
        if(sipManager!=null) {
            initalizePeerProfile(sipServer)
            initializeLocalProfile(sipUserName,sipPassword,sipServer,sipCallerId)
        }else showToast("Sip Manager is $sipManager")
    }

    private fun initalizePeerProfile(sipServer: String) {

        val peer=SipProfile.Builder(PhoneNumberUtils.normalizeNumber(args.contactNumber),sipServer)
        peersipProfile=peer.build()
        Log.i(MYTAG,"peer is ${peersipProfile?.uriString}")
    }

    private fun initializeLocalProfile(sipUserName: String,sipPassword: String,sipServer: String,sipCallerId: String) {

        if(me!=null) {
            Log.i(MYTAG,"initializing local profile, profile was not closed $me")
            closeLocalProfile()
        }



        val mysipProfileBuilder = SipProfile.Builder(sipUserName, sipServer)
                                            .setPassword(sipPassword)
                                            .setDisplayName(sipCallerId)

        me=mysipProfileBuilder.build()



        try {
            sipManager?.open(me)
            updateCallStatus("Opening connection...")
            Log.i(MYTAG," open ")
        }catch (s:SipException) {
            showToast(getString(R.string.sip_failure_message))
            startNavigation()
            Log.i(MYTAG," open error ${s.stackTrace}, ${s.cause}")
        }

        Log.i(MYTAG,"is connection opened ${sipManager?.isOpened(me?.uriString)}")

        viewModel.startRegTimeout()


    }

    private fun register() {
        try {
            sipManager?.register(me, 10, object : SipRegistrationListener {
                override fun onRegistering(p0: String?) {
                    Log.i(MYTAG, "registgering $p0")

                        val h = Handler(Looper.getMainLooper())
                        h.post(Runnable {
                            if(context!=null) updateCallStatus(getString(R.string.sip_registering))
                        })

                }

                override fun onRegistrationDone(p0: String?, p1: Long) {
                    Log.i(MYTAG, "registration done $p0, $p1")
                    val h = Handler(Looper.getMainLooper())
                    h.post(Runnable {
                          if(context!=null) {
                            updateCallStatus(getString(R.string.sip_reg_done))
                              Log.i(MYTAG," is registered callback: ${sipManager?.isRegistered(me?.uriString)}")

                              viewModel.startTimeout()}
                    })
                }

                override fun onRegistrationFailed(p0: String?, p1: Int, p2: String?) {
                    Log.i(MYTAG, "registration FAILED $p0, $p1,$p2")
                    val h = Handler(Looper.getMainLooper())
                    h.post(Runnable {
                        if(context!=null) {
                            updateCallStatus(getString(R.string.sip_reg_failed))
                            showToast(resources.getString(R.string.sip_reg_failed))

                            startNavigation()
                        }

                    })
                }
            })
        }catch (e:SipException){
            Log.i(MYTAG,"Registration SIP Exception, ${e.message}")
            showToast(resources.getString(R.string.something_went_wrong))

            startNavigation()
        }
    }

    private fun makeSipAudioCall() {

        viewModel.logCredentialsForSipCall(sipUsername  = me?.userName,
                                            sipPassword = me?.password,
                                            sipDisplayname = me?.displayName,
                                            sipServer = me?.sipDomain)
       sipAudioCall=sipManager?.makeAudioCall(me,peersipProfile,object: SipAudioCall.Listener(){

            override fun onCalling(call: SipAudioCall?) {
               Log.i(MYTAG,"makeSipAudioCall, on Calling")
               super.onCalling(call)
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   if(context!=null) updateCallStatus(getString(R.string.sip_calling))
                   //showToast("Calling, ${me?.uriString},${me?.password}")
               })

           }



           override fun onRingingBack(call: SipAudioCall?) {
               Log.i(MYTAG,"makeSipAudioCall, on RingingBack")
               //startToneCallWaiting()
               super.onRingingBack(call)

           }

           override fun onCallBusy(call: SipAudioCall?) {
               Log.i(MYTAG,"makeSipAudioCall, on CallBusy")
               super.onCallBusy(call)
               /*toneGenerator?.apply {
                   stopTone()
                   release()

               }*/
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   //showToast("Busy, ${me?.uriString},${me?.password}")
                   if(context!=null) updateCallStatus(getString(R.string.sip_busy))
               })
               //todo prekinivezu

           }


            override fun onCallEstablished(call: SipAudioCall?) {

                //super.onCallEstablished(call)

                call?.startAudio()
                call?.setSpeakerMode(setSpeakerMode)

                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                   // showToast("On Call Established, ${me?.uriString},${me?.password}")

                    if(context!=null){
                    updateCallStatus(getString(R.string.sip_call_established))

                    }
                })

                Log.i(MYTAG,"makeSipAudioCall, on CallEstablished")

            }

           override fun onCallEnded(call: SipAudioCall?) {
               super.onCallEnded(call)
               Log.i(MYTAG,"call ended listener")
               /*toneGenerator?.apply {
                   stopTone()
                   release()

               }*/
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   if(context!=null) {
                       //showToast(" Call Ended, ${me?.uriString},${me?.password}")
                        updateCallStatus(getString(R.string.sip_call_ended))
                       Log.i(MYTAG,"call ended listener runnable")
                       startNavigation()
                   }
               })

           }

            override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
                super.onError(call, errorCode, errorMessage)
               /* toneGenerator?.apply {
                    stopTone()
                    release()

                }*/
                call?.close()
                val h=Handler(Looper.getMainLooper())

                h.post(Runnable {
                    if(context!=null) {
                        updateCallStatus(getString(R.string.sip_failure_message))
                        //showToast("Error, code $errorCode, message $errorMessage, user and pass: ${me?.uriString}, ${me?.password}")
                        startNavigation()
                    }
                })

                Log.i(MYTAG,"call error, ${call.toString()}, code $errorCode, message $errorMessage")
            }
        },10)
    }




    private fun closeLocalProfile() {
        try {
            sipManager?.close(me?.uriString)
            Log.d(MYTAG, "closing profile me")
        } catch (ee: Throwable) {
            Log.d(MYTAG, "Failed to close local profile, $ee, ${ee.message}")
        }
    }

    private fun updateCallStatus(status: String?) {
        binding.statustextView.text=status
    }

    override fun onStart() {
        super.onStart()
        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(!wl.isHeld) wl.acquire()
    }


    override fun onPause() {
        super.onPause()
        if(sipAudioCall?.state==SipSession.State.IN_CALL) sipAudioCall?.endCall()
        sipAudioCall?.close()
        /*toneGenerator?.apply {
            stopTone()
            release()

        }*/
        closeLocalProfile()
        viewModel.resetSipCredentials()

        Log.i(MYTAG, "ON PAUSE")
    }

    override fun onStop() {
        super.onStop()

        requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        if(wl.isHeld) wl.release()


    }

    override fun onResume() {
        super.onResume()

        Log.i(MYTAG, "ON RESUME")

    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
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




    override fun onDestroyView() {
        super.onDestroyView()
        Log.i(MYTAG,"on DESTROY VIEW")
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"on DESTROY")
    }

    private fun playRingingSound(){

        val ringtone: Uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
        val r = RingtoneManager.getRingtone(
            requireContext(),
            ringtone
        )
        r.play()
    }
    /*private fun startToneCallWaiting(){
        toneGenerator = ToneGenerator(6, 50)
        //while(sipAudioCall?.state==SipSession.State.OUTGOING_CALL_RING_BACK){
            toneGenerator?.startTone(ToneGenerator.TONE_SUP_CALL_WAITING)
           // tg.startTone(48,1000)
            //Thread.sleep(2000)




    }*/


}
