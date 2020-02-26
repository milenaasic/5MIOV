package com.vertial.sipdnidphone.ui.RegistrationAuthorization

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import com.vertial.sipdnidphone.data.Repo
import kotlinx.coroutines.launch

private const val MY_TAG="MY_RegAuthActivVieModel"

class RegAuthActivityViewModel(val myRepository: Repo, application: Application) : AndroidViewModel(application) {

    lateinit var enteredPhoneNumber:String

    val userData=myRepository.getUserData()


    val registrationNetworkError=myRepository.registrationNetworkError
    val registrationNetworkSuccess=myRepository.registrationSuccess

    val authorizationNetworkError=myRepository.authorizationNetworkError
    val authorizationNetworkSuccess=myRepository.authorizationSuccess

    init {

        Log.i(MY_TAG,("init"))
    }

    fun registerButtonClicked(phoneNumber:String){
        enteredPhoneNumber=phoneNumber
        Log.i(MY_TAG,("registration button clicked"))
        //send registrtion phone number to server and go to authorization fragment
        viewModelScope.launch {
            myRepository.sendRegistationToServer(phoneNumber)
         }

    }

    fun submitButtonClicked(smsToken:String){

        viewModelScope.launch {
            myRepository.authorizeThisUser(enteredPhoneNumber,smsToken)
        }

    }
}