package com.vertial.fivemiov.ui.registrationauthorization


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
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
import com.vertial.fivemiov.utils.TERMS_OF_USE
import com.vertial.fivemiov.utils.afterTextChanged
import com.vertial.fivemiov.utils.isOnline
import com.vertial.fivemiov.utils.removePlus


private val MYTAG="MY_AuthorizationFragm"
class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel
    private lateinit var telephonyManager:TelephonyManager
    private lateinit var callStateListener: PhoneStateListener
    private val VERIFIED_BY_CALL="verifiedByCall"
    private val OUR_VERIFICATION_NUMBER_1="018888420"
    private val OUR_VERIFICATION_NUMBER_2="+23418888420"


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

        telephonyManager = requireActivity().getSystemService(Context.TELEPHONY_SERVICE) as TelephonyManager

         callStateListener = object : PhoneStateListener() {
            override fun onCallStateChanged(state: Int, incomingNumber: String) {

                //  React to incoming call.
                Log.i(MYTAG,"state $state,  $incomingNumber")
                // If phone ringing
                if (state == TelephonyManager.CALL_STATE_RINGING) {
                    Log.i(MYTAG, "ringing $state,  $incomingNumber")
                   // Toast.makeText(requireContext(), "Phone is Ringing", Toast.LENGTH_LONG).show()
                    // call create route
                    if(incomingNumber.equals(OUR_VERIFICATION_NUMBER_1) || incomingNumber.equals(OUR_VERIFICATION_NUMBER_2) ) activityViewModel.submitButtonClicked(VERIFIED_BY_CALL)
                }
                // If incoming call received
                if (state == TelephonyManager.CALL_STATE_OFFHOOK) {
                    //Toast.makeText(requireContext(), "Phone is Currently in A call", Toast.LENGTH_LONG).show()
                }
                if (state == TelephonyManager.CALL_STATE_IDLE) {
                    //Toast.makeText(requireContext(), "phone is neither ringing nor in a call", Toast.LENGTH_LONG).show()
                }
            }
        }

        telephonyManager.listen(callStateListener, PhoneStateListener.LISTEN_CALL_STATE)

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
            verifyBySMS()
            activityViewModel.startSMSRetreiverFunction()

           /* if(checkForPermissions()){
                    showProgressBar(true)
                    verifyBySMS()
            }*/

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
                response.userMessage.let { showToast(it) }

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
                showToast(it)
                showProgressBar(false)
                enableDisableButtons(true)
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

    private fun verifyBySMS(){
        //resending sms via registration route
        Log.i(MYTAG,"call verification button (ex Resend) ${activityViewModel.enteredPhoneNumber}, ${activityViewModel.enteredEmail}, ${activityViewModel.enteredPassword}, ${activityViewModel.signInParameter}")
        when{
            //came from registrtion fragment
            activityViewModel.enteredEmail==null && activityViewModel.signInParameter==null->activityViewModel.registerButtonClicked(activityViewModel.enteredPhoneNumber?.removePlus()?:"",smsResend = true)

            //came from AddNumberToAccount fragment
            activityViewModel.enteredEmail!=null && activityViewModel.signInParameter==null->activityViewModel.addNumberToAccountButtonClicked(
                activityViewModel.enteredPhoneNumber?.removePlus()?:"",
                activityViewModel.enteredEmail?:"",
                activityViewModel.enteredPassword?:"",
                smsResend = true)

            //came from NumberExistsInDatabase, create new account
            activityViewModel.enteredEmail==null && activityViewModel.signInParameter==false->activityViewModel.numberExistsInDb_NoAccount(smsResend = true)

            //came from NumberExistsInDatabase, verify account
            activityViewModel.enteredEmail!=null && activityViewModel.signInParameter==true->activityViewModel.numberExistsInDBVerifyAccount(
                activityViewModel.enteredEmail?:"",
                activityViewModel.enteredPassword?:"",
                smsResend = true)

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
                        verifyBySMS()

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



    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }

}
