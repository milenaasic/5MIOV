package com.vertial.sipdnidphone.ui.RegistrationAuthorization


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.databinding.FragmentAuthorizationBinding
import com.vertial.sipdnidphone.databinding.FragmentRegistrationBinding


class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_authorization,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.submitButton.setOnClickListener {
            activityViewModel.submitButtonClicked(binding.tokenEditText.text.toString())
        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.authorizationNetworkSuccess.observe(viewLifecycleOwner, Observer {
            showToast(it)

        })

        activityViewModel.authorizationNetworkError.observe(viewLifecycleOwner, Observer {
            showSnackBar(it)

        })
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }


}
