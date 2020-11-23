package app.adinfinitum.ello.ui.fragment_detail_contact


import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.*
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
import com.google.android.material.snackbar.Snackbar
import app.adinfinitum.ello.R
import app.adinfinitum.ello.databinding.FragmentDetailContactBinding
import app.adinfinitum.ello.model.PhoneItem
import app.adinfinitum.ello.model.RecentCall
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.isPhoneNumberValid
import app.adinfinitum.ello.utils.removePlus
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

        /*val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService

        val repo= RepoContacts(requireActivity().contentResolver,
                                database,
                                apiService,
                            resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
                                )*/

        viewModel=ViewModelProvider(this,DetailContactViewModelFactory(
                                                        args.contactLookUpKey,
                                                        (requireActivity().application as MyApplication).myContainer.repoContacts,
                                                        requireActivity().application))
                                    .get(DetailContactViewModel::class.java)


        phoneAdapter= DetailContactAdapter(
                SipItemClickListener{

                        if(checkForPermissions()){
                            viewModel.insertCallIntoDB(RecentCall(recentCallName = args.displayName,recentCallPhone = it,recentCallTime = System.currentTimeMillis()))
                            findNavController().navigate(DetailContactDirections.actionDetailContactToSipFragment(args.displayName,it))}


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
        phoneAdapter.notifyDataSetChanged()
    }


    fun makePrenumberPhoneCall(myphone:String){

       val normphone = PhoneNumberUtils.normalizeNumber(myphone)?.removePlus()
        if(normphone==null){
            viewModel.logStateOrErrorToMyServer(
                mapOf(
                        Pair("process","SIM Card Call from Contacs"),
                        Pair("error state","normalized number to call is NULL: $normphone")
                    )
            )
        }
        normphone?.let {phone->

            if (phone.isPhoneNumberValid()) {
                val phoneWithHash = phone.plus("#")

                val intentToCall = Intent(Intent.ACTION_CALL).apply {
                    val callingNumber = resources.getString(
                        R.string.prenumber_call,
                        myPrefixNumber,
                        Uri.encode(phoneWithHash)
                    )

                    setData(Uri.parse(callingNumber))

                    Log.i(MYTAG, "sim phone call uri :$callingNumber")

                    viewModel.logStateOrErrorToMyServer(
                        mapOf(
                            Pair("process","SIM Card Call from Contacs"),
                            Pair("state","calling number:$callingNumber")
                        )
                    )

                }

                if (intentToCall.resolveActivity(requireActivity().packageManager) != null) {
                    viewModel.insertCallIntoDB(
                        RecentCall(
                            recentCallName = args.displayName,
                            recentCallPhone = myphone,
                            recentCallTime = System.currentTimeMillis()
                        )
                    )
                    startActivity(intentToCall)
                } else showSnackBar(resources.getString(R.string.unable_to_make_call))

            } else showSnackBar(resources.getString(R.string.not_valid_phone_number))
        }


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
