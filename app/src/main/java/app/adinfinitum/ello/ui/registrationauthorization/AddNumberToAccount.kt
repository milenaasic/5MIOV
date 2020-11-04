package app.adinfinitum.ello.ui.registrationauthorization


import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.NetResponse_AddNumberToAccount
import app.adinfinitum.ello.databinding.FragmentAddNumberToAccountBinding
import app.adinfinitum.ello.utils.*

private const val MY_TAG="MY_AddNumberToAccount"
class AddNumberToAccount : Fragment() {

    private lateinit var binding:FragmentAddNumberToAccountBinding
    private var activityViewModel:RegAuthActivityViewModel?=null
    private lateinit var netAddNumberToAccountResponse: NetResponse_AddNumberToAccount
    val MY_PERMISSIONS_ADD_NUMBER_READ_PHONE_STATE_and_READ_CALL_LOG=33

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_add_number_to_account,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        if(!activityViewModel?.enteredPhoneNumber.isNullOrBlank() && !activityViewModel?.enteredPhoneNumber.isNullOrEmpty()) {
            binding.addNmbPhoneEditText.setText(activityViewModel?.enteredPhoneNumber)
        }else binding.addNmbPhoneEditText.setText(PLUS_NIGERIAN_PREFIX)

        /*binding.addphoneTextView.apply {
            when (isVerificationByCallEnabled()){
                true-> text=resources.getString(R.string._5miov_will_call_to_verify_your_number)
                false->text=resources.getString(R.string._5miov_will_send_sms_with_token_to_verify_your_number)
            }

        }*/

        binding.addphoneButton.setOnClickListener {

            binding.rootAddNumberConstrLayout.requestFocus()
            hidekeyboard()
            if(allEnteredFieldsAreValid()){
                showTermsOfUseDailog(
                                    (binding.addNmbPhoneEditText.text.toString()).removePlus(),
                                    binding.addNmbEmailEditText.text.toString(),
                                    binding.addNmbPassEditText.text.toString()
                )

            }else{
                it.isEnabled=true
            }

        }


