package com.vertial.sipdnidphone.ui.RegistrationAuthorization


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider

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


        }


        return binding.root
    }



}
