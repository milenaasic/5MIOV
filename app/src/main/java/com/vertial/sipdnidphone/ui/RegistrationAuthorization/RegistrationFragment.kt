package com.vertial.sipdnidphone.ui.RegistrationAuthorization

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.telephony.PhoneNumberUtils
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.databinding.FragmentRegistrationBinding
import com.vertial.sipdnidphone.utils.NO_NETWORK_ERROR

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
            if(registrationNumberIsValid()) activityViewModel.registerButtonClicked(binding.phoneNumberEditText.text.toString())
            else showSnackBar(resources.getString(R.string.not_valid_phone_number))
        }

        binding.addNumToAccountButton.setOnClickListener {
            //Log.i(MY_TAG,"registrtion button je clicked}")
            findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAddNumberToAccount())
        }


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                showSnackBar(it)
            }
         })

         activityViewModel.registrationNetworkSuccess.observe(viewLifecycleOwner, Observer {
             if(it!=null){
                 showToast(it)
                 findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
             }
          })
    }

    private fun registrationNumberIsValid(): Boolean {
        Log.i(MY_TAG,"check da li je broj ok ${PhoneNumberUtils.isGlobalPhoneNumber(binding.phoneNumberEditText.text.toString())}")
        return PhoneNumberUtils.isGlobalPhoneNumber(binding.phoneNumberEditText.text.toString())
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message,Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message,Toast.LENGTH_LONG).show()
    }


}






