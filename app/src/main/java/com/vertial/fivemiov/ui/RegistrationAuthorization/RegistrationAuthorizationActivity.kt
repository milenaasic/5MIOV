package com.vertial.fivemiov.ui.RegistrationAuthorization

import android.content.ComponentName
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
import com.vertial.fivemiov.data.RepoSIPE1
import com.vertial.fivemiov.database.MyDatabase
import com.vertial.fivemiov.databinding.ActivityRegistrationAuthorizationBinding
import com.vertial.fivemiov.ui.main_activity.MainActivity
import com.vertial.fivemiov.ui.emty_logo_fragment.EmptyLogoFragmentDirections
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN
import com.vertial.fivemiov.utils.isOnline
import kotlin.math.sign

private val MYTAG="MY_RegAuthActivity"

class RegistrationAuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationAuthorizationBinding
    private lateinit var viewModel: RegAuthActivityViewModel
    //private lateinit var navController: NavController

    companion object{
        const val ENTERED_PHONE_NUMBER = "entered_phone_number"
        const val ENTERED_EMAIL="entered_email"
        const val ENTERED_PASSWORD="entered_password"
        const val SIGN_IN_PARAMETER="sign_in_parameter"
        const val UNDEFINED_STATE="undefined_state"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_registration_authorization)


        val myDatabaseDao= MyDatabase.getInstance(this).myDatabaseDao
        val myApi= MyAPI.retrofitService

        val myRepository= Repo(myDatabaseDao,myApi)
        val mySIPE1Repo=RepoSIPE1(myDatabaseDao,myApi)

        if(!isOnline(application)) showSnackbar(resources.getString(R.string.no_internet))

        /*val myApp=application as MyApplication
        val myAppContanier=myApp.myAppContainer*/

        viewModel = ViewModelProvider(this, RegAuthViewModelFactory(myRepository,mySIPE1Repo,application))
            .get(RegAuthActivityViewModel::class.java)

        if (savedInstanceState != null) {
            Log.i(MYTAG, "usao u onSaveInstance nije null")
            val rememberedPhone=savedInstanceState.getString(ENTERED_PHONE_NUMBER)
            val rememberedEmail=savedInstanceState.getString(ENTERED_EMAIL)
            val rememberedPassword=savedInstanceState.getString(ENTERED_PASSWORD)
            val rememberedSignInParam=savedInstanceState.getString(SIGN_IN_PARAMETER)
           if(viewModel!=null) {
               viewModel.apply {
                   enteredPhoneNumber = rememberedPhone
                   enteredEmail = rememberedEmail
                   enteredPassword = rememberedPassword
                   if (rememberedSignInParam == UNDEFINED_STATE) viewModel.signInParameter = null
                   else viewModel.signInParameter = rememberedSignInParam?.toBoolean()

               }
           }
            Log.i(MYTAG, " prosao kroz savedInst!=null i $rememberedPhone,$rememberedEmail,$rememberedPassword,$rememberedSignInParam")


        }


            viewModel.userData.observe(this, Observer {user->

            Log.i(MYTAG,("user u bazi je $user"))
            if(user!=null) {
                if (user.userPhone != EMPTY_PHONE_NUMBER && !user.userPhone.isNullOrEmpty() && user.userToken!= EMPTY_TOKEN && !user.userToken.isNullOrEmpty()) {
                    startActivity(Intent(this, MainActivity::class.java))
                    finish()

                } else {
                    //Ukoliko ima sacuvano stanje nije u emptyLogoFragment-u nego u fragmentu u kom je bila app kda je unistena
                    if(savedInstanceState==null) {
                        findNavController(R.id.registration_navhost_fragment).navigate(
                            EmptyLogoFragmentDirections.actionEmptyLogoFragmentToRegistrationFragment()
                        )
                    }else {Log.i(MYTAG, "observeUserData i saved instance state nije null")}

                }
            }

         })

    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
                putString(ENTERED_PHONE_NUMBER,viewModel.enteredPhoneNumber)
                putString(ENTERED_EMAIL,viewModel.enteredEmail)
                putString(ENTERED_PASSWORD,viewModel.enteredPassword)
                putString(SIGN_IN_PARAMETER,viewModel.signInParameter.toString()?: UNDEFINED_STATE)
        }
        super.onSaveInstanceState(outState)
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.root,message, Snackbar.LENGTH_LONG).show()
    }
}
