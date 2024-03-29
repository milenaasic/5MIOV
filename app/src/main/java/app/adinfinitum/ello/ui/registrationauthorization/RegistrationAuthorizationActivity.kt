package app.adinfinitum.ello.ui.registrationauthorization

import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import app.adinfinitum.ello.databinding.ActivityRegistrationAuthorizationBinding
import app.adinfinitum.ello.ui.AppSignatureHelper
import app.adinfinitum.ello.ui.emty_logo_fragment.EmptyLogoFragmentDirections
import app.adinfinitum.ello.ui.main_activity.MainActivity
import app.adinfinitum.ello.ui.myapplication.MyApplication
import app.adinfinitum.ello.ui.registrationauthorization.models.SignInForm
import app.adinfinitum.ello.ui.registrationauthorization.models.SignInProcessAuxData
import app.adinfinitum.ello.utils.*
import org.acra.ACRA

private val MYTAG="MY_RegAuthActivity"

class RegistrationAuthorizationActivity : AppCompatActivity() {

    private lateinit var binding: ActivityRegistrationAuthorizationBinding
    private lateinit var viewModel: RegAuthActivityViewModel
    private val SPLASH_SCREEN_DURATION_IN_MILLIS=1000L

    companion object{
        const val SIGN_IN_FORM_STATE="sign_in_form_state"
        const val SIGN_IN_PROCESS_AUX_DATA_STATE="sign_in_process_aux_data_state"

    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding=DataBindingUtil.setContentView(this,R.layout.activity_registration_authorization)
        val app=application as MyApplication
        viewModel = ViewModelProvider(this,
                                    RegAuthViewModelFactory(app.repo,
                                                            app.repoUser,
                                                            app.repoPrenumberAndWebApiVer,
                                                            app.repoRemoteDataSource,
                                                            app.repoLogToServer,
                                                            app)
                                    )
            .get(RegAuthActivityViewModel::class.java)

        if(!isOnline(application)) showSnackbar(resources.getString(R.string.no_internet))

        if (savedInstanceState != null) {
            val rememberedSignInForm:SignInForm?=savedInstanceState.getParcelable(SIGN_IN_FORM_STATE)
            val rememberedSignInAuxData:SignInProcessAuxData?=savedInstanceState.getParcelable(
                SIGN_IN_PROCESS_AUX_DATA_STATE)

            rememberedSignInForm?.let {
                viewModel.reinitializeSignInForm(it)
            }

            rememberedSignInAuxData?.let {
                viewModel.reinitializeSignInProcessAuxData(it)
             }
        }


        viewModel.userData.observe(this, { user->

            Log.i(MYTAG,("User in DB: $user"))
            ACRA.getErrorReporter().putCustomData("REGISTRATION_AUTHORIZATION_ACTIVITY_observe_user_data_phone",user.userPhone)
            ACRA.getErrorReporter().putCustomData("REGISTRATION_AUTHORIZATION_ACTIVITY_observe_user_data_token",user.userToken)

            if(user!=null) {
                if (user.userPhone != EMPTY_PHONE_NUMBER && !user.userPhone.isEmpty() && user.userToken!= EMPTY_TOKEN && !user.userToken.isEmpty()) {
                    //halt for splash screen to be seen
                     Handler(Looper.getMainLooper()).postDelayed({
                        gotoMainActivity()
                    }, SPLASH_SCREEN_DURATION_IN_MILLIS)


                } else {

                    if(savedInstanceState==null) {
                        Handler(Looper.getMainLooper()).postDelayed({
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

        //todo when first uploaded to Play store add SignatureHelper to read it
    }



    private fun gotoMainActivity() {
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        outState.apply {
                putParcelable(SIGN_IN_FORM_STATE,viewModel.signInForm)
                putParcelable(SIGN_IN_PROCESS_AUX_DATA_STATE,viewModel.signInProcessAuxData)
        }
        super.onSaveInstanceState(outState)
    }



    override fun onDestroy() {
        super.onDestroy()
        Log.i(MYTAG,"On Destroy")

    }

    private fun showSnackbar(message: String) {
        Snackbar.make(binding.regAuthActivityCoordLayout,message, Snackbar.LENGTH_LONG).show()
    }

   /* override fun onBackPressed() {
        Log.i(MYTAG,"BACK PRESSED before")
        super.onBackPressed()
        Log.i(MYTAG,"BACK PRESSED after")
    }

    override fun onNavigateUp(): Boolean {
        Log.i(MYTAG,"onNavigateUp()")
        return super.onNavigateUp()
    }*/
}
