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
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentAddNumberToAccountBinding
import com.vertial.fivemiov.utils.isEmailValid
import com.vertial.fivemiov.utils.isPasswordValid
import com.vertial.fivemiov.utils.isPhoneNumberValid
import com.vertial.fivemiov.utils.removePlus

private const val MY_TAG="MY_AddNumberToAccount"
class AddNumberToAccount : Fragment() {

    private lateinit var binding:FragmentAddNumberToAccountBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_add_number_to_account,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }



        binding.addphoneButton.setOnClickListener {
            binding.rootAddNumberConstrLayout.isEnabled=false
            it.isEnabled=false
            hidekeyboard()
            if(allEnteredFieldsAreValid()){
                showProgressBar(true)
                activityViewModel.addNumberToAccountButtonClicked(
                    (binding.addNmbPhoneEditText.text.toString()).removePlus(),
                    binding.addNmbEmailEditText.text.toString(),
                    binding.addNmbPassEditText.text.toString()
                    )
            }else{
                it.isEnabled=true
            }

        }

        binding.addNmbPassEditText.setOnEditorActionListener { view, action, keyEvent ->
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

        activityViewModel.addNumberToAccuntNetworkError.observe(viewLifecycleOwner,Observer{
            if(it!=null){
                showSnackBar(it)
            }
            binding.addphoneButton.isEnabled=true
            showProgressBar(false)

        })


        activityViewModel.addNumberToAccuntNetworkSuccess.observe(viewLifecycleOwner,Observer{
            if(it!=null){
                showToast(it)
                findNavController().navigate(AddNumberToAccountDirections.actionAddNumberToAccountToAuthorizationFragment())
            }
            binding.addphoneButton.isEnabled=true
            showProgressBar(false)

        })

    }

    private fun allEnteredFieldsAreValid(): Boolean {
        var b:Boolean=true
        if(!(binding.addNmbPhoneEditText.text.toString()).isPhoneNumberValid()) {
            b=false
            binding.addNmbPhoneEditText.setError(resources.getString(R.string.not_valid_phone_number))
        }
        if(!(binding.addNmbEmailEditText.text.toString()).isEmailValid()) {
            b=false
            binding.addNmbEmailEditText.setError(resources.getString(R.string.not_valid_email))
        }
        if(!(binding.addNmbPassEditText.text.toString()).isPasswordValid()) {
            b=false
            binding.addNmbPassEditText.setError(resources.getString(R.string.not_valid_password))
        }
        return b
    }



    private fun hidekeyboard(){
        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.rootAddNumberConstrLayout.windowToken, 0)
    }

    private fun showProgressBar(bool:Boolean){
        if(bool){
            binding.rootAddNumberConstrLayout.alpha=0.2f
            binding.addNumberProgressBar.visibility=View.VISIBLE
        }else{
            binding.rootAddNumberConstrLayout.alpha=1f
            binding.addNumberProgressBar.visibility=View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }



}
