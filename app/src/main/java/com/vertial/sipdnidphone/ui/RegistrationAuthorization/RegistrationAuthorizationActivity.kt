package com.vertial.sipdnidphone.ui.RegistrationAuthorization

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.findNavController
import com.vertial.sipdnidphone.R
import com.vertial.sipdnidphone.api.MyAPI
import com.vertial.sipdnidphone.data.Repo
import com.vertial.sipdnidphone.database.MyDatabase
import com.vertial.sipdnidphone.databinding.ActivityRegistrationAuthorizationBinding
import com.vertial.sipdnidphone.ui.MainActivity
import com.vertial.sipdnidphone.ui.MainActivityViewModel
import com.vertial.sipdnidphone.ui.MainActivityViewModelFactory
import com.vertial.sipdnidphone.ui.emty_logo_fragment.EmptyLogoFragmentDirections
import com.vertial.sipdnidphone.utils.EMPTY_TOKEN

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
        // val viewModel = ViewModelProvider(this, YourViewModelFactory).get(YourViewModel::class.java)


        viewModel = ViewModelProvider(this, RegAuthViewModelFactory(myRepository,application))
            .get(RegAuthActivityViewModel::class.java)

        viewModel.userData.observe(this, Observer {user->

            Log.i(MYTAG,("user u bazi je $user"))
            if(user.userToken != EMPTY_TOKEN){
                startActivity(Intent(this, MainActivity::class.java))
                finish()

            }else {

                findNavController(R.id.registration_navhost_fragment).navigate(
                        EmptyLogoFragmentDirections.actionEmptyLogoFragmentToRegistrationFragment())

            }

         })

    }
}
