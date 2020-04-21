package com.vertial.fivemiov.ui.fragment_set_email_and_password

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import java.lang.Exception

private val MYTAG="MY_SetEmailPass_VIEWMOD"

class SetEmailPassFragmentViewModel(val myrepository: Repo, application: Application) : AndroidViewModel(application) {

    val setAccountEmailAndPassSuccess=myrepository.setAccountEmailAndPassSuccess
    val setAccountEmailAndPassError=myrepository.setAccountEmailAndPassError

    fun setAccountAndEmailForUser(email:String,password:String){
        var authtoken:String=""
        var phoneNumber:String=""
        //pokupi broj i token iz baze
            viewModelScope.launch {
                val deferredPhone = viewModelScope.async(IO) {
                    //delay(3000)
                    myrepository.getPhoneNumberFromDB()
                }
                try {
                     phoneNumber = deferredPhone.await()

                } catch (e: Exception) {
                    Log.i(MYTAG,"db greska ${e.message}")
                }

                val deferredToken = viewModelScope.async(IO) {
                    //delay(3000)
                    myrepository.getTokenFromDB()
                }
                try {
                    authtoken = deferredToken.await()

                } catch (e: Exception) {
                    Log.i(MYTAG,"db greska ${e.message}")
                }
                Log.i(MYTAG, "setEmialView MOdel $phoneNumber,$authtoken,$email,$password")
                if(phoneNumber.isNotEmpty() && authtoken.isNotEmpty()) myrepository.setAccountEmailAndPasswordForUser(phoneNumber,authtoken,email, password)
            }

    }

    fun resetSetEmailAndPassNetSuccess(){
        myrepository.resetSetAccountEmailAndPassNetSuccess()
    }

    fun resetSetEmailAndPasstNetErrorr(){
        myrepository.resetSetAccountEmailAndPassNetError()
    }

}
