package app.adinfinitum.ello.ui.registrationauthorization

import android.Manifest
import android.app.Activity
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.os.SystemClock
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
import app.adinfinitum.ello.api.NetResponse_Registration
import app.adinfinitum.ello.databinding.FragmentRegistrationBinding
import app.adinfinitum.ello.utils.*

private const val MY_TAG="MY_RegistrationFragment"

class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var activityViewModel: RegAuthActivityViewModel
    val MY_PERMISSIONS_REGISTRATION_READ_PHONE_STATE_and_READ_CALL_LOG=30


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_registration, container, false)

        activityViewModel = requireActivity().run {
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.register2TextView.apply {
                when (isVerificationByCallEnabled()){
                    true-> text=resources.getString(R.string._ello_will_call_to_verify_your_number)
                    false->text=resources.getString(R.string._ello_will_send_sms_with_token_to_verify_your_number)
                }
         }

        binding.phoneNumberEditText.apply {
            setText(PLUS_PREFIX)
            afterTextChanged {
                activityViewModel.afterPhoneNumberEditTextChanged(it)
                binding.enterPhoneTextInputLayout.error = null

            }

            setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) binding.enterPhoneTextInputLayout.error = null

            }

            setOnEditorActionListener { view, action, keyEvent ->

                when (action) {
                    EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {
                        hidekeyboard()
                        view.clearFocus()
                        true
                    }
                    else -> false
                }
            }

         }



        binding.registerButton.setOnClickListener {

            hidekeyboard()
            binding.rootRegContLayout.requestFocus()

            if (!activityViewModel.isOnline()) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener
            }
            activityViewModel.registerButtonClicked()

        }

        binding.addNumToAccountButton.setOnClickListener {
            findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAddNumberToAccount())
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationResult.observe(viewLifecycleOwner, Observer {

            if(!it.hasBeenHandled){
                val result=it.getContentIfNotHandled()

                result?.enteredPhoneError?.let {
                    binding.enterPhoneTextInputLayout.error=resources.getString(it)
                    return@Observer
                }

                result?.let {
                    if(result.showTermsOfUseDialog) {
                        showTermsOfUseDailog()
                        return@Observer
                    }

                }

                if(result?.mustAskForPermission==true){
                    binding.registerButton.isEnabled = true
                    showProgressBar(false)
                    checkForPermissions()
                    return@Observer
                }

                when(result?.navigateToFragment){
                    activityViewModel.NAVIGATE_TO_AUTHORIZATION_FRAGMENT-> {
                            findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
                            return@Observer
                            }
                    activityViewModel.NAVIGATE_TO_NMB_EXISTS_IN_DB_FRAGMENT->{
                            findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToNumberExistsInDatabase())
                            return@Observer
                            }
                    else->{}
                }

                result?.showSnackBarMessage?.let {
                    showSnackBar(it)
                }

                result?.let {
                    if(it.showSnackBarErrorMessage) showSnackBar(resources.getString(R.string.something_went_wrong))
                }

                binding.registerButton.isEnabled = true
                showProgressBar(false)

            }

        })



    }



    private fun startRegistraion(){

        binding.registerButton.isEnabled = false
        showProgressBar(true)

        activityViewModel.sendRegistrationToServer()

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


    private fun showTermsOfUseDailog() {

        val alertDialog: AlertDialog? = activity?.let {

            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater

            builder.setView(inflater.inflate(R.layout.terms_of_use_dialog, null))
            // Add action buttons
            builder
                .setPositiveButton(resources.getString(R.string.terms_of_use_accept),
                    DialogInterface.OnClickListener { _, id ->
                        // sign in the user
                       startRegistraion()

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
                    this.justificationMode = LineBreaker.JUSTIFICATION_MODE_INTER_WORD
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
                            startRegistraion()
                    }else {

                        /*binding.registerButton.isEnabled = true
                        showProgressBar(false)
                        showSnackBar(getString(R.string.call_log_permission_not_granted))*/

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
        return activityViewModel.signInProcessAuxData.verificationByCallEnabled
    }




}










