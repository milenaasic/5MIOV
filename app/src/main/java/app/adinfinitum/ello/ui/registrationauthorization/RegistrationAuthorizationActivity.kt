package app.adinfinitum.ello.ui.registrationauthorization

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import com.google.android.gms.auth.api.phone.SmsRetriever
import com.google.android.gms.tasks.Task
import com.google.android.material.snackbar.Snackbar
import app.adinfinitum.ello.R
import app.adinfinitum.ello.api.MyAPI
import app.adinfinitum.ello.data.Repo
import app.adinfinitum.ello.data.RepoSIPE1
import app.adinfinitum.ello.database.MyDatabase
import app.adinfinitum.ello.databinding.ActivityRegistrationAuthorizationBinding
import app.adinfinitum.ello.ui.AppSignatureHelper
import app.adinfinitum.ello.ui.emty_logo_fragment.EmptyLogoFragmentDirections
import app.adinfinitum.ello.ui.main_activity.MainActivity
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.utils.*
import org.acra.ACRA

private val MYTAG="MY_RegAuthActivity"

class RegistrationAuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationAuthorizationBinding
    private lateinit var viewModel: RegAuthActivityViewModel
    private lateinit var smsBroadcastReceiver: SMSAuthorizationBroadcastReceiver
    private val SPLASH_SCREEN_DURATION_IN_MILLIS=1000L

    //is user verification by call enabled or not
    var verificationByCallEnabled=false
    var verificationCallerId=""

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
        val mobileAppVersion=(application as MyApplication).mobileAppVersion
        val myRepository= Repo(myDatabaseDao,myApi, mobileAppVer = resources.getString(R.string.mobile_app_version_header,mobileAppVersion))
        val mySIPE1Repo=RepoSIPE1(myDatabaseDao,myApi, mobileAppVer =resources.getString(R.string.mobile_app_version_header,mobileAppVersion))

        if(!isOnline(application)) showSnackbar(resources.getString(R.string.no_internet))



        viewModel = ViewModelProvider(this, RegAuthViewModelFactory(myRepository,mySIPE1Repo,application))
            .get(RegAuthActivityViewModel::class.java)

        if (savedInstanceState != null) {

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
            Log.i(MYTAG, " savedInstanceState!=null,its values are $rememberedPhone,$rememberedEmail,$rememberedPassword,$rememberedSignInParam")


        }


        initializeSMSBroadcastReceiver()


        viewModel.userData.observe(this, Observer {user->

            Log.i(MYTAG,("User in DB: $user"))
            ACRA.getErrorReporter().putCustomData("REGISTRATION_AUTHORIZATION_ACTIVITY_observe_user_data_phone",user.userPhone)
            ACRA.getErrorReporter().putCustomData("REGISTRATION_AUTHORIZATION_ACTIVITY_observe_user_data_token",user.userToken)

            if(user!=null) {
                if (user.userPhone != EMPTY_PHONE_NUMBER && !user.userPhone.isNullOrEmpty() && user.userToken!= EMPTY_TOKEN && !user.userToken.isNullOrEmpty()) {
                    //halt for splash screen to be seen
                     Handler().postDelayed(Runnable {
                        gotoMainActivity()
                    }, SPLASH_SCREEN_DURATION_IN_MILLIS)


                } else {

                    if(savedInstanceState==null) {
                        Handler().postDelayed(Runnable {
                            findNavController(R.id.registration_navhost_fragment).navigate(
                                EmptyLogoFragmentDirections.actionEmptyLogoFragmentToRegistrationFragment()
                            )
                        }, SPLASH_SCREEN_DURATION_IN_MILLIS)

                    }else {
                        Log.i(MYTAG, "observeUserData, user phone=EMPTY_PHONE_NUMBER and savedInstanceState!= null")
                    }

                }
            }

         })


         viewModel.startSMSRetreiver.observe(this, Observer {

             if(!it.hasBeenHandled) {
                 if(it.getContentIfNotHandled()==true) startMySMSRetreiver()

             }

         })
         smsBroadcastReceiver.receivedSMSMessage.observe(this, Observer {

                if(it!=null){
                    smsBroadcastReceiver.resetReceivedSMSMessage()
                    val navController= findNavController(R.id.registration_navhost_fragment)
                    if(navController.currentDestination?.id==R.id.authorizationFragment) {
                       viewModel.setSMSVerificationTokenForAuthFragment(it)

                    }
                }
          })

    }



    private fun gotoMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
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


    fun initializeSMSBroadcastReceiver(){
        smsBroadcastReceiver = SMSAuthorizationBroadcastReceiver()
       val filter = IntentFilter().apply {
            addAction(SmsRetriever.SMS_RETRIEVED_ACTION)
        }
        registerReceiver(smsBroadcastReceiver, filter)

    }

    private fun startMySMSRetreiver(){

        Log.i("MY_SMSAuthBroadcastAct","  entered function start MySMSReceiver")
        val client = SmsRetriever.getClient(this)
        val task: Task<Void> = client.startSmsRetriever()
        Log.i("MY_SMSAuthBroadcastAct","  client $client, $task, ${client.apiOptions},${client.instanceId}")

        task.addOnSuccessListener {
            Log.i("MY_SMSAuthBroadcastAct","  Successfully started retriever, expect broadcast intent")
            // Successfully started retriever, expect broadcast intent
        }

        task.addOnFailureListener {
            Log.i("MY_SMSAuthBroadcastAct","  SMS  retriever failure, ${it.message}")
            // Failed to start retriever, inspect Exception for more details
        }

        

    }




    override fun onDestroy() {
        //application.unregisterReceiver(smsBroadcastReceiver)
        unregisterReceiver(smsBroadcastReceiver)
         super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }


    private fun showSnackbar(message: String) {
        Snackbar.make(binding.regAuthActivityCoordLayout,message, Snackbar.LENGTH_LONG).show()
    }
}
