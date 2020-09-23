package com.vertial.fivemiov.ui.registrationauthorization

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.text.LineBreaker.JUSTIFICATION_MODE_INTER_WORD
import android.os.Build
import android.os.Bundle
import android.text.Html
import android.text.Layout
import android.util.Log
import androidx.fragment.app.Fragment
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
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.NetResponse_Registration
import com.vertial.fivemiov.databinding.FragmentRegistrationBinding
import com.vertial.fivemiov.utils.*

private const val MY_TAG="MY_RegistrationFragment"
class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var activityViewModel: RegAuthActivityViewModel
    val MY_PERMISSIONS_REGISTRATION_READ_PHONE_STATE_and_READ_CALL_LOG=30
    private lateinit var netResponseRegistration: NetResponse_Registration

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false)

        activityViewModel = requireActivity().run {
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.phoneNumberEditText.setText(PLUS_NIGERIAN_PREFIX)

        /*binding.register2TextView.apply {
                when (isVerificationByCallEnabled()){
                    true-> text=resources.getString(R.string._5miov_will_call_to_verify_your_number)
                    false->text=resources.getString(R.string._5miov_will_send_sms_with_token_to_verify_your_number)
                }
         }*/

        binding.registerButton.setOnClickListener {

            hidekeyboard()
            binding.rootRegContLayout.requestFocus()


            if (!isOnline(requireActivity().application)) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener
            }


            val enteredPhoneNumber = binding.phoneNumberEditText.text.toString()
            if (enteredPhoneNumber.isPhoneNumberValid()) {
                showTermsOfUseDailog(enteredPhoneNumber)

            } else {

                binding.enterPhoneTextInputLayout.setError(resources.getString(R.string.not_valid_phone_number))
            }
        }

        binding.addNumToAccountButton.setOnClickListener {


            if(!binding.phoneNumberEditText.text.isNullOrBlank() && !binding.phoneNumberEditText.text.isNullOrEmpty() ) {
                    activityViewModel.enteredPhoneNumber=binding.phoneNumberEditText.text.toString()
            }
            findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAddNumberToAccount())
        }

        binding.phoneNumberEditText.setOnEditorActionListener { view, action, keyEvent ->

            when (action) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {
                    hidekeyboard()
                    view.clearFocus()
                    true
                }
                else -> false
            }
        }

        binding.phoneNumberEditText.apply {
            afterTextChanged { binding.enterPhoneTextInputLayout.error = null }
            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) binding.enterPhoneTextInputLayout.error = null

            }
        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationNetworkError.observe(viewLifecycleOwner, Observer {

            if (it != null) {
                showSnackBar(resources.getString(R.string.something_went_wrong))
                activityViewModel.resetRegistrationNetErrorr()
                binding.registerButton.isEnabled = true
                showProgressBar(false)
            }
        })

        activityViewModel.registrationNetSuccess.observe(viewLifecycleOwner, Observer { response ->

            if (response != null) {

               //set variable to define if registration process should use call or sms verification
                (requireActivity() as RegistrationAuthorizationActivity).verificationByCallEnabled = response.callVerificationEnabled

                netResponseRegistration=response

                when (isVerificationByCallEnabled()) {

                    true->{
                        // in verifyByCall mode extract number to receive call from (verificationCallerId)
                        if (response.verificationCallerId.isNotEmpty()) {
                            (requireActivity() as RegistrationAuthorizationActivity).verificationCallerId =
                                response.verificationCallerId
                        }

                        if(checkForPermissions()) checkResponseSuccessAndActAccordingly(response = response)

                    }
                    false->{
                        checkResponseSuccessAndActAccordingly(response = response)

                    }

                }

            }

        })
    }

    private fun checkResponseSuccessAndActAccordingly(response:NetResponse_Registration){

        when {
            response.success == true && response.phoneNumberAlreadyAssigned == false -> {

                // in verifyBySMS mode start listening for SMS
                if(!isVerificationByCallEnabled()) activityViewModel.startSMSRetreiverFunction()

                showToast(response.userMessage)
                activityViewModel.resetRegistrationNetSuccess()
                findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
            }

            response.success == true && response.phoneNumberAlreadyAssigned == true -> {
                //showToast(response.userMessage)
                activityViewModel.resetRegistrationNetSuccess()
                findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToNumberExistsInDatabase())
            }

            response.success == false -> {
                showSnackBar(response.userMessage)
                activityViewModel.resetRegistrationNetSuccess()
            }

        }

        binding.registerButton.isEnabled = true
        showProgressBar(false)

    }

    private fun startRegistraion(enteredPhoneNumber:String){

        binding.registerButton.isEnabled = false
        showProgressBar(true)

        when(isVerificationByCallEnabled()){
            true->{
                activityViewModel.registerButtonClicked(
                    enteredPhoneNumber.removePlus(),
                    smsResend = false,
                    verificationMethod = VERIFICATION_METHOD_CALL
                )
            }
            false->{
                activityViewModel.registerButtonClicked(
                    enteredPhoneNumber.removePlus(),
                    smsResend = false,
                    verificationMethod = VERIFICATION_METHOD_SMS
                )
            }

        }


    }



    private fun hidekeyboard() {
        val inputMethodManager =
            requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.rootRegContLayout.windowToken, 0)
    }

    private fun showProgressBar(bool: Boolean) {
        if (bool) {
            binding.rootRegContLayout.alpha = 0.2f
            binding.progressBar.visibility = View.VISIBLE
        } else {
            binding.rootRegContLayout.alpha = 1f
            binding.progressBar.visibility = View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE).setAction("OK") {}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()


    }


    private fun showTermsOfUseDailog(enteredPhoneNumber: String) {

        val alertDialog: AlertDialog? = activity?.let {

            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater

            builder.setView(inflater.inflate(R.layout.terms_of_use_dialog, null))
            // Add action buttons
            builder
                .setPositiveButton(resources.getString(R.string.terms_of_use_accept),
                    DialogInterface.OnClickListener { dialog, id ->
                        // sign in the user ...
                       startRegistraion(enteredPhoneNumber)
                        //activityViewModel.startSMSRetreiverFunction()
                    })
                .setNegativeButton(resources.getString(R.string.terms_of_use_cancel),
                    DialogInterface.OnClickListener { dialog, id ->
                        dialog.cancel()
                        //binding.registerButton.isEnabled = true
                    })

            builder.create()

        }
        alertDialog?.setOnShowListener {
            alertDialog.findViewById<TextView>(R.id.termsofusetextView)?.apply {
                text = HtmlCompat.fromHtml(TERMS_OF_USE, HtmlCompat.FROM_HTML_MODE_LEGACY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.justificationMode = Layout.JUSTIFICATION_MODE_INTER_WORD
                }
             }

        }

        alertDialog?.show()


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
                    MY_PERMISSIONS_REGISTRATION_READ_PHONE_STATE_and_READ_CALL_LOG
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
            MY_PERMISSIONS_REGISTRATION_READ_PHONE_STATE_and_READ_CALL_LOG -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                            checkResponseSuccessAndActAccordingly(netResponseRegistration)
                    }else {
                        activityViewModel.resetRegistrationNetSuccess()
                        binding.registerButton.isEnabled = true
                        showProgressBar(false)
                        showSnackBar(getString(R.string.call_log_permission_not_granted))

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

    private fun isVerificationByCallEnabled():Boolean{
        return (requireActivity() as RegistrationAuthorizationActivity).verificationByCallEnabled
    }




}










