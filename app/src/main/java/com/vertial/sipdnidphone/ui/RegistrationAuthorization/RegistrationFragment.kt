package com.vertial.sipdnidphone.ui.RegistrationAuthorization

import android.content.Context
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.databinding.FragmentRegistrationBinding
import com.vertial.sipdnidphone.utils.NO_NETWORK_ERROR


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
            if(registrationNumberIsValid()) activityViewModel.registerButtonClicked(binding.phoneNumberEditText.text.toString())
            else showSnackBar(resources.getString(R.string.not_valid_phone_number))
        }

        /*activityViewModel.navigateToAuthFragment.observe(viewLifecycleOwner, Observer {
            if(it){
                findNavController().navigate(RegistrationFragmentDirections.actionRegistrationFragmentToAuthorizationFragment())
                activityViewModel.navigationToAuthFragmentFinished()

            }

         })*/


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.registrationNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                showSnackBar(it)

            }

         })
    }

    private fun registrationNumberIsValid(): Boolean {
        return true
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message,Snackbar.LENGTH_LONG).show()
    }


}






