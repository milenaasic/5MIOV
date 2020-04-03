package com.vertial.fivemiov.ui.sipfragment



import android.net.sip.*
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentSipBinding
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.utils.removePlus


private val MYTAG="MY_Sip fragment"
class SipFragment : Fragment() {

    private lateinit var binding:FragmentSipBinding
    private lateinit var args:SipFragmentArgs
    private lateinit var viewModel: SipViewModel


    /*val sipManager: SipManager? by lazy(LazyThreadSafetyMode.NONE) {
        SipManager.newInstance(context)
    }*/

    private lateinit var sipManager: SipManager
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

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao

        viewModel = ViewModelProvider(this, SipViewModelFactory(database,requireActivity().application))
            .get(SipViewModel::class.java)

        binding.sipendbutton.setOnClickListener{
            sipAudioCall?.endCall()
            closeLocalProfile()
            viewModel.navigateBack()

        }

        binding.sipMicButton.setOnClickListener{
            if(sipAudioCall!=null) {
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
            if(sipAudioCall!=null) {
                when (setSpeakerMode) {
                    null -> {
                        sipAudioCall?.setSpeakerMode(true)
                        setSpeakerMode = true
                        binding.speakerFAB.setImageResource(R.drawable.ic_volume_mute)
                        /*binding.sipspeakerButton.icon =
                            resources.getDrawable(R.drawable.ic_speaker, null)*/

                    }
                    true -> {
                        sipAudioCall?.setSpeakerMode(false)
                        setSpeakerMode = false
                        binding.speakerFAB.setImageResource(R.drawable.ic_volume_off)
                        //binding.sipspeakerButton.icon =
                         //   resources.getDrawable(R.drawable.ic_close_icon, null)
                    }
                    false -> {
                        sipAudioCall?.setSpeakerMode(true)
                        setSpeakerMode = true
                        binding.speakerFAB.setImageResource(R.drawable.ic_volume_mute)
                        //binding.sipspeakerButton.icon =
                           // resources.getDrawable(R.drawable.ic_speaker, null)

                    }
                }
            }

        }


        initializeManager()

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        return binding.root

    }

