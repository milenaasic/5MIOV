package com.vertial.fivemiov.ui.registrationauthorization


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.text.Layout
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentAuthorizationBinding
import com.vertial.fivemiov.utils.*


private val MYTAG="MY_AuthorizationFragm"
class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel
    private  var telephonyManager:TelephonyManager?=null
    private  var callStateListener: PhoneStateListener?=null
    private val VERIFIED_BY_CALL="verifiedByCall"
    private val webAPPIsMakingCall_Code="55"
    private val TIME_TO_WAIT_FOR_CALL=8000L



    companion object{
        val MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_and_READ_CALL_LOG=20

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_authorization,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }




        when(isVerificationByCallEnabled()){
            true->{
                //show progress bar
                showProgressBar(true)
                binding.apply {
                    noCallReceivedExplanation.visibility=View.VISIBLE
                    resendSmsButton.text=resources.getString(R.string.send_sms)
                    noSmsOrCallText.text=resources.getString(R.string.no_call_try_sms)
                }

            }

            false->{

            }


        }

        binding.authPhoneTextView.text=String.format(resources.getString(R.string.add_plus_before_phone,activityViewModel.enteredPhoneNumber))

        binding.submitButton.setOnClickListener {
            hidekeyboard()

            binding.authorizationRootLayout.requestFocus()
            if(!isOnline(requireActivity().application)) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener}


            if(binding.tokenEditText.text.toString().isBlank()){
                binding.tokenTextInputLayout.setError(resources.getString(R.string.enter_token))

            } else {
                enableDisableButtons(false)
                showProgressBar(true)
                activityViewModel.submitButtonClicked(binding.tokenEditText.text.toString())
            }


        }

        binding.tokenEditText.setOnEditorActionListener { view, action, _ ->
            when (action){
                EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
                    hidekeyboard()
                    view.clearFocus()

                    true
                }
                else->false
            }
        }

        binding.resendSmsButton.setOnClickListener{
            hidekeyboard()
            binding.authorizationRootLayout.requestFocus()
            enableDisableButtons(false)

            showProgressBar(true)
            when (isVerificationByCallEnabled()){
                true->{
                    activityViewModel.startSMSRetreiverFunction()
                     verifyBySMSorCallAgain(VERIFICATION_METHOD_SMS)
                }
                false->{
                    activityViewModel.startSMSRetreiverFunction()
                    verifyBySMSorCallAgain(VERIFICATION_METHOD_SMS)
                }

            }


        }


        binding.tokenEditText.apply {
            afterTextChanged { binding.tokenTextInputLayout.error=null }
            setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) binding.tokenTextInputLayout.error=null

            }
        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.authorizationNetworkSuccess.observe(viewLifecycleOwner, Observer {response->
            if(response!=null){

                activityViewModel.resetAuthorization_NetSuccess()
                showProgressBar(false)
                enableDisableButtons(true)
                //response.userMessage.let { showToast(it) }

            }
        })

        activityViewModel.authorizationNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                activityViewModel.resetAuthorization_NetError()
                showProgressBar(false)
                enableDisableButtons(true)
                showSnackBar(resources.getString(R.string.something_went_wrong))
            }
        })

        activityViewModel.smsResendNetworkSuccess.observe(viewLifecycleOwner, Observer {
            Log.i(MYTAG," sms resend success , value $it")
            if(it!=null){
                activityViewModel.resetSMSResend_NetSuccess()
                if(it.startsWith(webAPPIsMakingCall_Code)){
                    showToast(it.substring(2))

                }else{
                    showToast(it.substring(2))
                    showProgressBar(false)
                    enableDisableButtons(true)
                }
            }
        })

        activityViewModel.smsResendNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                Log.i(MYTAG," sms resend error , value $it")
                activityViewModel.resetSMSResend_NetError()
                showSnackBar(resources.getString(R.string.something_went_wrong))
                showProgressBar(false)
                enableDisableButtons(true)
            }
        })


        //SMS verification token
        activityViewModel.verificationTokenForAuthFragment.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                binding.tokenEditText.setText(it)
                activityViewModel.resetSMSVerificationTOkenForAuthFragToNull()

            }

        })


    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        when(isVerificationByCallEnabled()){
            true-> {
                //initalize call listener
                telephonyManager =
                    requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager
                callStateListener = object : PhoneStateListener() {
                    override fun onCallStateChanged(state: Int, incomingNumber: String) {

                        //  React to incoming call.
                        Log.i(MYTAG, "state $state,  $incomingNumber")
                        // If phone ringing
                        if (state == TelephonyManager.CALL_STATE_RINGING) {
                            Log.i(MYTAG, "ringing $state,  $incomingNumber")
                            // Toast.makeText(requireContext(), "Phone is Ringing", Toast.LENGTH_LONG).show()
                            // call create route
                            val numberToAwait =
                                (requireActivity() as RegistrationAuthorizationActivity).verificationCallerId
                            Log.i(MYTAG, "number to await is $numberToAwait")
                            if (incomingNumber.equals(numberToAwait)) {
                                activityViewModel.submitButtonClicked(VERIFIED_BY_CALL)
                                telephonyManager?.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
                                }
                        }
                        // If incoming call received
                        /*if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                            //Toast.makeText(requireContext(), "Phone is Currently in A call", Toast.LENGTH_LONG).show()
                        }
                        if (state == TelephonyManager.CALL_STATE_IDLE) {
                            //Toast.makeText(requireContext(), "phone is neither ringing nor in a call", Toast.LENGTH_LONG).show()
                        }*/
                    }
                }

                telephonyManager?.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)
                startFirstCountDownTimer()
            }

        }
    }

    private fun verifyBySMSorCallAgain(verificationMethod:String){

        //resending sms via signIn route
        Log.i(MYTAG,"call verification button (ex Resend) ${activityViewModel.enteredPhoneNumber}, ${activityViewModel.enteredEmail}, ${activityViewModel.enteredPassword}, ${activityViewModel.signInParameter}")
        when{
            //came from registrtion fragment
            activityViewModel.enteredEmail==null && activityViewModel.signInParameter==null->activityViewModel.registerButtonClicked(
                                                                                activityViewModel.enteredPhoneNumber?.removePlus()?:"",
                                                                                    smsResend = true,
                                                                                    verificationMethod = verificationMethod)

            //came from AddNumberToAccount fragment
            activityViewModel.enteredEmail!=null && activityViewModel.signInParameter==null->activityViewModel.addNumberToAccountButtonClicked(
                activityViewModel.enteredPhoneNumber?.removePlus()?:"",
                activityViewModel.enteredEmail?:"",
                activityViewModel.enteredPassword?:"",
                smsResend = true,
                verificationMethod = verificationMethod)

            //came from NumberExistsInDatabase, create new account
            activityViewModel.enteredEmail==null && activityViewModel.signInParameter==false->activityViewModel.numberExistsInDb_NoAccount(smsResend = true,verificationMethod = verificationMethod)

            //came from NumberExistsInDatabase, verify account
            activityViewModel.enteredEmail!=null && activityViewModel.signInParameter==true->activityViewModel.numberExistsInDBVerifyAccount(
                activityViewModel.enteredEmail?:"",
                activityViewModel.enteredPassword?:"",
                smsResend = true,
                verificationMethod = verificationMethod)

        }



    }

    override fun onStart() {
        super.onStart()

    }



    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }

    private fun hidekeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun showProgressBar(bool:Boolean){
        if(bool){
            binding.authorizationRootLayout.alpha=0.2f
            binding.authProgressBar.visibility=View.VISIBLE
        }else{
            binding.authorizationRootLayout.alpha=1f
            binding.authProgressBar.visibility=View.GONE
        }

    }

    private fun enableDisableButtons(b:Boolean){
        if(b){
                 binding.apply {
                     submitButton.isEnabled = true
                     resendSmsButton.isEnabled = true
                 }
         }else{
                binding.apply {
                    submitButton.isEnabled = false
                    resendSmsButton.isEnabled = false

                 }


         }

    }

    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED

            ) {
                requestPermissions(arrayOf(
                    Manifest.permission.READ_PHONE_STATE,
                    Manifest.permission.READ_CALL_LOG),
                    MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_and_READ_CALL_LOG
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
            MY_PERMISSIONS_REQUEST_READ_PHONE_STATE_and_READ_CALL_LOG-> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        showProgressBar(true)
                        verifyBySMSorCallAgain(VERIFICATION_METHOD_CALL)

                    }else {
                        showSnackBar("Unable to verify account by phone call - permissions not granted")
                        enableDisableButtons(true)
                    }

                    //if (grantResults[0] != PackageManager.PERMISSION_GRANTED) showSnackBar("Need READ PHONE STATE permission to verify your account by phone call")

                    //if (grantResults[1] != PackageManager.PERMISSION_GRANTED) showSnackBar("Need READ CALL LOG permission to verify your account by phone call")

                }

                return
            }

            else -> {  }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        if(isVerificationByCallEnabled()) telephonyManager?.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }

    private fun startFirstCountDownTimer(){
        Log.i(MYTAG,"startFirstCountDownTimer() Timer started")
        object:CountDownTimer(TIME_TO_WAIT_FOR_CALL,TIME_TO_WAIT_FOR_CALL){
            override fun onTick(millisUntilFinished: Long) {
                Log.i(MYTAG,"first timer:$millisUntilFinished")
            }

            override fun onFinish() {
                Log.i(MYTAG,"first timer:finished")
                showDialogToConfirmUserNunber()
            }
        }.start()

    }

    private fun startSecondCoundDownTImer(){
        object:CountDownTimer(TIME_TO_WAIT_FOR_CALL,TIME_TO_WAIT_FOR_CALL){
            override fun onTick(millisUntilFinished: Long) {
                Log.i(MYTAG,"second timer:$millisUntilFinished")
            }

            override fun onFinish() {
                Log.i(MYTAG,"second timer:finished")
                //both calls failed  show Authorization Fragment
                showProgressBar(false)
                showSnackBar("Unable to make a call, try SMS!")
            }
        }.start()

    }


    private fun isVerificationByCallEnabled():Boolean{
        return (requireActivity() as RegistrationAuthorizationActivity).verificationByCallEnabled
    }

    private fun showDialogToConfirmUserNunber(){
        val alertDialog: AlertDialog? = activity?.let {

            val builder = AlertDialog.Builder(it)

            // Add action buttons
            builder
                .setTitle("Is this your phone number ${activityViewModel.enteredPhoneNumber} ?")
                .setPositiveButton("YES",
                    DialogInterface.OnClickListener { dialog, id ->
                        verifyBySMSorCallAgain(VERIFICATION_METHOD_EXPENSIVE_CALL)
                        startSecondCoundDownTImer()
                    })
                .setNegativeButton("NO",
                    DialogInterface.OnClickListener { dialog, id ->

                        dialog.cancel()
                        showProgressBar(false)
                        showSnackBar(resources.getString(R.string.enter_correct_number))
                    })
                .setCancelable(false)

            builder.create()

        }

        alertDialog?.show()


    }



    /*private fun makeListOfVerificationCallerIds(verifivationCallerId:String):List<String>{
        return (verifivationCallerId as CharSequence).split(",")
    }*/

    /*private fun checkIfCurrentCallIdIsInList(callIdsList:List<String>):Boolean{
        var isInList=false
        for (item in callIdsList){
            if()
        }
        return isInList
    }*/

}
