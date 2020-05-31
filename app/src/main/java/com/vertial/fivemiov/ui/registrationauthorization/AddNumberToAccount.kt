package com.vertial.fivemiov.ui.registrationauthorization


import android.app.Activity
import android.content.DialogInterface
import android.os.Build
import android.os.Bundle
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentAddNumberToAccountBinding
import com.vertial.fivemiov.utils.*

private const val MY_TAG="MY_AddNumberToAccount"
class AddNumberToAccount : Fragment() {

    private lateinit var binding:FragmentAddNumberToAccountBinding
    private var activityViewModel:RegAuthActivityViewModel?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_add_number_to_account,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        if(!activityViewModel?.enteredPhoneNumber.isNullOrBlank() && !activityViewModel?.enteredPhoneNumber.isNullOrEmpty()) {
            binding.addNmbPhoneEditText.setText(activityViewModel?.enteredPhoneNumber)
        }else binding.addNmbPhoneEditText.setText(PLUS_NIGERIAN_PREFIX)

        binding.addphoneButton.setOnClickListener {

            //it.isEnabled=false
            binding.rootAddNumberConstrLayout.requestFocus()
            hidekeyboard()
            if(allEnteredFieldsAreValid()){
                showTermsOfUseDailog(
                                    (binding.addNmbPhoneEditText.text.toString()).removePlus(),
                                    binding.addNmbEmailEditText.text.toString(),
                                    binding.addNmbPassEditText.text.toString()
                )
                /*showProgressBar(true)
                activityViewModel?.addNumberToAccountButtonClicked(
                    (binding.addNmbPhoneEditText.text.toString()).removePlus(),
                    binding.addNmbEmailEditText.text.toString(),
                    binding.addNmbPassEditText.text.toString()
                    )*/
            }else{
                it.isEnabled=true
            }

        }


        binding.addNmbPassEditText.setOnEditorActionListener { view, action, keyEvent ->
            when (action){
                EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
                    hidekeyboard()
                    view.clearFocus()
                    Log.i(MY_TAG," usao u oneditor action listener iz nmbPhoneedittext TRUE")
                    true
                }
                else->{
                    Log.i(MY_TAG," usao u oneditor action listener iz nmbPhoneedittext")
                    false
                }

            }

        }

        //Kada EditTextlayouts imaju fokus treba da se iskljuci greska
        binding.apply {

            addNmbPhoneEditText.setOnFocusChangeListener { view, hasFocus ->
                if(hasFocus) binding.addPhoneTextInputLayout.error=null
                Log.i(MY_TAG,"text input layout ima fokus")
            }
            addNmbPhoneEditText.afterTextChanged {binding.addPhoneTextInputLayout.error=null }

            addNmbEmailEditText.setOnFocusChangeListener { view, hasFocus ->
                if(hasFocus) binding.enterEmailTextInputLayout.error=null
                Log.i(MY_TAG,"text input layout ima fokus")
            }

            addNmbEmailEditText.afterTextChanged { binding.enterEmailTextInputLayout.error=null }

            addNmbPassEditText.setOnFocusChangeListener { view, hasFocus ->
                if(hasFocus) binding.addNmbEnterPassTextInputLayout.error=null
                Log.i(MY_TAG,"text input layout ima fokus")
             }
             addNmbPassEditText.afterTextChanged { binding.addNmbEnterPassTextInputLayout.error=null }

         }

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel?.addNumberToAccuntNetworkError?.observe(viewLifecycleOwner,Observer{
            if(it!=null) {
                showSnackBar(resources.getString(R.string.something_went_wrong))
                activityViewModel?.resetAddNumberToAccountNetError()
                binding.addphoneButton.isEnabled = true
                showProgressBar(false)
            }
        })


        activityViewModel?.addNumberToAccuntNetworkSuccess?.observe(viewLifecycleOwner,Observer{response->
            Log.i(MY_TAG,"net response je ${response.toString()}")

            if(response!=null) {
                when {
                    response.success == true -> {
                        showToast(response.usermessage)
                        activityViewModel?.resetAddNumberToAccountNetSuccess()
                        activityViewModel?.startSMSRetreiverFunction()
                        findNavController().navigate(AddNumberToAccountDirections.actionAddNumberToAccountToAuthorizationFragment())
                    }

                    response.success == false -> {
                        showSnackBar(response.usermessage)
                        activityViewModel?.resetAddNumberToAccountNetSuccess()
                    }
                }

                binding.addphoneButton.isEnabled = true
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
        if(!(binding.addNmbPhoneEditText.text.toString()).isPhoneNumberValid()) {
            b=false
            binding.addPhoneTextInputLayout.setError(resources.getString(R.string.not_valid_phone_number))
        }
        if(!(binding.addNmbEmailEditText.text.toString()).isEmailValid()) {
            b=false
            binding.enterEmailTextInputLayout.setError(resources.getString(R.string.not_valid_email))
        }
        if(!(binding.addNmbPassEditText.text.toString()).isPasswordValid()) {
            b=false
            binding.addNmbEnterPassTextInputLayout.setError(resources.getString(R.string.not_valid_password))
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
        Snackbar.make(binding.root,message, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }

    private fun startAddNumberToAccount(phone: String,email: String,password: String) {
            binding.addphoneButton.isEnabled=false
            showProgressBar(true)
            activityViewModel?.addNumberToAccountButtonClicked(
                phoneNumber = phone,
                email = email,
                password = password
            )


    }

    private fun showTermsOfUseDailog(phone: String,email:String,password:String) {
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
                        startAddNumberToAccount(phone,email,password)
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
