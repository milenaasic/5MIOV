package app.adinfinitum.ello.ui.registrationauthorization


import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.DialogInterface
import android.content.pm.PackageManager
import android.graphics.text.LineBreaker
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AlertDialog
import androidx.core.text.HtmlCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import app.adinfinitum.ello.R
import app.adinfinitum.ello.databinding.FragmentAddNumberToAccountBinding
import app.adinfinitum.ello.utils.PLUS_PREFIX
import app.adinfinitum.ello.utils.TERMS_OF_USE
import app.adinfinitum.ello.utils.afterTextChanged
import com.google.android.material.snackbar.Snackbar

private const val MY_TAG="MY_AddNumberToAccount"
class AddNumberToAccount : Fragment() {

    private lateinit var binding:FragmentAddNumberToAccountBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel

    val MY_PERMISSIONS_ADD_NUMBER_READ_PHONE_STATE_and_READ_CALL_LOG=33

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.i(MY_TAG, "handleOnBackPressed() ${activityViewModel.signInForm.email}")
                    activityViewModel.resetSignInFormEmailAndPassword()
                    Log.i(MY_TAG, " after handleOnBackPressed() ${activityViewModel.signInForm.email}")
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

        binding= DataBindingUtil.inflate(
            inflater,
            R.layout.fragment_add_number_to_account,
            container,
            false
        )

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.addphoneTextView.apply {
            when (activityViewModel.signInProcessAuxData.verificationByCallEnabled){
                true -> text = resources.getString(R.string._ello_will_call_to_verify_your_number)
                false -> text =
                    resources.getString(R.string._ello_will_send_sms_with_token_to_verify_your_number)
            }

        }

        binding.addNmbPhoneEditText.apply {

            if(!activityViewModel.signInForm.phoneNmb.isNullOrBlank()) setText(activityViewModel.signInForm.phoneNmb)
            else setText(PLUS_PREFIX)

            afterTextChanged {
                activityViewModel.afterPhoneNumberEditTextChanged(text.toString())
                binding.addPhoneTextInputLayout.error=null
            }

            setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) binding.addPhoneTextInputLayout.error=null

            }
         }

         binding.addNmbEmailEditText.apply {

             afterTextChanged {
                activityViewModel.afterEmailEditTextChanged(text.toString())
                binding.enterEmailTextInputLayout.error=null
             }
             setOnFocusChangeListener { _, hasFocus ->
                 if(hasFocus) binding.enterEmailTextInputLayout.error=null
          }

         }

         binding.addNmbPassEditText.apply {

             afterTextChanged {
                activityViewModel.afterPasswordEditTextChanged(text.toString())
                binding.addNmbEnterPassTextInputLayout.error=null
             }
             setOnFocusChangeListener { _, hasFocus ->
                 if(hasFocus) binding.addNmbEnterPassTextInputLayout.error=null

             }

             setOnEditorActionListener { view, action, _ ->
                 when (action) {
                     EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {
                         hidekeyboard()
                         view.clearFocus()
                         true
                     }
                     else -> {
                         false
                     }

                 }
             }

          }



        binding.addphoneButton.setOnClickListener {

            binding.rootAddNumberConstrLayout.requestFocus()
            hidekeyboard()
            if (!activityViewModel.isOnline()) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener
            }

            activityViewModel.addNumberToAccountButtonClicked()

        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.addNumberToAccountResult.observe(viewLifecycleOwner, Observer {

            if (!it.hasBeenHandled) {

                val result = it.getContentIfNotHandled()

                result?.let {

                    if (it.enteredPhoneError != null || it.enteredEmailError != null || it.enteredPasswordError != null) {
                        it.enteredPhoneError?.let { errorId ->
                            binding.addPhoneTextInputLayout.error = resources.getString(errorId)
                        }
                        it.enteredEmailError?.let { errorId ->
                            binding.enterEmailTextInputLayout.error = resources.getString(errorId)
                        }
                        it.enteredPasswordError?.let { errorId ->
                            binding.addNmbEnterPassTextInputLayout.error =
                                resources.getString(errorId)
                        }
                        return@Observer
                    }
                }

                result?.let {
                    if (result.showTermsOfUseDialog) {
                        showTermsOfUseDailog()
                        return@Observer
                    }
                }

                if (result?.mustAskForPermission == true) {
                    binding.addphoneButton.isEnabled = true
                    showProgressBar(false)
                    checkForPermissions()
                    return@Observer
                }

                if (result?.navigateToFragment == activityViewModel.NAVIGATE_TO_AUTHORIZATION_FRAGMENT) {
                    findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
                    result.showToastMessage?.let { message ->
                        showToast(message)
                    }
                }

                result?.showSnackBarMessage?.let{
                    showSnackBar(it)
                }

                result?.let {
                    if(it.showSnackBarErrorMessage) showSnackBar(resources.getString(R.string.something_went_wrong))
                }

                binding.addphoneButton.isEnabled = true
                showProgressBar(false)

            }
        })
    }


    override fun onDestroy() {
        Log.i(MY_TAG, "onDestroy(), activityViewModel:${activityViewModel.toString()}")
        super.onDestroy()

    }




    private fun hidekeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.rootAddNumberConstrLayout.windowToken, 0)
    }

    private fun showProgressBar(bool: Boolean){
        if(bool){
            binding.rootAddNumberConstrLayout.alpha=0.2f
            binding.addNumberProgressBar.visibility=View.VISIBLE
        }else{
            binding.rootAddNumberConstrLayout.alpha=1f
            binding.addNumberProgressBar.visibility=View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root, message, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_LONG).show()
    }


    private fun startAddNumberToAccount() {
            binding.addphoneButton.isEnabled=false
            showProgressBar(true)
            activityViewModel.sendAddNumberToAccountToServer()

    }

    private fun showTermsOfUseDailog() {

        val alertDialog: AlertDialog? = activity?.let {

            val builder = AlertDialog.Builder(it)
            val inflater = it.layoutInflater

            builder.setView(inflater.inflate(R.layout.terms_of_use_dialog, null))
            // Add action buttons
            builder
                .setPositiveButton(resources.getString(R.string.terms_of_use_accept),
                    DialogInterface.OnClickListener { _, _ ->
                        // sign in the user ...
                        startAddNumberToAccount()
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


    private fun checkForPermissions():Boolean{
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true
        else {
            if (requireActivity().checkSelfPermission(Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED ||
                requireActivity().checkSelfPermission(Manifest.permission.READ_CALL_LOG) != PackageManager.PERMISSION_GRANTED

            ) {
                requestPermissions(
                    arrayOf(
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.READ_CALL_LOG
                    ),
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

                    if (grantResults[0] == PackageManager.PERMISSION_GRANTED && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                        startAddNumberToAccount()
                    } else {
                        /*binding.addphoneButton.isEnabled = true
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




}
