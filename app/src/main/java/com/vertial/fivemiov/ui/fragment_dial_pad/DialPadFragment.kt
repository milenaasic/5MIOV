package com.vertial.fivemiov.ui.fragment_dial_pad


import android.Manifest
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneNumberFormattingTextWatcher
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.TypedValue
import android.view.*
import android.view.accessibility.AccessibilityEvent
import android.widget.Button
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentContainerView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.RepoContacts
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentDialPadBinding
import com.vertial.fivemiov.model.RecentCall
import com.vertial.fivemiov.ui.initializeSharedPrefToFalse
import com.vertial.fivemiov.ui.myapplication.MyApplication
import com.vertial.fivemiov.utils.*
import kotlin.math.roundToInt


private val MYTAG="MY_DialPadFragment"
class DialPadFragment : Fragment() {

    private lateinit var binding: FragmentDialPadBinding
    private lateinit var viewModel: DialpadFragmViewModel
    private lateinit var myPrenumber:String
    private lateinit var myRecentCallList:List<RecentCall>
    private lateinit var clipboard:ClipboardManager
    private var currentCursorPosition=0

    private lateinit var bsb:BottomSheetBehavior<FragmentContainerView>

    companion object{
        val MY_PERMISSIONS_REQUEST_MAKE_CALL_and_SIP_and_AUDIO_DIALPAD=11
        val FORMATTING_COUNTRY_CODE="US"
        val NOTHING=""
        val MAX_CHARS_ALLOWED_IN_LAYOUT=25

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_dial_pad,container,false)

        val database=MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService=MyAPI.retrofitService
        val repo=RepoContacts(  requireActivity().contentResolver,
                                database,
                                apiService,
                                resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
        )


        viewModel = ViewModelProvider(this, DialpadFragmentViewModelFactory(repo,requireActivity().application))
            .get(DialpadFragmViewModel::class.java)

        binding.apply {

            button1.setOnClickListener { appendDigit((it as Button).text) }
            button2.setOnClickListener { appendDigit((it as Button).text) }
            button3.setOnClickListener { appendDigit((it as Button).text) }
            button4.setOnClickListener { appendDigit((it as Button).text) }
            button5.setOnClickListener { appendDigit((it as Button).text) }
            button6.setOnClickListener { appendDigit((it as Button).text) }
            button7.setOnClickListener { appendDigit((it as Button).text) }
            button8.setOnClickListener { appendDigit((it as Button).text) }
            button9.setOnClickListener { appendDigit((it as Button).text) }
            button10.setOnClickListener { appendDigit((it as Button).text) }
            button11.setOnClickListener { appendDigit((it as Button).text) }
            //erase
            button12.setOnClickListener { deleteDigit() }

            // prenumber call
            buttonPrenumberCallDialpad.setOnClickListener { if(checkForPermissions()) {
                                                                                        binding.editTextEnterNumber.isCursorVisible=false
                                                                                        makePhoneCall()
                                                                                        }
                                                            }

            // sip call
            buttonSipCallDialpadFrag.setOnClickListener {

                binding.editTextEnterNumber.isCursorVisible=false

                if(isVOIPsupported(requireContext())){
                        enableCallButtons(false)
                        val phoneNumber=binding.editTextEnterNumber.text.toString()
                        val normPhoneNumber=PhoneNumberUtils.normalizeNumber(phoneNumber)
                        if(checkForPermissions()) {
                            if(normPhoneNumber.isPhoneNumberValid()) {
                                viewModel.insertCallIntoDB(RecentCall(recentCallName = phoneNumber,recentCallPhone = phoneNumber,recentCallTime = System.currentTimeMillis()))
                                findNavController().navigate(DialPadFragmentDirections.actionDialPadFragmentToSipFragment(normPhoneNumber,normPhoneNumber))
                            }
                            else {
                                showSnackBar(resources.getString(R.string.not_valid_phone_number))
                                enableCallButtons(true)
                            }
                        }

                } else  showSnackBar(resources.getString(R.string.VOIP_not_supported))

             }

         }


         binding.editTextEnterNumber.apply {
             setShowSoftInputOnFocus(false)
             addTextChangedListener(PhoneNumberFormattingTextWatcher(FORMATTING_COUNTRY_CODE))
             addTextChangedListener { afterTextChanged {
                 val start=binding.editTextEnterNumber.selectionStart
                 val end=binding.editTextEnterNumber.selectionEnd
                 if(start==end)
                     Log.i(MYTAG, " after text changed, start $start, end $end")
                 currentCursorPosition=start
              } }

          }