    private fun createPeerSipProfile(contactNumber: String) {
        contactNumber.removePlus()

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

        /*viewModel.timeoutCallEnded.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigateUp()
                viewModel.callEndedTimeoutFinished()
            }
        })*/

        viewModel.navigateUp.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigateUp()
                viewModel.navigateBackFinished()
            }
        })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_logout).isVisible=false
    }

    private fun initializeManager() {
        sipManager=SipManager.newInstance(requireContext())
        initalizePeerProfile()
        initializeLocalProfile()

    }

    private fun initalizePeerProfile() {
        //val peer=SipProfile.Builder("ankeanke","iptel.org")
        //val username=args.contactNumber.

        val peer=SipProfile.Builder("0038163352717","45.63.117.19")
        peersipProfile=peer.build()
        Log.i(MYTAG,"peer je ${peersipProfile?.uriString}")
    }

    private fun initializeLocalProfile() {

        if(me!=null) {
            Log.i(MYTAG,"me nije null, zatvara se profil")
            closeLocalProfile()
        }

        /*val user: User?=viewModel.getSipAccountInfo()
        Log.i(MYTAG,"user je $user")*/


        Log.i(MYTAG,"sipManager je $sipManager")
        val mysipProfileBuilder = SipProfile.Builder("7936502090", "45.63.117.19").setPassword("rasa123321")
        //val mysipProfileBuilder = SipProfile.Builder("milena", "iptel.org").setPassword("Milena77")
        me=mysipProfileBuilder.build()
        Log.i(MYTAG,"me je ${me?.uriString}")


        
        try {
            sipManager.open(me)
            updateCallStatus("Opening connection...")
            Log.i(MYTAG," open ")
        }catch (s:SipException) {
            showToast(getString(R.string.sip_failure_message))
            viewModel.navigateBack()
            Log.i(MYTAG," open greska ${s.stackTrace}, ${s.cause}")
        }

        Log.i(MYTAG,"da li je otvoren ${sipManager.isOpened(me?.uriString)}")

        viewModel.startRegTimeout()

        /*Log.i(MYTAG,"is opened ${sipManager.isOpened(me?.uriString)}")
        Log.i(MYTAG,"is registered ${sipManager.isRegistered(me?.uriString)}")
        Log.i(MYTAG,"podaci ${me?.sipDomain},${me?.password},${me?.userName}")*/

    }

    private fun register() {
        try {
            sipManager.register(me, 10, object : SipRegistrationListener {
                override fun onRegistering(p0: String?) {
                    Log.i(MYTAG, "registgering $p0")
                    val h = Handler(Looper.getMainLooper())
                    h.post(Runnable {
                        updateCallStatus(getString(R.string.sip_registering))

                    })
                }

                override fun onRegistrationDone(p0: String?, p1: Long) {
                    Log.i(MYTAG, "registration done $p0, $p1")
                    val h = Handler(Looper.getMainLooper())
                    h.post(Runnable {
                        updateCallStatus(getString(R.string.sip_reg_done))
                        viewModel.startTimeout()
                    })
                }

                override fun onRegistrationFailed(p0: String?, p1: Int, p2: String?) {
                    Log.i(MYTAG, "registration FAILED $p0, $p1,$p2")
                    val h = Handler(Looper.getMainLooper())
                    h.post(Runnable {
                        updateCallStatus(getString(R.string.sip_reg_failed))
                        //viewModel.startRegTimeout()
                    })
                }
            })
        }catch (e:SipException){
            Log.i(MYTAG,"Registration SIP Exception, ${e.message}")
           showToast(getString(R.string.sip_failure_message))
            closeLocalProfile()
            viewModel.navigateBack()
        }
    }

    private fun makeSipAudioCall() {
       sipAudioCall=sipManager.makeAudioCall(me,peersipProfile,object: SipAudioCall.Listener(){

            override fun onCalling(call: SipAudioCall?) {
                super.onCalling(call)
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus(getString(R.string.sip_calling))
                 })

            }

           override fun onCallBusy(call: SipAudioCall?) {
               super.onCallBusy(call)
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   updateCallStatus(getString(R.string.sip_busy))
               })

           }


            override fun onCallEstablished(call: SipAudioCall?) {
                super.onCallEstablished(call)
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus(getString(R.string.sip_call_established))
                    binding.speakerFAB.isEnabled=true
                    binding.sipMicButton.isEnabled=true
                })
                call?.startAudio()
                call?.setSpeakerMode(false)
                if(call?.isMuted==true) {
                        call.toggleMute()

                }

            }

           override fun onCallEnded(call: SipAudioCall?) {
               super.onCallEnded(call)
               call?.endCall()
               val h=Handler(Looper.getMainLooper())
               h.post(Runnable {
                   updateCallStatus(getString(R.string.sip_call_ended))
                   viewModel.navigateBack()
               })

           }

            override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
                super.onError(call, errorCode, errorMessage)
                call?.close()
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("error, ${call.toString()}, code $errorCode, message $errorMessage")
                    closeLocalProfile()
                    viewModel.navigateBack()
                    showToast(getString(R.string.sip_failure_message))
                })

                Log.i(MYTAG,"error, ${call.toString()}, code $errorCode, message $errorMessage")
            }
        },10)
    }


    fun updateCallStatus(status: String?) {
            binding.statustextView.text=status
    }

    fun closeLocalProfile() {
        try {
            sipManager?.close(me?.uriString)
            Log.d(MYTAG, "closing profile me")
        } catch (ee: Exception) {
            Log.d(MYTAG, "Failed to close local profile.", ee)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        sipAudioCall?.endCall()
        closeLocalProfile()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }

    private fun showSnackBar(s:String) {
        Snackbar.make(binding.root,s, Snackbar.LENGTH_LONG).show()
    }


}
