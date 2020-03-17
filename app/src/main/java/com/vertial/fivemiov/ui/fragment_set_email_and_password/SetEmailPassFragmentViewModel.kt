package com.vertial.fivemiov.ui.fragment_set_email_and_password

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import kotlinx.coroutines.Dispatchers.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

private val MYTAG="MY_SetEmailPass_VIEWMOD"

class SetEmailPassFragmentViewModel(val myrepository: Repo, application: Application) : AndroidViewModel(application) {

    val setAccountEmailAndPassSuccess=myrepository.setAccountEmailAndPassSuccess
    val setAccountEmailAndPassError=myrepository.setAccountEmailAndPassError

    fun setAccountAndEmailForUser(email:String,password:String){
        var authtoken:String=""
        var phoneNumber:String=""
        //pokupi bojr i token iz baze u repo-u
            viewModelScope.launch {
                val deferredPhone = viewModelScope.async(IO) {
                    myrepository.getPhoneNumberFromDB()
                }
                try {
                     phoneNumber = deferredPhone.await()

                } catch (e: Exception) {
                    Log.i(MYTAG,"db greska ${e.message}")
                }

                val deferredToken = viewModelScope.async(IO) {
                    myrepository.getTokenFromDB()
                }
                try {
                    authtoken = deferredToken.await()

                } catch (e: Exception) {
                    Log.i(MYTAG,"db greska ${e.message}")
                }

                myrepository.setAccountEmailAndPasswordForUser(phoneNumber,authtoken,email, password)
            }

    }

}
