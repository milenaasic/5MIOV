package com.vertial.fivemiov.ui.registrationauthorization

import android.app.Activity
import android.content.DialogInterface
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
import com.vertial.fivemiov.databinding.FragmentRegistrationBinding
import com.vertial.fivemiov.utils.*

private const val MY_TAG="MY_RegistrationFragment"
class RegistrationFragment : Fragment() {

    private lateinit var binding: FragmentRegistrationBinding
    private lateinit var activityViewModel: RegAuthActivityViewModel

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

        binding.registerButton.setOnClickListener {

            //showTermsOfUseDailog()
           // it.isEnabled = false
            hidekeyboard()
            binding.rootRegContLayout.requestFocus()
            if (!isOnline(requireActivity().application)) {
                showSnackBar(resources.getString(R.string.no_internet))
               // it.isEnabled = true
                return@setOnClickListener
            }

            Log.i(MY_TAG, "registrtion button je clicked}")

            val enteredPhoneNumber = binding.phoneNumberEditText.text.toString()
            if (enteredPhoneNumber.isPhoneNumberValid()) {
                showTermsOfUseDailog(enteredPhoneNumber)

                /*showTermsOfUseDailog()
                it.isEnabled = false
                showProgressBar(true)
                activityViewModel.registerButtonClicked(
                    enteredPhoneNumber.removePlus(),
                    smsResend = false
                )*/

            } else {
               // it.isEnabled = true
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
            Log.i(MY_TAG, "action listener , action je $action")
            when (action) {
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED -> {
                    Log.i(MY_TAG, "action listener , usao u action done")
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

        //simulate crash
       /* val s:String?=null
        s!!.length*/

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationNetworkError.observe(viewLifecycleOwner, Observer {
            Log.i(MY_TAG, "net reg greska je ${it}")

            if (it != null) {
                showSnackBar(resources.getString(R.string.something_went_wrong))
                activityViewModel.resetRegistrationNetErrorr()
                binding.registerButton.isEnabled = true
                showProgressBar(false)
            }
        })

        activityViewModel.registrationNetSuccess.observe(viewLifecycleOwner, Observer { response ->
            Log.i(MY_TAG, "net reg response j e ${response.toString()}")

            if (response != null) {
                when {
                    response.success == true && response.phoneNumberAlreadyAssigned == false -> {
                        showToast(response.userMessage)
                        activityViewModel.resetRegistrationNetSuccess()
                        activityViewModel.startSMSRetreiverFunction()
                        findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
                    }

                    response.success == true && response.phoneNumberAlreadyAssigned == true -> {
                        showToast(response.userMessage)
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
        })
    }

    private fun startRegistraion(enteredPhoneNumber:String){

        binding.registerButton.isEnabled = false
        showProgressBar(true)
        activityViewModel.registerButtonClicked(
            enteredPhoneNumber.removePlus(),
            smsResend = false
        )

    }

   /* private fun registerButtonClicked() {
        val it = binding.registerButton
        it.isEnabled = false
        hidekeyboard()
        binding.rootRegContLayout.requestFocus()
        if (!isOnline(requireActivity().application)) {
            showSnackBar(resources.getString(R.string.no_internet))
            it.isEnabled = true
            return
        }


        Log.i(MY_TAG, "registrtion button je clicked}")


        val enteredPhoneNumber = binding.phoneNumberEditText.text.toString()
        if (enteredPhoneNumber.isPhoneNumberValid()) {
            it.isEnabled = false
            showProgressBar(true)
            activityViewModel.registerButtonClicked(
                enteredPhoneNumber.removePlus(),
                smsResend = false
            )

        } else {
            it.isEnabled = true
            binding.enterPhoneTextInputLayout.setError(resources.getString(R.string.not_valid_phone_number))
        }

    }*/

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
        Log.i(MY_TAG, "On Destroy")

    }


    private fun showTermsOfUseDailog(enteredPhoneNumber: String) {
        Log.i(MY_TAG, "showTermsOfUseDailog")
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
}










