package com.vertial.fivemiov.ui.RegistrationAuthorization


import android.app.Activity
import android.os.Bundle
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
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentAuthorizationBinding
import com.vertial.fivemiov.databinding.FragmentRegistrationBinding
import com.vertial.fivemiov.utils.isOnline


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

        binding.authPhoneTextView.text=activityViewModel.enteredPhoneNumber

        binding.submitButton.setOnClickListener {

            hidekeyboard()

            if(!isOnline(requireActivity().application)) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener}

            it.isEnabled=false

            if(binding.tokenEditText.text.toString().isNullOrBlank()){
                        binding.tokenEditText.setError(resources.getString(R.string.enter_token))
                        it.isEnabled=true
            } else {
                    showProgressBar(true)
                    activityViewModel.submitButtonClicked(binding.tokenEditText.text.toString())
            }
        }

        binding.tokenEditText.setOnEditorActionListener { view, action, keyEvent ->
            when (action){
                EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
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

        activityViewModel.authorizationNetworkSuccess.observe(viewLifecycleOwner, Observer {
            showToast(it)
            showProgressBar(false)
            binding.submitButton.isEnabled=true
        })

        activityViewModel.authorizationNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null)showSnackBar(it)
            showProgressBar(false)
            binding.submitButton.isEnabled=true
        })
    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
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

}
