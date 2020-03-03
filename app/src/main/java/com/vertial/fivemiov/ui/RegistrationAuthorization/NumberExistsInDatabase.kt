package com.vertial.fivemiov.ui.RegistrationAuthorization


import android.app.Activity
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.snackbar.Snackbar

import com.vertial.fivemiov.R
import com.vertial.fivemiov.databinding.FragmentNumberExistsInDatabaseBinding

/**
 * A simple [Fragment] subclass.
 */
class NumberExistsInDatabase : Fragment() {

    private lateinit var binding: FragmentNumberExistsInDatabaseBinding
    private lateinit var activityViewModel:RegAuthActivityViewModel


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        binding= DataBindingUtil.inflate(inflater,R.layout.fragment_number_exists_in_database,container, false)

        activityViewModel=requireActivity().run{
            ViewModelProvider(this)[RegAuthActivityViewModel::class.java]
        }

        binding.numExistsPhoneTextView.text=activityViewModel.enteredPhoneNumber


        return binding.root
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
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(),message, Toast.LENGTH_LONG).show()
    }



}
