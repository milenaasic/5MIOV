package com.vertial.fivemiov.ui.fragment_set_email_and_password


import android.app.Activity
import android.location.SettingInjectorService
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.FragmentSetEmailAndPasswordBinding
import com.vertial.fivemiov.ui.RegistrationAuthorization.isEmailValid
import com.vertial.fivemiov.ui.RegistrationAuthorization.isPasswordValid
import com.vertial.fivemiov.ui.RegistrationAuthorization.isPhoneNumberValid


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
        val repo= Repo(database,apiService)

        viewModel = ViewModelProvider(this, SetEmailPassViewModelFactory(repo,requireActivity().application))
            .get(SetEmailPassFragmentViewModel::class.java)


        return binding.root
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        super.onCreateOptionsMenu(menu, inflater)
           menu.findItem(R.id.menu_item_myaccount).setVisible(false)
           menu.findItem(R.id.dialPadFragment).setVisible(false)
           menu.findItem(R.id.menu_item_sync_contacts).setVisible(false)
    }


    private fun allEnteredFieldsAreValid(): Boolean {
        var b:Boolean=true

        if(!(binding.setAccountEmailEditText.text.toString()).isEmailValid()) {
            b=false
            binding.setAccountEmailEditText.setError(resources.getString(R.string.not_valid_email))
        }
        if(!(binding.setAccountPassEditText.text.toString()).isPasswordValid()) {
            b=false
            binding.setAccountPassEditText.setError(resources.getString(R.string.not_valid_password))
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
