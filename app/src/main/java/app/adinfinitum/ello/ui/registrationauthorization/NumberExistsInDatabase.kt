package app.adinfinitum.ello.ui.registrationauthorization


import android.app.Activity
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import app.adinfinitum.ello.R
import app.adinfinitum.ello.databinding.FragmentNumberExistsInDatabaseBinding
import app.adinfinitum.ello.utils.*

private val MY_TAG="MY_NumberExistsInDB"
class NumberExistsInDatabase : Fragment() {

    private lateinit var binding: FragmentNumberExistsInDatabaseBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel

    override fun onAttach(context: Context) {
        super.onAttach(context)

        requireActivity().onBackPressedDispatcher.addCallback(this,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    Log.i(MY_TAG, "handleOnBackPressed() ${activityViewModel.signInForm.email}")
                    activityViewModel.resetSignInFormEmailAndPassword()
                    activityViewModel.resetSignInProcessAuxData()
                    Log.i(MY_TAG, " after handleOnBackPressed() ${activityViewModel.signInForm.email}")
                    isEnabled=false
                    requireActivity().onBackPressed()
                }

            }
        )

    }
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_number_exists_in_database,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.numExistsPhoneTextView.apply {
            text=activityViewModel.signInForm.phoneNmb
        }

        //email edit text
        binding.nmbExistsEmailEditText.apply {

            afterTextChanged {
                activityViewModel.afterEmailEditTextChanged(it)
                binding.nmbExistsEmailTextInputLayout.error=null
            }
            setOnFocusChangeListener { _, hasFocus ->
                if(hasFocus) binding.nmbExistsEmailTextInputLayout.error=null
            }
         }

         //password edit text
         binding.nmbExistsPassEditText.apply {
             afterTextChanged {
                activityViewModel.afterPasswordEditTextChanged(it)
                binding.nmbExistsEnterPassTextInputLayout.error=null
             }
             setOnFocusChangeListener { _, hasFocus ->
                 if(hasFocus) binding.nmbExistsEnterPassTextInputLayout.error=null
             }

             setOnEditorActionListener { view, action, _ ->
                 when (action){
                     EditorInfo.IME_ACTION_DONE,EditorInfo.IME_ACTION_UNSPECIFIED-> {
                         hidekeyboard()
                         view.clearFocus()
                         true
                     }
                     else->false
                 }
             }

         }


        binding.nmbExistsSubmitButton.setOnClickListener {
            submitButtonClickedUIChanges()

            activityViewModel.numberExistsInDBVerifyAccountButtonClicked(
                binding.nmbExistsEmailEditText.text.toString(),
                binding.nmbExistsPassEditText.text.toString()
            )

        }



        binding.dontHaveAccountButton.setOnClickListener {
            submitButtonClickedUIChanges()
            activityViewModel.numberExistsInDBNOAccountButtonClicked()

        }


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        activityViewModel.nmbExistsInDBResult.observe(viewLifecycleOwner, Observer {

            if(!it.hasBeenHandled) {
                    val result=it.getContentIfNotHandled()

                    result?.let{
                        if ( it.enteredEmailError!= null || it.enteredPasswordError!= null) {

                            it.enteredEmailError?.let{errorId->
                                binding.nmbExistsEmailTextInputLayout.error=resources.getString(errorId)
                            }
                            it.enteredPasswordError?.let{errorId->
                                binding.nmbExistsEnterPassTextInputLayout.error=resources.getString(errorId)
                            }
                            binding.nmbExistsSubmitButton.isEnabled = true
                            binding.dontHaveAccountButton.isEnabled = true
                            showProgressBar(false)
                            return@Observer
                        }


                        if(it.navigateToFragment==activityViewModel.NAVIGATE_TO_AUTHORIZATION_FRAGMENT){
                            findNavController().navigate(NumberExistsInDatabaseDirections.actionNumberExistsInDatabaseToAuthorizationFragment())
                            it.showToastMessage?.let {message->
                                showToast(message)
                            }
                            return@Observer
                        }

                        it.showSnackBarMessage?.let {
                            showSnackBar(it)
                         }

                        if(it.showSnackBarErrorMessage==true) showSnackBar(resources.getString(R.string.something_went_wrong))

                        binding.nmbExistsSubmitButton.isEnabled = true
                        binding.dontHaveAccountButton.isEnabled = true
                        showProgressBar(false)

                    }

            }
         })

    }

    override fun onDestroy() {
        Log.i(MY_TAG,"onDestroy")
        super.onDestroy()

    }

    override fun onDestroyView() {
        Log.i(MY_TAG,"onDestroyView()")
        super.onDestroyView()
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

    private fun submitButtonClickedUIChanges(){
        binding.apply {
            nmbExistsRoot.requestFocus()
            dontHaveAccountButton.isEnabled=false
            binding.nmbExistsSubmitButton.isEnabled=false
         }

        hidekeyboard()
        showProgressBar(true)
    }

}
