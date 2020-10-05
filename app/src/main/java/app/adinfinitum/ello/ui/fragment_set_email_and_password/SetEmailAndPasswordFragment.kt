package app.adinfinitum.ello.ui.fragment_set_email_and_password


import android.app.Activity
import android.os.Bundle
import android.view.*
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar

import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.FragmentSetEmailAndPasswordBinding
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.*

private const val MYTAG="MY_SetEmail&PassFragm"
class SetEmailAndPasswordFragment : Fragment() {

    private lateinit var binding: FragmentSetEmailAndPasswordBinding
    private lateinit var viewModel:SetEmailPassFragmentViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_set_email_and_password,container,false)

        val database= MyDatabase.getInstance(requireContext()).myDatabaseDao
        val apiService= MyAPI.retrofitService
        val repo= Repo( database,
                        apiService,
                        resources.getString(R.string.mobile_app_version_header,(requireActivity().application as MyApplication).mobileAppVersion)
                        )



        viewModel = ViewModelProvider(this, SetEmailPassViewModelFactory(repo,requireActivity().application))
            .get(SetEmailPassFragmentViewModel::class.java)


        binding.setAccountSubmitButton.setOnClickListener {
            it.isEnabled=false
            binding.rootSetEmailPass.requestFocus()
            hidekeyboard()

            if(!isOnline(requireActivity().application)) {
                it.isEnabled=true
                showSnackBar(resources.getString(R.string.no_internet))
                return@setOnClickListener}

            if(allEnteredFieldsAreValid()) {

                showProgressBar(true)

                    viewModel.setAccountAndEmailForUser(
                                binding.setAccountEmailEditText.text.toString(),
                                binding.setAccountPassEditText.text.toString()
                    )

            } else  it.isEnabled=true
         }

        binding.setAccountConfirmpassEditText.setOnEditorActionListener { view, action, keyEvent ->

            when (action){
                EditorInfo.IME_ACTION_DONE, EditorInfo.IME_ACTION_UNSPECIFIED-> {
                    hidekeyboard()
                    view.clearFocus()
                    true
                }
                else->false
            }
        }

        binding.apply {
            setAccountEmailEditText.apply {
                    afterTextChanged { binding.setAccountEmailTextInputLayout.error=null }
                    setOnFocusChangeListener { view, hasFocus ->
                        if(hasFocus)  binding.setAccountEmailTextInputLayout.error=null
                     }
             }

             setAccountPassEditText.apply {
                    afterTextChanged { binding.setAccountPassTextInputLayout.error=null }
                    setOnFocusChangeListener { view, hasFocus ->
                     if(hasFocus)  binding.setAccountPassTextInputLayout.error=null
                    }
              }

              setAccountConfirmpassEditText.apply {
                afterTextChanged { binding.setAccountConfirmpassTextInputLayout.error=null }
                  setOnFocusChangeListener { view, hasFocus ->
                      if(hasFocus)  binding.setAccountConfirmpassTextInputLayout.error=null
                  }
               }

        }

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.setAccountEmailAndPassSuccess.observe(viewLifecycleOwner, Observer {response->
            if(response!=null){
                showProgressBar(false)
                showToast(response.userMsg)
                if(response.success==true){
                    findNavController().navigate(SetEmailAndPasswordFragmentDirections.actionSetEmailAndPasswordFragmentToDialPadFragment())
                }else binding.setAccountSubmitButton.isEnabled=true
                viewModel.resetSetEmailAndPassNetSuccess()
            }
         })

         viewModel.setAccountEmailAndPassError.observe(viewLifecycleOwner, Observer {
             if(it!=null){
                viewModel.resetSetEmailAndPasstNetErrorr()

                 showSnackBar(getString(R.string.something_went_wrong))
                 showProgressBar(false)
                 binding.setAccountSubmitButton.isEnabled=true
             }
          })

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
           menu.findItem(R.id.menu_item_myaccount).setVisible(false)
           menu.findItem(R.id.menu_item_share).setVisible(false)
           menu.findItem(R.id.aboutFragment).setVisible(false)
    }


    override fun onStop() {
        super.onStop()
        hidekeyboard()
    }


    private fun allEnteredFieldsAreValid(): Boolean {
        var b:Boolean=true

        if(!(binding.setAccountEmailEditText.text.toString()).isEmailValid()) {
            b=false
            binding.setAccountEmailTextInputLayout.setError(resources.getString(R.string.not_valid_email))
        }
        if(!(binding.setAccountPassEditText.text.toString()).isPasswordValid()) {
            b=false
            binding.setAccountPassTextInputLayout.setError(resources.getString(R.string.not_valid_password))
        }

        if(!binding.setAccountConfirmpassEditText.text.toString().equals(binding.setAccountPassEditText.text.toString())){
            b=false
            binding.setAccountConfirmpassTextInputLayout.setError(getString(R.string.confirm_password_error))
        }
        return b
    }


    private fun hidekeyboard(){

        val inputMethodManager = requireActivity().getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
        inputMethodManager.hideSoftInputFromWindow(binding.rootSetEmailPass.windowToken, 0)
    }

    private fun showProgressBar(bool:Boolean){
        if(bool){
            binding.rootSetEmailPass.alpha=0.2f
            binding.progressBarsetAccount.visibility=View.VISIBLE
        }else{
            binding.rootSetEmailPass.alpha=1f
            binding.progressBarsetAccount.visibility=View.GONE
        }

    }

    private fun showSnackBar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }



}