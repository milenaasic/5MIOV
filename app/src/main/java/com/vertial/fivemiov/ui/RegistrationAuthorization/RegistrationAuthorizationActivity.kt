package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.material.snackbar.Snackbar
import com.vertial.fivemiov.R
import com.vertial.fivemiov.api.MyAPI
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityRegistrationAuthorizationBinding
import com.vertial.fivemiov.ui.MainActivity
import com.vertial.fivemiov.ui.emty_logo_fragment.EmptyLogoFragmentDirections
import com.vertial.fivemiov.utils.EMPTY_TOKEN
import com.vertial.fivemiov.utils.isOnline

private val MYTAG="MY_RegAuthActivity"

class RegistrationAuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationAuthorizationBinding
    private lateinit var viewModel: RegAuthActivityViewModel
    //private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_registration_authorization)


        val myDatabaseDao= MyDatabase.getInstance(this).myDatabaseDao
        val myApi= MyAPI.retrofitService

        val myRepository= Repo(myDatabaseDao,myApi)

        if(!isOnline(application)) showSnackbar(resources.getString(R.string.no_internet))

        viewModel = ViewModelProvider(this, RegAuthViewModelFactory(myRepository,application))
            .get(RegAuthActivityViewModel::class.java)

        viewModel.userData.observe(this, Observer {user->

            Log.i(MYTAG,("user u bazi je $user"))
            if(user!=null) {
                if (user.userToken != EMPTY_TOKEN) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                } else {

                    findNavController(R.id.registration_navhost_fragment).navigate(
                        EmptyLogoFragmentDirections.actionEmptyLogoFragmentToRegistrationFragment()
                    )

                }
            }

         })

    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }
}
