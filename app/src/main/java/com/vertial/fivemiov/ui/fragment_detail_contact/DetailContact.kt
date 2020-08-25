package com.vertial.fivemiov.ui.fragment_detail_contact


import android.Manifest
import android.animation.ObjectAnimator
import android.animation.PropertyValuesHolder
import android.annotation.SuppressLint
import android.annotation.TargetApi
import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.*
import android.net.sip.SipManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.telephony.PhoneNumberUtils
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentDetailContactBinding
import com.vertial.fivemiov.model.PhoneItem
import com.vertial.fivemiov.model.RecentCall
import com.vertial.fivemiov.ui.fragment_dial_pad.DialPadFragment
import com.vertial.fivemiov.ui.fragment_main.MainFragment
import com.vertial.fivemiov.ui.myapplication.MyApplication
import com.vertial.fivemiov.utils.isPhoneNumberValid
import com.vertial.fivemiov.utils.isVOIPsupported
import java.util.*
import java.util.Locale.US

private val MYTAG="MY_DETAIL_CONTACT_FRAGM"

class DetailContact : Fragment() {

    private lateinit var binding: FragmentDetailContactBinding
    private lateinit var viewModel: DetailContactViewModel
    private lateinit var args:DetailContactArgs
    private lateinit var phoneAdapter:DetailContactAdapter

    private lateinit var myPrefixNumber:String

    private var br: BroadcastReceiver?=null

    companion object{
        val MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL_and_AUDIO=15

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        args = DetailContactArgs.fromBundle(requireArguments())
        setHasOptionsMenu(true)
        getNetworkStatusChangedInfo()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding=DataBindingUtil.inflate(inflater,R.layout.fragment_detail_contact,container,false)

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService

        val repo= RepoContacts(requireActivity().contentResolver,
                                database,
                                apiService,
                            resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
                                )

        viewModel=ViewModelProvider(this,DetailContactViewModelFactory(args.contactLookUpKey,repo,requireActivity().application)).get(DetailContactViewModel::class.java)


        phoneAdapter= DetailContactAdapter(
                SipItemClickListener{

                    if(isVOIPsupported(requireContext())){
                        if(checkForPermissions()){
                            viewModel.insertCallIntoDB(RecentCall(recentCallName = args.displayName,recentCallPhone = it,recentCallTime = System.currentTimeMillis()))
                            findNavController().navigate(DetailContactDirections.actionDetailContactToSipFragment(args.displayName,it))}
                    }
                    else  showSnackBar(resources.getString(R.string.VOIP_not_supported))
                },
                PrenumberItemClickListener(requireActivity()) {activity, phone ->
                        if(checkForPermissions()) makePrenumberPhoneCall(phone)
                    },
                requireActivity().application
                )


        binding.detailContactRecView.adapter=phoneAdapter
        binding.detailContactRecView.addItemDecoration(DividerItemDecoration(requireContext(),DividerItemDecoration.VERTICAL))


        binding.displayNameTextView.text=args.displayName

        checkForPermissions()

        //requireActivity().window.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        return binding.root

    }



    fun networkAvailabiltyChanged() {
        Log.i(MYTAG,"funkcija networkAvailabiltyChanged ")
        phoneAdapter.notifyDataSetChanged()
    }


    fun makePrenumberPhoneCall(myphone:String){


       val phone = PhoneNumberUtils.normalizeNumber(myphone)

       if (phone.isPhoneNumberValid()) {
           val intentToCall = Intent(Intent.ACTION_CALL).apply {
               setData(Uri.parse(resources.getString(R.string.prenumber_call,myPrefixNumber,phone)))

           }

           if (intentToCall.resolveActivity(requireActivity().packageManager) != null) {
                viewModel.insertCallIntoDB(RecentCall(recentCallName = args.displayName,recentCallPhone = myphone,recentCallTime = System.currentTimeMillis()))
               startActivity(intentToCall)
           } else showSnackBar(resources.getString(R.string.unable_to_make_call))

       } else showSnackBar(resources.getString(R.string.not_valid_phone_number))


   }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.phoneList.observe(viewLifecycleOwner, Observer {list->
                if(list!=null){

                    val formatedNumbersList= mutableListOf<PhoneItem>()
                    for (item in list) {
                        formatedNumbersList.add (
                                PhoneItem(
                                    (PhoneNumberUtils.formatNumber(item.phoneNumber, US.toString()))?:item.phoneNumber,
                                    item.phoneType,
                                    item.photoUri
                                    )
                        )
                    }
                    Log.i(MYTAG," user phone list: $formatedNumbersList")
                    phoneAdapter.dataList=formatedNumbersList
                }

         })

         viewModel.prefixNumber.observe(viewLifecycleOwner, Observer {
                if(it!=null) myPrefixNumber=it
          })
    }

    override fun onStart() {
        super.onStart()

    }

    override fun onResume() {
        super.onResume()
        phoneAdapter.notifyDataSetChanged()
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
        menu.findItem(R.id.menu_item_share).isVisible=false
        menu.findItem(R.id.menu_item_myaccount).isVisible=false
        menu.findItem(R.id.aboutFragment).isVisible=false
    }

    private fun showSnackBar(s:String) {
        Snackbar.make(binding.detailFragmnLinearLayout,s, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }


    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE,Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL_and_AUDIO
                )
                return false
            } else return true
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {

        when (requestCode) {
            MY_PERMISSIONS_REQUEST_MAKE_PHONE_CALL_and_AUDIO-> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        showSnackBar(resources.getString(R.string.no_permission_make_phone_call))
                    }


                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {

                    } else {
                        showSnackBar(resources.getString(R.string.no_audio_permission))
                    }
                }

                return
            }

            else -> { }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    private fun getNetworkStatusChangedInfo() {

        val connMgr =
            requireActivity().getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val networkRequest = NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            connMgr.registerNetworkCallback(networkRequest,object : ConnectivityManager.NetworkCallback() {

                override fun onAvailable(network: Network) {
                    val h = Handler(Looper.getMainLooper())
                    h.post {
                        this@DetailContact.networkAvailabiltyChanged()
                        Log.i(MYTAG, "network capability avail ${network}")
                    }
                }


                override fun onLost(network: Network) {
                    if (connMgr.activeNetwork==null) {
                         val h = Handler(Looper.getMainLooper())
                         h.post { this@DetailContact.networkAvailabiltyChanged()
                             Log.i(MYTAG, "network capability LOST ${network}")}
                    }
                }
            })

        }

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.N) {

        br = object : BroadcastReceiver() {

        override fun onReceive(p0: Context?, intent: Intent?) {
        if (intent != null) {
            if (intent.getExtras() != null) {
                val ni = intent.getExtras()


                val info:NetworkInfo=ni?.get(ConnectivityManager.EXTRA_NETWORK_INFO) as NetworkInfo
                if (info != null && info?.state == NetworkInfo.State.CONNECTED) {
                    Log.i(MYTAG, "Network " + info.getTypeName() + " connected")
                    networkAvailabiltyChanged()
                }
            }

            if(intent.getBooleanExtra(ConnectivityManager.EXTRA_NO_CONNECTIVITY,false)) {
                Log.i(MYTAG, "There's no network connectivity ")
                networkAvailabiltyChanged()
                }
        }


        }
        }

        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        requireActivity().registerReceiver(br, filter)


        }

    }

        override fun onDestroy() {
        super.onDestroy()
        if(br!=null) requireActivity().unregisterReceiver(br)
        }



}
