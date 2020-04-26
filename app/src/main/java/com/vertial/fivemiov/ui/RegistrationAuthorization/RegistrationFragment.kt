package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.app.Activity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
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

    private lateinit var binding:FragmentRegistrationBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_registration,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.phoneNumberEditText.setText(PLUS_NIGERIAN_PREFIX)

        binding.registerButton.setOnClickListener {

            hidekeyboard()
            binding.rootRegContLayout.requestFocus()
            if(!isOnline(requireActivity().application)) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener}


            Log.i(MY_TAG,"registrtion button je clicked}")
            it.isEnabled=false

            val enteredPhoneNumber=binding.phoneNumberEditText.text.toString()
            if(enteredPhoneNumber.isPhoneNumberValid()){
                    it.isEnabled=false
                    showProgressBar(true)
                    activityViewModel.startSMSRetreiver()
                    activityViewModel.registerButtonClicked(enteredPhoneNumber.removePlus(),smsResend = false)

                }else {
                    it.isEnabled=true
                     binding.enterPhoneTextInputLayout.setError(resources.getString(R.string.not_valid_phone_number))
                }
        }

        binding.addNumToAccountButton.setOnClickListener {
            findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAddNumberToAccount())
        }

        binding.phoneNumberEditText.setOnEditorActionListener { view, action, keyEvent ->
        Log.i(MY_TAG,"action listener , action je $action")
            when (action){
                EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
                    Log.i(MY_TAG,"action listener , usao u action done")
                    hidekeyboard()
                    view.clearFocus()
                    true
                }
                else->false
            }
         }

         binding.phoneNumberEditText.apply {
             afterTextChanged { binding.enterPhoneTextInputLayout.error=null }
             setOnFocusChangeListener { view, hasFocus ->
                 if(hasFocus) binding.enterPhoneTextInputLayout.error=null

             }
          }
        
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationNetworkError.observe(viewLifecycleOwner, Observer {
            Log.i(MY_TAG,"net reg greska je ${it}")

            if(it!=null){
                showSnackBar(resources.getString(R.string.something_went_wrong))
                activityViewModel.resetRegistrationNetErrorr()
                binding.registerButton.isEnabled=true
                showProgressBar(false)
            }
         })

         activityViewModel.registrationNetSuccess.observe(viewLifecycleOwner, Observer {response->
             Log.i(MY_TAG,"net reg response j e ${response.toString()}")

             if(response!=null) {
                 when {
                     response.success == true && response.phoneNumberAlreadyAssigned == false -> {
                         showToast(response.userMessage)
                         activityViewModel.resetRegistrationNetSuccess()
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



    private fun hidekeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.rootRegContLayout.windowToken, 0)
    }

    private fun showProgressBar(bool:Boolean){
        if(bool){
            binding.rootRegContLayout.alpha=0.2f
            binding.progressBar.visibility=View.VISIBLE
        }else{
            binding.rootRegContLayout.alpha=1f
            binding.progressBar.visibility=View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message,Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message,Toast.LENGTH_LONG).show()
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MY_TAG,"On Destroy")

    }


}






