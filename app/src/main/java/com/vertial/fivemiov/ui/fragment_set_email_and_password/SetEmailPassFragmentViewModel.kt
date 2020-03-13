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
        //pokupi token iz baze u repo-u
            viewModelScope.launch {
                val deferredToken = viewModelScope.async(IO) {
                    myrepository.getTokenFromDB()
                }
                try {
                    val token = deferredToken.await()
                    myrepository.setAccountEmailAndPasswordForUser(email, password, token)

                } catch (e: Exception) {
                    Log.i(MYTAG,"db greska ${e.message}")
                }
            }

    }

}
