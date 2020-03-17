package com.vertial.fivemiov.ui.sipfragment


import android.net.sip.SipManager
import android.net.sip.SipProfile
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentSipBinding


private val MYTAG="MY_Sip fragment"
class SipFragment : Fragment() {

    private lateinit var binding:FragmentSipBinding
    private lateinit var args:SipFragmentArgs

    val sipManager: SipManager? by lazy(LazyThreadSafetyMode.NONE) {
        SipManager.newInstance(context)
    }
    private var sipProfile: SipProfile? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args= SipFragmentArgs.fromBundle(arguments!!)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_sip,container,false)
        binding.nametextView.text=args.contactName

        val builder = SipProfile.Builder("username", "domain")
            .setPassword("password")

        sipProfile=builder.build()


        isVOIPsupported()

        return binding.root
    }




    fun isVOIPsupported(){
        Log.i(MYTAG," is voip supoported ${SipManager.isVoipSupported(context)}")
        Log.i(MYTAG," is sip api supoported ${SipManager.isApiSupported(context)}")

    }





    fun closeLocalProfile() {
        try {
            sipManager?.close(sipProfile?.uriString)
        } catch (ee: Exception) {
            Log.d(MYTAG, "Failed to close local profile.", ee)
        }
    }


}