          binding.editTextEnterNumber.setOnClickListener {

              if(binding.editTextEnterNumber.length()!=0) {
                                    binding.editTextEnterNumber.isCursorVisible=true
                  val start=binding.editTextEnterNumber.selectionStart
                  val end=binding.editTextEnterNumber.selectionEnd
                  if(start==end)
                      Log.i(MYTAG, " onclicklistener, start $start, end $end")
                  currentCursorPosition=start
              }

           }

        binding.setEmailAndPassButton.setOnClickListener{
            findNavController().navigate(DialPadFragmentDirections.actionDialPadFragmentToSetEmailAndPasswordFragment())
        }

        registerForContextMenu(binding.editTextEnterNumber)


        configureBottomSlidePanel()

        setRecentCallsFragmentHeight()


        return binding.root
    }

    private fun configureBottomSlidePanel() {


        bsb=BottomSheetBehavior.from(binding.recentcallsFragment)
        bsb.state = BottomSheetBehavior.STATE_HIDDEN


    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.myPrenumber.observe(viewLifecycleOwner, Observer {
            if(it!=null) myPrenumber=it
        })

        viewModel.userData.observe(viewLifecycleOwner, Observer {user->
           if(user!=null) {
               Log.i(MYTAG, " user from DB: $user")
               if (user.userEmail == EMPTY_EMAIL) binding.setEmailAndPassButton.visibility =
                   View.VISIBLE
               else binding.setEmailAndPassButton.visibility = View.GONE
           }
       })

        viewModel.recentCallList.observe(viewLifecycleOwner, Observer {
            if(it!=null) myRecentCallList=it
        })


        viewModel.getCreditNetSuccess.observe(viewLifecycleOwner, Observer {response->

             if(response!=null){
                if(response.success==true && response.credit.isNotEmpty() && response.currency.isNotEmpty()){
                    binding.creditTextView.text=resources.getString(R.string.current_credit,response.credit,response.currency)

                }
                 viewModel.resetGetCreditNetSuccess()

            }

         })

         viewModel.getCreditNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null) viewModel.resetGetCreditNetErrorr()

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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        clipboard= requireActivity().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager

    }

    override fun onStart() {
        super.onStart()
        binding.dialpadConstrLayout.requestFocus()
        if(isOnline(requireActivity().application)) viewModel.getCredit()
        else binding.creditTextView.text="No internet"

    }

    private fun appendDigit(char:CharSequence){
        binding.editTextEnterNumber.apply {

            text.insert(this.getSelectionStart(), char)
            isCursorVisible=true
         }
    }

    private fun deleteDigit(){

        val charCount:Int = binding.editTextEnterNumber.length()

        when (charCount){
            0->{}
            1->{ binding.editTextEnterNumber.text.delete(charCount - 1, charCount)
                binding.editTextEnterNumber.isCursorVisible=false}
            else->{
                if(currentCursorPosition!=0 && charCount>=currentCursorPosition)  binding.editTextEnterNumber.text.delete(currentCursorPosition - 1, currentCursorPosition)
            }

        }

    }

    private fun enableCallButtons(b:Boolean){
        if(b) {
                binding.buttonPrenumberCallDialpad.isEnabled=true
                binding.buttonSipCallDialpadFrag.isEnabled=true
        }else {
            binding.buttonPrenumberCallDialpad.isEnabled=false
            binding.buttonSipCallDialpadFrag.isEnabled=false
        }

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.dial_pad_menu,menu)
        //menu.findItem(R.id.menuItemRecentCalls).isVisible=false

    }

    override fun onCreateContextMenu(
        menu: ContextMenu,
        v: View,
        menuInfo: ContextMenu.ContextMenuInfo?
    ) {
        super.onCreateContextMenu(menu, v, menuInfo)
        val inflater: MenuInflater = requireActivity().menuInflater
        inflater.inflate(R.menu.enter_phone_number_context_menu, menu)
    }


    override fun onContextItemSelected(item: MenuItem): Boolean {

        return when (item.itemId) {
            R.id.menuPaste -> {
                val item = clipboard.primaryClip?.getItemAt(0)
                pastePhoneNumber(item?.text.toString())
                true
            }

            else -> super.onContextItemSelected(item)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when (item.itemId){
            R.id.menuItemRecentCalls->{
                if(!myRecentCallList.isNullOrEmpty()) {
                        if(bsb.state==BottomSheetBehavior.STATE_HIDDEN)  bsb.state = BottomSheetBehavior.STATE_EXPANDED
                        else bsb.state = BottomSheetBehavior.STATE_HIDDEN
                }else showSnackBar(resources.getString(R.string.no_recent_calls))
                return true
            }
        else->return super.onOptionsItemSelected(item)
        }
    }

    fun pastePhoneNumber(pasteData:String) {

        bsb.state = BottomSheetBehavior.STATE_HIDDEN
        if(!pasteData.isNullOrEmpty() && pasteData.isNotBlank() && pasteData.length<= MAX_CHARS_ALLOWED_IN_LAYOUT){
            val pasteDataNormalized=PhoneNumberUtils.normalizeNumber(pasteData.toString())

            if(pasteDataNormalized.isPhoneNumberValid()){
                binding.editTextEnterNumber.apply {
                    text=Editable.Factory.getInstance().newEditable(pasteData)
                    currentCursorPosition=pasteData.lastIndex+1
                    setSelection(pasteData.lastIndex+1)
                }
            }

        } else showSnackBar(resources.getString(R.string.clipboard_invalid_data_type))
    }

    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
                enableCallButtons(true)
                return true
        }
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.USE_SIP) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
            ) {
                requestPermissions(arrayOf(Manifest.permission.CALL_PHONE,Manifest.permission.USE_SIP,Manifest.permission.RECORD_AUDIO),
                    MY_PERMISSIONS_REQUEST_MAKE_CALL_and_SIP_and_AUDIO_DIALPAD
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
            MY_PERMISSIONS_REQUEST_MAKE_CALL_and_SIP_and_AUDIO_DIALPAD-> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(MYTAG,"grantResults 0  ${grantResults[0]}")
                    } else {
                        showSnackBar(resources.getString(R.string.no_permission_make_phone_call))
                    }


                    if (grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(MYTAG,"grantResults 1  ${grantResults[1]}")
                    } else {
                        showSnackBar(resources.getString(R.string.no_SIP_permission))
                    }

                    if (grantResults[2] == PackageManager.PERMISSION_GRANTED) {
                        Log.i(MYTAG,"grantResults 2 audio  ${grantResults[2]}")
                    } else {
                        showSnackBar(resources.getString(R.string.no_audio_permission))
                    }
                }
                enableCallButtons(true)
                return
            }

            else -> {
                enableCallButtons(true)
            }
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    private fun makePhoneCall() {
        val enteredPhone=binding.editTextEnterNumber.text.toString()
        val phone = PhoneNumberUtils.normalizeNumber(enteredPhone)

        if (phone.isPhoneNumberValid()) {

            val intentToCall = Intent(Intent.ACTION_CALL).apply {
                setData(Uri.parse(resources.getString(R.string.prenumber_call,myPrenumber,phone)))
                Log.i(MYTAG, "uri is ${resources.getString(R.string.prenumber_call,myPrenumber,phone)} ")
            }

            if (intentToCall.resolveActivity(requireActivity().packageManager) != null) {
                viewModel.insertCallIntoDB(RecentCall(recentCallName = enteredPhone,recentCallPhone = enteredPhone,recentCallTime = System.currentTimeMillis()))
                startActivity(intentToCall)
            } else showSnackBar(resources.getString(R.string.unable_to_make_call))

        } else showSnackBar(resources.getString(R.string.not_valid_phone_number))

    }

    private fun setRecentCallsFragmentHeight(){
        val dialPadHeight=binding.dialNumbersLayout.layoutParams.height
        Log.i(MYTAG," dialpad height: $dialPadHeight, screenDensity je ${resources.displayMetrics.density}")
        val params=binding.recentcallsFragment.layoutParams
        params.height=calculateRecentCallsFragmentHeight()
        binding.recentcallsFragment.layoutParams=params

    }



    fun calculateRecentCallsFragmentHeight():Int{

        val guidelinePositionInLayout=0.7
       val screenHeight=resources.displayMetrics.heightPixels

        var actionBarHeight = 56*resources.displayMetrics.density.toInt()
        val statusBarHeightDP = 30
        val typedValue = TypedValue()
        if (requireActivity().theme.resolveAttribute(
                android.R.attr.actionBarSize,
                typedValue,
                true
            )
        ) {
            actionBarHeight =
                TypedValue.complexToDimensionPixelSize(typedValue.data, resources.displayMetrics)
        }

        Log.i(MYTAG,"density is ${resources.displayMetrics.density}, a actionbar height $actionBarHeight")

        val dialPadFragHeight=screenHeight-actionBarHeight-statusBarHeightDP*resources.displayMetrics.density

        val percentOfReturnHeight=guidelinePositionInLayout*dialPadFragHeight

        return percentOfReturnHeight.roundToInt()
    }


    private fun showSnackBar(s:String) {
        Snackbar.make(binding.coordLayDialpadFragment,s, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

}
