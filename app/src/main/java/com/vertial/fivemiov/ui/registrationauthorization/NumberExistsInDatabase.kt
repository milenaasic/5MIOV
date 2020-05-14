package com.vertial.fivemiov.ui.registrationauthorization


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
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentNumberExistsInDatabaseBinding
import com.vertial.fivemiov.utils.afterTextChanged
import com.vertial.fivemiov.utils.isEmailValid
import com.vertial.fivemiov.utils.isPasswordValid

private val MY_TAG="MY_NumberExistsInDB"
class NumberExistsInDatabase : Fragment() {

    private lateinit var binding: FragmentNumberExistsInDatabaseBinding
    private var activityViewModel:RegAuthActivityViewModel?=null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_number_exists_in_database,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.numExistsPhoneTextView.text=String.format(resources.getString(R.string.add_plus_before_phone,activityViewModel?.enteredPhoneNumber?:" "))

        binding.nmbExistsSubmitButton.setOnClickListener {
            binding.nmbExistsRoot.requestFocus()
            it.isEnabled=false
            binding.dontHaveAccountButton.isEnabled=false
            hidekeyboard()
            if(allEnteredFieldsAreValid()){
                showProgressBar(true)
                activityViewModel?.signInParameter=true
                activityViewModel?.numberExistsInDBVerifyAccount(
                    binding.nmbExistsEmailEditText.text.toString(),
                    binding.nmbExistsPassEditText.text.toString()
                )
            }else{
                it.isEnabled=true
                binding.dontHaveAccountButton.isEnabled=true
            }

        }

        binding.nmbExistsPassEditText.setOnEditorActionListener { view, action, keyEvent ->
            when (action){
                EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
                    hidekeyboard()
                    view.clearFocus()
                    true
                }
                else->false
            }
        }

        binding.dontHaveAccountButton.setOnClickListener {
            binding.nmbExistsRoot.requestFocus()
            it.isEnabled=false
            binding.nmbExistsSubmitButton.isEnabled=false
            showProgressBar(true)
            activityViewModel?.signInParameter=false
            activityViewModel?.numberExistsInDb_NoAccount()

         }

        //Kada EditText polja imaju fokus treba da se iskljuci greska
        binding.apply {

            nmbExistsEmailEditText.setOnFocusChangeListener { view, hasFocus ->
                if(hasFocus) binding.nmbExistsEmailTextInputLayout.error=null
            }
            nmbExistsEmailEditText.afterTextChanged {binding.nmbExistsEmailTextInputLayout.error=null  }

            nmbExistsPassEditText.setOnFocusChangeListener { view, hasFocus ->
                if(hasFocus) binding.nmbExistsEnterPassTextInputLayout.error=null
            }
            nmbExistsPassEditText.afterTextChanged { binding.nmbExistsEnterPassTextInputLayout.error=null }


        }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel?.nmbExistsInDBUserHasAccountSuccess?.observe(viewLifecycleOwner, Observer {response->
            if(response!=null) {
                when{
                    response.success==true->{
                            showToast(response.userMessage)
                            activityViewModel?.resetNmbExistsInDB_VerifyAccount_NetSuccess()
                            activityViewModel?.startSMSRetreiverFunction()
                            findNavController().navigate(NumberExistsInDatabaseDirections.actionNumberExistsInDatabaseToAuthorizationFragment())
                    }
                    response.success==false->{
                        showSnackBar(response.userMessage)
                        activityViewModel?.resetNmbExistsInDB_VerifyAccount_NetSuccess()

                    }
                }
                binding.nmbExistsSubmitButton.isEnabled = true
                binding.dontHaveAccountButton.isEnabled = true
                showProgressBar(false)
            }
         })

        activityViewModel?.nmbExistsInDBUserHasAccountError?.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                showSnackBar(resources.getString(R.string.something_went_wrong))
                binding.nmbExistsSubmitButton.isEnabled = true
                binding.dontHaveAccountButton.isEnabled = true
                activityViewModel?.resetNmbExistsInDB_VerifyAccount_NetError()
                showProgressBar(false)
            }
        })

        activityViewModel?.nmbExistsInDB_NoAccountSuccess?.observe(viewLifecycleOwner, Observer {response->
            if(response!=null) {
                binding.nmbExistsSubmitButton.isEnabled = true
                binding.dontHaveAccountButton.isEnabled = true
                activityViewModel?.resetNmbExistsInDB_NOAccount_NetSuccess()
                showProgressBar(false)
                activityViewModel?.startSMSRetreiverFunction()
                findNavController().navigate(NumberExistsInDatabaseDirections.actionNumberExistsInDatabaseToAuthorizationFragment())
            }
        })

        activityViewModel?.nmbExistsInDB_NoAccountError?.observe(viewLifecycleOwner, Observer {
            if(it!=null) {
                showSnackBar(resources.getString(R.string.something_went_wrong))
                binding.nmbExistsSubmitButton.isEnabled = true
                binding.dontHaveAccountButton.isEnabled = true
                activityViewModel?.resetNmbExistsInDB_NOAccount_NetError()
                showProgressBar(false)
            }
        })
    }

    override fun onDestroy() {
        Log.i(MY_TAG,"onDestroy()")
        if(activityViewModel!=null) activityViewModel?.resetSignUpParameters()
        super.onDestroy()

    }

    override fun onDestroyView() {
        Log.i(MY_TAG,"onDestroyView()")
        super.onDestroyView()
    }


    private fun allEnteredFieldsAreValid(): Boolean {

        var b:Boolean=true

        if(!(binding.nmbExistsEmailEditText.text.toString()).isEmailValid()) {
            b=false
            binding.nmbExistsEmailTextInputLayout.setError(resources.getString(R.string.not_valid_email))
        }
        if(!(binding.nmbExistsPassEditText.text.toString()).isPasswordValid()) {
            b=false
            binding.nmbExistsEnterPassTextInputLayout.setError(resources.getString(R.string.not_valid_password))
        }
        return b
    }


    private fun hidekeyboard(){

        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.nmbExistsRoot.windowToken, 0)
    }


    private fun showProgressBar(bool:Boolean){
        if(bool){
            binding.nmbExistsRoot.alpha=0.2f
            binding.progressBarNumExists.visibility=View.VISIBLE
        }else{
            binding.nmbExistsRoot.alpha=1f
            binding.progressBarNumExists.visibility=View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }



}
