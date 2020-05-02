package com.vertial.fivemiov.ui.fragment_set_email_and_password

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.vertial.fivemiov.data.Repo
import com.vertial.fivemiov.model.User
import com.vertial.fivemiov.utils.EMPTY_PHONE_NUMBER
import com.vertial.fivemiov.utils.EMPTY_TOKEN
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

            viewModelScope.launch {
                val deferredUser = viewModelScope.async(IO) {
                    //delay(3000)
                    myrepository.getUser()
                }
                try {
                     val myUser = deferredUser.await()
                    if(myUser!=null){
                        Log.i(MYTAG, "setEmialView MOdel ${myUser.userPhone},$myUser.authtoken,$email,$password")
                        if( myUser.userPhone.isNotEmpty()
                            && myUser.userPhone!= EMPTY_PHONE_NUMBER
                            && myUser.userToken.isNotEmpty()
                            && myUser.userToken!= EMPTY_TOKEN) myrepository.setAccountEmailAndPasswordForUser(phoneNumber = myUser.userPhone,
                                                                                                                token=myUser.userToken,
                                                                                                                email = email,
                                                                                                                password = password)

                    }

                } catch (e: Exception) {
                    Log.i(MYTAG,"db greska ${e.message}")
                }



            }

    }

    fun resetSetEmailAndPassNetSuccess(){
        myrepository.resetSetAccountEmailAndPassNetSuccess()
    }

    fun resetSetEmailAndPasstNetErrorr(){
        myrepository.resetSetAccountEmailAndPassNetError()
    }

}
