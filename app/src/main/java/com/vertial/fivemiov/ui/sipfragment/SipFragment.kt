package com.vertial.fivemiov.ui.sipfragment



import android.net.sip.*
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoSIPE1
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentSipBinding
import com.vertial.fivemiov.utils.removePlus
import kotlinx.coroutines.Dispatchers


private val MYTAG="MY_Sip fragment"
class SipFragment : Fragment() {

    private lateinit var binding:FragmentSipBinding
    private lateinit var args:SipFragmentArgs
    private lateinit var viewModel: SipViewModel

    private var isFragmentInitialized=false
    /*val sipManager: SipManager? by lazy(LazyThreadSafetyMode.NONE) {
        SipManager.newInstance(context)
    }*/

    private var sipManager: SipManager?=null
    private var me:SipProfile? = null
    private var peersipProfile:SipProfile?=null
    private var sipAudioCall:SipAudioCall?=null
    private var setSpeakerMode:Boolean?=null



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args= SipFragmentArgs.fromBundle(arguments!!)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_sip,container,false)
        binding.apply {
            nametextView.text=args.contactName
            sipnumbertextView.text=args.contactNumber
            sipMicButton.isEnabled=false
            speakerFAB.isEnabled=false
        }

        val database= MyDatabase.getInstance(requireActivity().application).myDatabaseDao
        val mApi= MyAPI.retrofitService
        val mySipRepo=RepoSIPE1(database,mApi)
        val myRepo= Repo(database,mApi)

        /*val myApp=requireActivity().application as MyApplication
        val myAppContanier=myApp.myAppContainer*/

        viewModel = ViewModelProvider(this, SipViewModelFactory(mySipRepo,myRepo,requireActivity().application))
            .get(SipViewModel::class.java)



            binding.sipendbutton.setOnClickListener {
                //sipAudioCall?.endCall()
                sipAudioCall?.close()
                closeLocalProfile()
                viewModel.navigateBack()

            }

            binding.sipMicButton.setOnClickListener {
                if (sipAudioCall != null) {
                    if (sipAudioCall?.isMuted == true) {
                        sipAudioCall?.toggleMute()
                        binding.sipMicButton.setImageResource(R.drawable.ic_mic_on)
                        //resources.getDrawable(R.drawable.ic_volume_mute, null)
                        Log.i(MYTAG, "mute button set to false")

                    } else {
                        sipAudioCall?.toggleMute()
                        binding.sipMicButton.setImageResource(R.drawable.ic_mic_off)
                        //resources.getDrawable(R.drawable.ic_volume_off, null)
                        Log.i(MYTAG, "mute button set to true ")
                    }

                }
            }


            binding.speakerFAB.setOnClickListener {
                if (sipAudioCall != null) {
                    when (setSpeakerMode) {
                        null -> {
                            sipAudioCall?.setSpeakerMode(true)
                            setSpeakerMode = true
                            binding.speakerFAB.setImageResource(R.drawable.ic_volume_mute)

                        }
                        true -> {
                            sipAudioCall?.setSpeakerMode(false)
                            setSpeakerMode = false
                            binding.speakerFAB.setImageResource(R.drawable.ic_volume_off)

                        }
                        false -> {
                            sipAudioCall?.setSpeakerMode(true)
                            setSpeakerMode = true
                            binding.speakerFAB.setImageResource(R.drawable.ic_volume_mute)

                        }
                    }
                }

            }


            requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

            viewModel.getSipAccountCredentials()

            return binding.root

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
                if (response.sipUserName.isNotEmpty()&& response.sipPassword.isNotEmpty() && response.sipServer.isNotEmpty() && response.sipCallerId.isNotEmpty()){
                    initializeManager(sipUserName = response.sipUserName,sipPassword = response.sipPassword,sipServer = response.sipServer,sipCallerId = response.sipCallerId)
                    viewModel.resetgetSipAccountCredentialsNetSuccess()
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

    }


    override fun onStart() {
        super.onStart()
        if(isFragmentInitialized) findNavController().navigateUp()
        else isFragmentInitialized=true
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_logout).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false
    }

    private fun initializeManager(sipUserName:String, sipPassword:String, sipServer:String,sipCallerId:String) {
        Log.i(MYTAG,"initialize manager")
        sipManager=SipManager.newInstance(requireContext())
        if(sipManager!=null) {
            initalizePeerProfile(sipServer)
            initializeLocalProfile(sipUserName,sipPassword,sipServer,sipCallerId)
        }else showToast("Sip Manager is $sipManager")
    }

    private fun initalizePeerProfile(sipServer: String) {
        val peer=SipProfile.Builder("ankeanke","iptel.org")

        //val peer=SipProfile.Builder(PhoneNumberUtils.normalizeNumber(args.contactNumber),sipServer)
        peersipProfile=peer.build()
        Log.i(MYTAG,"peer je ${peersipProfile?.uriString}")
    }

    private fun initializeLocalProfile(sipUserName: String,sipPassword: String,sipServer: String,sipCallerId: String) {

        if(me!=null) {
            Log.i(MYTAG,"me nije null, zatvara se profil")
            closeLocalProfile()
        }

        Log.i(MYTAG,"initialize local profile")


        val mysipProfileBuilder = SipProfile.Builder("7936502090", "45.63.117.19").setPassword("rasa123321")
        //val mysipProfileBuilder = SipProfile.Builder("milena", "iptel.org").setPassword("Milena77")

       // val mysipProfileBuilder = SipProfile.Builder(sipUserName, sipServer).setPassword(sipPassword)
        me=mysipProfileBuilder.build()
        Log.i(MYTAG,"me je ${me?.uriString}")


        try {
            sipManager?.open(me)
            updateCallStatus("Opening connection...")
            Log.i(MYTAG," open ")
        }catch (s:SipException) {
            showToast(getString(R.string.sip_failure_message))
            viewModel.navigateBack()
            Log.i(MYTAG," open greska ${s.stackTrace}, ${s.cause}")
        }

        Log.i(MYTAG,"da li je otvoren ${sipManager?.isOpened(me?.uriString)}")

        viewModel.startRegTimeout()

        /*Log.i(MYTAG,"is opened ${sipManager.isOpened(me?.uriString)}")
        Log.i(MYTAG,"is registered ${sipManager.isRegistered(me?.uriString)}")
        Log.i(MYTAG,"podaci ${me?.sipDomain},${me?.password},${me?.userName}")*/

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
                              Log.i(MYTAG," da li je registered on reg done callback ${sipManager?.isRegistered(me?.uriString)}")

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
                            closeLocalProfile()
                            viewModel.navigateBack()
                        }

                    })
                }
            })
        }catch (e:SipException){
            Log.i(MYTAG,"Registration SIP Exception, ${e.message}")
            showToast(resources.getString(R.string.something_went_wrong))
            closeLocalProfile()
            viewModel.navigateBack()
        }
    }

    private fun makeSipAudioCall() {
       sipAudioCall=sipManager?.makeAudioCall(me,peersipProfile,object: SipAudioCall.Listener(){

            override fun onCalling(call: SipAudioCall?) {
                Log.i(MYTAG,"makeSipAudioCall, on Calling callback")
                super.onCalling(call)
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    if(context!=null) updateCallStatus(getString(R.string.sip_calling))
                 })

            }

           override fun onCallBusy(call: SipAudioCall?) {
               super.onCallBusy(call)
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   if(context!=null) updateCallStatus(getString(R.string.sip_busy))
               })

           }


            override fun onCallEstablished(call: SipAudioCall?) {
                super.onCallEstablished(call)
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    if(context!=null){
                    updateCallStatus(getString(R.string.sip_call_established))
                    binding.speakerFAB.isEnabled=true
                    binding.sipMicButton.isEnabled=true
                    }
                })
                call?.startAudio()
                call?.setSpeakerMode(false)
                if(call?.isMuted==true) {
                        call.toggleMute()

                }

            }

           override fun onCallEnded(call: SipAudioCall?) {
               super.onCallEnded(call)
               Log.i(MYTAG,"call ended listener")
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   if(context!=null) {
                        updateCallStatus(getString(R.string.sip_call_ended))
                       Log.i(MYTAG,"call ended listener runnable")

                       viewModel.navigateBack()
                   }
               })

           }

            override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
                super.onError(call, errorCode, errorMessage)
                call?.close()
                val h=Handler(Looper.getMainLooper())

                h.post(Runnable {
                    if(context!=null) {
                        viewModel.navigateBack()
                        showToast(getString(R.string.sip_failure_message))
                    }
                })

                Log.i(MYTAG,"error, ${call.toString()}, code $errorCode, message $errorMessage")
            }
        },10)
    }



    private fun finishFunctions(){
        try {
            sipAudioCall?.endCall()
            Log.i(MYTAG, "end call is FinishFunctions uradjen")
        }catch (e:Throwable) {
            Log.i(MYTAG, "end call is FinishFunctions $e")
        }

        sipAudioCall?.close()
        closeLocalProfile()
        viewModel.resetSipCredentials()

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


    override fun onPause() {
        Log.d(MYTAG, "on PAUSE")
        sipAudioCall?.close()
       closeLocalProfile()
        //findNavController().navigateUp()
        super.onPause()
    }



    /*override fun onDestroyView() {
        Log.d(MYTAG, "onDestroyView sipAudio Call je $sipAudioCall")
        sipAudioCall?.endCall()
        Log.d(MYTAG, "onDestroyView sipAudio Call posle end call je $sipAudioCall")
        sipAudioCall?.close()
        Log.d(MYTAG, "onDestroyView sipAudio Call posle close je $sipAudioCall")
        closeLocalProfile()
        viewModel.resetSipCredentials()

        super.onDestroyView()
    }*/

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }


}
