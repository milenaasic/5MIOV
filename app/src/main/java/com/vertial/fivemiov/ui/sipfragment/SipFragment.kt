package com.vertial.fivemiov.ui.sipfragment


import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
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
        binding.nametextView.text=args.contactName

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao

        viewModel = ViewModelProvider(this, SipViewModelFactory(database,requireActivity().application))
            .get(SipViewModel::class.java)

        binding.sipendbutton.setOnClickListener{
            closeLocalProfile()
            findNavController().navigateUp()

        }

        initializeManager()

        requireActivity().getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
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



    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.dialPadFragment).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.menu_item_logout).isVisible=false
    }

    private fun initializeManager() {
        sipManager=SipManager.newInstance(requireContext())
        initializeLocalProfile()

    }

    private fun initializeLocalProfile() {

        if(sipManager==null)return


        if(me!=null) {
            Log.i(MYTAG,"me nije null, zatvara se profil")
            closeLocalProfile()
        }

        /*val user: User?=viewModel.getSipAccountInfo()
        Log.i(MYTAG,"user je $user")*/


        Log.i(MYTAG,"sipManager je $sipManager")
        //val mysipProfileBuilder = SipProfile.Builder("7936502090", "45.63.117.19").setPassword("rasa123321")
        val mysipProfileBuilder = SipProfile.Builder("milena", "iptel.org").setPassword("Milena77")
        me=mysipProfileBuilder.build()
        Log.i(MYTAG,"me je ${me?.uriString}")

        val peer=SipProfile.Builder("ankeanke","iptel.org")
        peersipProfile=peer.build()
        Log.i(MYTAG,"peer je ${peersipProfile?.uriString}")
        
        try {
            sipManager.open(me)
            updateCallStatus("Opening connection...")
            Log.i(MYTAG," open ")
        }catch (s:SipException) {
            updateCallStatus("open, ${s.message }, ${s.cause}")
            Log.i(MYTAG," open greska ${s.stackTrace}, ${s.cause}")
        }
        Log.i(MYTAG,"da li je otvoren ${sipManager.isOpened(me?.uriString)}")

        viewModel.startRegTimeout()

        Log.i(MYTAG,"is opened ${sipManager.isOpened(me?.uriString)}")
        Log.i(MYTAG,"is registered ${sipManager.isRegistered(me?.uriString)}")
        Log.i(MYTAG,"podaci ${me?.sipDomain},${me?.password},${me?.userName}")

    }

    private fun register() {
        sipManager.register(me,10,object: SipRegistrationListener {
            override fun onRegistering(p0: String?) {
                Log.i(MYTAG,"registgering $p0")
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("on registering..")

                })
            }

            override fun onRegistrationDone(p0: String?, p1: Long) {
                Log.i(MYTAG,"registration done $p0, $p1")
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("registration DONE")
                   viewModel.startTimeout()
                })
            }

            override fun onRegistrationFailed(p0: String?, p1: Int, p2: String?) {
                Log.i(MYTAG,"registration FAILED $p0, $p1,$p2")
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("registration FAILED")
                    viewModel.startRegTimeout()
                    })
            }

        })
    }

    private fun makeSipAudioCall() {
        sipManager.makeAudioCall(me,peersipProfile,object: SipAudioCall.Listener(){

            override fun onCalling(call: SipAudioCall?) {
                super.onCalling(call)
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("calling..")
                 })

            }

            override fun onCallEstablished(call: SipAudioCall?) {
                super.onCallEstablished(call)
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("cal established..")
                })
                call?.startAudio()
                //call?.setSpeakerMode(true)
                if(call?.isMuted==true) call?.toggleMute()


            }

            override fun onError(call: SipAudioCall?, errorCode: Int, errorMessage: String?) {
                super.onError(call, errorCode, errorMessage)
                // code -2 je decline
                val h=Handler(Looper.getMainLooper())
                h.post(Runnable {
                    updateCallStatus("error, ${call.toString()}, code $errorCode, message $errorMessage")
                    call?.close()
                })
                //updateCallStatus("something went wrong...")
                Log.i(MYTAG,"error, ${call.toString()}, code $errorCode, message $errorMessage")
            }


        },20)
    }


    fun updateCallStatus(status: String?) {
            binding.statustextView.text=status
    }

    override fun onDestroyView() {
        super.onDestroyView()
        closeLocalProfile()
    }


    fun closeLocalProfile() {
        try {
            //ugasi call ako je u toku
            sipManager?.close(me?.uriString)
            Log.d(MYTAG, "closing profile me")
        } catch (ee: Exception) {
            Log.d(MYTAG, "Failed to close local profile.", ee)
        }
    }


    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }




    private fun showSnackBar(s:String) {
        Snackbar.make(binding.root,s, Snackbar.LENGTH_LONG).show()
    }



}
