package com.vertial.fivemiov.ui.registrationauthorization


import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentAuthorizationBinding
import com.vertial.fivemiov.utils.isOnline
import com.vertial.fivemiov.utils.removePlus


private val MYTAG="MY_AuthorizationFragm"
class AuthorizationFragment : Fragment() {

    private lateinit var binding: FragmentAuthorizationBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding= DataBindingUtil.inflate(inflater, R.layout.fragment_authorization,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.authPhoneTextView.text=String.format(resources.getString(R.string.add_plus_before_phone,activityViewModel.enteredPhoneNumber))

        binding.submitButton.setOnClickListener {

            hidekeyboard()
            binding.authorizationRootLayout.requestFocus()
            if(!isOnline(requireActivity().application)) {
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener}

            enableDisableButtons(false)

            if(binding.tokenEditText.text.toString().isBlank()){
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

        binding.resendSmsButton.setOnClickListener{
            hidekeyboard()
            binding.authorizationRootLayout.requestFocus()
            enableDisableButtons(false)
            showProgressBar(true)
            //resending sms via registration route
            Log.i(MYTAG,"resend dugme ${activityViewModel.enteredPhoneNumber}, ${activityViewModel.enteredEmail}, ${activityViewModel.enteredPassword}, ${activityViewModel.signInParameter}")
            when{
                //dosao iz fragmenta Registration
                activityViewModel.enteredEmail==null && activityViewModel.signInParameter==null->activityViewModel.registerButtonClicked(activityViewModel.enteredPhoneNumber?.removePlus()?:"",smsResend = true)

                //dosao preko fragmenta AddNumberToAccount
                activityViewModel.enteredEmail!=null && activityViewModel.signInParameter==null->activityViewModel.addNumberToAccountButtonClicked(
                                                                                                    activityViewModel.enteredPhoneNumber?.removePlus()?:"",
                                                                                                    activityViewModel.enteredEmail?:"",
                                                                                                    activityViewModel.enteredPassword?:"",
                                                                                                        smsResend = true)

                //dosao preko NumberExistsInDatabase, create new account
                activityViewModel.enteredEmail==null && activityViewModel.signInParameter==false->activityViewModel.numberExistsInDb_NoAccount(smsResend = true)

                //dosao preko NumberExistsInDatabase, create new accounte
                activityViewModel.enteredEmail!=null && activityViewModel.signInParameter==true->activityViewModel.numberExistsInDBVerifyAccount(
                                                                                            activityViewModel.enteredEmail?:"",
                                                                                            activityViewModel.enteredPassword?:"",
                                                                                            smsResend = true)

            }


        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.authorizationNetworkSuccess.observe(viewLifecycleOwner, Observer {response->
            if(response!=null){
            activityViewModel.resetAuthorization_NetSuccess()
            showProgressBar(false)
            enableDisableButtons(true)
            response.userMessage.let { showToast(it) }

            }
        })

        activityViewModel.authorizationNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                activityViewModel.resetAuthorization_NetError()
                showProgressBar(false)
                enableDisableButtons(true)
                showSnackBar(resources.getString(R.string.something_went_wrong))
            }
        })

        activityViewModel.smsResendNetworkSuccess.observe(viewLifecycleOwner, Observer {
            Log.i(MYTAG," sms resend success , value $it")
            if(it!=null){
                activityViewModel.resetSMSResend_NetSuccess()
                showToast(it)
                showProgressBar(false)
                enableDisableButtons(true)
            }
        })

        activityViewModel.smsResendNetworkError.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                Log.i(MYTAG," sms resend success , value $it")
                activityViewModel.resetSMSResend_NetError()
                showSnackBar(resources.getString(R.string.something_went_wrong))
                showProgressBar(false)
                enableDisableButtons(true)
            }
        })


        //SMS verification token
        activityViewModel.verificationTokenForAuthFragment.observe(viewLifecycleOwner, Observer {
            if(it!=null){
                binding.tokenEditText.setText(it)
                activityViewModel.resetSMSVerificationTOkenForAuthFragToNull()

            }



        })



    }



    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_INDEFINITE).setAction("OK"){}.show()
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

    private fun enableDisableButtons(b:Boolean){
        if(b){
                 binding.apply {
                     submitButton.isEnabled = true
                     resendSmsButton.isEnabled = true
                 }
         }else{
                binding.apply {
                    submitButton.isEnabled = false
                    resendSmsButton.isEnabled = false

                 }


         }

    }

    private fun networkResenSMSSuccessOrError(){
        showProgressBar(false)
        enableDisableButtons(true)

    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }

}
