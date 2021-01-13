package app.adinfinitum.ello.ui.registrationauthorization


import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.CountDownTimer
import android.telephony.PhoneStateListener
import android.telephony.TelephonyManager
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import app.adinfinitum.ello.R
import app.adinfinitum.ello.databinding.FragmentAuthorizationBinding
import app.adinfinitum.ello.utils.*


private val MYTAG="MY_AuthorizationFragm"
class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel
    //private  var telephonyManager:TelephonyManager?=null
    //private  var callStateListener: PhoneStateListener?=null
   // private val VERIFIED_BY_CALL="verifiedByCall"
    //private val TIME_TO_WAIT_FOR_CALL=6000L

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    activityViewModel.stopListeningForSMSOrPhoneCall()
                    Log.i(MYTAG, " after handleOnBackPressed() }")
                    isEnabled=false
                    requireActivity().onBackPressed()
                }

            }
        )

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
                showProgressBar(false)
            }


        }

        binding.authPhoneTextView.text=activityViewModel.signInForm.phoneNmb

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
                activityViewModel.submitAuthorizationButtonClicked(binding.tokenEditText.text.toString())
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
            activityViewModel.resendSMSButtonClicked(VERIFICATION_METHOD_SMS)

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

        activityViewModel.authorizationResult.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                val result=it.getContentIfNotHandled()

                result?.let {


                    it.showToastMessage?.let {
                        showToast(it)
                     }

                    it.showSnackBarMessage?.let {
                        showSnackBar(it)
                    }

                    if(it.showSnackBarErrorMessage) showSnackBar(resources.getString(R.string.something_went_wrong))

                    if(it.hideProgressBar){
                        showProgressBar(false)
                        enableDisableButtons(true)
                    }

                }

            }

         })


        //SMS verification token
        activityViewModel.verificationTokenForAuthFragment.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                binding.tokenEditText.setText(it.getContentIfNotHandled())

            }
        })

        activityViewModel.showConfirmPhoneNumberDialog.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
               val result= it.getContentIfNotHandled()
               result?.let {
                if(it) showDialogToConfirmUserNunber()
                }

            }
        })

        activityViewModel.expensivePhoneCallAuthorizationFailed.observe(viewLifecycleOwner, Observer {
            if(!it.hasBeenHandled){
                val result= it.getContentIfNotHandled()
                result?.let {
                    if(it) showToast("Call Authorization Failed. Try SMS")
                    showProgressBar(false)
                    enableDisableButtons(true)
                }

            }
        })
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



    override fun onDestroyView() {
        super.onDestroyView()
       // if(isVerificationByCallEnabled()) telephonyManager?.listen(callStateListener, PhoneStateListener.LISTEN_NONE)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }



    private fun isVerificationByCallEnabled():Boolean{
        return activityViewModel.isVerificationByCallEnabled()
    }

    private fun showDialogToConfirmUserNunber(){
        val alertDialog: AlertDialog? = activity?.let {

            val builder = AlertDialog.Builder(it)

            // Add action buttons
            builder
                .setTitle("Is this your phone number ${activityViewModel.getEnteredPhoneNumber()} ?")
                .setPositiveButton("YES",
                    DialogInterface.OnClickListener { _, _ ->
                        activityViewModel.resendSMSButtonClicked(VERIFICATION_METHOD_EXPENSIVE_CALL)
                        //startSecondCoundDownTImer()
                    })
                .setNegativeButton("NO",
                    DialogInterface.OnClickListener { dialog, _ ->

                        dialog.cancel()
                        showProgressBar(false)
                        showSnackBar(resources.getString(R.string.enter_correct_number))
                    })
                .setCancelable(false)

            builder.create()

        }

        alertDialog?.show()


    }


}