        binding.addNmbPassEditText.setOnEditorActionListener { view, action, _ ->
            when (action){
                EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
                    hidekeyboard()
                    view.clearFocus()
                    true
                }
                else->{
                    false
                }

            }

        }

        //if edittextlayout has focus turn off error message
        binding.apply {

            addNmbPhoneEditText.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) binding.addPhoneTextInputLayout.error=null

            }
            addNmbPhoneEditText.afterTextChanged {binding.addPhoneTextInputLayout.error=null }

            addNmbEmailEditText.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) binding.enterEmailTextInputLayout.error=null

            }

            addNmbEmailEditText.afterTextChanged { binding.enterEmailTextInputLayout.error=null }

            addNmbPassEditText.setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) binding.addNmbEnterPassTextInputLayout.error=null

             }
             addNmbPassEditText.afterTextChanged { binding.addNmbEnterPassTextInputLayout.error=null }

         }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel?.addNumberToAccuntNetworkError?.observe(viewLifecycleOwner,Observer{
            Log.i(MY_TAG,"addNumberToAccuntNetworkError.observe $it,  ${it.hasBeenHandled}")
            if(!it.hasBeenHandled) {
                showSnackBar(resources.getString(R.string.something_went_wrong))
                //activityViewModel?.resetAddNumberToAccountNetError()
                binding.addphoneButton.isEnabled = true
                showProgressBar(false)
            }
        })


        activityViewModel?.addNumberToAccuntNetworkSuccess?.observe(viewLifecycleOwner,Observer{
            Log.i(MY_TAG,"addNumberToAccuntNetworkSuccess.observe $it,  ${it.hasBeenHandled}")
            if(!it.hasBeenHandled) {

               it.getContentIfNotHandled()?.let { response ->

                   //set variable to define if registration process should use call or sms verification
                   (requireActivity() as RegistrationAuthorizationActivity).verificationByCallEnabled =
                       response.callVerificationEnabled

                   netAddNumberToAccountResponse = response

                   when (isVerificationByCallEnabled()) {

                       true -> {
                           // in verifyByCall mode extract number to receive call from (verificationCallerId)
                           if (response.verificationCallerId.isNotEmpty()) {
                               (requireActivity() as RegistrationAuthorizationActivity).verificationCallerId =
                                   response.verificationCallerId
                           }

                           if (checkForPermissions()) checkResponseSuccessAndActAccordinglyAddNMB(
                               response = response
                           )

                       }
                       false -> {
                           checkResponseSuccessAndActAccordinglyAddNMB(response = response)

                       }

                   }
               }

            }

        })


    }

    private fun checkResponseSuccessAndActAccordinglyAddNMB(response: NetResponse_AddNumberToAccount) {

        when {
            response.success == true -> {

                // in verifyBySMS mode start listening for SMS
                if(!isVerificationByCallEnabled()) activityViewModel?.startSMSRetreiverFunction()

                showToast(response.usermessage)
                //activityViewModel?.resetAddNumberToAccountNetSuccess()
                findNavController().navigate(AddNumberToAccountDirections.actionAddNumberToAccountToAuthorizationFragment())
            }

            response.success == false -> {
                showSnackBar(response.usermessage)
                //activityViewModel?.resetAddNumberToAccountNetSuccess()
            }
        }

        binding.addphoneButton.isEnabled = true
        showProgressBar(false)


    }

    override fun onDestroy() {
        Log.i(MY_TAG,"onDestroy(), activityViewModel:${activityViewModel.toString()}")
        if(activityViewModel!=null) activityViewModel?.resetSignUpParameters()
        super.onDestroy()

    }



    private fun allEnteredFieldsAreValid(): Boolean {
        var b:Boolean=true
        if(!(binding.addNmbPhoneEditText.text.toString()).isPhoneNumberValid()) {
            b=false
            binding.addPhoneTextInputLayout.setError(resources.getString(R.string.not_valid_phone_number))
        }
        if(!(binding.addNmbEmailEditText.text.toString()).isEmailValid()) {
            b=false
            binding.enterEmailTextInputLayout.setError(resources.getString(R.string.not_valid_email))
        }
        if(!(binding.addNmbPassEditText.text.toString()).isPasswordValid()) {
            b=false
            binding.addNmbEnterPassTextInputLayout.setError(resources.getString(R.string.not_valid_password))
        }
        return b
    }



    private fun hidekeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.rootAddNumberConstrLayout.windowToken, 0)
    }

    private fun showProgressBar(bool:Boolean){
        if(bool){
            binding.rootAddNumberConstrLayout.alpha=0.2f
            binding.addNumberProgressBar.visibility=View.VISIBLE
        }else{
            binding.rootAddNumberConstrLayout.alpha=1f
            binding.addNumberProgressBar.visibility=View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }

    private fun startAddNumberToAccount(phone: String,email: String,password: String) {
            binding.addphoneButton.isEnabled=false
            showProgressBar(true)

        when(isVerificationByCallEnabled()) {
            true -> {
                activityViewModel?.addNumberToAccountButtonClicked(
                    phoneNumber = phone,
                    email = email,
                    password = password,
                    verificationMethod = VERIFICATION_METHOD_CALL
                )

            }

            false -> {
                activityViewModel?.addNumberToAccountButtonClicked(
                    phoneNumber = phone,
                    email = email,
                    password = password,
                    verificationMethod = VERIFICATION_METHOD_SMS
                )

            }
        }





    }

    private fun showTermsOfUseDailog(phone: String,email:String,password:String) {
        Log.i(MY_TAG, "showTermsOfUseDailog")
        val alertDialog: AlertDialog? = activity?.let {

            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater

            builder.setView(inflater.inflate(R.layout.terms_of_use_dialog, null))
            // Add action buttons
            builder
                .setPositiveButton(resources.getString(R.string.terms_of_use_accept),
                    DialogInterface.OnClickListener { _, id ->
                        // sign in the user ...
                        startAddNumberToAccount(phone,email,password)
                        //activityViewModel?.startSMSRetreiverFunction()
                    })
                .setNegativeButton(resources.getString(R.string.terms_of_use_cancel),
                    DialogInterface.OnClickListener { dialog, _ ->
                        dialog.cancel()

                    })

            builder.create()

        }
        alertDialog?.setOnShowListener {
            alertDialog.findViewById<TextView>(R.id.termsofusetextView)?.apply {
                text = HtmlCompat.fromHtml(TERMS_OF_USE, HtmlCompat.FROM_HTML_MODE_LEGACY)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    this.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
                }
            }
        }

        alertDialog?.show()


    }


    private fun isVerificationByCallEnabled():Boolean{
        return (requireActivity() as RegistrationAuthorizationActivity).verificationByCallEnabled
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
                    MY_PERMISSIONS_ADD_NUMBER_READ_PHONE_STATE_and_READ_CALL_LOG
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
            MY_PERMISSIONS_ADD_NUMBER_READ_PHONE_STATE_and_READ_CALL_LOG -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty()) {

                    if(grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED){
                        checkResponseSuccessAndActAccordinglyAddNMB(netAddNumberToAccountResponse)
                    }else {
                        //activityViewModel?.resetAddNumberToAccountNetSuccess()
                        binding.addphoneButton.isEnabled = true
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




}
