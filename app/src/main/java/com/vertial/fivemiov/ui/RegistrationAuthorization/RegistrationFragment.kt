package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.app.Activity
import android.os.Bundle
import android.telephony.PhoneNumberUtils
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
import com.vertial.fivemiov.databinding.FragmentRegistrationBinding

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


        binding.registerButton.setOnClickListener {
            Log.i(MY_TAG,"registrtion button je clicked}")
            it.isEnabled=false
            hidekeyboard()
            val enteredPhoneNumber=binding.phoneNumberEditText.text.toString()
            if(enteredPhoneNumber.isPhoneNumberValid()){
                    it.isEnabled=false
                    showProgressBar(true)
                    activityViewModel.registerButtonClicked(enteredPhoneNumber.removePlus())

                }else {
                    it.isEnabled=true
                     binding.phoneNumberEditText.setError(resources.getString(R.string.not_valid_phone_number))
                }
        }

        binding.addNumToAccountButton.setOnClickListener {
            //Log.i(MY_TAG,"registrtion button je clicked}")
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

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                showSnackBar(resources.getString(R.string.something_went_wrong))
            }
            binding.registerButton.isEnabled=true
            showProgressBar(false)

         })

         activityViewModel.registrationNetSuccessIsNmbAssigned.observe(viewLifecycleOwner, Observer {isNumberAssigned->
             if(!isNumberAssigned){
                 //showToast(it)
                 findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
             }else {
                 findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToNumberExistsInDatabase())
             }
             binding.registerButton.isEnabled=true
             showProgressBar(false)
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
        Snackbar.make(binding.root,message,Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message,Toast.LENGTH_LONG).show()
    }


}






